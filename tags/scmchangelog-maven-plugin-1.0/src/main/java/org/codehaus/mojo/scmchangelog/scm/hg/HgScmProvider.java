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
package org.codehaus.mojo.scmchangelog.scm.hg;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.mojo.scmchangelog.changelog.log.GrammarEnum;
import org.codehaus.mojo.scmchangelog.scm.hg.command.changelog.HgChangeLogCommand;
import org.codehaus.mojo.scmchangelog.scm.hg.command.list.HgListCommand;

/**
 * Wrapper over SvnExeScmProvider to use xml output from Subversion.
 * @author ehsavoie
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="svn"
 * @see org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider
 */
public class HgScmProvider
    extends org.apache.maven.scm.provider.hg.HgScmProvider
{

  /**
   * The comment grammar to be used.
   */
  private GrammarEnum grammar;

  /**
   * Creates a new instance of SvnXmlExeScmProvider.
   * @param commentGrammar the grammar tobe used.
   */
  public HgScmProvider( GrammarEnum commentGrammar )
  {
    this.grammar = commentGrammar;
  }

  /**
   * Returns a new instance of SvnCommand to execute a
   * <code>svn list --xml</code> command.
   * @return a SvnListCommand.
   */
  public HgListCommand getListCommand()
  {
    return new HgListCommand();
  }

  /**
   * Execute the list command for the Mercuial repository.
   * @param repository the repository.
   * @param fileSet the files.
   * @param parameters the command parameters.
   * @return a list of Tag.
   * @throws org.apache.maven.scm.ScmException in case of an error with the scm command.
   * @see org.codehaus.mojo.scmchangelog.tags.Tag
   */
  protected ListScmResult list( ScmProviderRepository repository,
      ScmFileSet fileSet, CommandParameters parameters )
      throws ScmException
  {
    Command command = getListCommand();
    command.setLogger( getLogger() );
    return ( ListScmResult ) command.execute( repository, fileSet, parameters );
  }

  /**
   * Execute the changelog command for the Mercuial repository.
   * @param repository the repository
   * @param fileSet the files.
   * @param parameters the command parameters.
   * @return a list of BetterChangeSet.
   * @throws org.apache.maven.scm.ScmException  in case of an error with the scm command.
   * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.provider.ScmProviderRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
   * @see org.codehaus.mojo.scmchangelog.scm.hg.changelog.BetterChangeSet
   */
  public ChangeLogScmResult changelog( ScmProviderRepository repository,
      ScmFileSet fileSet, CommandParameters parameters )
      throws ScmException
  {
    HgChangeLogCommand command = new HgChangeLogCommand();
    command.setLogger( getLogger() );

    return ( ChangeLogScmResult ) command.execute( repository, fileSet,
        parameters );
  }

  /**
   * Returns the String to be used as issue separator.
   * @return the String to be used as issue separator.
   */
  public String getCommentSeparator()
  {
    return this.grammar.getIssueSeparator();
  }
}
