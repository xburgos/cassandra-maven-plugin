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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract Patch class
 *
 */
public abstract class AbstractPatchMojo extends AbstractMojo
{

    /**
     * List of files to ignore like <code>CVS</code> and <code>.git</code>
     */
    public static final List < String > DEFAULT_IGNORED_PATCHES;

    /**
     * List of patterns to ignore like <code>.svn/**</code> and <code>CVS/**</code>
     */
    public static final List < String > DEFAULT_IGNORED_PATCH_PATTERNS;

    static
    {
        List < String > ignored = new ArrayList < String > ();

        ignored.add( ".svn" );
        ignored.add( "CVS" );
        ignored.add( ".git" );

        DEFAULT_IGNORED_PATCHES = ignored;
        
        List < String > ignoredPatterns = new ArrayList < String > ();

        ignoredPatterns.add( ".svn/**" );
        ignoredPatterns.add( "CVS/**" );
        ignoredPatterns.add( ".git/**" );

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
     * The list of patch file names (without directory information), supplying the order in which patches should be
     * applied.
     * 
     * @parameter
     */
    private List < String > patches;

    /**
     * Returns a list of patch file names as strings without the directory information
     * 
     * @return List of strings containing the names of patch files
     */
    protected List < String > getPatches()
    {
        return patches;
    }

    /**
     * Tells the patch goals to ignore default things like <code>.git</code> and <code>.svn</code>
     * 
     * @return boolean value if Mojo should ignore standard things like <code>.git</code> and <code>.svn</code>
     */
    protected boolean useDefaultIgnores()
    {
        return useDefaultIgnores;
    }

    /**
     * Returns the MavenProject
     * 
     * @return the MavenProject object
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Entry point for the Mojo
     * 
     * @throws MojoExecutionException Procedural or configuration failure
     * @throws MojoFailureException Typically happens with a bad configuration like a type-o in a patch name
     */
    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( patches == null || patches.isEmpty() )
        {
            getLog().info( "Patching is disabled for this project." );
            return;
        }
        
        doExecute();
    }

    /**
     * Entry point for most patch-maven-plugin goals
     * 
     * @throws MojoExecutionException Procedural or configuration failure
     * @throws MojoFailureException Typically happens with a bad configuration like a type-o in a patch name
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
