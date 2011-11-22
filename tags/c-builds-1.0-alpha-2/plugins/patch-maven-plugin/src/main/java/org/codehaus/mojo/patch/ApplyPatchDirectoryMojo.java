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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
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
 * @goal apply-directory
 * @phase generate-sources
 */
public class ApplyPatchDirectoryMojo
    extends AbstractPatchMojo
{

    public static final List PATCH_FAILURE_WATCH_PHRASES;

    public static final List DEFAULT_IGNORED_PATCHES;

    static
    {
        List ignored = new ArrayList();

        ignored.add( ".svn" );
        ignored.add( "CVS" );

        DEFAULT_IGNORED_PATCHES = ignored;

        List watches = new ArrayList();

        watches.add( "fail" );
        watches.add( "skip" );
        watches.add( "reject" );

        PATCH_FAILURE_WATCH_PHRASES = watches;
    }

    /**
     * Whether to skip this mojo's execution.
     * 
     * @parameter default-value="false" alias="patch.apply.skip"
     */
    private boolean skipApplication;

    /**
     * The target directory for applying patches. Files in this directory will be modified.
     * 
     * @parameter alias="patchTargetDir" default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File targetDirectory;

    /**
     * The list of patch file names (without directory information), supplying the order in which patches should be
     * applied.
     * 
     * @parameter
     */
    private List patches;

    /**
     * When the strictPatching flag is set, this parameter is useful to mark certain contents of the patch-source
     * directory that should be ignored without causing the build to fail.
     * 
     * @parameter
     */
    private List ignoredPatches;

    /**
     * Whether to exclude default ignored patch items, such as .svn or CVS directories.
     * 
     * @parameter default-value="true"
     */
    private boolean useDefaultIgnoredPatches;

    /**
     * Flag that, when set to true, will make sure that all patches included in the 'patches' list must be present and
     * describe the full contents of the patch directory. If strictPatching is set to true, and the patches list has a
     * value that does not correspond to a file in the patch directory, the build will fail. If strictPatching is set to
     * true, and the patch directory contains files not listed in the patches parameter, the build will fail. If set to
     * false, only the patches listed in the patches parameter that have corresponding files will be applied; the rest
     * will be ignored. Default value for this parameter is true.
     * 
     * @parameter default-value="true"
     * @required
     */
    private boolean strictPatching;

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
     * List of phrases to watch for in patch-command output. If one is found, it will cause the build to fail. All
     * phrases should be lower-case ONLY.
     * 
     * @parameter
     */
    private List patchFailureWatchPhrases = PATCH_FAILURE_WATCH_PHRASES;

    /**
     * Apply the patches. Give preference to patchFile over patchSourceDir/patches, and preference to originalFile over
     * workDir.
     */
    public void doExecute()
        throws MojoExecutionException, MojoFailureException
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

        PatchContext ctx = PatchContext.read( getSessionContext(), getProject() );

        File patchSourceDir = ctx.getPatchDirectory();

        if ( patchSourceDir == null )
        {
            throw new MojoExecutionException( "Patch directory was not set. Please ensure that you "
                            + "have the resolve-patches mojo bound earlier in the lifecycle.\n\n"
                            + "(Tip: use 'mvn help:effective-pom' for more information.)" );
        }

        List foundPatchFiles = new ArrayList( Arrays.asList( patchSourceDir.list() ) );

        for ( Iterator it = patches.iterator(); it.hasNext(); )
        {
            String patch = (String) it.next();

            File patchFile = new File( patchSourceDir, patch );

            if ( !patchFile.exists() )
            {
                if ( strictPatching )
                {
                    throw new MojoFailureException(
                                                    this,
                                                    "Patch operation cannot proceed.",
                                                    "Cannot find specified patch: \'"
                                                                    + patch
                                                                    + "\' in patch-source directory: \'"
                                                                    + patchSourceDir
                                                                    + "\'.\n\nEither fix this error, or relax strictPatching." );
                }
                else
                {
                    getLog().info( "Skipping patch: " + patch + " listed in the patches parameter; it is missing." );
                }
            }
            else
            {
                foundPatchFiles.remove( patch );

                addPatch( antCaller, patchFile );
            }
        }

        List ignored = new ArrayList();

        if ( ignoredPatches != null )
        {
            ignored.addAll( ignoredPatches );
        }

        if ( useDefaultIgnoredPatches )
        {
            ignored.addAll( DEFAULT_IGNORED_PATCHES );
        }

        if ( strictPatching && !foundPatchFiles.isEmpty() )
        {
            StringBuffer extraFileBuffer = new StringBuffer();

            extraFileBuffer.append( "Found unlisted patch files:" );

            for ( Iterator it = foundPatchFiles.iterator(); it.hasNext(); )
            {
                String patch = (String) it.next();

                if ( !ignored.contains( patch ) )
                {
                    extraFileBuffer.append( "\n  " ).append( patch );
                }
            }

            extraFileBuffer.append( "\n\nEither remove these files, add them to the patches configuration list, or relax strictPatching." );

            throw new MojoExecutionException( extraFileBuffer.toString() );
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream( baos );

        antCaller.setErrorStream( stream );
        antCaller.setOutputStream( stream );

        try
        {
            antCaller.executeTasks( getProject() );
        }
        catch ( AntExecutionException e )
        {
            throw new MojoExecutionException( "Error applying patches.", e );
        }

        String output = new String( baos.toByteArray() ).toLowerCase();

        for ( Iterator it = patchFailureWatchPhrases.iterator(); it.hasNext(); )
        {
            String phrase = (String) it.next();

            if ( output.indexOf( phrase ) > -1 )
            {
                throw new MojoExecutionException( "Failed to apply patches (detected watch-phrase: \'" + phrase
                                + "\' in output). "
                                + "If this is in error, configure the patchFailureWatchPhrases parameter." );
            }
        }
    }

    /**
     * Add a new Patch task to the Ant calling mechanism. Give preference to originalFile/destFile, then workDir, and
     * finally ${basedir}.
     */
    private void addPatch( AntCaller antCaller, File patchFile )
    {
        Patch patchTask = new Patch();

        patchTask.setDir( targetDirectory );
        patchTask.setPatchfile( patchFile );
        patchTask.setStrip( strip );
        patchTask.setIgnorewhitespace( ignoreWhitespace );
        patchTask.setReverse( reverse );
        patchTask.setBackups( backups );

        patchTask.setQuiet( false );

        antCaller.addTask( patchTask );
    }

}
