package org.codehaus.mojo.multibuild.optimizer;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.project.extras.ScanningUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * If the resulting package file from a previous build is encountered, this mojo will determine
 * whether that file is newer than the newest source file. If so, it will advise any other plugins
 * that check to skip the build.
 *
 * @goal check-package-staleness
 * @phase validate
 */
public class SkipIfBuiltOptimizerMojo
    extends AbstractMojo
{
    
    private static final Set DEFAULT_INCLUDES;
    
    static
    {
        Set defaultIncludes = new HashSet();
        
        defaultIncludes.add( "**/*" );
        
        DEFAULT_INCLUDES = defaultIncludes;
    }
    
    /**
     * Whether to override the behavior of this mojo and force a build under any circumstances.
     * 
     * @parameter default-value="false" expression="${forceBuild}" alias="build.optimizer.override"
     */
    private boolean forceBuild;
    
    /**
     * The list of source directories to check for lastMod.
     * 
     * @parameter default-value="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List compileSourceRoots;
    
    /**
     * The directory to check for updates, if not the compileSourceRoots...
     * 
     * @parameter
     */
    private File checkDirectory;
    
    /**
     * The list of patterns for files which should be included in the lastMod check.
     * @parameter
     */
    private Set includes;
    
    /**
     * The list of patterns for files which should be excluded from the lastMod check.
     * @parameter
     */
    private Set excludes;
    
    /**
     * The artifact resulting from the project being built.
     * 
     * @parameter default-value="${project.artifact}"
     * @required
     * @readonly
     */
    private Artifact projectArtifact;
    
    /**
     * The project currently being built.
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The source artifact to check, if the <code>checkArtifact</code> flag is set.
     * 
     * @parameter default-value="tar.gz"
     */
    private String type;
    
    /**
     * Flag telling this mojo to check the lastMod on the source artifact rather
     * than other source locations.
     * 
     * @parameter default-value="false" expression="${checkSourceArtifact}"
     */
    private boolean checkArtifact;
    
    /**
     * URL from which to retrieve the project sources.
     * 
     * @parameter default-value="${srcUrl}"
     */
    private String archiveUrl;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;
    
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * @component
     */
    private ArtifactFactory artifactFactory;
    
    /**
     * @component
     */
    private ArtifactResolver resolver;
    
    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( forceBuild )
        {
            getLog().debug( "The current build has been forced. Optimizations are disabled." );
            return;
        }
        
        File buildResult = projectArtifact.getFile();

        if ( buildResult == null || !buildResult.exists() )
        {
            getLog().debug( "Project's package file: " + buildResult + " does not exist. Build should continue." );
            return;
        }
        
        Long lastMod = null;
        
        if ( checkArtifact )
        {
            try
            {
                lastMod = checkArtifact();
            }
            catch ( ArtifactResolutionException e )
            {
                getLog().debug( "Error checking for package staleness: " + e.getMessage() + ". Build will proceed.", e );
                lastMod = null;
            }
            catch ( ArtifactNotFoundException e )
            {
                getLog().debug( "Error checking for package staleness: " + e.getMessage() + ". Build will proceed.", e );
                lastMod = null;
            }
        }
        else
        {
            lastMod = checkFiles();
        }
        
        if ( lastMod == null || lastMod.longValue() > buildResult.lastModified() )
        {
            StringBuffer message = new StringBuffer();
            
            message.append( "Project's package file: ").append( buildResult ).append( " is stale. Build should continue." );
            
            if ( lastMod != null )
            {
                message.append( "\nLatest source modification: " ).append( new Date( lastMod.longValue() ) );
            }
            
            message.append( "\nPackage file modification: " ).append( new Date( buildResult.lastModified() ) );
        }
        else
        {
            getLog().info( "Project sources have not changed since last build. Build should be skipped." );
            
            BuildAdvisor.skipProjectBuild( project, session.getContainer().getContext() );
        }
    }
    
    private Long checkArtifact() throws ArtifactResolutionException, ArtifactNotFoundException
    {
        Long lastMod = null;
        
        File sourceArchive = null;

        if ( StringUtils.isNotEmpty( archiveUrl ) )
        {
            sourceArchive = new File( archiveUrl );
            
            if ( sourceArchive.exists() )
            {
                getLog().info( "Using source archive from local directory: " + sourceArchive );
                
                sourceArchive = new File( archiveUrl );
            }
            else
            {
                getLog().info( "Using source archive from repository." );
                
                Artifact sourceArtifact = artifactFactory.createArtifactWithClassifier( projectArtifact.getGroupId(),
                                                                                        projectArtifact.getArtifactId(),
                                                                                        projectArtifact.getVersion(),
                                                                                        type, "sources" );
                
                resolver.resolve( sourceArtifact, remoteRepositories, localRepository );
                
                sourceArchive = sourceArtifact.getFile();
            }
        }
        
        if ( sourceArchive != null && sourceArchive.exists() )
        {
            lastMod = new Long( sourceArchive.lastModified() );
        }
        
        return lastMod;
    }

    private Long checkFiles()
    {
        Long lastMod = null;
        
        List toCheck;
        if ( checkDirectory != null )
        {
            getLog().info( "Checking alternate source location: " + checkDirectory + " for staleness." );
            
            toCheck = Collections.singletonList( checkDirectory.getAbsolutePath() );
        }
        else
        {
            getLog().info( "Checking compile source roots: " + compileSourceRoots + " for staleness." );
            
            toCheck = compileSourceRoots;
        }
        
        if ( toCheck != null && !toCheck.isEmpty() )
        {
            for ( Iterator it = toCheck.iterator(); it.hasNext(); )
            {
                String sourcePath = (String) it.next();

                if ( sourcePath != null )
                {
                    File sourceLocation = new File( sourcePath );
                    
                    if ( sourceLocation.exists())
                    {
                        long lm = 0;
                        
                        if ( sourceLocation.isDirectory() )
                        {
                            Set includePatterns;
                            if ( includes == null || includes.isEmpty() )
                            {
                                includePatterns = DEFAULT_INCLUDES;
                            }
                            else
                            {
                                includePatterns = includes;
                            }

                            lm = ScanningUtils.getLatestLastMod( sourceLocation, includePatterns, excludes );
                        }
                        else
                        {
                            lm = sourceLocation.lastModified();
                        }

                        if ( lastMod == null || lm > lastMod.longValue() )
                        {
                            lastMod = new Long( lm );
                        }
                    }
                }                
            }
        }
        
        return lastMod;
    }
    
}
