package org.codehaus.mojo.resolver.bod;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.resolver.bod.build.BuildConfiguration;
import org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriteConfiguration;

public class BuildOnDemandResolutionRequest
{
    
    public static final String MODE_BUILD_ON_DEMAND = "build-on-demand";
    
    public static final String MODE_BINARY_ONLY = "binary-only";
    
    public static final String MODE_SOURCE_ONLY = "source-only";
    
    private static final Set < String > MODES;
    
    static
    {
        Set < String > modes = new HashSet < String > ();
        
        modes.add( MODE_BUILD_ON_DEMAND );
        modes.add( MODE_BINARY_ONLY );
        modes.add( MODE_SOURCE_ONLY );
        
        MODES = modes;
    }

    private MavenProject project;

    private List < MavenProject > currentPendingProjects;

    private Set < String > completedBuilds;

    private ArtifactRepository localRepository;

    private PomRewriteConfiguration pomRewriteConfiguration;

    private BuildConfiguration buildPrototype;

    private File projectsDirectory;
    
    private Settings settings;
    
    private String mode = MODE_BUILD_ON_DEMAND;
    
    public BuildConfiguration getBuildPrototype()
    {
        return buildPrototype;
    }

    public BuildOnDemandResolutionRequest setBuildPrototype( BuildConfiguration buildPrototype )
    {
        this.buildPrototype = buildPrototype;
        return this;
    }

    public Set < String > getCompletedBuilds()
    {
        return completedBuilds;
    }

    public BuildOnDemandResolutionRequest setCompletedBuilds( Set < String > completedBuilds )
    {
        this.completedBuilds = completedBuilds;
        return this;
    }

    public PomRewriteConfiguration getPomRewriteConfiguration()
    {
        return pomRewriteConfiguration;
    }

    public BuildOnDemandResolutionRequest setPomRewriteConfiguration( PomRewriteConfiguration injectedInfo )
    {
        this.pomRewriteConfiguration = injectedInfo;
        return this;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public BuildOnDemandResolutionRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
        return this;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public BuildOnDemandResolutionRequest setProject( MavenProject project )
    {
        this.project = project;
        return this;
    }

    public File getProjectsDirectory()
    {
        return projectsDirectory;
    }

    public BuildOnDemandResolutionRequest setProjectsDirectory( File projectsDirectory )
    {
        this.projectsDirectory = projectsDirectory;
        return this;
    }

    public List < MavenProject > getCurrentPendingProjects()
    {
        return currentPendingProjects;
    }

    public BuildOnDemandResolutionRequest setCurrentPendingProjects( List < MavenProject > reactorProjects )
    {
        this.currentPendingProjects = reactorProjects;
        return this;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public BuildOnDemandResolutionRequest setSettings( Settings settings )
    {
        this.settings = settings;
        return this;
    }
    
    public String getMode()
    {
        return mode;
    }
    
    public boolean useBuildOnDemandMode()
    {
        return MODE_BUILD_ON_DEMAND.equals( mode );
    }

    public boolean useBinaryOnlyMode()
    {
        return MODE_BINARY_ONLY.equals( mode );
    }

    public boolean useSourceOnlyMode()
    {
        return MODE_SOURCE_ONLY.equals( mode );
    }
    
    public BuildOnDemandResolutionRequest setMode( String mode )
    {
        if ( mode == null || !MODES.contains( mode.toLowerCase() ) )
        {
            throw new IllegalArgumentException( getInvalidModeMessage( mode ) );
        }
        
        this.mode = mode.toLowerCase();
        
        return this;
    }
    
    public static boolean isModeValid( String mode )
    {
        return mode != null && MODES.contains( mode.toLowerCase() );
    }

    public static String getInvalidModeMessage( String mode )
    {
        return "Invalid build-on-demand resolution mode: \'" + mode + "\' (must be one of: \'" + MODE_BUILD_ON_DEMAND
                        + "\', \'" + MODE_BINARY_ONLY + "\', \'" + MODE_SOURCE_ONLY + "\').";
    }

}
