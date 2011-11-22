package org.codehaus.mojo.profile;

import java.util.Iterator;
import java.util.Properties;
import java.util.Collections;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.profiles.activation.ProfileActivator;
import org.apache.maven.profiles.activation.DefaultProfileActivationContext;
import org.apache.maven.profiles.activation.ProfileActivationContext;
import org.apache.maven.project.build.model.ModelLineage;
import org.apache.maven.project.build.model.ModelLineageBuilder;
import org.apache.maven.project.build.model.DefaultModelLineageBuilder;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @plexus.component role="org.apache.maven.profiles.activation.ProfileActivator" 
 *                   role-hint="modelProperty"
 *                   instantiation-strategy="per-lookup"
 * 
 * @author jdcasey
 */
public class ModelPropertyProfileActivator
    implements ProfileActivator, LogEnabled
{
    
    /**
     * Configured as part of the CustomActivator's setup of this ProfileActivator, before the
     * CustomActivator delegates the profile-activation process to it. This IS a required element,
     * and it can be reversed (negated) using a '!' prefix. Reversing the name means one of two things:
     * <br/>
     * <ul>
     *   <li>If the value configuration is null, make sure the property doesn't exist in the lineage.</li>
     *   <li>If the value configuration does exist, make sure the retrieved value doesn't match it.</li>
     * </ul>  
     * 
     * @plexus.configuration
     *   default-value=null
     */
    private String name;
    
    /**
     * Configured as part of the CustomActivator's setup of this ProfileActivator, before the
     * CustomActivator delegates the profile-activation process to it. This is NOT a required element,
     * and it can be reversed (negated) using a '!' prefix.
     * 
     * @plexus.configuration
     *   default-value=null
     */
    private String value;

    /**
     * MavenSession instance used to get the model.
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * MavenProject instance used to get the model.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @plexus.requirement
     */
    private ProfileActivationContext myContext;

    // initialized by the container, or lazily.
    private Logger logger;
    
    public ModelPropertyProfileActivator()
    {
        // provided for Plexus activation
    }
    
    protected ModelPropertyProfileActivator( MavenSession session, MavenProject project, String name, ProfileActivationContext profActCtxt )
    {
        this.session = session;
        this.project = project;
        this.name = name;
        this.myContext = profActCtxt;
    }

    protected ModelPropertyProfileActivator( MavenSession session, MavenProject project, String name, String value, ProfileActivationContext profActCtxt )
    {
        this.session = session;
        this.project = project;
        this.name = name;
        this.value = value;
        this.myContext = profActCtxt;
    }

    protected ModelPropertyProfileActivator( String name, ProfileActivationContext profActCtxt )
    {
        this.name = name;
        this.myContext = profActCtxt;
    }

    protected ModelPropertyProfileActivator( String name, String value, ProfileActivationContext profActCtxt )
    {
        this.name = name;
        this.value = value;
        this.myContext = profActCtxt;
    }

    public boolean canDetermineActivation( Profile profile )
    {
        return canDetermineActivation( profile, this.myContext);
    }

    public boolean canDetermineActivation( Profile profile, ProfileActivationContext context )
    {
        if ( checkConfigurationSanity() && context != null )
        {
            return getModelLineage( ) != null;
        }
        return false;
    }

    private boolean checkConfigurationSanity()
    {
        return name != null;
    }

    public boolean isActive( Profile profile )
    {
        return isActive( profile, this.myContext);
    }

    public boolean isActive( Profile profile, ProfileActivationContext projectContext )
    {
        // currently, just make sure the name configuration is set.
        if ( !checkConfigurationSanity() )
        {
            getLogger().debug( "Skipping profile: " + profile.getId() + ". Reason: modelProperty activation is missing 'name' configuration." );
            return false;
        }
        
        if ( projectContext == null )
        {
            return false;
        }
        
        ModelLineage lineage = getModelLineage( );
        
        if ( lineage == null )
        {
            return false;
        }
        
        String propertyName = name;
        boolean reverse = false;
        
        if ( propertyName.startsWith( "!" ) )
        {
            reverse = true;
            propertyName = propertyName.substring( 1 );
        }
        
        String checkValue = value;
        if ( checkValue != null && checkValue.startsWith( "!" ) )
        {
            reverse = true;
            checkValue = checkValue.substring( 1 );
        }
        
        boolean matches = false;
        
        // iterate through the Model instances that will eventually be calculated as one 
        // inheritance-assembled Model, and see if we can activate the profile based on properties
        // found within one of them. NOTE: iteration starts with the child POM, and goes back through
        // the ancestry.
        for ( Iterator it = lineage.modelIterator(); it.hasNext(); )
        {
            Model model = (Model) it.next();
            
            getLogger().debug( "Searching model: " + model.getId() + " for property: " + propertyName + " having value: " + checkValue + " (if null, only checking for property presence)." );
            
            Properties properties = model.getProperties();
            
            if ( properties == null )
            {
                getLogger().debug( "no properties here. continuing down the lineage." );
                continue;
            }
            
            String retrievedValue = properties.getProperty( propertyName );
            
            if ( value != null )
            {
                // local-most values win, so if the retrievedValue != null in the current POM, NEVER
                // look in the parent POM for a match.
                // If the retrievedValue == null, though, we need to stop looking for a match here.
                if ( retrievedValue == null )
                {
                    getLogger().debug( "property not found here. continuing down the lineage." );
                    continue;
                }
                
                matches = checkValue.equals( retrievedValue );
                
                // if we get here, retrievedValue != null, and we're looking at the local-most POM, so:
                // 
                // if startsWith '!' (reverse == true) and values don't match (match == false), return true
                // if NOT startsWith '!' (reverse == false) and values DO match (match == true), return true
                // else return false
                if ( reverse != matches )
                {
                    getLogger().debug( "Searching for property-value match: matches: " + matches + "; reversed: " + reverse + "; profile: " + profile.getId() + " should be activated." );
                    break;
                }
            }
            // if the value is not specified, then we have to search the entire ancestry before we
            // can say for certain that a property is missing.
            else
            {
                // if startsWith '!' (reverse == true) and retrievedValue == null (true), return true
                // if NOT startsWith '!' (reverse == false) and NOT retrievedValue == null (false), return true
                matches = retrievedValue != null;
                
                getLogger().debug( "Searching for property presence: matches: " + matches + "; reversed: " + reverse + "; stopping lineage search." );
                
                if ( matches )
                {
                    break;
                }
            }
            
            // if we can't definitely say we're activating the profile, go to the next model in the
            // lineage, and try again.
        }
        
        // if we get to the end of the lineage without activating the profile, return false.
        return reverse != matches;
    }
    
    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "ModelPropertyProfileActivator:internal" );
        }
        
        return logger;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    private ModelLineage getModelLineage( )
    {
        getLogger().debug("Build ModelLineage for " + project.getFile());
        Parent parent = project.getModel().getParent();
        if ( parent == null)
        {
            getLogger().debug("  " + project.getFile() + " has no parent specified");
        }
        try
        {
            DefaultModelLineageBuilder builder = (DefaultModelLineageBuilder) session.lookup( ModelLineageBuilder.ROLE, "default" );
            return builder.buildModelLineage(
                project.getFile(), 
                session.getLocalRepository(),
                Collections.EMPTY_LIST,
                null,
                false,
                true);
        }
        catch ( ComponentLookupException e )
        {
            getLogger().error("Get get ModelLineageBuilder for " + project.getFile() + e);
            return null;
        }
        catch ( ProjectBuildingException e )
        {
            getLogger().error("Get get ModelLineage for " + project.getFile() + e);
            return null;
        }
    }
}
