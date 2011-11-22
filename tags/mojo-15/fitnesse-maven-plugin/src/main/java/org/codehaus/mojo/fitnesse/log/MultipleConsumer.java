package org.codehaus.mojo.fitnesse.log;

import org.codehaus.plexus.util.cli.StreamConsumer;

public class MultipleConsumer implements FitnesseStreamConsumer
{
    LogConsumer mLog;
    FileConsumer mFile;
    private boolean mHasGeneratedResultFile = false;
    
    public MultipleConsumer(LogConsumer pLog, FileConsumer pFile)
    {
        super();
        mLog = pLog;
        mFile = pFile;
    }

    public void consumeLine( String pMessage )
    {
        mLog.consumeLine( pMessage );
        mFile.consumeLine( pMessage );
        mHasGeneratedResultFile = mHasGeneratedResultFile || pMessage.startsWith( "Formatting as html" );
    }

    public boolean hasGeneratedResultFile()
    {
        return mHasGeneratedResultFile;
    }

    public StreamConsumer getLogConsumer()
    {
        return mLog;
    }

    public FileConsumer getFileConsumer()
    {
        return mFile;
    }

}
