/*
The MIT License

Copyright (c) 2004, The Codehaus

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package org.codehaus.mojo.scmchangelog;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.scm.log.ScmLogger;

/**
 * Implementation of ScmLogger using java.util.logging API.
 * @author ehsavoie
 * @version $Id$
 * @see org.apache.maven.scm.log.ScmLogger
 */
public class JavaScmLogger
    implements ScmLogger
{

  /**
   * The inner logger implementation using java.util.logging API.
   */
  private Logger logger = Logger.getLogger( JavaScmLogger.class.getName() );

  /**
   * The current log level.
   */
  private Level currentLevel;

  /**
   * Instantiate a new logger.
   * @param level the log level.
   */
  public JavaScmLogger( Level level )
  {
    this.currentLevel = level;
  }

  /**
   * Indicates if we are at a FINE or more verbose level.
   * @return true if debug is enabled - false otherwise.
   */
  public boolean isDebugEnabled()
  {
    return this.currentLevel.intValue() <= Level.FINE.intValue();
  }

 /**
  * Trace a message with a FINE level.
  * @param content the message to be traced.
  */
  public void debug( String content )
  {
    if ( isDebugEnabled() )
    {
      logger.log( Level.FINE, content );
    }
  }

  /**
   * Trace, at the FINE level, a message  associated with an exception.
   * @param content the message to be traced.
   * @param error the exception to be traced.
   */
  public void debug( String content, Throwable error )
  {
    if ( isDebugEnabled() )
    {
      logger.log( Level.FINE, content, error );
    }
  }

  /**
   * Trace, at the FINE level, an exception.
   * @param error the exception to be traced.
   */
  public void debug( Throwable error )
  {
    if ( isDebugEnabled() )
    {
      logger.log( Level.FINE, "", error );
    }
  }

  /**
   * Indicates if we are at a INFO or more verbose level.
   * @return true if info is enabled - false otherwise.
   */
  public boolean isInfoEnabled()
  {
    return this.currentLevel.intValue() <= Level.INFO.intValue();
  }

 /**
  * Trace a message with a INFO level.
  * @param content the message to be traced.
  */
  public void info( String content )
  {
    if ( isInfoEnabled() )
    {
      logger.log( Level.INFO, content );
    }
  }

  /**
   * Trace, at the INFO level, a message  associated with an exception.
   * @param content the message to be traced.
   * @param error the exception to be traced.
   */
  public void info( String content, Throwable error )
  {
    if ( isInfoEnabled() )
    {
      logger.log( Level.INFO, content, error );
    }
  }

  /**
   * Trace, at the INFO level, an exception.
   * @param error the exception to be traced.
   */
  public void info( Throwable error )
  {
    if ( isInfoEnabled() )
    {
      logger.log( Level.INFO, "", error );
    }
  }

  /**
   * Indicates if we are at a WARNING or more verbose level.
   * @return true if warning is enabled - false otherwise.
   */
  public boolean isWarnEnabled()
  {
    return this.currentLevel.intValue() <= Level.WARNING.intValue();
  }

  /**
  * Trace a message with a WARNING level.
  * @param content the message to be traced.
  */
  public void warn( String content )
  {
    if ( isWarnEnabled() )
    {
      logger.log( Level.WARNING, content );
    }
  }

  /**
   * Trace, at the WARNING level, a message  associated with an exception.
   * @param content the message to be traced.
   * @param error the exception to be traced.
   */
  public void warn( String content, Throwable error )
  {
    if ( isWarnEnabled() )
    {
      logger.log( Level.WARNING, content, error );
    }
  }

  /**
   * Trace, at the WARNING level, an exception.
   * @param error the exception to be traced.
   */
  public void warn( Throwable error )
  {
    if ( isWarnEnabled() )
    {
      logger.log( Level.WARNING, "", error );
    }
  }

  /**
   * Indicates if we are at a SEVERE or more verbose level.
   * @return true if error is enabled - false otherwise.
   */
  public boolean isErrorEnabled()
  {
    return this.currentLevel.intValue() <= Level.SEVERE.intValue();
  }

 /**
  * Trace a message with a ERROR level.
  * @param content the message to be traced.
  */
  public void error( String content )
  {
    if ( isErrorEnabled() )
    {
      logger.log( Level.SEVERE, content );
    }
  }

  /**
   * Trace, at the SEVERE level, a message  associated with an exception.
   * @param content the message to be traced.
   * @param error the exception to be traced.
   */
  public void error( String content, Throwable error )
  {
    if ( isErrorEnabled() )
    {
      logger.log( Level.SEVERE, content, error );
    }
  }

  /**
   * Trace, at the SEVERE level, an exception.
   * @param error the exception to be traced.
   */
  public void error( Throwable error )
  {
    if ( isErrorEnabled() )
    {
      logger.log( Level.SEVERE, "", error );
    }
  }
}
