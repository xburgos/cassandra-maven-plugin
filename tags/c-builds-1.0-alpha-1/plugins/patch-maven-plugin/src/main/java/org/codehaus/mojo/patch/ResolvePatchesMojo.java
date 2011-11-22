package org.codehaus.mojo.patch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
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
 * @goal resolve-patches
 * @phase initialize
 * @author jdcasey
 */
public class ResolvePatchesMojo extends AbstractPatchMojo
{

    /**
     * @parameter default-value="src/patches"
     * @required
     */
    private File patchDirectory;
    
    /**
     * @parameter default-value="${project.build.directory}/unpacked-patches"
     * @required
     */
    private File patchArtifactUnpackDirectory;

    /**
     * @parameter default-value="patches"
     * @required
     */
    private String patchArtifactClassifier;

    /**
     * @parameter default-value="tar.gz"
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

    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = new PatchContext();
        
        boolean useArtifact = retrieveAndUnpackPatchArtifact();
        
        if ( useArtifact )
        {
            ctx.setPatchArtifactResolved( true );
            ctx.setPatchDirectory( patchArtifactUnpackDirectory );
        }
        else if ( patchDirectory.exists() )
        {
            ctx.setPatchArtifactResolved( false );
            ctx.setPatchDirectory( patchDirectory );
        }
        else
        {
            ctx.setPatchArtifactResolved( false );
        }
        
        ctx.store( getSessionContext(), getProject() );
    }

    private boolean retrieveAndUnpackPatchArtifact() throws MojoExecutionException
    {
        Artifact patchArtifact =
            new DerivedArtifact( projectArtifact, patchArtifactClassifier, patchArtifactType );
        
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
        
        UnArchiver unarchiver = null;
        
        try
        {
            unarchiver = archiverManager.getUnArchiver( patchArtifactFile );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find un-archiver for patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
        }
        
        unarchiver.setSourceFile( patchArtifactFile );
        unarchiver.setDestDirectory( patchArtifactUnpackDirectory );
        
        try
        {
            unarchiver.extract();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to unpack patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to unpack patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
        }
        
        return true;
    }

}
