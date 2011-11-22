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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;

/**
 * Apply a set of patches to project sources.
 * 
 * @goal apply-directory
 * @phase generate-sources
 */
public class ApplyPatchDirectoryMojo
    extends AbstractPatchMojo
{

    /**
     * phrases which indicate a failure returned by the patch utility
     */
    public static final List < String > PATCH_FAILURE_WATCH_PHRASES;

    static
    {
        List < String > watches = new ArrayList < String > ();

        watches.add( "fail" );
        watches.add( "skip" );
        watches.add( "reject" );

        PATCH_FAILURE_WATCH_PHRASES = watches;
    }

    /**
     * Whether to skip this mojo's execution.
     * 
     * @parameter default-value="false" alias="patch.apply.skip"
     * @since 1.0-alpha-2
     */
    private boolean skipApplication;

    /**
     * Flag to enable/disable optimization file from being written. This file tracks the patches that were applied the
     * last time this mojo actually executed. It is required for cases where project-sources optimizations are enabled,
     * since project-sources will not be re-unpacked if they are at least as fresh as the source archive. If we avoid
     * re-unpacking project sources, we need to make sure we don't reapply patches. This flag is true by default. <br/>
     * <b>NOTE:</b> If the list of patches changes and this flag is enabled, a `mvn clean` must be executed before the
     * next build, to remove the tracking file.
     * 
     * Functionality deprecated.  CBUILDS is favoring standard artifact handlers like wagon-maven-plugin and
     * maven-dependency-plugin and deleted project-sources-maven-plugin.  This feature was error prone anyway.
     * If you are working with remote tarballs, run <code>mvn clean install</code> and redo your build.  If you
     * are running from a source code system, its best not to use patches, just edit code in place.
     * 
     * @parameter default-value="false"
     * @since 1.0-alpha-2
     * @deprecated
     */
    private boolean optimizations;

    /**
     * This is the tracking file used to maintain a list of the patches applied to the unpacked project sources which
     * are currently in the target directory. If this file is present, and project-source unpacking is optimized
     * (meaning it won't re-unpack unless the project-sources archive is newer), this mojo will not execute and no
     * patches will be applied in the current build.
     * 
     * @parameter default-value="${project.build.directory}/optimization-files/patches-applied.txt"
     * @since 1.0-alpha-2
     */
    private File patchTrackingFile;

    /**
     * The target directory for applying patches. Files in this directory will be modified.
     * 
     * @parameter alias="patchTargetDir" default-value="${project.build.sourceDirectory}"
     * @since 1.0-alpha-2
     * @required
     */
    private File targetDirectory;

    /**
     * When the strictPatching flag is set, this parameter is useful to mark certain contents of the patch-source
     * directory that should be ignored without causing the build to fail.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private List < String > ignoredPatches;

    /**
     * Flag that, when set to true, will make sure that all patches included in the 'patches' list must be present and
     * describe the full contents of the patch directory. If strictPatching is set to true, and the patches list has a
     * value that does not correspond to a file in the patch directory, the build will fail. If strictPatching is set to
     * true, and the patch directory contains files not listed in the patches parameter, the build will fail. If set to
     * false, only the patches listed in the patches parameter that have corresponding files will be applied; the rest
     * will be ignored. Default value for this parameter is false.
     * 
     * @parameter default-value="false"
     * @since 1.0-alpha-2
     * @required
     */
    private boolean strictPatching;

    /**
     * The number of directories to be stripped from patch file paths, before applying, starting from the leftmost, or
     * root-est.
     * 
     * @parameter
     * @since 1.0-alpha-5
     */
    private int strip = 0;

    /**
     * Whether to ignore whitespaces when applying the patches.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private boolean ignoreWhitespace = true;

    /**
     * Whether to treat these patches as having reversed source and dest in the patch syntax.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private boolean reverse = false;

    /**
     * Whether to make backups of the original files before modding them.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private boolean backups = false;

    /**
     * List of phrases to watch for in patch-command output. If one is found, it will cause
     * the build to fail. All phrases should be lower-case ONLY.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private List < String > patchFailureWatchPhrases = PATCH_FAILURE_WATCH_PHRASES;
    
    /**
     * If your project has never been deployed to a remote repo, your patches will need to be found
     * locally, and <code>patchDirectory</code> will be the location where patch-maven-plugin will
     * find any patches you want to apply.  When downloading patches from a remote repo, this
     * parameter and local patches are ignored.  When patch-maven-plugin uses local patches, it
     * will also generate a zip or tarball bundle of those patches and attach the artifact to the
     * project for later release and deployment to the maven repos.
     *
     * @parameter expression="${patchDirectory}" default-value="src/patches"
     * @since 1.0-beta-1
     * @required
     */
    private File patchDirectory;

    /**
     * If your project's patches are retrieved from a remote repository (as an attached project
     * artifact), the patches will be downloaded and installed into the directory specified by 
     * <code>patchArtifactUnpackDirectory</code>. 
     * 
     * @parameter default-value="${project.build.directory}/unpacked-patches"
     * @since 1.0-beta-1
     * @required
     */
    private File patchArtifactUnpackDirectory;

    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * Apply the patches. Give preference to patchFile over patchSourceDir/patches, and preference to 
     * originalFile over workDir.
     *
     * @throws MojoFailureException thrown if <code>strictPatching</code> set and no patches to apply
     * @throws MojoExecutionException thown if error encountered during patch process
     */
    public void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skipApplication )
        {
            getLog().info( "Skipping patchfile application (per configuration)." );
            return;
        }

        patchTrackingFile.getParentFile().mkdirs();

        PatchContext ctx = new PatchContext( );

        File patchSourceDir = ctx.getPatchDirectory( patchArtifactUnpackDirectory, patchDirectory );

        if ( patchSourceDir == null )
        {
            throw new MojoExecutionException( "Patch directory was not set. Please ensure that you "
                            + "have the resolve-patches mojo bound earlier in the lifecycle.\n\n"
                            + "(Tip: use 'mvn help:effective-pom' for more information.)" );
        }

        List < String > foundPatchFiles = new ArrayList < String > ( Arrays.asList( patchSourceDir.list() ) );

        Map < String, Commandline > patchesApplied = findPatchesToApply( foundPatchFiles, patchSourceDir );

        checkStrictPatchCompliance( foundPatchFiles );

        String output = applyPatches( patchesApplied );

        checkForWatchPhrases( output );

        writeTrackingFile( patchesApplied );
    }

    /**
     * Find and return a list of patches to apply
     * 
     * @param foundPatchFiles list of String objects of patch files found to apply to the project
     * @param patchSourceDir String object of the directory of patches
     * @return Returns a map of patches to apply
     * @throws MojoFailureException thrown if <code>strictPatching</code> set and no patches to be applied
     */
    private Map < String, Commandline > findPatchesToApply( List < String > foundPatchFiles, File patchSourceDir )
        throws MojoFailureException
    {
        List < String > patches = getPatches();

        Map < String, Commandline > patchesApplied = new LinkedHashMap < String, Commandline > ( patches.size() );

        for ( Iterator < String > it = patches.iterator(); it.hasNext(); )
        {
            String patch = it.next();

            File patchFile = new File( patchSourceDir, patch );

            getLog().debug( "Looking for patch: " + patch + " in: " + patchFile );

            if ( !patchFile.exists() )
            {
                if ( strictPatching )
                {
                    throw new MojoFailureException( this,
                        "Patch operation cannot proceed.",
                        "Cannot find specified patch: \'" + patch
                        + "\' in patch-source directory: \'" + patchSourceDir
                        + "\'.\n\nEither fix this error, or relax strictPatching." );
                }
                else
                {
                    getLog().info( "Skipping patch: " + patch 
                        + " listed in the patches parameter; it is missing." );
                }
            }
            else
            {
                foundPatchFiles.remove( patch );

                patchesApplied.put( patch, createPatchCommand( patchFile ) );
            }
        }

        return patchesApplied;
    }

    /**
     * Extra checks done if using <code>strictPatching</code>, will filter patch files from an ignored list
     * depending on settings of <code>ignoredPatches</code> and <code>useDefaultIgnores</code>
     * 
     * @param foundPatchFiles List of patches to apply
     * @throws MojoExecutionException thrown if all the patch files passed are to be ignored
     */
    private void checkStrictPatchCompliance( List < String > foundPatchFiles )
        throws MojoExecutionException
    {
        if ( strictPatching )
        {
            List < String > ignored = new ArrayList < String > ();

            if ( ignoredPatches != null )
            {
                ignored.addAll( ignoredPatches );
            }

            if ( useDefaultIgnores() )
            {
                ignored.addAll( DEFAULT_IGNORED_PATCHES );
            }

            List < String > limbo = new ArrayList < String > ( foundPatchFiles );

            for ( Iterator < String > it = ignored.iterator(); it.hasNext(); )
            {
                String ignoredFile = it.next();

                limbo.remove( ignoredFile );
            }

            if ( !limbo.isEmpty() )
            {
                StringBuffer extraFileBuffer = new StringBuffer();

                extraFileBuffer.append( "Found " + limbo.size() + " unlisted patch files:" );

                for ( Iterator < String > it = foundPatchFiles.iterator(); it.hasNext(); )
                {
                    String patch = it.next();

                    extraFileBuffer.append( "\n  \'" ).append( patch ).append( '\'' );
                }

                extraFileBuffer.append( "\n\nEither remove these files, add them to the "
                    + "patches configuration list, or relax strictPatching." );

                throw new MojoExecutionException( extraFileBuffer.toString() );
            }
        }
    }

    /**
     * 
     * @param patchesApplied Map of String which is the patch and Commandline which is the shell command for the patch
     * @return the output of the patch command is consumed and returned
     * @throws MojoExecutionException thrown when a CommadLineException is caught running the patch command
     */
    private String applyPatches( Map < String, Commandline > patchesApplied )
        throws MojoExecutionException
    {
        final StringWriter outputWriter = new StringWriter();

        StreamConsumer consumer = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( line );
                }

                outputWriter.write( line + "\n" );
            }
        };

        for ( Iterator < Entry < String, Commandline > > it = patchesApplied.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry < String, Commandline > entry = it.next();
            String patchName = ( String ) entry.getKey();
            Commandline cli = ( Commandline ) entry.getValue();

            try
            {
                int result = cliManager.execute( cli, consumer, consumer );
                
                if ( result != 0 )
                {
                    throw new MojoExecutionException( "Patch command failed (exit value != 0). "
                        + "Please see debug output for more information." );
                }
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException( "Failed to apply patch: " + patchName
                                + ". See debug output for more information.", e );
            }
        }

        return outputWriter.toString();
    }

    /**
     * Method writes a tracking file which lists the patches applied for the project
     * 
     * @param patchesApplied Map of String which is the patch and Commandline which is the shell command for the patch
     * @throws MojoExecutionException thrown when an IOException is caught
     */
    private void writeTrackingFile( Map < String, Commandline > patchesApplied )
        throws MojoExecutionException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( patchTrackingFile );

            for ( Iterator < String > it = patchesApplied.keySet().iterator(); it.hasNext(); )
            {
                String patch = it.next();
                writer.write( patch );

                if ( it.hasNext() )
                {
                    writer.write( System.getProperty( "line.separator" ) );
                }
            }

            writer.flush();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write patch-tracking file: "
                + patchTrackingFile, e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * Method scans for error text sent by the patch command
     * 
     * @param output Standard output collected from running a patch command
     * @throws MojoExecutionException throws this exception when a patch error is detected
     */
    private void checkForWatchPhrases( String output )
        throws MojoExecutionException
    {
        for ( Iterator < String > it = patchFailureWatchPhrases.iterator(); it.hasNext(); )
        {
            String phrase = it.next();

            if ( output.indexOf( phrase ) > -1 )
            {
                throw new MojoExecutionException(
                    "Failed to apply patches (detected watch-phrase: \'" + phrase + "\' in output). "
                    + "If this is in error, configure the patchFailureWatchPhrases parameter." );
            }
        }
    }

    /**
     * Add a new Patch task to the Ant calling mechanism. Give preference to originalFile/destFile,
     * then workDir, and finally ${basedir}.
     * 
     * @param patchFile Java File object of patch file to create
     * @return a constructed command line which can be executed to create a patch file
     */
    private Commandline createPatchCommand( File patchFile )
    {
        Commandline cli = new Commandline( new BourneShell() );
        
        cli.setExecutable( "patch" );

        cli.setWorkingDirectory( targetDirectory.getAbsolutePath() );

        cli.createArg().setLine( "-p" + strip );

        if ( ignoreWhitespace )
        {
            cli.createArg().setValue( "-l" );
        }

        if ( reverse )
        {
            cli.createArg().setValue( "-R" );
        }

        if ( backups )
        {
            cli.createArg().setValue( "-b" );
        }

        cli.createArg().setLine( "--input=" + patchFile.getAbsolutePath() );

        return cli;
    }

}
