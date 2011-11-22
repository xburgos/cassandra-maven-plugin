package org.codehaus.mojo.project.archive;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.project.extras.ScanningUtils;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @goal resolve-project-sources
 * @phase initialize
 * @author jdcasey
 */
public class ResolveProjectSourcesMojo
    extends AbstractProjectSourcesMojo
{

    /**
     * If this flag is set, the mojo will not attempt project-source-artifact resolution, but will 
     * instead simply verify * and then set the project-source location (in the ProjectSourceContext).
     * 
     * @parameter expression="${skipResolution}" default-value="false"
     */
    private boolean skipResolution;

     /**
     * If this flag is set, the mojo will not unpack the archive, just place it in the project
     * ${sourceUnpackDirectory}
     * 
     * @parameter expression="${skipUnpack}" default-value="false"
     */
    private boolean skipUnpack;
    
    /**
     * If set to false, don't attempt to optimize the unpack step based on the pre-existence of the
     * unpack directory and its contents. By default, optimizations are enabled.
     * 
     * @parameter default-value="true"
     */
    private boolean optimizations;

    /**
     * Flag denoting whether the current project build directory can be used as a valid sourceLocation.
     * Projects will * either have a source archive from elsewhwere (third-party build), or it will
     * have an initial sourceLocation of * the current project directory (internal projects).
     * 
     * @parameter default-value="false"
     */
    private boolean projectDirIsSourceLocation;

    /**
     * This is the location of the project source code (in archive form, for third-party projects...
     * or, in directory * form, for internal projects) on the local filesystem, for new builds that
     * don't have project-sources stored in * the repository already.
     * 
     * @parameter expression="${sourceLocation}" default-value="${project.basedir}"
     */
    private File sourceLocation;

    /**
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}"
     * @required
     */
    private File sourceUnpackDirectory;

    /**
     * This is the subpath within the unpacked patch-archive, where patches should reside.
     * 
     * @parameter
     */
    private String sourceUnpackSubpath;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectSourceContext ctx;
        try
        {
            ctx = (ProjectSourceContext) getContainer().lookup( ProjectSourceContext.ROLE,
                ProjectSourceContext.ROLE_HINT );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Failed to create cookie context store: ", e );
        }


        File sourceArchiveFile = null;

        if ( !skipResolution )
        {
            sourceArchiveFile = retrieveSourceArtifact();

            ctx.setSourceArtifactResolved( sourceArchiveFile != null );
            ctx.setOriginalProjectSourceLocation( sourceArchiveFile );

            getLog().debug( "After resolution, sourceArchiveFile is: " + sourceArchiveFile );
        }

        // this will happen if we skipResolution, or fail to resolve the project-sources artifact.
        if ( sourceArchiveFile == null )
        {
            sourceArchiveFile = sourceLocation;
            getLog().debug( "Using local file/directory for project-sources: " + sourceArchiveFile );
            ctx.setOriginalProjectSourceLocation( sourceLocation );

            getLog().debug( "After failing/skipping resolution, sourceArchiveFile set to: " + sourceArchiveFile );
        }

        if ( sourceArchiveFile == null || !sourceArchiveFile.exists() )
        {
            getLog().debug(
                            "Something is wrong. Does sourceArchiveFile \'" + sourceArchiveFile + "\' exist? "
                                            + sourceArchiveFile.exists() );

            throw new MojoExecutionException(
                                              "Cannot find project sources. You may need to specify the location using -DsourceLocation." );
        }

        File projectDirectory = null;

        if ( sourceArchiveFile.isDirectory() )
        {
            if ( !projectDirIsSourceLocation )
            {
                throw new MojoExecutionException( "Project sources cannot originate from a directory. "
                                + "This often happens after a source release download fails." );
            }
            else
            {
                getLog().debug( "Using directory: " + sourceArchiveFile + " as the location of project sources." );
                projectDirectory = sourceArchiveFile;
            }
        }
        else
        {
            getLog().debug( "Unpacking: " + sourceArchiveFile + "\nto: " + sourceUnpackDirectory );

            try
            {
                unpackArchive( sourceArchiveFile, sourceUnpackDirectory );
            }
            catch ( NoSuchArchiverException e )
            {
                throw new MojoExecutionException( "Failed to unpack source archive: " + sourceArchiveFile, e );
            }
            catch ( ArchiverException e )
            {
                throw new MojoExecutionException( "Failed to unpack source archive: " + sourceArchiveFile, e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to unpack source archive: " + sourceArchiveFile, e );
            }

            if ( sourceUnpackSubpath != null )
            {
                projectDirectory = new File( sourceUnpackDirectory, sourceUnpackSubpath );
            }
            else
            {
                projectDirectory = sourceUnpackDirectory;
            }
        }

        ctx.setProjectSourceDirectory( projectDirectory );

        setProjectPrimarySourceDirectory( projectDirectory );

        try
        {
            storeContext( ctx );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Failed to store cookie context: " + sourceArchiveFile, e );
        }
    }

    private void setProjectPrimarySourceDirectory( File projectDirectory )
    {
        Model model = getProject().getModel();
        Build build = model.getBuild();

        if ( build == null )
        {
            build = new Build();
            model.setBuild( build );
        }

        build.setSourceDirectory( projectDirectory.getPath() );
    }

    private void unpackArchive( File archiveFile, File targetDirectory )
        throws NoSuchArchiverException, ArchiverException, IOException
    {
        boolean unpack = true;
        if ( targetDirectory.exists() )
        {
            if ( optimizations )
            {
                long targetLastMod =
                    ScanningUtils.getLatestLastMod( targetDirectory, Collections.singleton( "**/*" ), Collections.EMPTY_SET );
                
                long archiveLastMod = archiveFile.lastModified();

                unpack = archiveLastMod > targetLastMod;
            }
            else
            {
                FileUtils.deleteDirectory( targetDirectory );
            }
        }

        targetDirectory.mkdirs();

        if ( skipUnpack )
        {
            getLog().info( "Copying " + archiveFile + " to " + targetDirectory);
            FileUtils.copyFileToDirectory(archiveFile, targetDirectory);
        }
        else if ( unpack )
        {
            UnArchiver unArchiver = getArchiverManager().getUnArchiver( archiveFile );
            getLog().info( "Using UnArchiver: " + unArchiver + " for: " + archiveFile );
            unArchiver.setDestDirectory( targetDirectory );
            unArchiver.setSourceFile( archiveFile );
            unArchiver.extract();
        }
    }

    private File retrieveSourceArtifact()
        throws MojoExecutionException
    {
        Artifact projectSourcesArtifact = getProjectSourcesArtifact();

        try
        {
            artifactResolver.resolveAlways( projectSourcesArtifact, remoteRepositories, getLocalRepository() );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Failed to resolve patch-artifact: " + projectSourcesArtifact.getId(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            getLog().debug( "Could not find patch-artifact: " + projectSourcesArtifact, e );
        }

        getLog().debug( "Was project-sources artifact resolved? " + projectSourcesArtifact.isResolved() );
        getLog().debug( "Project-sources artifact file: " + projectSourcesArtifact.getFile() );

        if ( !projectSourcesArtifact.isResolved() )
        {
            return null;
        }

        return projectSourcesArtifact.getFile();
    }

}
