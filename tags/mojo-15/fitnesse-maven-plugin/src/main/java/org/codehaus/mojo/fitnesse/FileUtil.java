package org.codehaus.mojo.fitnesse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FileUtil
{

    private FileUtil()
    {
        super();
    }

    public static String getString( File pIn ) throws IOException
    {

        FileInputStream tFileInputStream = null;
        try
        {
            tFileInputStream = new FileInputStream( pIn );
            String tResult = getString( tFileInputStream );
            return tResult;
        }
        finally
        {
            if ( tFileInputStream != null )
            {
                tFileInputStream.close();
            }
        }
    }

    public static String getString( InputStream pIn ) throws IOException
    {
        StringBuffer tBuf = new StringBuffer();
        byte[] tbytes = new byte[512];
        int tReadBytes = pIn.read( tbytes );
        while ( tReadBytes >= 0 )
        {
            tBuf.append( new String( tbytes, 0, tReadBytes, "UTF-8" ) );
            tReadBytes = pIn.read( tbytes );
        }
        return tBuf.toString();
    }

}
