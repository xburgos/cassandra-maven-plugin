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
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.project.extras.DerivedArtifact;
import org.codehaus.mojo.tools.project.extras.ScanningUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * @goal resolve-patches
 * @phase initialize
 * @author jdcasey
 */
public class ResolvePatchesMojo
    extends AbstractPatchMojo
    implements Contextualizable
{

    /**
     * If set to false, don't attempt to optimize the unpack step based on the pre-existence of the
     * unpack directory and its contents. By default, optimizations are enabled.
     * 
     * @parameter default-value="true"
     */
    private boolean optimizations;

    /**
     * Where to find the patches for this project
     *
     * @parameter expression="${patchDirectory}" default-value="src/patches"
     * @required
     */
    private File patchDirectory;

    /**
     * Scratch directory used to unpack the patches prior to application of the patch.
     *
     * @parameter default-value="${project.build.directory}/unpacked-patches"
     * @required
     */
    private File patchArtifactUnpackDirectory;

    /**
     * This is the subpath within the unpacked patch-archive, where patches should reside.
     * 
     * @parameter
     */
    private String patchArtifactUnpackSubpath;

    /**
     * Classifier is a suffix in the filename, but it is before the filename externsion.
     *
     * @parameter default-value="patches"
     * @required
     */
    private String patchArtifactClassifier;

    /**
     * The filename extension, typically ".zip"
     *
     * @parameter default-value="zip"
     * @required
     */
    private String patchArtifactType;

    /**
     * @parameter default-value="${project.artifact}"
     * @required
     * @readonly
     */
    private Artifact projectArtifact;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * @parameter default-value="${localRepository}"
     * @required
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

     // contextualized.
    private PlexusContainer container;

    protected void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        //MavenSession session = this.getSession();
        //MavenProject project = session.getCurrentProject();
        //System.out.println("ResolvePatchesMojo " + project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        //System.out.flush();
        PatchContext ctx = new PatchContext( this.getSession(), buildAdvisor );

        boolean useArtifact = retrieveAndUnpackPatchArtifact();

        if ( useArtifact )
        {
            ctx.setPatchArtifactResolved( true );

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

            ctx.setPatchDirectory( patchArtifactUnpackDirectory );
        }
        else if ( patchDirectory.exists() && patchDirectory.list().length > 0 )
        {
            ctx.setPatchArtifactResolved( false );
            ctx.setPatchDirectory( patchDirectory );
        }
        else
        {
            throw new MojoExecutionException(
                                              "Patching configured, but no valid patch artifact or patch directory could be found." );
        }

        getLog().debug( "Using patches from: " + ctx.getPatchDirectory() );

        ctx.store();
    }

    private boolean retrieveAndUnpackPatchArtifact()
        throws MojoExecutionException
    {
        ArtifactHandler handler;
        try
        {
            handler = (ArtifactHandler) container.lookup( ArtifactHandler.ROLE, patchArtifactType );
        }
        catch ( ComponentLookupException e )
        {
            getLog().debug(
                            "Cannot lookup ArtifactHandler for archive type: " + patchArtifactType
                                            + "; constructing stub artifact handler." );

            // use the defaults...it should be enough for our uses.
            handler = new DefaultArtifactHandler( patchArtifactType );
        }

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
        
        boolean unpack = true;
        if ( patchArtifactUnpackDirectory.exists() )
        {
            if ( optimizations )
            {
                long targetLastMod =
                    ScanningUtils.getLatestLastMod( patchArtifactUnpackDirectory, Collections.singleton( "**/*" ),
                                                    Collections.EMPTY_SET );
                
                getLog().info( "Latest lastMod in " + patchArtifactUnpackDirectory + " is: " + targetLastMod );
                
                long archiveLastMod = patchArtifactFile.lastModified();
                
                getLog().info( "lastMod of " + patchArtifactFile + " is: " + patchArtifactFile.lastModified() );

                unpack = archiveLastMod > targetLastMod;
            }
        }

        if ( unpack )
        {
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
        }
        else
        {
            getLog().debug(
                            "NOT unpacking patch archive; unpack directory: " + patchArtifactUnpackDirectory
                                            + " is up-to-date." );
        }

        return true;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
