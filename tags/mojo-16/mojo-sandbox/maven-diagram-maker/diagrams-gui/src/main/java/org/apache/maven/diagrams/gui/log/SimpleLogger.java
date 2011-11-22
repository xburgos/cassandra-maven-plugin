package org.apache.maven.diagrams.gui.log;

import org.apache.maven.diagrams.connector_api.logger.Logger;

public class SimpleLogger implements Logger
{
    public void debug( CharSequence arg0 )
    {
        System.out.println( "[DEBUG] " + arg0 );
    }

    public void debug( CharSequence arg0, Throwable arg1 )
    {
        System.out.println( "[DEBUG] " + arg0 );
        arg1.printStackTrace( System.out );
    }

    public void debug( Throwable arg1 )
    {
        System.out.print( "[DEBUG] " );
        arg1.printStackTrace( System.out );
    }

    public void error( CharSequence arg0 )
    {
        System.out.println( "[ERROR] " + arg0 );
    }

    public void error( CharSequence arg0, Throwable arg1 )
    {
        System.out.println( "[ERROR] " + arg0 );
        arg1.printStackTrace( System.out );
    }

    public void error( Throwable arg1 )
    {
        System.out.print( "[ERROR] " );
        arg1.printStackTrace( System.out );
    }

    public void info( CharSequence arg0 )
    {
        System.out.println( "[INFO] " + arg0 );
    }

    public void info( CharSequence arg0, Throwable arg1 )
    {
        System.out.println( "[INFO] " + arg0 );
        arg1.printStackTrace( System.out );
    }

    public void info( Throwable arg1 )
    {
        System.out.print( "--[INFO] " );
        arg1.printStackTrace( System.out );
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public boolean isInfoEnabled()
    {
        return true;
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void warn( CharSequence arg0 )
    {
        System.out.println( "[WARN] " + arg0 );
    }

    public void warn( CharSequence arg0, Throwable arg1 )
    {
        System.out.println( "[WARN] " + arg0 );
        arg1.printStackTrace( System.out );
    }

    public void warn( Throwable arg1 )
    {
        System.out.print( "[WARN] " );
        arg1.printStackTrace( System.out );
    }
}
