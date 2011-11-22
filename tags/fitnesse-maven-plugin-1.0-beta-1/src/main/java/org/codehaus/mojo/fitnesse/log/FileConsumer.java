package org.codehaus.mojo.fitnesse.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileConsumer implements FitnesseStreamConsumer
{

    FileWriter mOutputWriter;

    private boolean mHasGeneratedResultFile = false;

    private String mLineSep = System.getProperty( "line.separator" );

    /** Only for test. */
    public FileConsumer( )
    {
    }

    public FileConsumer( File pOutputFile )
    {
        super();
        try
        {
            mOutputWriter = new FileWriter( pOutputFile );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to write into file" );
        }
    }

    public synchronized void consumeLine( String pMessage )
    {
        try
        {
            mOutputWriter.write( pMessage + mLineSep );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to write into file" );
        }
        mHasGeneratedResultFile = mHasGeneratedResultFile || pMessage.startsWith( "Formatting as html" );
}

    public boolean hasGeneratedResultFile()
    {
        return mHasGeneratedResultFile;
    }

    public void close()
    {
        try
        {
            mOutputWriter.flush();
            mOutputWriter.close();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to write into file" );
        }
    }

}
