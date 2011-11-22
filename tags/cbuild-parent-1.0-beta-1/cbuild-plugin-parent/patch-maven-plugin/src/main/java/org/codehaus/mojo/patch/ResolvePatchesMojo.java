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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.project.extras.DerivedArtifact;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * The CBUILDS plugins support building your project out of a checked out piece of source code
 * from a revision management system, or from a tarball on a remote maven repository.  When in the
 * mode of building from a remote repository, the patches (if any) will need to be retrieved and
 * unpacked.  Later patch goals detect which mode the project is using by looking for patches in 
 * your target directory.
 * 
 * @goal resolve-patches
 * @phase initialize
 * @author jdcasey
 */
public class ResolvePatchesMojo extends AbstractPatchMojo
{

    /**
     * If set to false, don't attempt to optimize the unpack step based on the pre-existence of the
     * unpack directory and its contents. By default, optimizations are not enabled since the http-wagon
     * implementation does not preserve the remote file's date.  This feature may have been developed
     * to support re-entrancy of a build, but the value of re-entrancy once you are patching an upstream
     * project is pretty questionable.  For this reason, the feature has been deprecated.
     * 
     * @parameter default-value="false"
     * @since 1.0-alpha-2
     * @deprecated
     */
    private boolean optimizations;

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
     * This is the subpath within the unpacked patch-archive, where patches should reside.
     * Patch artifacts are typically bundled without a subdirectory so this parameter is
     * typically not set.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private String patchArtifactUnpackSubpath;

    /**
     * Classifier is a suffix in the filename, but it is before the filename extension.  A 
     * typical patch artifact will typically have the name similar to 
     * <code>ProjName-1.2.3-patches.tar.gz</code>.
     *
     * @parameter default-value="patches"
     * @since 1.0-alpha-2
     * @required
     */
    private String patchArtifactClassifier;

    /**
     * The filename extension for your patch artifact, typically ".tar.gz"
     *
     * @parameter default-value="tar.gz"
     * @since 1.0-alpha-2
     * @required
     */
    private String patchArtifactType;

    /**
     * @parameter default-value="${project.artifact}"
     * @required
     * @since 1.0-alpha-2
     * @readonly
     */
    private Artifact projectArtifact;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @since 1.0-alpha-2
     * @readonly
     */
    private List < ArtifactRepository > remoteRepositories;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @since 1.0-alpha-2
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArchiverManager archiverManager;

    /**
     * Downloads patch files from repos
     * 
     * @throws MojoExecutionException thrown if no patch can be found
     * @throws MojoFailureException not thrown, inherited from abstract class
     */
    protected void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        boolean useArtifact = retrieveAndUnpackPatchArtifact();

        if ( useArtifact )
        {
            File patchDir = patchArtifactUnpackDirectory;

            if ( patchArtifactUnpackSubpath != null )
            {
                patchDir = new File( patchDir, patchArtifactUnpackSubpath );

                if ( !patchDir.exists() )
                {
                    throw new MojoExecutionException( "Sub-path does not exist in unpacked patch-artifact: "
                                    + patchArtifactUnpackSubpath + " (full path should be: "
                                    + patchDir.getAbsolutePath() + ")." );
                }
            }
        }
        else if ( patchDirectory.exists() && patchDirectory.list().length > 0 )
        {
            getLog().debug( "Using patches from: " + patchDirectory );
        }
        else
        {
            throw new MojoExecutionException(
                "Patching configured, but no valid patch artifact or patch directory could be found." );
        }
    }

    /**
     * Will downlaod patches and unpack them
     * TODO implement tar unarchiver or don't support unpacking
     * 
     * @return will return true if patches were downloaded and unpacked
     * @throws MojoExecutionException thrown if error finding or unpacking patch archive
     */
    private boolean retrieveAndUnpackPatchArtifact()
        throws MojoExecutionException
    {
        ArtifactHandler handler = new DefaultArtifactHandler( patchArtifactType );

        Artifact patchArtifact =
            new DerivedArtifact( projectArtifact, patchArtifactClassifier, patchArtifactType, handler );

        try
        {
            artifactResolver.resolveAlways( patchArtifact, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Failed to resolve patch-artifact: " + patchArtifact.getId(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            getLog().debug( "Could not find patch-artifact: " + patchArtifact, e );
        }

        if ( !patchArtifact.isResolved() )
        {
            return false;
        }

        File patchArtifactFile = patchArtifact.getFile();

        getLog().debug( "Unpacking: " + patchArtifactFile );
        
        UnArchiver unarchiver = null;

        try
        {
            unarchiver = archiverManager.getUnArchiver( patchArtifactFile );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find un-archiver for patch-archive: "
                            + patchArtifactFile.getAbsolutePath(), e );
        }

        patchArtifactUnpackDirectory.mkdirs();

        unarchiver.setSourceFile( patchArtifactFile );
        unarchiver.setDestDirectory( patchArtifactUnpackDirectory );

        try
        {
            unarchiver.extract();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack patch-archive: "
                            + patchArtifactFile.getAbsolutePath(), e );
        }

        return true;
    }

}
