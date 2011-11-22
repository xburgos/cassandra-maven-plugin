package org.codehaus.mojo.remotesrc;

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
import org.codehaus.mojo.tools.fs.archive.manager.ArchiverManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Retrieve project sources from some external URL, and unpack them (into target, by default). Dependency resolution is
 * required here to keep from downloading a potentially huge source tarball if the dependencies cannot be satisfied.
 * 
 * @goal unpack
 * @requiresDependencyResolution compile
 */
public class SourceUnpackMojo
    extends AbstractRemoteSourceMojo
{

    public static final String TGZ_TYPE = "tar.gz";

    public static final String TGZ_TYPE2 = "tgz";

    public static final String TBZ_TYPE = "tar.Z";

    public static final String TBZ_TYPE2 = "tar.bz2";

    public static final String[] UNPACK_TYPES = { TGZ_TYPE, TGZ_TYPE2, TBZ_TYPE, TBZ_TYPE2, "tar" };

    /**
     * The source archive to unpack.
     * 
     * @parameter
     */
    private File sourceArchive;

    /**
     * Whether to skip the unpack step.
     * 
     * @parameter default-value="false"
     */
    private boolean skipUnpack;

    /**
     * The target directory into which the project source archive should be expanded.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File expandTarget;

    /**
     * Whether to force this to unpack, regardless of whether there is already a directory in place.
     * 
     * @parameter default-value=false expression="${source.unpack.overwrite}"
     */
    private boolean overwrite;

    /**
     * The resulting unpacked directory to check when !overwrite.
     * 
     * @parameter default-value="${workDir}"
     * @required
     */
    private File workDir;

    /**
     * @component role-hint="local-overrides"
     */
    private ArchiverManager archiverManager;

    /**
     * Perform the source archive retrieval, and unpack it.
     * 
     * This involves:
     * 
     * o o Setting up the transfer monitor (displays progress for the download) o Download the archive to a temp file o
     */
    protected void doExecute()
        throws MojoExecutionException
    {
        // If we're set to NOT overwrite, we need to determine whether the unpack
        // code should run.
        if ( !overwrite && workDir.exists() )
        {
            getLog().info(
                           "Skipping unpack step because working directory already exists. Set '-Dsource.unpack.overwrite=true to override." );
            return;
        }

        try
        {
            // make sure the expansion target exists before we try to expand there.
            if ( !expandTarget.exists() )
            {
                expandTarget.mkdirs();
            }

            // give artifact-resolution results precedence.
            File resolvedSourceArchive = getSourceArchiveFile( getProject().getId() );

            if ( resolvedSourceArchive != null )
            {
                sourceArchive = resolvedSourceArchive;
            }

            if ( !sourceArchive.exists() )
            {
                throw new MojoExecutionException(
                                                  "Source archive cannot be unpacked; it does not exist! (archive location: "
                                                                  + sourceArchive + ")" );
            }

            getLog().debug( "Unpacking: " + sourceArchive );
            
            unpackArchive( sourceArchive, expandTarget );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to unpack source archive.", e );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack source archive.", e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack source archive.", e );
        }
    }

    private void unpackArchive( File archiveFile, File targetDirectory )
        throws NoSuchArchiverException, ArchiverException, IOException
    {
        UnArchiver unArchiver = archiverManager.getUnArchiver( archiveFile );

        getLog().info( "Using UnArchiver: " + unArchiver + " for: " + archiveFile );

        targetDirectory.mkdirs();

        unArchiver.setDestDirectory( targetDirectory );
        unArchiver.setSourceFile( archiveFile );

        unArchiver.extract();
    }

    protected boolean isSkip()
    {
        return skipUnpack;
    }

    protected CharSequence getSkipMessage()
    {
        return "Skipping source-archive unpack step (per configuration).";
    }

}
