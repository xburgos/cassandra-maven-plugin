package org.codehaus.mojo.bod.source;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.location.ArtifactLocatorStrategy;
import org.apache.maven.shared.io.location.FileLocatorStrategy;
import org.apache.maven.shared.io.location.Location;
import org.apache.maven.shared.io.location.Locator;
import org.apache.maven.shared.io.location.URLLocatorStrategy;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.build.DependencyBuildRequest;
import org.codehaus.mojo.bod.source.locator.DirectoryLocatorStrategy;
import org.codehaus.mojo.bod.source.locator.ScmLocatorStrategy;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpander;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpansionException;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpansionRequest;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.interpolation.MapBasedValueSource;
import org.codehaus.plexus.util.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.util.interpolation.RegexBasedInterpolator;



/**
 * @plexus.component role="org.apache.maven.plugin.depbuild.source.ProjectSourceResolver"
 *            role-hint="default"
 *            
 * @author jdcasey
 *
 */
public class DefaultProjectSourceResolver
    extends AbstractLogEnabled
    implements ProjectSourceResolver
{

    public static final String ROLE_HINT = "default";

    /**
     * @plexus.requirement
     */
    private ArtifactResolver artifactResolver;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private ArchiveExpander archiveExpander;

    public DefaultProjectSourceResolver()
    {
    }

    public DefaultProjectSourceResolver( ArtifactFactory artifactFactory, ArtifactResolver artifactResolver,
                                         ArchiveExpander archiveExpander )
    {
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.archiveExpander = archiveExpander;
        
        enableLogging( new ConsoleLogger( Logger.LEVEL_INFO, "default" ) );
    }

    public File resolveProjectSources( MavenProject project, File projectsDirectory,
                                       ArtifactRepository localRepository, MessageHolder errors )
    {
        Properties projectProperties = project.getProperties();

        String type = projectProperties.getProperty( PROJECT_SOURCE_ARCHIVE_EXTENSION );
        
        if ( type == null )
        {
            type = DEFAULT_SOURCE_ARCHIVE_TYPE;
        }

        String url = projectProperties.getProperty( PROJECT_SOURCE_URL );

        // if no URL is specified, try to resolve the project-archive artifact.
        if ( url == null )
        {
            url = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion() + ":" + type + ":"
                + DEFAULT_PROJECT_ARCHIVE_ARTIFACT_CLASSIFIER;
        }

        String projectDirectoryName = projectProperties.getProperty( PROJECT_SOURCE_DIRECTORY_NAME );
        
        if ( projectDirectoryName == null )
        {
            RegexBasedInterpolator rbi = new RegexBasedInterpolator();
            rbi.addValueSource( new ObjectBasedValueSource( project ) );
            rbi.addValueSource( new MapBasedValueSource( System.getProperties() ) );
            
            projectDirectoryName = rbi.interpolate( DEFAULT_SOURCE_DIRECTORY_NAME_PATTERN, "project" );
        }

        MessageHolder messageHolder = new DefaultMessageHolder();
        Locator locator = createLocator( localRepository, project.getRemoteArtifactRepositories(), type, messageHolder );

        Location sourceLocation = locator.resolve( url );
        
        if ( sourceLocation == null )
        {
            errors.addMessage( "Failed to resolve project sources:\n\n" + messageHolder.render() );
            return null;
        }

        return getProjectSourceDirectory( sourceLocation, errors, projectsDirectory, project, projectDirectoryName );
    }

    protected File getProjectSourceDirectory( Location sourceLocation, MessageHolder errors, File projectsDirectory,
                                              MavenProject project, String projectDirectoryName )
    {
        File sources = null;

        try
        {
            sources = sourceLocation.getFile();

        }
        catch ( IOException e )
        {
            errors.addMessage( "Error retrieving project sources.", e );

            return null;
        }

        if ( !sources.isDirectory() )
        {
            ArchiveExpansionRequest request = new ArchiveExpansionRequest();

            request.setSourceArchive( sources );
            request.setExpandTarget( projectsDirectory );
            request.setOverwriteCheckSubpath( projectDirectoryName );

            try
            {
                archiveExpander.expand( request );
            }
            catch ( IOException e )
            {
                errors.addMessage( "Error unpacking project sources.", e );

                return null;
            }
            catch ( ArchiveExpansionException e )
            {
                errors.addMessage( "Error unpacking project sources.", e );

                return null;
            }

            File sourceDir = new File( projectsDirectory, projectDirectoryName );

            if ( !sourceDir.exists() )
            {
                errors.newMessage().append( "Source archive " );
                errors.append( sources.getAbsolutePath() );
                errors.append( " was successfully unpacked into " );
                errors.append( projectsDirectory.getAbsolutePath() );

                errors.append( "\n\n\tHowever, the project source directory: " );
                errors.append( sourceDir.getAbsolutePath() );
                errors.append( " does not exist." );

                errors.append( "\n\tTo correct this, please ensure the \'" );
                errors.append( PROJECT_SOURCE_DIRECTORY_NAME );
                errors.append( "\' property is correctly set in the pom.xml.\n\t(Project-ID: " );
                errors.append( project.getId() );
                errors.append( ")" );

                return null;
            }
            else
            {
                sources = sourceDir;
            }
        }

        return sources;
    }

    public File resolveLatestProjectSources( MavenProject project, MessageHolder errors, DependencyBuildRequest request )
    {
        Properties projectProperties = project.getProperties();

        String projectDirectoryName = projectProperties.getProperty( PROJECT_SOURCE_DIRECTORY_NAME );
        
        if ( projectDirectoryName == null )
        {
            RegexBasedInterpolator rbi = new RegexBasedInterpolator();
            rbi.addValueSource( new ObjectBasedValueSource( project ) );
            rbi.addValueSource( new MapBasedValueSource( System.getProperties() ) );
            
            projectDirectoryName = rbi.interpolate( DEFAULT_SOURCE_DIRECTORY_NAME_PATTERN, "project" );
            
            //TODO replace with a more elegant way of omitting the "-SNAPSHOT" from the name
            if ( projectDirectoryName.endsWith( "-SNAPSHOT" ) )
            {
                projectDirectoryName = projectDirectoryName.substring( 0, projectDirectoryName.indexOf("-SNAPSHOT") );
            }
        }

        File sourceDir = new File( request.getProjectsDirectory(), projectDirectoryName );
        
        MessageHolder messageHolder = new DefaultMessageHolder();
        Locator locator = createLocator( project, request, sourceDir, messageHolder );

        String url = request.getWorkspaceUrl();
        
        getLogger().info("Workspace URL: "+url);    
        
        Location sourceLocation = locator.resolve( url );
        
        if ( sourceLocation == null )
        {
            errors.addMessage( "Failed to resolve project sources:\n\n" + messageHolder.render() );
            return null;
        }
        
        try
        {
            return sourceLocation.getFile();
        }
        catch ( Exception e )
        {
            errors.addMessage( "Failed to resolve project sources:\n\n" + messageHolder.render() );
            return null;
        }

    }

    protected Locator createLocator( ArtifactRepository localRepository, List remoteArtifactRepositories, String type, MessageHolder messageHolder )
    {
        List sourceLocationStrategies = new ArrayList();

        sourceLocationStrategies.add( new FileLocatorStrategy() );
        sourceLocationStrategies.add( new URLLocatorStrategy() );
        sourceLocationStrategies.add( new ArtifactLocatorStrategy( artifactFactory, artifactResolver, localRepository,
                                                                   remoteArtifactRepositories, type ) );

        return new Locator( sourceLocationStrategies, messageHolder );
    }
    
    protected Locator createLocator( MavenProject project, DependencyBuildRequest request, 
                                             File sourceDir, MessageHolder messageHolder )
    {
        List sourceLocationStrategies = new ArrayList();

        sourceLocationStrategies.add( new DirectoryLocatorStrategy( project, sourceDir, 
                                                                    DEFAULT_LOCAL_SOURCE_INCLUDES, DEFAULT_LOCAL_SOURCE_EXCLUDES ) );
        sourceLocationStrategies.add( new ScmLocatorStrategy( project, request.getManager(), 
                                                              request.getUsername(), request.getPassword(),
                                                              request.getSettings(), sourceDir ) );

        return new Locator( sourceLocationStrategies, messageHolder );
    }
    
}
