package org.codehaus.mojo.resolver.bod.pom.rewrite;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Used to override local property and other configuration definitions in 
 * dependency POMs for a particular app-assembly build. It essentially hijacks
 * the properties of dependency POMs to inject application-specific information,
 * to provide uniformity in the stack.
 * 
 * @author jdcasey
 * 
 * @plexus.component role="org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriter"
 *                   role-hint="default"
 *
 */
public class DefaultPomRewriter
    implements PomRewriter, Contextualizable
{

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    private PlexusContainer container;

    private MavenXpp3Reader modelReader = new MavenXpp3Reader();

    private MavenXpp3Writer modelWriter = new MavenXpp3Writer();

    public DefaultPomRewriter()
    {
        // used for plexus-based instantiation.
    }

    public DefaultPomRewriter( MavenProjectBuilder projectBuilder, PlexusContainer container )
    {
        this.projectBuilder = projectBuilder;
        this.container = container;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    // FIXME: We need a way to purge a project from the project-builder cache!
    private void purgeFromProjectBuilderCache( MavenProject project )
        throws ProjectBuildingException
    {
        // IFF this is a DefaultMavenProjectBuilder, we have to use setAccessible() to
        // purge the project from these caches:
        //
        // private Map rawProjectCache = new HashMap();
        // private Map processedProjectCache = new HashMap();
        if ( projectBuilder instanceof DefaultMavenProjectBuilder )
        {
            try
            {
                Field[] fields = new Field[2];

                fields[0] = ReflectionUtils.getFieldByNameIncludingSuperclasses( "rawProjectCache", projectBuilder
                    .getClass() );

                fields[1] = ReflectionUtils.getFieldByNameIncludingSuperclasses( "processedProjectCache",
                                                                                 projectBuilder.getClass() );

                // cache key: groupId + ":" + artifactId + ":" + version
                String cacheKey = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();

                for ( int i = 0; i < fields.length; i++ )
                {
                    boolean accessible = fields[i].isAccessible();

                    try
                    {
                        fields[i].setAccessible( true );
                        Map cache = (Map) fields[i].get( projectBuilder );

                        cache.remove( cacheKey );
                    }
                    finally
                    {
                        fields[i].setAccessible( accessible );
                    }
                }
            }
            catch ( IllegalAccessException e )
            {
                throw new ProjectBuildingException( project.getId(), "Failed to purge project-builder cache.", e );
            }
            catch ( SecurityException e )
            {
                throw new ProjectBuildingException( project.getId(), "Failed to purge project-builder cache.", e );
            }
        }
    }

    public List rewrite( List projectInstances, PomRewriteConfiguration configuration,
                         ArtifactRepository localRepository, MessageHolder errors )
    {
        String includes = null;
        String excludes = null;
        
        if ( configuration == null || configuration.isEmpty() )
        {
            StringBuffer incl = new StringBuffer();
            for ( Iterator it = projectInstances.iterator(); it.hasNext(); )
            {
                MavenProject project = (MavenProject) it.next();
                
                incl.append( ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() ) );
                
                if ( it.hasNext() )
                {
                    incl.append( ',' );
                }
            }
            
            includes = incl.toString();
            excludes = "";
        }
        else
        {
            includes = configuration.getIncludesAsCSV();
            excludes = configuration.getExcludesAsCSV();
        }

        List changedInstances = new ArrayList( projectInstances.size() );

        for ( Iterator it = projectInstances.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();
            String versionlessKey = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            // if explicitly included OR NOT explicitly excluded...
            if ( includes.indexOf( versionlessKey ) > -1 || excludes.indexOf( versionlessKey ) < 0 )
            {
                MavenProject original = project;

                try
                {
                    project = rewriteProject( project, configuration, localRepository );

                    project.setFile( original.getFile() );
                }
                catch ( IOException e )
                {
                    errors.addMessage( "Failed to rewrite project: " + project.getId(), e );
                }
                catch ( ProjectBuildingException e )
                {
                    errors.addMessage( "Failed to rewrite project: " + project.getId(), e );
                }
            }

            changedInstances.add( project );
        }

        return changedInstances;
    }

    private Model rewriteModelTo( Model model, PomRewriteConfiguration configuration, File target )
        throws IOException
    {
        DistributionManagement distMgmt = model.getDistributionManagement();
        if ( distMgmt != null )
        {
            DistributionManagement newDistMgmt = new DistributionManagement();
            newDistMgmt.setDownloadUrl( distMgmt.getDownloadUrl() );
            newDistMgmt.setRelocation( distMgmt.getRelocation() );
            newDistMgmt.setRepository( distMgmt.getRepository() );
            newDistMgmt.setSite( distMgmt.getSite() );
            newDistMgmt.setSnapshotRepository( distMgmt.getSnapshotRepository() );
            
            model.setDistributionManagement( newDistMgmt );
        }
        
        if ( configuration != null )
        {
            Properties injectorProperties = configuration.getProperties();

            Properties modelProperties = model.getProperties();
            modelProperties.putAll( injectorProperties );
        }
        
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( target );

            modelWriter.write( writer, model );
        }
        finally
        {
            IOUtil.close( writer );
        }

        return model;
    }

    public void rewriteOnDisk( File pomFile, PomRewriteConfiguration configuration, MessageHolder errors )
    {
        FileReader reader = null;

        Model model = null;

        try
        {
            reader = new FileReader( pomFile );

            model = modelReader.read( reader );
        }
        catch ( IOException e )
        {
            errors.addMessage( "Error reading POM from: " + pomFile, e );
        }
        catch ( XmlPullParserException e )
        {
            errors.addMessage( "Error reading POM from: " + pomFile, e );
        }
        finally
        {
            IOUtil.close( reader );
        }

        if ( model != null )
        {
            try
            {
                if ( configuration != null && configuration.getParentArtifactId() != null )
                {
                    model.getParent().setArtifactId( configuration.getParentArtifactId() );
                }

                rewriteModelTo( model, configuration, pomFile );
            }
            catch ( IOException e )
            {
                errors.addMessage( "Error writing POM to: " + pomFile, e );
            }
        }
    }

    private MavenProject rewriteProject( MavenProject project, PomRewriteConfiguration configuration,
                                         ArtifactRepository localRepository )
        throws IOException, ProjectBuildingException
    {
        purgeFromProjectBuilderCache( project );

        MavenProject rewritten = project;

        Model model = project.getOriginalModel();

        File tempPom = File.createTempFile( project.getArtifactId() + ".rewritten.", ".pom" );
        tempPom.deleteOnExit();

        model = rewriteModelTo( model, configuration, tempPom );

        ProfileManager profileMgr = new DefaultProfileManager( container );
        profileMgr.explicitlyActivate( project.getActiveProfiles() );

        rewritten = projectBuilder.build( tempPom, localRepository, profileMgr );

        return rewritten;
    }

}
