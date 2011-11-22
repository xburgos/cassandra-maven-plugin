package org.codehaus.mojo.tools.fs.archive;

import java.util.Map;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarLongFileMode;

public final class ArchiverManagerUtils
{

    public static final String TAR_LONG_FILE_MODE_OPTION = "tarLongFileMode";

    private ArchiverManagerUtils()
    {
    }

    public static Archiver getArchiver( ArchiverManager archiverManager, String extension, Map archiverOptions )
        throws NoSuchArchiverException, ArchiverException
    {
        Archiver archiver;
        
        if ( extension.startsWith( "tar" ) )
        {
            archiver = archiverManager.getArchiver( "tar" );

            if ( extension.length() > 3 )
            {
                // it's not a straight tar archive, but a compressed one. Figure out the compression
                // method.
                String compressionType = extension.substring( "tar.".length() );

                TarArchiver.TarCompressionMethod compressionMethod = new TarArchiver.TarCompressionMethod();

                if ( "gz".equals( compressionType ) )
                {
                    compressionMethod.setValue( "gzip" );
                }
                else if ( "bz2".equals( compressionType ) )
                {
                    compressionMethod.setValue( "bzip2" );
                }

                if ( compressionMethod.getValue() == null )
                {
                    throw new ArchiverException( "Invalid tar compression method specified: " + compressionType );
                }
                else
                {
                    ( (TarArchiver) archiver ).setCompression( compressionMethod );
                }
            }

            if ( archiverOptions != null )
            {
                String tarLongFileMode = (String) archiverOptions.get( TAR_LONG_FILE_MODE_OPTION );

                if ( tarLongFileMode != null )
                {
                    TarLongFileMode tarFileMode = new TarLongFileMode();

                    tarFileMode.setValue( tarLongFileMode );

                    ( (TarArchiver) archiver ).setLongfile( tarFileMode );
                }
            }
        }
        else
        {
            archiver = archiverManager.getArchiver( extension );
        }

        return archiver;
    }

}
