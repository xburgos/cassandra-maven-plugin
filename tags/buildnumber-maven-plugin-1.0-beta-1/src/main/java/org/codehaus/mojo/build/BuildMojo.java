package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2005 Learning Commons, University of Calgary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.svn.command.update.SvnUpdateScmResult;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This mojo is designed to give you a build number. So when you might make 100 builds of version
 * 1.0-SNAPSHOT, you can differentiate between them all. The build number is based on the revision
 * number retrieved from scm. It only works with subversion, currently. This mojo can also check to make
 * sure that you have checked everything into scm, before issuing the build number. That behaviour can be suppressed,
 * and then the latest local build number is used. Build numbers are not reflected
 * in your artifact's filename (automatically), but can be added to the metadata. You can access the build
 * number in your pom with ${buildNumber}. You can also access ${timestamp}.
 *
 * @author <a href="mailto:woodj@ucalgary.ca">Julian Wood</a>
 * @version $Id$
 * @goal create
 * @requiresProject
 * @description create a timestamp and a build number from scm or an integer sequence
 */
public class BuildMojo
        extends AbstractMojo
{

    /**
     * @parameter expression="${project.scm.developerConnection}"
     * @readonly
     */
    private String urlScm;

    /**
     * The username that is used when connecting to the SCM system.
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * The password that is used when connecting to the SCM system.
     *
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * @parameter expression="${tag}"
     * @todo This doesn't seem to be used. Can it be removed?
     */
    private String tag;

    /**
     * The tag base directory in subversion, you must define it if you don't
     * use the standard svn layout (branches/tags/trunk).
     *
     * @parameter expression="${tagBase}"
     */
    private String tagBase;

    /**
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * You can rename the buildNumber property name to another property name if desired.
     *
     * @parameter expression="${maven.buildNumber.buildNumberPropertyName}" default-value="buildNumber"
     */
    private String buildNumberPropertyName;

    /**
     * You can rename the timestamp property name to another property name if desired.
     * 
     * @parameter expression="${maven.buildNumber.timestampPropertyName}" default-value="timestamp"
     */
    private String timestampPropertyName;

    /**
     * If this is made true, we check for modified files, and if there are any, we fail the build. Note that this
     * used to be inverted (skipCheck), but needed to be changed to allow releases to work. This corresponds to
     * 'svn status'.
     *
     * @parameter expression="${maven.buildNumber.doCheck}"  default-value="false"
     */
    private boolean doCheck;

    /**
     * If this is made true, then the revision will be updated to the latest in the repo, otherwise it will
     * remain what it is locally. Note that this used to be inverted (skipUpdate), but needed to be changed to
     * allow releases to work. This corresponds to 'svn update'.
     *
     * Note that these expressions (doCheck, doUpdate, etc) are the first thing evaluated. If there is no matching
     * expression, we get the default-value. If there is (ie -Dmaven.buildNumber.doCheck=false), we get that value.
     * The configuration, however, gets the last say, through use of the getters/setters below. So if
     * <doCheck>true</doCheck>, then normally that's the final value of the param in question. However, this mojo
     * reverses that behaviour, such that the command line parameters get the last say.
     *
     * @parameter expression="${maven.buildNumber.doUpdate}"  default-value="false"
     */
    private boolean doUpdate;

    /**
     * Specify a message as specified by java.text.MessageFormat.
     *
     * @parameter
     */
    private String format;

    /**
     * Specify the corresponding items for the format message, as specified by java.text.MessageFormat. Special
     * item values are "timestamp" and "buildNumber/d*".
     * 
     * @parameter
     */
    private List items;

    /**
     * @component
     */
    private ScmManager scmManager;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private ScmLogDispatcher logger;

    private String revision;

    public void setScmManager( ScmManager scmManager )
    {
        this.scmManager = scmManager;
    }

    public void setUrlScm( String urlScm )
    {
        this.urlScm = urlScm;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setTag( String tag )
    {
        this.tag = tag;
    }

    public void setTagBase( String tagBase )
    {
        this.tagBase = tagBase;
    }

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public void setDoCheck( boolean doCheck )
    {
        String doCheckSystemProperty = System.getProperty( "maven.buildNumber.doCheck" );
        if ( doCheckSystemProperty != null )
        {
            // well, this gets the final say
            this.doCheck = Boolean.valueOf( doCheckSystemProperty ).booleanValue();
        }
        else
        {
            this.doCheck = doCheck;
        }
    }

    public void setDoUpdate( boolean doUpdate )
    {
        String doUpdateSystemProperty = System.getProperty( "maven.buildNumber.doUpdate" );
        if ( doUpdateSystemProperty != null )
        {
            // well, this gets the final say
            this.doUpdate = Boolean.valueOf( doUpdateSystemProperty ).booleanValue();
        }
        else
        {
            this.doUpdate = doUpdate;
        }
    }

    void setFormat( String format )
    {
        this.format = format;
    }

    void setItems( List items )
    {
        this.items = items;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Date now = Calendar.getInstance().getTime();
        if ( format != null )
        {
            // needs to be an array
            // look for special values
            Object[] itemAry = new Object[items.size()];
            for ( int i = 0; i < items.size(); i++ )
            {
                Object item = items.get( i );
                if ( item instanceof String )
                {
                    String s = (String) item;
                    if ( s.equals( "timestamp" ) )
                    {
                        itemAry[i] = now;
                    }
                    else if ( s.startsWith( "buildNumber" ) )
                    {
                        // check for properties file
                        File propertiesFile = new File( basedir, "buildNumber.properties" );

                        // create if not exists
                        if ( !propertiesFile.exists() )
                        {
                            try
                            {
                                propertiesFile.createNewFile();
                            }
                            catch ( IOException e )
                            {
                                throw new MojoExecutionException( "Couldn't create properties file: "
                                    + propertiesFile, e );
                            }
                        }

                        Properties properties = new Properties( );
                        String buildNumberString = null;
                        try
                        {
                            // get the number for the buildNumber specified
                            properties.load( new FileInputStream( propertiesFile ) );
                            buildNumberString = properties.getProperty( s );
                            if ( buildNumberString == null )
                            {
                                buildNumberString = "0";
                            }
                            int buildNumber = Integer.valueOf( buildNumberString ).intValue();

                            // store the increment
                            properties.setProperty( s, String.valueOf( ++buildNumber ) );
                            properties.store( new FileOutputStream( propertiesFile ),
                                              "maven.buildNumber.plugin properties file" );

                            // use in the message (format)
                            itemAry[i] = new Integer( buildNumber );
                        }
                        catch ( NumberFormatException e )
                        {
                            throw new MojoExecutionException(
                                "Couldn't parse buildNumber in properties file to an Integer: " + buildNumberString );
                        }
                        catch ( IOException e )
                        {
                            throw new MojoExecutionException( "Couldn't load properties file: " + propertiesFile, e );
                        }
                    }
                    else
                    {
                        itemAry[i] = item;
                    }
                }
                else
                {
                    itemAry[i] = item;
                }
            }

            revision = MessageFormat.format( format, itemAry );
        }
        else
        {
            if ( doCheck )
            {
                // we fail if there are local mods
                checkForLocalModifications();
            }
            else
            {
                getLog().info( "Checking for local modifications: skipped." );
            }

            if ( doUpdate )
            {
                // we update your local repo
                // even after you commit, your revision stays the same until you update, thus this action
                List changedFiles = update();
                for ( Iterator i = changedFiles.iterator(); i.hasNext(); )
                {
                    ScmFile file = (ScmFile) i.next();
                    getLog().info( "Updated: " + file );
                }
                if ( changedFiles.size() == 0 )
                {
                    getLog().info( "No files needed updating." );
                }
            }
            else
            {
                getLog().info( "Updating project files from SCM: skipped." );
            }

            revision = getRevision();
        }

        if ( project != null )
        {
            String timestamp = String.valueOf( now.getTime() );
            getLog().info( MessageFormat.format( "Storing buildNumber: {0} at timestamp: {1}",
                                                 new Object[] { revision, timestamp} ) );
            project.getProperties().put( buildNumberPropertyName, revision );
            project.getProperties().put( timestampPropertyName, timestamp );
        }
    }


    private void checkForLocalModifications()
        throws MojoExecutionException
    {
        getLog().info( "Verifying there are no local modifications ..." );

        List changedFiles;

        try
        {
            changedFiles = getStatus();
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "An error has occurred while checking scm status.", e );
        }

        if ( !changedFiles.isEmpty() )
        {
            StringBuffer message = new StringBuffer();

            for ( Iterator i = changedFiles.iterator(); i.hasNext(); )
            {
                ScmFile file = (ScmFile) i.next();

                message.append( file.toString() );

                message.append( "\n" );
            }

            throw new MojoExecutionException( "Cannot create the build number because you have local modifications : \n"
                + message );
        }

    }

    public List update()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            ScmProvider scmProvider = scmManager.getProviderByRepository( repository );

            UpdateScmResult result = scmProvider.update( repository,
                                                         new ScmFileSet( new File( basedir.getAbsolutePath() ) ),
                                                         null );

            checkResult( result );

            if ( result instanceof SvnUpdateScmResult )
            {
                String revision = ( (SvnUpdateScmResult) result ).getRevision();
                getLog().info( "Got a revision during update: " + revision );
                this.revision = revision;
            }

            return result.getUpdatedFiles();
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Couldn't update project.", e );
        }

    }


    public List getStatus()
        throws ScmException
    {

        ScmRepository repository = getScmRepository();

        ScmProvider scmProvider = scmManager.getProviderByRepository( repository );

        StatusScmResult result = scmProvider.status( repository,
                                                     new ScmFileSet( new File( basedir.getAbsolutePath() ) ) );

        checkResult( result );

        return result.getChangedFiles();

    }

    /**
     * Get the revision info from the repository. For svn, it is svn info
     *
     * @return
     * @throws MojoExecutionException
     */
    public String getRevision()
        throws MojoExecutionException
    {

        if ( format != null )
        {
            return revision;
        }

        try
        {
            ScmRepository repository = getScmRepository();

            InfoScmResult result = info( repository, new ScmFileSet( new File( basedir.getAbsolutePath() ) ) );

            checkResult( result );

            return result.getRevision();
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot get the revision information from the scm repository : \n"
                + e.getLocalizedMessage(), e );

        }

    }

    /**
     * Get info from svn.
     *
     * @param repository
     * @param fileSet
     * @return
     * @throws ScmException
     * @todo this should be rolled into org.apache.maven.scm.provider.ScmProvider
     * and org.apache.maven.scm.provider.svn.SvnScmProvider
     */
    public InfoScmResult info( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        SvnInfoCommand command = new SvnInfoCommand();

        command.setLogger( getLogger() );

        return (InfoScmResult) command.execute( repository.getProviderRepository(), fileSet, null );
    }

    /**
     * @return
     * @todo normally this would be handled in AbstractScmProvider
     */
    private ScmLogger getLogger()
    {
        if ( logger == null )
        {
            logger = new ScmLogDispatcher();
        }
        return logger;
    }


    private ScmRepository getScmRepository()
        throws ScmException
    {
        ScmRepository repository;

        try
        {
            repository = scmManager.makeScmRepository( urlScm );

            ScmProviderRepository scmRepo = repository.getProviderRepository();

            if ( !StringUtils.isEmpty( username ) )
            {
                scmRepo.setUser( username );
            }
            if ( !StringUtils.isEmpty( password ) )
            {
                scmRepo.setPassword( password );
            }

            if ( repository.getProvider().equals( "svn" ) )
            {
                SvnScmProviderRepository svnRepo = (SvnScmProviderRepository) repository.getProviderRepository();

                if ( tagBase != null && tagBase.length() > 0 )
                {
                    svnRepo.setTagBase( tagBase );
                }
            }
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't load the scm provider.", e );
        }

        return repository;
    }

    private void checkResult( ScmResult result )
        throws ScmException
    {
        if ( !result.isSuccess() )
        {
            // TODO: improve error handling
            System.err.println( "Provider message:" );

            System.err.println( result.getProviderMessage() );

            System.err.println( "Command output:" );

            System.err.println( result.getCommandOutput() );

            throw new ScmException( "Error!" );
        }
    }


}
