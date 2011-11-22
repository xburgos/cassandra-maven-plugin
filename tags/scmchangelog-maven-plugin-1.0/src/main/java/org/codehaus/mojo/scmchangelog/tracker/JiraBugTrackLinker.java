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
package org.codehaus.mojo.scmchangelog.tracker;

import java.text.MessageFormat;

/**
 * An implementation for the JIRA bug tracker.
 * @author ehsavoie
 * @version $Id$
 */
public class JiraBugTrackLinker
    implements BugTrackLinker
{

  /**
   * The url as a pattern for the links.
   */
  private String pattern;

  /**
   * Creates a new instance of JiraBugTrackLinker
   * @param jiraUrl the url to the instance of Jira.
   */
  public JiraBugTrackLinker( String jiraUrl )
  {
    String url = jiraUrl;
    if( jiraUrl.endsWith( "/" ) )
    {
      url = jiraUrl.substring( 0, jiraUrl.lastIndexOf( '/' ) );
    }
    this.pattern = url.substring( 0, url.lastIndexOf( '/' ) ) + "/{0}";
  }

  /**
   * Computes the link to the description of the specified bug
   * for Jira.
   * @param bugNumber the id of the bug.
   * @return the url to the description of the bug in Jira.
   */
  public String getLinkUrlForBug( String bugNumber )
  {
    return MessageFormat.format( pattern, new Object[]
        {
          bugNumber
        } );
  }
}
