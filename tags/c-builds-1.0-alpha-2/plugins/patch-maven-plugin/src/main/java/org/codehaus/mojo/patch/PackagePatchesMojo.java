package org.codehaus.mojo.patch;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * @goal package-patches
 * @phase package
 * @author jdcasey
 *
 */
public class PackagePatchesMojo extends AbstractPatchMojo
{
    
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
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;
    
    /**
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     */
    private String version;
    
    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File patchArchiveDestDir;
    
    /**
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;
    
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = PatchContext.read( getSessionContext(), getProject() );
        
        if ( ctx.isPatchArtifactResolved() )
        {
            getLog().debug( "Skipping patch-package step, since project patches were resolved at the beginning of the build." );
            return;
        }
        else
        {
            File patchDir = ctx.getPatchDirectory();
            
            File patchArchive = archivePatchArtifact( patchDir );
            
            projectHelper.attachArtifact( getProject(), patchArtifactType, patchArtifactClassifier, patchArchive );
            
            // find the attachment we just made...
            List attachments = getProject().getAttachedArtifacts();
            for ( Iterator it = attachments.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();
                
                if ( patchArtifactClassifier.equals( artifact.getClassifier() ) && patchArtifactType.equals( artifact.getType() ) )
                {
                    ctx.setPatchArtifact( artifact );
                }
            }
        }
        
        ctx.store( getSessionContext(), getProject() );
    }

    private File archivePatchArtifact( File patchDir ) throws MojoExecutionException
    {
        File destFile = new File( patchArchiveDestDir, artifactId + "-" + version + "-" + patchArtifactClassifier + "." + patchArtifactType );
        
        Archiver archiver;
        
        try
        {
            archiver = archiverManager.getArchiver( destFile );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find archiver for artifact type: " + patchArtifactType, e );
        }
        
        archiver.setDestFile( destFile );
        
        try
        {
            archiver.addDirectory( patchDir );
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
        
        return destFile;
    }

}
