package org.codehaus.mojo.patch;

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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.taskdefs.Patch;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;

/**
 * Apply a set of patches to project sources.
 * 
 * @goal apply
 */
public class ApplyPatchesMojo extends AbstractPatchMojo
{

    /**
     * Whether to skip this mojo's execution.
     * 
     * @parameter default-value="false" alias="patch.apply.skip"
     */
    private boolean skipApplication;

    // cwb >>>>
    // parameter alias="patchTargetDir"
    // default-value="${project.build.directory}/${project.artifactId}-${project.version}

    /**
     * The target directory for applying patches. Files in this directory will be modified.
     * 
     * @parameter alias="patchTargetDir" default-value="${project.build.directory}"
     */
    private File workDir;

    /**
     * The original file which will be modified by the patch. Mutually exclusive with workDir.
     * 
     * @parameter
     */
    private File originalFile;

    /**
     * The file which is the original file, plus modifications from the patch. Mutually exclusive with workDir.
     * 
     * @parameter
     */
    private File destFile;

    /**
     * The source directory from which to find patch files.
     * 
     * @parameter default-value="${project.build.directory}/patches"
     */
    private File patchSourceDir;

    /**
     * The list of patch files (relative to 'patchSourceDir') which should be applied.
     * 
     * @parameter
     */
    private List patches;

    /**
     * The single patch file to apply. Mutually exclusive with 'patches'.
     * 
     * @parameter
     */
    private File patchFile;

    /**
     * The number of directories to be stripped from patch file paths, before applying, starting from the leftmost, or
     * root-est.
     * 
     * @parameter
     */
    private int strip = 0;

    /**
     * Whether to ignore whitespaces when applying the patches.
     * 
     * @parameter
     */
    private boolean ignoreWhitespace = true;

    /**
     * Whether to treat these patches as having reversed source and dest in the patch syntax.
     * 
     * @parameter
     */
    private boolean reverse = false;

    /**
     * Whether to make backups of the original files before modding them.
     * 
     * @parameter
     */
    private boolean backups = false;

    /**
     * The Ant messageLevel to use.
     * 
     * @parameter expression="${messageLevel}" default-value="info"
     */
    private String messageLevel;

    /**
     * Apply the patches. Give preference to patchFile over patchSourceDir/patches, and preference to originalFile over
     * workDir.
     */
    public void doExecute() throws MojoExecutionException, MojoFailureException
    {
        if ( skipApplication )
        {
            getLog().info( "Skipping patchfile application (per configuration)." );
            return;
        }

        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );

        if ( messageLevel != null )
        {
            antCaller.setMessageLevel( messageLevel );
        }

        if ( patchFile != null )
        {
            if ( !patchFile.exists() )
            {
                throw new MojoFailureException( this, "Patch operation cannot proceed.",
                                                "Cannot find specified patch: \'" + patchFile.getPath() );
            }
            else
            {
                addPatch( antCaller, patchFile );
            }
        }
        else if ( patches != null )
        {
            for ( Iterator it = patches.iterator(); it.hasNext(); )
            {
                String patch = (String) it.next();

                File patchFile = new File( patchSourceDir, patch );

                if ( !patchFile.exists() )
                {
                    throw new MojoFailureException( this, "Patch operation cannot proceed.",
                                                    "Cannot find specified patch: \'" + patch
                                                                    + "\' in patch-source directory: \'"
                                                                    + patchSourceDir + "\'." );
                }
                else
                {
                    addPatch( antCaller, patchFile );
                }
            }
        }
        else
        {
            getLog().info( "Nothing to do." );
            return;
        }

        try
        {
            antCaller.executeTasks( getProject() );
        }
        catch ( AntExecutionException e )
        {
            throw new MojoExecutionException( "Error applying patches.", e );
        }
    }

    /**
     * Add a new Patch task to the Ant calling mechanism. Give preference to originalFile/destFile, then workDir, and
     * finally ${basedir}.
     */
    private void addPatch( AntCaller antCaller, File patchFile )
    {
        Patch patchTask = new Patch();

        if ( originalFile != null )
        {
            patchTask.setOriginalfile( originalFile );

            if ( destFile != null )
            {
                patchTask.setDestfile( destFile );
            }
        }
        else if ( workDir != null )
        {
            patchTask.setDir( workDir );
        }
        else
        {
            getLog().info(
                           "We'll be patching the project basedir...keep all hands and legs inside the bus, and hold on!" );
        }

        patchTask.setPatchfile( patchFile );
        patchTask.setStrip( strip );
        patchTask.setIgnorewhitespace( ignoreWhitespace );
        patchTask.setReverse( reverse );
        patchTask.setBackups( backups );

        patchTask.setQuiet( false );

        antCaller.addTask( patchTask );
    }

}
