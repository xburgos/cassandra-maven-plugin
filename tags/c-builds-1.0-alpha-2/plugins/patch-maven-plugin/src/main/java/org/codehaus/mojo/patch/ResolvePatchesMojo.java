package org.codehaus.mojo.patch;

import java.io.File;
import java.io.IOException;
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
public class ResolvePatchesMojo extends AbstractPatchMojo
    implements Contextualizable
{
    
    /**
     * If this flag is set, the mojo will not attempt patch-artifact resolution, but will instead 
     * simply verify and then set the patch-source directory (parameter) as the location where patches
     * are available for other mojos to access/apply.
     * 
     * @parameter expression="${skipResolution}" default-value="false"
     */
    private boolean skipResolution;

    /**
     * @parameter expression="${patchDirectory}" default-value="src/patches"
     * @required
     */
    private File patchDirectory;
    
    /**
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
     * @parameter default-value="patches"
     * @required
     */
    private String patchArtifactClassifier;

    /**
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

    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = new PatchContext();
        
        boolean useArtifact = ( skipResolution ? false : retrieveAndUnpackPatchArtifact() );
        
        if ( useArtifact )
        {
            ctx.setPatchArtifactResolved( true );
            
            File patchDir = patchArtifactUnpackDirectory;
            
            if ( patchArtifactUnpackSubpath != null )
            {
                patchDir = new File( patchDir, patchArtifactUnpackSubpath );
                
                if ( !patchDir.exists() )
                {
                    throw new MojoExecutionException( "Sub-path does not exist in unpacked patch-artifact: " + patchArtifactUnpackSubpath + " (full path should be: " + patchDir.getAbsolutePath() + ")." );
                }
            }
            
            ctx.setPatchDirectory( patchArtifactUnpackDirectory );
        }
        else if ( patchDirectory.exists() )
        {
            ctx.setPatchArtifactResolved( false );
            ctx.setPatchDirectory( patchDirectory );
        }
        else
        {
            throw new MojoExecutionException( "No patches found. Either reconfigure the patch plugin, or set <patchesEnabled>false</patchesEnabled>." );
        }
        
        getLog().debug( "Using patches from: " + ctx.getPatchDirectory() );
        
        ctx.store( getSessionContext(), getProject() );
    }

    private boolean retrieveAndUnpackPatchArtifact() throws MojoExecutionException
    {
        ArtifactHandler handler;
        try
        {
            handler = (ArtifactHandler) container.lookup( ArtifactHandler.ROLE, patchArtifactType );
        }
        catch ( ComponentLookupException e )
        {
            getLog().debug( "Cannot lookup ArtifactHandler for archive type: " + patchArtifactType + "; constructing stub artifact handler." );
            
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
        
        UnArchiver unarchiver = null;
        
        try
        {
            unarchiver = archiverManager.getUnArchiver( patchArtifactFile );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find un-archiver for patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
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
            throw new MojoExecutionException( "Failed to unpack patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to unpack patch-archive: " + patchArtifactFile.getAbsolutePath(), e );
        }
        
        return true;
    }

    public void contextualize( Context context ) throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
