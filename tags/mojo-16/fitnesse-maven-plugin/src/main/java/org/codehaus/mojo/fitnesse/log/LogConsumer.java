package org.codehaus.mojo.fitnesse.log;

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

public class LogConsumer implements FitnesseStreamConsumer
{

    private Log mLog;

    private Level mLevel;

    private boolean mHasGeneratedResultFile = false;

    /** Only for test. */
    public LogConsumer( )
    {
    }

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

    public Level getLevel()
    {
        return mLevel;
    }

}
