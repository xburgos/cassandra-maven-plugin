package org.apache.maven.plugins.diagrams;

import org.apache.maven.diagrams.connector_api.logger.Logger;
import org.apache.maven.plugin.logging.Log;

public class PluginLoggerToDiagramsLoggerAdapter implements Logger
{
    private Log pluginLogger;

    public PluginLoggerToDiagramsLoggerAdapter( Log log )
    {
        pluginLogger = log;
    }

    public void debug( CharSequence arg0 )
    {
        pluginLogger.debug( arg0 );

    }

    public void debug( CharSequence arg0, Throwable arg1 )
    {
        pluginLogger.debug( arg0, arg1 );
    }

    public void debug( Throwable arg0 )
    {
        pluginLogger.debug( arg0 );
    }

    public void error( CharSequence arg0 )
    {
        pluginLogger.error( arg0 );

    }

    public void error( CharSequence arg0, Throwable arg1 )
    {
        pluginLogger.error( arg0, arg1 );
    }

    public void error( Throwable arg0 )
    {
        pluginLogger.error( arg0 );
    }

    public void info( CharSequence arg0 )
    {
        pluginLogger.info( arg0 );
    }

    public void info( CharSequence arg0, Throwable arg1 )
    {
        pluginLogger.info( arg0, arg1 );

    }

    public void info( Throwable arg0 )
    {
        pluginLogger.debug( arg0 );

    }

    public boolean isDebugEnabled()
    {
        return pluginLogger.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {

        return pluginLogger.isErrorEnabled();
    }

    public boolean isInfoEnabled()
    {
        return pluginLogger.isInfoEnabled();
    }

    public boolean isWarnEnabled()
    {
        return pluginLogger.isWarnEnabled();
    }

    public void warn( CharSequence arg0 )
    {
        pluginLogger.warn( arg0 );

    }

    public void warn( CharSequence arg0, Throwable arg1 )
    {
        pluginLogger.warn( arg0, arg1 );
    }

    public void warn( Throwable arg0 )
    {
        pluginLogger.warn( arg0 );
    }

}
