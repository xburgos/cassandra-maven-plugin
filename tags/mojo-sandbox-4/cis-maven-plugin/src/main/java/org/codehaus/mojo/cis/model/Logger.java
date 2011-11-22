package org.codehaus.mojo.cis.model;

import org.apache.maven.plugin.logging.Log;

public class Logger implements org.codehaus.plexus.logging.Logger
{
    private final Log log;

    public Logger( Log log )
    {
        this.log = log;
    }

    public void debug( String message )
    {
        log.debug( message );
    }

    public void debug( String message, Throwable throwable )
    {
        log.debug( message, throwable );
    }

    public void error( String message )
    {
        log.error( message );
    }

    public void error( String message, Throwable throwable )
    {
        log.error( message, throwable );
    }

    public void fatalError( String message )
    {
        log.error( message );
    }

    public void fatalError( String message, Throwable throwable )
    {
        log.error( message, throwable );
    }

    public org.codehaus.plexus.logging.Logger getChildLogger( String pArg0 )
    {
        throw new IllegalStateException( "Not implemented" );
    }

    public String getName()
    {
        throw new IllegalStateException( "Not implemented" );
    }

    public int getThreshold()
    {
        throw new IllegalStateException( "Not implemented" );
    }

    public void info( String message )
    {
        log.info( message );
    }

    public void info( String message, Throwable throwable )
    {
        log.info( message, throwable );
    }

    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    public boolean isFatalErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled();
    }

    public void warn( String message )
    {
        log.warn( message );
    }

    public void warn( String message, Throwable throwable )
    {
        log.warn( message, throwable );
    }

}
