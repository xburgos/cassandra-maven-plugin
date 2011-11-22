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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

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
    private List < String > patches;

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
     * @parameter default-value="0"
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
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * Apply the patches. Give preference to patchFile over patchSourceDir/patches, and preference to originalFile over
     * workDir.
     * 
     * @throws MojoFailureException thrown if listed patch file does not exist
     * @throws MojoExecutionException thown if error encountered during patch process
     */
    public void doExecute() throws MojoExecutionException, MojoFailureException
    {
        if ( skipApplication )
        {
            getLog().info( "Skipping patchfile application (per configuration)." );
            return;
        }

        Map < String, Commandline > patchCommandsByFileName = new LinkedHashMap < String, Commandline > ();
        
        if ( patchFile != null )
        {
            if ( !patchFile.exists() )
            {
                throw new MojoFailureException( this, "Patch operation cannot proceed.",
                                                "Cannot find specified patch: \'" + patchFile.getPath() );
            }
            else
            {
                patchCommandsByFileName.put( patchFile.getName(), createPatchCommand( patchFile ) );
            }
        }
        else if ( patches != null )
        {
            for ( Iterator < String > it = patches.iterator(); it.hasNext(); )
            {
                String patch = (String) it.next();

                File myPatch = new File( patchSourceDir, patch );

                if ( !myPatch.exists() )
                {
                    throw new MojoFailureException( this, "Patch operation cannot proceed.", 
                      "Cannot find specified patch: \'" + patch + "\' in patch-source directory: \'"
                      + patchSourceDir + "\'." );
                }
                else
                {
                    patchCommandsByFileName.put( patch, createPatchCommand( myPatch ) );
                }
            }
        }
        else
        {
            getLog().info( "Nothing to do." );
            return;
        }

        StreamConsumer consumer = cliManager.newDebugStreamConsumer();
        
        for ( Iterator < Entry < String, Commandline > > it = patchCommandsByFileName.entrySet().iterator();
            it.hasNext(); )
        {
            Map.Entry < String, Commandline > entry = it.next();
            String patchName = (String) entry.getKey();
            Commandline cli = (Commandline) entry.getValue();
            
            try
            {
                int result = cliManager.execute( cli, consumer, consumer );
                
                if ( result != 0 )
                {
                    throw new MojoExecutionException(
                      "Patch command failed (exit value != 0). Please see debug output for more information." );
                }
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException(
                  "Failed to apply patch: " + patchName + ". See debug output for more information.", e );
            }
        }
    }

    /**
     * Add a new Patch task to the Ant calling mechanism. Give preference to originalFile/destFile, then workDir, and
     * finally ${basedir}.
     * 
     * @param myPatch patch file to use in the patch command
     * @return command line built up and ready to execute
     */
    private Commandline createPatchCommand( File myPatch )
    {
        Commandline cli = new Commandline();
        cli.setExecutable( "patch" );
        
        
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

        if ( originalFile != null )
        {
            cli.createArg().setValue( originalFile.getAbsolutePath() );

            if ( destFile != null )
            {
                cli.createArg().setLine( "-o " + destFile.getAbsolutePath() );
            }
            
            cli.createArg().setValue( myPatch.getAbsolutePath() );
        }
        else
        {
            if ( workDir != null )
            {
                cli.setWorkingDirectory( workDir.getAbsolutePath() );
            }
            else
            {
                getLog().info(
                  "We'll be patching the project basedir...keep all hands and legs inside the bus, and hold on!" );
            }
            
            cli.createArg().setLine( "--input=" + myPatch.getAbsolutePath() );
        }

        return cli;
    }

}
