package org.codehaus.mojo.resolver.bod.mojo;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionManager;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionRequest;
import org.codehaus.mojo.resolver.bod.build.BuildConfiguration;
import org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriteConfiguration;

/**
 * Builds project dependencies as needed.
 *
 * @goal resolve
 * @phase initialize
 */
public class BuildOnDemandMojo
    extends AbstractMojo
{

    private static final BuildConfiguration DEFAULT_BUILD_CONFIGURATION;
    
    static
    {
        BuildConfiguration config = new BuildConfiguration();
        
        config.setGoals( Collections.singletonList( "install" ) );
        
        DEFAULT_BUILD_CONFIGURATION = config;
    }

    private static Set globalBuildCache;
    
    /**
     * Execution mode for the build-on-demand resolution feature. This parameter must have a value
     * of 'build-on-demand', 'binary-only', or 'source-only'. The 'build-on-demand' setting (the 
     * default) means that the resolver will build binaries using the deployed project 
     * sources/patches for any dependency that doesn't have the appropriate binary available in the 
     * repository.
     * 
     * @parameter expression="${buildOnDemand.mode}" default-value="build-on-demand"
     * @required
     */
    private String resolutionMode;

    /**
     * @parameter default-value="${reactorProjects}"
     * @required
     * @readonly
     */
    private List reactorProjects;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Flag indicating whether to track dependency builds in a global cache,
     * or per-main-project. If true, then all dependencies of all projects in
     * the current <b>multimodule</b> build will be built at most once. If false,
     * dependencies will be built at most once <b>per project in the the 
     * multimodule build</b>. This should usually be false, to enable building
     * application assemblies during the course of a multimodule build.
     * 
     * @parameter default-value="false" expression="${useGlobalBuildCache}"
     */
    private boolean useGlobalBuildCache;

    /**
     * Lifecycle phase or goal to execute when building dependency projects.
     *
     * @parameter
     */
    private BuildConfiguration build = DEFAULT_BUILD_CONFIGURATION;
    
    /**
     * Configuration for rewriting POMs to contain application-assembly-specific
     * information, such as a common package name prefix for all builds that 
     * constitute the app-assembly.
     * 
     * @parameter
     */
    private PomRewriteConfiguration rewrite;

    /**
     * This is the parent directory for any projects whose sources must be 
     * resolved and unpacked, in order to be built.
     * 
     * @parameter default-value="${project.build.directory}/dependency-projects"
     * @required
     */
    private File dependencyProjectsDir;

    /**
     * @component roleHint="default"
     */
    private BuildOnDemandResolutionManager buildOnDemandResolutionManager;
    
    /**
     * @parameter default-value="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !BuildOnDemandResolutionRequest.isModeValid( resolutionMode ) )
        {
            throw new MojoExecutionException( BuildOnDemandResolutionRequest.getInvalidModeMessage( resolutionMode ) );
        }
        
        Set buildCache;
        
        if ( useGlobalBuildCache )
        {
            synchronized ( this )
            {
                if ( globalBuildCache == null )
                {
                    globalBuildCache = new HashSet();
                }
            }
            
            buildCache = globalBuildCache;
        }
        else
        {
            buildCache = new HashSet();
        }
        
        if ( getLog().isDebugEnabled() )
        {
            build.setDebug( true );
        }
        
        try
        {
            BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
            
            // We'll override this, since it can cause massive problems during dependency builds.
            build.setOffline( settings.isOffline() );
            
            request.setMode( resolutionMode );
            request.setBuildPrototype( build );
            request.setCompletedBuilds( buildCache );
            request.setCurrentPendingProjects( reactorProjects );
            request.setLocalRepository( localRepository );
            request.setPomRewriteConfiguration( rewrite );
            request.setProject( project );
            request.setProjectsDirectory( dependencyProjectsDir );
            request.setSettings( settings );
            
            buildOnDemandResolutionManager.resolveDependencies( request );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            MojoFailureException error = new MojoFailureException( e, "Failed to build dependency projects.",
                                                                   "Failed to build dependency projects. Reason: "
                                                                       + e.getMessage() );

            error.initCause( e );

            throw error;
        }
    }

    public void setBuild( BuildConfiguration build )
    {
        this.build = build;
    }

    public void setDependencyProjectsDir( File dependencyProjectsDir )
    {
        this.dependencyProjectsDir = dependencyProjectsDir;
    }

    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    public void setReactorProjects( List reactorProjects )
    {
        this.reactorProjects = reactorProjects;
    }

    public void setDependencyBuilder( BuildOnDemandResolutionManager buildOnDemandResolutionManager )
    {
        this.buildOnDemandResolutionManager = buildOnDemandResolutionManager;
    }

    public void setUseGlobalBuildCache( boolean useGlobalBuildCache )
    {
        this.useGlobalBuildCache = useGlobalBuildCache;
    }
    
    public void setSettings( Settings settings )
    {
        this.settings = settings;
    }

    public void setResolutionMode( String resolutionMode )
    {
        this.resolutionMode = resolutionMode;
    }

}
