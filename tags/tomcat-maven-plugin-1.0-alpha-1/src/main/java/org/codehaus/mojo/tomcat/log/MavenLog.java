package org.codehaus.mojo.tomcat.log;

/*
 * Copyright 2006 Mark Hobson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

/**
 * A JCL log implementation that delegates to a Maven log.
 * 
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class MavenLog implements Log
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    /**
     * The name of the log factory attribute that holds the Maven log to use.
     */
    private static final String MAVEN_LOG_ATTRIBUTE = "maven.log";

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * The log factory to read attributes from.
     */
    private final LogFactory logFactory;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a new <code>MavenLog</code> from the specified log factory.
     * 
     * @param logFactory
     *            the log factory to read attributes from
     * @throws IllegalArgumentException
     *             if the specified log factory was null
     */
    public MavenLog( LogFactory logFactory )
    {
        if ( logFactory == null )
        {
            throw new IllegalArgumentException( "Log factory cannot be null" );
        }

        this.logFactory = logFactory;
    }

    // ----------------------------------------------------------------------
    // Log Implementation
    // ----------------------------------------------------------------------

    /*
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled()
    {
        return isDebugEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return getMavenLog().isDebugEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return getMavenLog().isInfoEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return getMavenLog().isWarnEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return getMavenLog().isErrorEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled()
    {
        return isErrorEnabled();
    }

    /*
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace( Object message )
    {
        if ( isTraceEnabled() )
        {
            debug( message );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace( Object message, Throwable throwable )
    {
        if ( isTraceEnabled() )
        {
            debug( message, throwable );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug( Object message )
    {
        if ( isDebugEnabled() )
        {
            getMavenLog().debug( String.valueOf( message ) );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug( Object message, Throwable throwable )
    {
        if ( isDebugEnabled() )
        {
            getMavenLog().debug( String.valueOf( message ), throwable );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info( Object message )
    {
        if ( isInfoEnabled() )
        {
            getMavenLog().info( String.valueOf( message ) );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info( Object message, Throwable throwable )
    {
        if ( isInfoEnabled() )
        {
            getMavenLog().info( String.valueOf( message ), throwable );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn( Object message )
    {
        if ( isWarnEnabled() )
        {
            getMavenLog().warn( String.valueOf( message ) );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn( Object message, Throwable throwable )
    {
        if ( isWarnEnabled() )
        {
            getMavenLog().warn( String.valueOf( message ), throwable );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error( Object message )
    {
        if ( isErrorEnabled() )
        {
            getMavenLog().error( String.valueOf( message ) );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error( Object message, Throwable throwable )
    {
        if ( isErrorEnabled() )
        {
            getMavenLog().error( String.valueOf( message ), throwable );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal( Object message )
    {
        if ( isFatalEnabled() )
        {
            error( message );
        }
    }

    /*
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal( Object message, Throwable throwable )
    {
        if ( isFatalEnabled() )
        {
            error( message, throwable );
        }
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /**
     * Gets the underlying Maven log to delegate to.
     * 
     * @return the Maven log
     */
    protected org.apache.maven.plugin.logging.Log getMavenLog()
    {
        org.apache.maven.plugin.logging.Log mavenLog =
            (org.apache.maven.plugin.logging.Log) logFactory.getAttribute( MAVEN_LOG_ATTRIBUTE );

        if ( mavenLog == null )
        {
            throw new LogConfigurationException( "The LogFactory attribute " + MAVEN_LOG_ATTRIBUTE + " must be set." );
        }

        return mavenLog;
    }
}
