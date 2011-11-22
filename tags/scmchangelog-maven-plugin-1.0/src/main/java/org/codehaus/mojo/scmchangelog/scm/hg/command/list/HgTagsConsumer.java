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
package org.codehaus.mojo.scmchangelog.scm.hg.command.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.codehaus.mojo.scmchangelog.tags.Tag;

/**
 * Consumer for the output of the command: <code>hg tags --verbose path</code>.
 * @author ehsavoie
 * @version $Id$
 */
class HgTagsConsumer
    extends HgConsumer
{

  /**
   * List of tags found in the Mercurial repository.
   */
  private final List repositoryStatus = new ArrayList();

  /**
   * Instantiate a new HgTagsConsumer.
   * @param logger the logger.
   */
  HgTagsConsumer( ScmLogger logger )
  {
    super( logger );
  }

  /**
   * Consume a line of the command output.
   * @param status null.
   * @param trimmedLine the line.
   */
  public void doConsume( ScmFileStatus status, String trimmedLine )
  {
    Logger.getLogger( HgTagsConsumer.class.getName() ).log( Level.INFO, trimmedLine );

    int startRevisionIndex = trimmedLine.lastIndexOf( ' ' );
    int endRevisionIndex = trimmedLine.lastIndexOf( ':' );
    String title = trimmedLine.substring( 0, startRevisionIndex );
    String revisionId = trimmedLine.substring( startRevisionIndex + 1,
        endRevisionIndex );
    Tag tag = new Tag( title );
    tag.setStartRevision( "0" );
    tag.setEndRevision( revisionId );
    repositoryStatus.add( tag );
  }

  /**
   * Return the list of Tag.
   * @return List&lt;Tag&gt;
   * @see org.codehaus.mojo.scmchangelog.tags.Tag
   */
  List getStatus()
  {
    return repositoryStatus;
  }
}
