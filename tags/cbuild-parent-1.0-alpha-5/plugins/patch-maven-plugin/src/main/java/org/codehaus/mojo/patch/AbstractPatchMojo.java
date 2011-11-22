package org.codehaus.mojo.patch;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.context.Context;

public abstract class AbstractPatchMojo extends AbstractMojo
{
    
    public static final List DEFAULT_IGNORED_PATCHES;
    
    public static final List DEFAULT_IGNORED_PATCH_PATTERNS;

    static
    {
        List ignored = new ArrayList();

        ignored.add( ".svn" );
        ignored.add( "CVS" );

        DEFAULT_IGNORED_PATCHES = ignored;
        
        List ignoredPatterns = new ArrayList();

        ignoredPatterns.add( ".svn/**" );
        ignoredPatterns.add( "CVS/**" );

        DEFAULT_IGNORED_PATCH_PATTERNS = ignoredPatterns;
    }
    
    /**
     * Whether to exclude default ignored patch items, such as .svn or CVS directories.
     * 
     * @parameter default-value="true"
     */
    private boolean useDefaultIgnores;

    /**
     * MavenProject instance used to read definitions from the POM like packaging.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * MavenSession will be used in Maven 3.0 so BuildAdvisor can use memory instead of a temp file.
     *
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;
    
    /**
     * BuildAdvisor is a get/set cookie store for plugins to share state.
     *
     * @component role-hint="default"
     */
    protected BuildAdvisor buildAdvisor;

    /**
     * The list of patch file names (without directory information), supplying the order in which patches should be
     * applied.
     * 
     * @parameter
     */
    private List patches;

    protected List getPatches()
    {
        return patches;
    }

    protected boolean useDefaultIgnores()
    {
        return useDefaultIgnores;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected MavenSession getSession()
    {
        return session;
    }

    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( patches == null || patches.isEmpty() )
        {
            getLog().info( "Patching is disabled for this project." );
            return;
        }
        
        if ( buildAdvisor.isProjectBuildSkipped( session ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        doExecute();
    }
    
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
