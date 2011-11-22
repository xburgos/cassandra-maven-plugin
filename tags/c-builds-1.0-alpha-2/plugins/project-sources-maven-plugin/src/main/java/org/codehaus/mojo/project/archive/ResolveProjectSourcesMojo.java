package org.codehaus.mojo.project.archive;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * @goal resolve-project-sources
 * @phase initialize
 * @author jdcasey
 */
public class ResolveProjectSourcesMojo
    extends AbstractProjectSourcesMojo
{

    /**
     * If this flag is set, the mojo will not attempt project-source-artifact resolution, but will instead simply verify
     * and then set the project-source location (in the ProjectSourceContext).
     * 
     * @parameter expression="${skipResolution}" default-value="false"
     */
    private boolean skipResolution;

    /**
     * This is the location of the project source code (in archive form, for third-party projects... or, in directory
     * form, for internal projects) on the local filesystem, for new builds that don't have project-sources stored in
     * the repository already.
     * 
     * @parameter expression="${sourceLocation}" default-value="${basedir}"
     */
    private File sourceLocation;

    /**
     * @parameter expression="${workDir}"
     *            default-value="${project.build.directory}/${project.artifactId}-${project.version}"
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
        ProjectSourceContext ctx = new ProjectSourceContext();

        File sourceArchiveFile = null;

        if ( !skipResolution )
        {
            sourceArchiveFile = retrieveSourceArtifact();

            ctx.setSourceArtifactResolved( sourceArchiveFile != null );
            ctx.setOriginalProjectSourceLocation( sourceArchiveFile );
        }
        
        // this will happen if we skipResolution, or fail to resolve the project-sources artifact.
        if ( sourceArchiveFile == null )
        {
            sourceArchiveFile = sourceLocation;
            getLog().debug( "Using local file/directory for project-sources: " + sourceArchiveFile );
            ctx.setOriginalProjectSourceLocation( sourceLocation );
        }

        if ( sourceArchiveFile == null || !sourceArchiveFile.exists() )
        {
            throw new MojoExecutionException(
                                              "Cannot find project sources. You may need to specify the location using -DsourceLocation." );
        }

        File projectDirectory = null;

        if ( sourceArchiveFile.isDirectory() )
        {
            getLog().info( "Using directory: " + sourceArchiveFile + " as the location of project sources." );
            projectDirectory = sourceArchiveFile;
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

        ctx.store( getSessionContext(), getProject() );
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
        UnArchiver unArchiver = getArchiverManager().getUnArchiver( archiveFile );
        
        targetDirectory.mkdirs();

        unArchiver.setDestDirectory( targetDirectory );
        unArchiver.setSourceFile( archiveFile );

        unArchiver.extract();
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
