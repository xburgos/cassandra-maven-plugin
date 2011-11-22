package org.codehaus.mojo.fitnesse;

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class LogConsumer implements StreamConsumer
{

    private Log mLog;

    private Level mLevel;

    private boolean mHasGeneratedResultFile = false;

    public LogConsumer( Log pLog, Level pLevel )
    {
        super();
        mLog = pLog;
        mLevel = pLevel;
    }

    public void consumeLine( String pMessage )
    {
        if ( Level.INFO.equals( mLevel ) )
        {
            mLog.info( pMessage );
        }
        else
        {
            mLog.error( pMessage );
        }
        mHasGeneratedResultFile = mHasGeneratedResultFile || pMessage.startsWith( "Formatting as html" );

    }

    public boolean hasGeneratedResultFile()
    {
        return mHasGeneratedResultFile;
    }

}
