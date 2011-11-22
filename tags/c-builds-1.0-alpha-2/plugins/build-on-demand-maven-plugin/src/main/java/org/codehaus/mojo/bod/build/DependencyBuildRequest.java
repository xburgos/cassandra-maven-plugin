package org.codehaus.mojo.bod.build;

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
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.bod.BuildConfiguration;
import org.codehaus.mojo.bod.PomRewriteConfiguration;

public class DependencyBuildRequest
{

    private MavenProject project;

    private List currentPendingProjects;

    private Set completedBuilds;

    private ArtifactRepository localRepository;

    private PomRewriteConfiguration pomRewriteConfiguration;

    private BuildConfiguration buildPrototype;

    private boolean force = true;

    private File projectsDirectory;
    
    private boolean useLatestProjectSources = true;
    
    private Settings settings;
    
    private String username;
    
    private String password;
    
    private ScmManager manager;
    
    private String workspaceUrl;
    
    public BuildConfiguration getBuildPrototype()
    {
        return buildPrototype;
    }

    public DependencyBuildRequest setBuildPrototype( BuildConfiguration buildPrototype )
    {
        this.buildPrototype = buildPrototype;
        return this;
    }

    public Set getCompletedBuilds()
    {
        return completedBuilds;
    }

    public DependencyBuildRequest setCompletedBuilds( Set completedBuilds )
    {
        this.completedBuilds = completedBuilds;
        return this;
    }

    public boolean isForce()
    {
        return force;
    }

    public DependencyBuildRequest setForce( boolean force )
    {
        this.force = force;
        return this;
    }

    public PomRewriteConfiguration getPomRewriteConfiguration()
    {
        return pomRewriteConfiguration;
    }

    public DependencyBuildRequest setPomRewriteConfiguration( PomRewriteConfiguration injectedInfo )
    {
        this.pomRewriteConfiguration = injectedInfo;
        return this;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public DependencyBuildRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public DependencyBuildRequest setProject( MavenProject project )
    {
        this.project = project;
        return this;
    }

    public File getProjectsDirectory()
    {
        return projectsDirectory;
    }

    public DependencyBuildRequest setProjectsDirectory( File projectsDirectory )
    {
        this.projectsDirectory = projectsDirectory;
        return this;
    }

    public List getCurrentPendingProjects()
    {
        return currentPendingProjects;
    }

    public DependencyBuildRequest setCurrentPendingProjects( List reactorProjects )
    {
        this.currentPendingProjects = reactorProjects;
        return this;
    }
    public boolean isUseLatestProjectSources()
    {
        return useLatestProjectSources;
    }

    public DependencyBuildRequest setUseLatestProjectSources( boolean useLatestSnapshotCodes )
    {
        this.useLatestProjectSources = useLatestSnapshotCodes;
        return this;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public DependencyBuildRequest setSettings( Settings settings )
    {
        this.settings = settings;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public DependencyBuildRequest setPassword( String password )
    {
        this.password = password;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public DependencyBuildRequest setUsername( String username )
    {
        this.username = username;
        return this;
    }

    public ScmManager getManager()
    {
        return manager;
    }

    public DependencyBuildRequest setManager( ScmManager manager )
    {
        this.manager = manager;
        return this;
    }

    public String getWorkspaceUrl()
    {
        return workspaceUrl;
    }

    public void setWorkspaceUrl( String workspaceUrl )
    {
        this.workspaceUrl = workspaceUrl;
    }
}
