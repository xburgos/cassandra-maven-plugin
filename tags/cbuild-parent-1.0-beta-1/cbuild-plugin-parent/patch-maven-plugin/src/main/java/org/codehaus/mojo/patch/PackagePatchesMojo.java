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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;
import org.codehaus.plexus.util.cli.shell.Shell;


/**
 * Mojo goal will package up all of your project's patches into one package
 * 
 * @goal package-patches
 * @phase package
 * @author jdcasey
 *
 */
public class PackagePatchesMojo extends AbstractPatchMojo
{
    
    /**
     * Classifier is a suffix in the filename, but it is before the filename externsion.
     *
     * @parameter default-value="patches"
     * @required
     * @since 1.0-alpha-2
     */
    private String patchArtifactClassifier;

    /**
     * The filename extension, typically "tar.gz", "tgz", "tar.bz2", ".zip"
     *
     * @parameter default-value="tar.gz"
     * @required
     * @since 1.0-alpha-2
     */
    private String patchArtifactType;

    /**
     * Handling mode for long file paths.  Not used in 1.0-beta
     * 
     * @parameter default-value="gnu"
     * @since 1.0-alpha-2
     * @deprecated
     */
    private String tarLongFileMode;
    
    /**
     * The name of the patch.  Usually the same name as the project.
     *
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     * @since 1.0-alpha-2
     */
    private String artifactId;
    
    /**
     * The patch version after it is created, usually the same as the project version
     *
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     * @since 1.0-alpha-2
     */
    private String version;

    /**
     * The directory where to write the bundle of patches.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     * @since 1.0-alpha-2
     */
    private File patchArchiveDestDir;

    /**
     * If your project has never been deployed to a remote repo, your patches will need to be found
     * locally, and <code>patchDirectory</code> will be the location where patch-maven-plugin will
     * find any patches you want to apply.  When downloading patches from a remote repo, this
     * parameter and local patches are ignored.  When patch-maven-plugin uses local patches, it
     * will also generate a zip or tarball bundle of those patches and attach the artifact to the
     * project for later release and deployment to the maven repos.
     *
     * @parameter expression="${patchDirectory}" default-value="src/patches"
     * @since 1.0-alpha-2
     * @required
     */
    private File patchDirectory;

    /**
     * If your project's patches are retrieved from a remote repository (as an attached project
     * artifact), the patches will be downloaded and installed into the directory specified by 
     * <code>patchArtifactUnpackDirectory</code>. 
     * 
     * @parameter default-value="${project.build.directory}/unpacked-patches"
     * @since 1.0-alpha-2
     * @required
     */
    private File patchArtifactUnpackDirectory;

    /**
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @throws MojoExecutionException thrown if error encountered while packaging patches
     * @throws MojoFailureException not actually thrown, part of abstract interface definition
     */
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = new PatchContext();
        
        if ( ctx.isPatchArtifactResolved( patchArtifactUnpackDirectory ) )
        {
            getLog().debug( "Skipping patch-package step, patch file resolved from previous release." );
            return;
        }
        else
        {
            File patchDir = ctx.getPatchDirectory( patchArtifactUnpackDirectory, patchDirectory );
            File patchArchive = new File( patchArchiveDestDir, artifactId + "-" + version
              + "-" + patchArtifactClassifier + "." + patchArtifactType );
            
            /* If Plexus gets a good implementation of tar, this can be deleted */
            if ( ( patchArtifactType.equals( "tar" ) ) || ( patchArtifactType.equals( "tar.gz" ) ) 
              || ( patchArtifactType.equals( "tgz" ) ) || ( patchArtifactType.equals( "tar.bz2" ) ) )
            {
                tarPatchArtifact( patchDir, patchArchive );
            }
            else
            {
                archivePatchArtifact( patchDir, patchArchive );
            }
            
            projectHelper.attachArtifact( getProject(), patchArtifactType, patchArtifactClassifier, patchArchive );
        }
    }

    /**
     * Package up a your project's patch files
     * 
     * @param patchDir directory of patches
     * @param destFile where to write the newly created patch file archive
     * @throws MojoExecutionException thrown if IO or Archiver exceptions are caught archiving patches
     */
    private void archivePatchArtifact( File patchDir, File destFile ) throws MojoExecutionException
    {
        
        Archiver archiver;
        
        try
        {
            archiver = archiverManager.getArchiver( patchArtifactType );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find archiver for artifact type: " + patchArtifactType, e );
        }
        
        archiver.setDestFile( destFile );
        
        try
        {
            String[] excludes = new String[0];
            if ( useDefaultIgnores() )
            {
                excludes = (String[]) DEFAULT_IGNORED_PATCH_PATTERNS.toArray( 
                  new String[ DEFAULT_IGNORED_PATCH_PATTERNS.size() ] );
            }
            
            archiver.addDirectory( patchDir, new String[]{ "**" }, excludes );
            archiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to archive patch-source directory.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to archive patch-source directory.", e );
        }

    }

    /**
     * This routine exists since the java tar implementation is very buggy
     *
     * @param patchDir directory of patches
     * @param destFile where to write the newly created patch tarball
     * @throws MojoExecutionException thrown if tar command throws a CommandLine exception while packaging patches
     */
    private void tarPatchArtifact( File patchDir, File destFile ) throws MojoExecutionException
    {
        /* create archive */
        StringBuffer cmd = new StringBuffer();
        cmd.append( "cd " + patchDir.getAbsolutePath() + " && tar -c" );
        
        /* Deal with compression types */
        if ( ( patchArtifactType.equals( "tar.gz" ) ) || ( patchArtifactType.equals( "tgz" ) ) )
        {
            cmd.append( "z" );
        }
        else if ( patchArtifactType.equals( "tar.bz2" ) )
        {
            cmd.append( "j" );
        }
        else if ( !patchArtifactType.equals( "tar" ) )
        {
            throw new MojoExecutionException( "Unknown tarball type: " + patchArtifactType );
        }
        cmd.append( "f " + destFile.getAbsolutePath() );

        /* Exclude version control stuff */
        String[] excludes = new String[0];
        if ( useDefaultIgnores() )
        {
            excludes = (String[]) DEFAULT_IGNORED_PATCHES.toArray( new String[ DEFAULT_IGNORED_PATCHES.size() ] );
        }

        for ( String exclude : excludes )
        {
            cmd.append( " --exclude " + exclude );
        }
        
        /* Grab the whole directory */
        cmd.append( " *" );

        /* run the tar command */
        Commandline cli = new Commandline();
        Shell shell = new BourneShell( true );
        shell.setQuotedArgumentsEnabled( false );
        cli.setShell( shell );
        cli.createArg().setLine( cmd.toString() );
        getLog().debug( "Executing: " + cmd.toString() );
        try
        {
            StreamConsumer consumer = cliManager.newInfoStreamConsumer();
            
            int result = cliManager.execute( cli, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( cmd.toString()
                  + " returned an exit value != 0. Aborting build; see command output above for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to execute. Reason: " + e.getMessage(), e );
        }

    }

}
