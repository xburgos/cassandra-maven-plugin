package org.codehaus.mojo.bod;

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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.bod.build.BuildException;
import org.codehaus.mojo.bod.build.DependencyBuildRequest;
import org.codehaus.mojo.bod.build.DependencyBuilder;

/**
 * Builds project dependencies as needed.
 *
 * @goal build
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
     * Flag telling this mojo to force the building of all dependency projects.
     *
     * @parameter default-value="true" expression="${force}"
     */
    private boolean force;
    
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
     * @component
     */
    private DependencyBuilder dependencyBuilder;
    
    /**
     * This flag is set to allow using the latest sources of a snapshot dependency.
     * 
     * @parameter expression="${useLatestProjectSources}" default-value="true"
     */
    private boolean useLatestProjectSources;
    
    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    
    /**
     * The user name (used by svn and starteam protocol).
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The user password (used by svn and starteam protocol).
     *
     * @parameter expression="${password}"
     */
    private String password;
    
    /**
     * @parameter expression="${component.org.apache.maven.scm.manager.ScmManager}"
     * @required
     * @readonly
     */
    private ScmManager manager;

    /**
     * The workspace URL from which to retrieve dependency sources from. 
     * Supported protocols are file:// (local directory) and SCM URL formats supported by Maven (e.g. scm:svn:https://) 
     *
     * @parameter expression="${workspaceUrl}"
     */
    private String workspaceUrl;

    public void execute()
        throws MojoFailureException
    {
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
            DependencyBuildRequest request = new DependencyBuildRequest();
            
            request.setBuildPrototype( build );
            request.setCompletedBuilds( buildCache );
            request.setCurrentPendingProjects( reactorProjects );
            request.setForce( force );
            request.setLocalRepository( localRepository );
            request.setPomRewriteConfiguration( rewrite );
            request.setProject( project );
            request.setProjectsDirectory( dependencyProjectsDir );
            request.setUseLatestProjectSources( useLatestProjectSources );
            request.setSettings( settings );
            request.setUsername( username );
            request.setPassword( password );
            request.setManager( manager );
            request.setWorkspaceUrl( workspaceUrl );
            
            dependencyBuilder.buildMissingDependencies( request );
        }
        catch ( BuildException e )
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

    public void setForce( boolean force )
    {
        this.force = force;
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

    public void setDependencyBuilder( DependencyBuilder dependencyBuilder )
    {
        this.dependencyBuilder = dependencyBuilder;
    }

    public void setUseGlobalBuildCache( boolean useGlobalBuildCache )
    {
        this.useGlobalBuildCache = useGlobalBuildCache;
    }

}
