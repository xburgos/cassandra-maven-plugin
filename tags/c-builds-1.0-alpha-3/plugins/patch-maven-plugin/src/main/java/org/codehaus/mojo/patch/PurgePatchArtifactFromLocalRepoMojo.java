package org.codehaus.mojo.patch;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal purge-local-patch-artifact
 * @phase install
 * @author jdcasey
 *
 */
public class PurgePatchArtifactFromLocalRepoMojo extends AbstractPatchMojo
{
    
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = PatchContext.read( getSessionContext(), getProject() );
        
        Artifact patchArtifact = ctx.getPatchArtifact();
        
        getLog().debug( "Purging: " + patchArtifact + " from local repository at: " + localRepository.getBasedir() );
        
        if ( patchArtifact != null )
        {
            String relativePath = localRepository.pathOf( patchArtifact );
            
            File patchArtifactLocalRepoFile = new File( localRepository.getBasedir(), relativePath );
            
            getLog().debug( "trying to purge: " + patchArtifactLocalRepoFile.getAbsolutePath() );
            
            if ( patchArtifactLocalRepoFile.exists() )
            {
                patchArtifactLocalRepoFile.delete();
            }
        }
    }

}
