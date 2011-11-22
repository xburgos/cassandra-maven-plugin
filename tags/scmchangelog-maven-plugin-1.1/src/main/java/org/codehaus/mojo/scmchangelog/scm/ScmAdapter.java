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
package org.codehaus.mojo.scmchangelog.scm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.mojo.scmchangelog.SvnTargetEnum;
import org.codehaus.mojo.scmchangelog.changelog.Release;
import org.codehaus.mojo.scmchangelog.changelog.log.GrammarEnum;
import org.codehaus.mojo.scmchangelog.changelog.log.SvnLogEntry;
import org.codehaus.mojo.scmchangelog.scm.hg.command.changelog.BetterChangeSet;
import org.codehaus.mojo.scmchangelog.tags.Tag;


/**
 * Adapter to be used to wrap the scm implementation.
 * @author ehsavoie
 * @version $Id$
 */
public class ScmAdapter
{
 /**
  * The ScmManager to access SCM elements.
  */
  private ScmManager manager;
  /**
   * The grammar used to extract elements from the comments.
   */
  private GrammarEnum grammar;
  /**
   * The maven logger.
   */
  private Log logger;

  /**
   * Constructor of ScmAdapter.
   * @param currentManager the ScmManager to access SCM elements.
   * @param currentGrammar the grammar used to extract elements from the comments.
   */
  public ScmAdapter( ScmManager currentManager, GrammarEnum currentGrammar )
  {
    this.manager = currentManager;
    this.grammar = currentGrammar;
  }

  /**
   * Returns the list of releases defined in the SCM.
   * @param repository the SCM repository.
   * @param fileSet the base fileset.
   * @return the list of releases defined in the SCM. <code>List&lt;Release&gt;</code>
   * @throws org.apache.maven.scm.ScmException in case of an error with the SCM.
   * @throws org.apache.maven.plugin.MojoExecutionException in case of an error in executing the Mojo.
   */
  public List getListOfReleases( ScmRepository repository, ScmFileSet fileSet )
      throws ScmException, MojoExecutionException
  {
    if ( "svn".equals( repository.getProvider() ) )
    {
      return getSvnListOfReleases( repository, fileSet );
    }

    if ( "hg".equals( repository.getProvider() ) )
    {
      return getHgListOfReleases( repository, fileSet );
    }

    return getScmListOfReleases( repository, fileSet );
  }

  /**
   * Returns the list of releases defined in the subversion repository.
   * @param repository the SCM repository.
   * @param fileSet the base fileset.
   * @return the list of releases defined in the SCM. <code>List&lt;Release&gt;</code>
   * @throws org.apache.maven.scm.ScmException in case of an error with the SCM.
   * @throws org.apache.maven.plugin.MojoExecutionException in case of an error in executing the Mojo.
   */
  protected List getSvnListOfReleases( ScmRepository repository,
      ScmFileSet fileSet )
      throws MojoExecutionException, ScmException
  {
    ListScmResult result = this.manager.list( repository, fileSet, false,
        getScmVersion( SvnTargetEnum.TAG, "" ) );
    final List tags = result.getFiles();
    getLogger().info( tags.toString() );
    final List releases = new ArrayList( 10 );
    Iterator iter = tags.iterator();

    while ( iter.hasNext() )
    {
      Tag tag = ( Tag ) iter.next();
      final ChangeLogScmResult logs = this.manager.changeLog( repository,
          fileSet,
          getScmVersion( SvnTargetEnum.TRUNK, tag.getStartRevision() ),
          getScmVersion( SvnTargetEnum.TRUNK, tag.getEndRevision() ), "" );
      if ( logs.getChangeLog() != null )
      {
        Release release = new Release( tag,
            logs.getChangeLog().getChangeSets() );
        releases.add( release );
      }
    }
    String endRevision = "0";
    if ( !tags.isEmpty() ) 
    {
      endRevision = ( ( Tag ) tags.get( tags.size() - 1 ) ).getEndRevision(); 
    }
    getLogger().info( "End revision : " + endRevision );
    final Tag trunk = new Tag( "trunk" );
    trunk.setStartRevision( endRevision );
    trunk.setDate( new Date() );
    trunk.setEndRevision( null );

    final ChangeLogScmResult logs = this.manager.changeLog( repository,
        fileSet, getScmVersion( SvnTargetEnum.TRUNK, endRevision ), null, "" );
    if ( logs.getChangeLog() != null )
    {
      final Release release = new Release( trunk,  logs.getChangeLog().getChangeSets() );
      releases.add( release );
    }
    Collections.reverse( releases );
    return releases;
  }

  /**
   * Returns the list of releases defined in a SCM repository.
   * @param repository the SCM repository.
   * @param fileSet the base fileset.
   * @return the list of releases defined in the SCM. <code>List&lt;Release&gt;</code>
   * @throws org.apache.maven.scm.ScmException in case of an error with the SCM.
   * @throws org.apache.maven.plugin.MojoExecutionException in case of an error in executing the Mojo.
   */
  protected List getScmListOfReleases( ScmRepository repository,
      ScmFileSet fileSet )
      throws MojoExecutionException, ScmException
  {
    throw new MojoExecutionException( "Unsupported SCM" );
  }

  /**
   * Returns the list of releases defined in the mercurial repository.
   * @param repository the SCM repository.
   * @param fileSet the base fileset.
   * @return the list of releases defined in the SCM. <code>List&lt;Release&gt;</code>
   * @throws org.apache.maven.scm.ScmException in case of an error with the SCM.
   * @throws org.apache.maven.plugin.MojoExecutionException in case of an error in executing the Mojo.
   */
  protected List getHgListOfReleases( ScmRepository repository,
      ScmFileSet fileSet )
      throws MojoExecutionException, ScmException
  {
    ListScmResult result = this.manager.list( repository, fileSet, false,
        getScmVersion( SvnTargetEnum.TAG, "" ) );
    final List tags = result.getFiles();
    final List releases = new ArrayList( 10 );
    Iterator iter = tags.iterator();
    String startRevision = "0";

    while ( iter.hasNext() )
    {
      Tag tag = ( Tag ) iter.next();
      getLogger().info( tag.toString() );

      final ChangeLogScmResult logs = this.manager.changeLog( repository,
          fileSet, getScmVersion( SvnTargetEnum.TRUNK, startRevision ),
          getScmVersion( SvnTargetEnum.TRUNK, tag.getEndRevision() ), "" );
      startRevision = tag.getEndRevision();
      getLogger().info( logs.getChangeLog().toString() );
      tag.setDate( logs.getChangeLog().getEndDate() );

      Release release = new Release( tag,
          getEntries( logs.getChangeLog().getChangeSets() ) );
      releases.add( release );
    }
    Collections.reverse( releases );
    return releases;
  }

  /**
   * Returns the list of log entries defined in the list of ChangeSet.
   * @param changeSets the list of ChangeSet.
   * @return the list of log entries defined in the list of ChangeSet. <code>List&lt;SvnLogEntry&gt;</code>
   */
  protected List getEntries( List changeSets )
  {
    List elements = new ArrayList( changeSets.size() );
    Iterator iter = changeSets.iterator();
    while ( iter.hasNext() )
    {
      BetterChangeSet changeSet = ( BetterChangeSet ) iter.next();
      SvnLogEntry entry = new SvnLogEntry();
      entry.setAuthor( changeSet.getAuthor() );
      entry.setDate( changeSet.getDate() );
      getLogger().info( changeSet.getComment() );
      entry.setMessage( grammar.extractMessage( changeSet.getComment() ) );
      entry.setRevision( changeSet.getRevision() );
      elements.add( entry );
    }
    return elements;
  }

  
  /**
   * Returns the Scm version.
   * @param versionType the type of version (tag, trunk, branch).
   * @param version the tag/branche name.
   * @return the corresponding ScmVersion.
   * @throws org.apache.maven.plugin.MojoExecutionException in case of an error in executing the Mojo.
   */
  public ScmVersion getScmVersion( SvnTargetEnum versionType, String version )
      throws MojoExecutionException
  {
    if ( SvnTargetEnum.TAG.equals( versionType ) )
    {
      return new ScmTag( version );
    } 
    else if ( SvnTargetEnum.BRANCH.equals( versionType ) )
    {
      return new ScmBranch( version );
    } 
    else if ( SvnTargetEnum.TRUNK.equals( versionType ) )
    {
      return new ScmRevision( version );
    }
    throw new MojoExecutionException( "Unknown version type : " 
        + versionType );
  }

  /**
   * The currentlogger.
   * @return the logger
   */
  public Log getLogger()
  {
    return logger;
  }

  /**
   * The current logger to be used.
   * @param logger the logger to set
   */
  public void setLogger( Log logger )
  {
    this.logger = logger;
  }
}
