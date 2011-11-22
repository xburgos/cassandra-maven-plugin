package org.codehaus.mojo.dependency;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Utility class with static helper methods
 * @author brianf
 *
 */
public class DependencyUtil
{
    /**
     * Does the actual copy of the file and logging.
     * 
     * @param artifact 
     *          represents the file to copy.
     * @param destFile 
     *          file name of destination file.
     * @param log 
     *          to use for output.
     * @throws MojoExecutionException 
     *          with a message if an error occurs.
     */
    protected static void copyFile( File artifact, File destFile, Log log )
        throws MojoExecutionException
    {
        if ( !destFile.exists() )
        {
            try
            {
                log.info( "Copying " + artifact.getAbsolutePath() + " to " + destFile );
                FileUtils.copyFile( artifact, destFile );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error copying artifact from " + artifact + " to " + destFile, e );
            }
        }
        else
        {
            log.info( artifact.getName() + " already exists." );
        }
    }

    /**
     * Unpacks the archive file, checking for a marker file to see if it should unpack again. Creates the marker 
     * file after unpacking.
     *
     * @param Artifact
     *          File to be unpacked.
     * @param unpackDirectory
     *          Location where to put the unpacked files.
     */
    protected static void unpackFile( Artifact artifact, File unpackDirectory, File markersDirectory,
                                     ArchiverManager archiverManager, Log log )
        throws MojoExecutionException
    {
        markersDirectory.mkdirs();

        File markerFile = new File( markersDirectory, artifact.getGroupId() + "." + artifact.getArtifactId() + "-"
            + artifact.getVersion() + ".unpacked" );

        if ( !markerFile.exists() )
        {
            try
            {
                unpackDirectory.mkdirs();

                unpack( artifact.getFile(), unpackDirectory, archiverManager, log );

                //create marker file
                markerFile.getParentFile().mkdirs();
                markerFile.createNewFile();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error creating marker file: " + markerFile );
            }
        }
        else
        {
            log.info( artifact.getFile().getName() + " already unpacked." );
        }
    }

    /**
     * Unpacks the archive file.
     *
     * @param file
     *          File to be unpacked.
     * @param location
     *          Location where to put the unpacked files.
     */
    private static void unpack( File file, File location, ArchiverManager archiverManager, Log log )
        throws MojoExecutionException
    {

        String archiveExt = FileUtils.getExtension( file.getAbsolutePath() ).toLowerCase();

        try
        {
            UnArchiver unArchiver;

            unArchiver = archiverManager.getUnArchiver( archiveExt );

            unArchiver.setSourceFile( file );

            unArchiver.setDestDirectory( location );

            unArchiver.extract();
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Unknown archiver type", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error unpacking file: " + file + "to: " + location, e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Error unpacking file: " + file + "to: " + location, e );
        }
    }

    /**
     * Builds the file name. If removeVersion is set, then the file name must be reconstructed 
     * from the artifactId, Classifier (if used) and Type. Otherwise, this method returns the
     * artifact file name.
     * 
     * @param artifact
     *          File to be formatted.
     * @param removeVersion
     *          Specifies if the version should be removed from the file name.
     * @return
     *          Formatted file name in the format artifactId-[classifier-][version].[type]
     */
    public static String getFormattedFileName( Artifact artifact, boolean removeVersion )
    {
        String destFileName = null;
        if ( !removeVersion )
        {
            destFileName = artifact.getFile().getName();
        }
        else
        {
            if ( artifact.getClassifier() != null )
            {
                destFileName = artifact.getArtifactId() + "-" + artifact.getClassifier() + "." + artifact.getType();
            }
            else
            {
                destFileName = artifact.getArtifactId() + "." + artifact.getType();
            }
        }
        return destFileName;
    }
}
