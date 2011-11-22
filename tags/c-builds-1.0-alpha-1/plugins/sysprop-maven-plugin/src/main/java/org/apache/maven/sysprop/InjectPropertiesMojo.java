package org.apache.maven.sysprop;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.ArtifactUtils;
import org.codehaus.plexus.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Inject system properties to the java runtime to be picked up by goals down in the lifecycle.
 *
 * @requiresProject true
 * @goal inject
 * @aggregator
 * @phase initialize
 */

public class InjectPropertiesMojo
    extends AbstractMojo
{
    public static final String INJECTED_PROJECT_IDS = "injected.projects";

    /**
     * The map to hold the properties to be injected to the System class properties.
     *
     * @parameter
     */
    private Map properties;

    /**
     * Whether to overwrite existing system properties.
     *
     * @parameter default-value="true" expression="${injectionOverwrite}"
     */
    private boolean overwrite;

    /**
     * List of project IDs which should be excluded from property injection.
     * Each entry in the list should contain groupId:artifactId of a single project.
     *
     * @parameter expression="${injectionExcludes}"
     */
    private String injectionExcludes;

    /**
     * The projects in the current build.
     *
     * @parameter default-value="${reactorProjects}"
     * @required
     * @readonly
     */
    private List projects;

    public void execute()
        throws MojoExecutionException
    {
        getLog().debug( "prefixRoot is: " + properties.get( "prefixRoot" ) );
        getLog().debug( "applicationName is: " + properties.get( "applicationName" ) );

        if ( properties == null || properties.size() == 0 )
        {
            getLog().info( "No properties to inject." );
            return;
        }

        List injected = new ArrayList();

        for ( Iterator it = projects.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();

            String projectId = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            if ( injectionExcludes == null || injectionExcludes.indexOf( projectId ) < 0 )
            {
                injectProperties( project );
                injected.add( projectId );
            }
        }

        Map sysprops = new HashMap( properties );

        if ( !injected.isEmpty() )
        {
            sysprops.put( INJECTED_PROJECT_IDS, StringUtils.join( injected.iterator(), ", " ) );
        }

        injectIntoSystemProperties( sysprops );

    }

    private void injectIntoSystemProperties( Map sysprops )
    {
        Properties props = System.getProperties();

        getLog().info( "Injecting properties into System properties." );

        if ( !overwrite )
        {
            getLog().debug( "Existing system property values will not be overwritten." );
        }

        for ( Iterator propertyIterator = sysprops.entrySet().iterator(); propertyIterator.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) propertyIterator.next();

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if ( key == null || value == null )
            {
                continue;
            }

            if ( overwrite || System.getProperty( key ) == null )
            {
                props.setProperty( key, value );

                getLog().debug( "Injected property: {\'" + key + "\' = \'" + value + "\'} into system properties." );
            }
        }

        System.setProperties( props );
    }

    private void injectProperties( MavenProject project )
    {
        Model model = project.getModel();

        Properties props = model.getProperties();

        String projectId = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

        getLog().debug( "Injecting properties into: " + projectId );

        if ( !overwrite )
        {
            getLog().debug( "Existing model property values will not be overwritten." );
        }

        for ( Iterator propertyIterator = properties.entrySet().iterator(); propertyIterator.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) propertyIterator.next();

            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if ( key == null || value == null )
            {
                continue;
            }

            if ( overwrite || System.getProperty( key ) == null )
            {
                props.setProperty( key, value );

                getLog().debug( "Injected property: {\'" + key + "\' = \'" + value + "\'} into model properties." );
            }
        }

        model.setProperties( props );
    }

}
