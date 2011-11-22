package org.codehaus.mojo.make;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.mojo.tools.project.extras.ArtifactPathResolver;
import org.codehaus.mojo.tools.project.extras.DependencyPathResolver;
import org.codehaus.mojo.tools.project.extras.PathResolutionException;
import org.codehaus.mojo.tools.project.extras.PrefixPropertyPathResolver;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;
import org.codehaus.plexus.util.cli.shell.Shell;

/**
 * Abstract base class for managing invocation of a Make-ish build target from Maven.
 * This class handles construction of the command-line and monitoring of the check file,
 * if specified. It will also handle chmod'ing the given shell command, if required.
 */
public abstract class AbstractMakeExecMojo
    extends AbstractMojo
{

    /**
     * The classic autoconf/make <code>DESTDIR</code> parameter which tells make to stage the
     * installation to a temp directory prior to packaging (allows for package
     * building without root access). <code>DESTDIR</code> is called a staged installation by the 
     * <a href=http://www.gnu.org/prep/standards/standards.html.gz#DESTDIR>GNU coding standards</a>
     * 
     * @parameter
     * @required
     */
    private String destDir;

    /**
     * MavenProject instance used to resolve property expressions from within Ant.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter expression="${workDir}" default-value="${project.build.sourceDirectory}"
     */
    private File workDir;

    /**
     * The dependency artifacts of this project, for resolving @pathOf(..)@ expressions.
     * These are of type org.apache.maven.artifact.Artifact, and are keyed by groupId:artifactId, 
     * using org.apache.maven.artifact.ArtifactUtils.versionlessKey(..) for consistent formatting.
     * 
     * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
     */
    private Set < Artifact > artifacts;

    /**
     * The shell command to execute. 
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     * @parameter default-value="make"
     */
    private String command;

    /**
     * The command-line arguments to include in the command invocation. 
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     * @parameter
     */
    private List < String > options;

    /**
     * resolveOptions is a setting to control if you want the make-maven-plugin to resolve
     * <code>@pathOf(..)@</code> expressions or not
     * @parameter default-value="true"
     */
    private boolean resolveOptions;

    /**
     * The Make target to execute. If this is null, we won't use it.
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     */
    private String target;

    /**
     * The file to check for modification before allowing the mojo to succeed. If the specified
     * file is not modified during this execution, the mojo should fail. If this file is not
     * specified, ignore this check.
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     */
    private String checkFile;

    /**
     * make-maven-plugin will define environment variables prior to execution of the commands
     * that it dispatches
     * @parameter
     */
    private Properties environment;
 
    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List < ArtifactRepository > remoteRepositories;

    /**
     * @parameter default-value="${project.pluginArtifactRepositories}"
     * @required
     * @readonly
     */
    private List < ArtifactRepository > remotePluginRepositories;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * plexus component to invoke shell commands
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * Result of search for the configuration script, so we don't have to re-search.
     */
    private File executable;

    /**
     * @parameter default-value="false"
     */
    private boolean searchForExecutable = false;

    /**
     * Configures the goal to ignore failures returned by the make command invocation
     * @parameter default-value="false"
     */
    private boolean ignoreFailures = false;

    /**
     * Configures the goal to ignore errors returned by the make command invocation
     * @parameter default-value="false"
     */
    private boolean ignoreErrors = false;

    /**
     * Configures the goal to change the make command to executable prior to invocation
     * @parameter default-value="false"
     */
    private boolean chmodUsed = false;

    /**
     * If set, the goal will be skipped
     * @parameter default-value="false"
     */
    private boolean skipped;

    /**
     * Behavior to take command and use explicit pathing instead of using the shell's path
     * @parameter default-value="false"
     */
    private boolean absoluteCommandPathUsed;

    /**
     * 0. Validate that all required non-parameters are set.
     * 
     * 1. If the check file is set, log the lastMod for it.
     * 
     * 2. If the command given doesn't resolve to an existing file on its own, then search for that 
     *    command file within the working directory.
     *    
     * 3. If chmodUsed == true, then we'll set the executable bit on the executable file using chmod.
     * 
     * 4. Construct the Ant Exec task using the supplied command, any command-line options, an optional
     *    Make target, working directory, and overrides for failure conditions. 
     *    
     * 5. Next, execute the resulting Exec task.
     * 
     * @throws MojoExecutionException thrown when an error occurs
     */
    public void execute() throws MojoExecutionException
    {
        if ( skipped )
        {
            getLog().info( "Skipping this step of the Make process." );
            return;
        }
        
        getLog().debug( "In AbstractMakeExecMojo.execute:: workDir= " + workDir );
        checkSanity();
        workDir.mkdirs();
        File check = null;
        long lastMod = 0;

        if ( checkFile != null )
        {
            check = new File( checkFile );
            lastMod = check.lastModified();
        }
        if ( searchForExecutable )
        {
            try
            {
                findExecutable();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to canonicalize executable path.", e );
            }
        }
        if ( chmodUsed )
        {
            executeChmod();
        }

        Commandline cli = new Commandline();
        Shell shell = new BourneShell( true );
        shell.setQuotedArgumentsEnabled( false );
        cli.setShell( shell );
        //This doesn't work in a login shell, apparently...
        //cli.setWorkingDirectory( workDir.getAbsolutePath() );
        cli.createArg().setLine( "cd " + workDir.getAbsolutePath() + " && " );

        if ( executable != null )
        {
            if ( absoluteCommandPathUsed )
            {
                cli.createArg().setLine( executable.getAbsolutePath() );
            }
            else
            {
                cli.createArg().setLine( subPath( executable, workDir ) );
            }
        }
        else
        {
            // assume the command is somewhere on the path...
            cli.createArg().setLine( command );
        }

        List < ArtifactRepository > repos = new ArrayList < ArtifactRepository > ();
        repos.addAll( remoteRepositories );
        repos.addAll( remotePluginRepositories );
        ArtifactPathResolver pathResolver = new PrefixPropertyPathResolver( projectBuilder,
            repos, localRepository, artifactFactory, getLog() );
        DependencyPathResolver resolver = new DependencyPathResolver( artifacts, pathResolver, getLog() );
        if ( options != null )
        {
            List < String > resolvedPaths = new ArrayList < String > ();
            for ( Iterator < String > it = options.iterator(); it.hasNext(); )
            {
                String option = it.next();
                if ( resolveOptions )
                {
                    try
                    {
                        option = resolver.resolveDependencyPaths( option );
                        resolvedPaths.add( option );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error resolving option: \'"
                            + option + "\'. Reason: " + e.getMessage(), e );
                    }
                    catch ( PathResolutionException e )
                    {
                        throw new MojoExecutionException( "Error resolving dependency paths for: "
                            + option + ". Reason: " + e.getMessage(), e );
                    }
                }
                cli.createArg().setLine( option );
            }
        }

        if ( environment != null && !environment.isEmpty() )
        {
            for ( Iterator it = environment.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                String key = String.valueOf( entry.getKey() );
                String val = String.valueOf( entry.getValue() );
                
                if ( resolveOptions )
                {
                    try
                    {
                        val = resolver.resolveDependencyPaths( val );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error resolving environment variable: \'"
                            + val + "\'. Reason: " + e.getMessage(), e );
                    }
                    catch ( PathResolutionException e )
                    {
                        throw new MojoExecutionException( "Error resolving dependency paths for: "
                            + val + ". Reason: " + e.getMessage(), e );
                    }
                }
                cli.addEnvironment( key, val );
                getLog().debug( "Setting envar: " + key + "=" + val );
            }                                           
        }

        if ( target != null && target.trim().length() > 0 )
        {
            cli.createArg().setLine( target );
        }
        getLog().debug( "Executing: " + StringUtils.join( cli.getShellCommandline(), " " ) );
        try
        {
            StreamConsumer consumer = cliManager.newInfoStreamConsumer();
            int result = cliManager.execute( cli, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( this.command
                  + " returned an exit value != 0. Aborting build; see command output above for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to execute. Reason: " + e.getMessage(), e );
        }
        if ( check != null && check.lastModified() <= lastMod )
        {
            throw new MojoExecutionException( "Check file: " + check + " not changed." );
        }
    }

    /**
     * Method tried to see if relative or explicit pathing should be used for make
     * command invocation
     * @param exec make command executable File
     * @param workDirectory shell working directory for invocation of the make operation
     * @return path which make be a relative or explicit path
     * @throws MojoExecutionException thrown if File.getCanonicalPath method fails
     */
    private String subPath( File exec, File workDirectory ) throws MojoExecutionException
    {
        String path;
        String workingPath;
        try
        {
            path = exec.getCanonicalPath();
            workingPath = workDirectory.getCanonicalPath();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to canonicalize paths.", e );
        }
        
        if ( path.startsWith( workingPath ) )
        {
            path = path.substring( workingPath.length() );
            
            if ( path.startsWith( File.separator ) )
            {
                path = path.substring( 1 );
            }
            
            path = "./" + path;
        }
        
        return path;
    }

    /**
     * Be sure we have all of the required non-parameter variables set before we try to
     * execute.
     */
    private void checkSanity()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "The following usage errors were encountered:\n" );

        boolean errorsOccurred = false;

        if ( command == null )
        {
            buffer.append( "\no Make command cannot be null." );
            errorsOccurred = true;
        }
        
        if ( workDir == null )
        {
            buffer.append( "\no working directory cannot be null." );
            errorsOccurred = true;
        }

        if ( errorsOccurred )
        {
            throw new IllegalStateException( buffer.toString() );
        }
    }

    /**
     * Read if goal is configured to change the make command to executable prior to execution
     * @return boolean on chmod prior to invocation is requested
     */
    protected final boolean isChmodUsed()
    {
        return chmodUsed;
    }

    /**
     * Configure the goal to change the make command to executable prior to invocation
     * @param chmodUsed boolean to set plugin behavior to chmod the command prior to invocation
     */
    protected final void setChmodUsed( boolean chmodUsed )
    {
        this.chmodUsed = chmodUsed;
    }

    /**
     * See if the goal is configured to check for a file after execution
     * @return String value for a file to check or <code>null</code>
     */
    protected final String getCheckFile()
    {
        return checkFile;
    }

    /**
     * Set a file to check after a goal executes
     * @param checkFile String object of a file to check after goal completes
     */
    protected final void setCheckFile( String checkFile )
    {
        this.checkFile = checkFile;
    }

    /**
     * Get the current setting for the make command used
     * @return the make command
     */
    protected final String getCommand()
    {
        return command;
    }

    /**
     * set the make command to dispatch to the shell, typically it is just <code>make</code>
     * @param command overloads the make command
     */
    protected final void setCommand( String command )
    {
        this.command = command;
    }

    /**
     * Reads the behavior setting for the path used for the command to be dispatched to the shell
     * @return absolute paths being used or not
     */
    protected final boolean isAbsoluteCommandPathUsed()
    {
        return absoluteCommandPathUsed;
    }

    /**
     * sets a behavior option of the goal to use the absolute path for the command run by the shell
     * @param absolute boolean to change path name of command to be run by the shell
     */
    protected final void setAbsoluteCommandPathUsed( boolean absolute )
    {
        this.absoluteCommandPathUsed = absolute;
    }

    /**
     * Gets the make command options
     * @return list of make command options
     */
    protected final List < String > getOptions()
    {
        return options;
    }

    /**
     * Sets the goal's options to the make command
     * @param options list of options, may be parsed for <code>@pathOf(..)@</code> style macros
     */
    protected final void setOptions( List < String > options )
    {
        this.options = options;
    }

    /**
     * returns the maven project
     * @return maven project object
     */
    protected final MavenProject getProject()
    {
        return project;
    }

    /**
     * Read the make target command, usually something like <code>compile</code> or <code>clean</code>
     * @return the make target
     */
    protected final String getTarget()
    {
        return target;
    }

    /**
     * Each goal (unpack, clean, compile, dist, etc) will use this method to set the make target command
     * @param target the target command
     */
    protected final void setTarget( String target )
    {
        this.target = target;
    }

    /**
     * Set the goal behavior when failures are encountered
     * @param ignoreFailures boolean to set goal behavior when failures are encountered
     */
    protected final void setIgnoreFailures( boolean ignoreFailures )
    {
        this.ignoreFailures = ignoreFailures;
    }

    /**
     * Read the goal setting for skipping failures
     * @return boolean setting if goal will ignore failures
     */
    protected final boolean ignoreFailures()
    {
        return ignoreFailures;
    }

    /**
     * Set goal behavior when errors are encountered
     * @param ignoreErrors boolean to set goal behavior if an error is encountered
     */
    protected final void setIgnoreErrors( boolean ignoreErrors )
    {
        this.ignoreErrors = ignoreErrors;
    }

    /**
     * Read the goal setting for skipping errors
     * @return boolean setting if goal will ignore errors
     */
    protected final boolean ignoreErrors()
    {
        return ignoreErrors;
    }

    /**
     * Returns the value for <code>DESTDIR</code>
     * @return the <code>DESTDIR</code> which is a scratch installation target for <code>make install</code>.
     *         <code>DESTDIR</code> is called a staged installation by the 
     *         <a href=http://www.gnu.org/prep/standards/standards.html.gz#DESTDIR>GNU coding standards</a>
     */
    protected String getDestDir()
    {
        return destDir;
    }

    /**
     * set the working directory
     * @param workDir the directory where you want the shell working directory to be when make runs
     */
    protected void setWorkDir( File workDir )
    {
        this.workDir = workDir;
    }

    /**
     * the shell working directory to run the make operations
     * @return directory where make will do its work
     */
    protected File getWorkDir()
    {
        return workDir;
    }

    /**
     * Locate the command, and cache the resulting File for later reference.
     * @throws IOException thrown if java can not run a File operation
     */
    private void findExecutable() throws IOException
    {
        // 1. check for the configuration script as-is, in case it's fully qualified.
        executable = new File( command );

        if ( !executable.exists() )
        {
            getLog().debug( "Cannot find command executable file: \'" + command + "\'. Checking work directory." );
            
            // 2. Check for the configuration script inside the working directory.
            executable = new File( workDir, command );

            // If we can't find it in #1 or #2, then die.
            if ( !executable.exists() )
            {
                executable = null;

                getLog().debug(
                               "Cannot find command executable file: \'" + command + "\' in work directory: \'"
                                   + workDir + "\'. We will assume this command is on the system path." );
            }
        }
    }

    /**
     * Add the Ant task used to make the command executable.
     * @throws MojoExecutionException thrown if the <code>chmod</code> operation encounters a problem
     */
    private void executeChmod() throws MojoExecutionException
    {
        if ( executable != null )
        {
            Commandline cli = new Commandline();
            cli.setExecutable( "chmod" );
            cli.createArg().setLine( "+x" );
            
            cli.createArg().setLine( executable.getAbsolutePath() );
            
            StreamConsumer consumer = cliManager.newDebugStreamConsumer();
            
            try
            {
                int result = cliManager.execute( cli, consumer, consumer );
                
                if ( result != 0 )
                {
                    throw new MojoExecutionException( "chmod command returned an exit value != 0. Aborting build; "
                        + "see debug output for more information." );
                }
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException( "Failed to chmod +x " + executable.getPath() 
                                                  + ". Reason: " + e.getMessage(), e );
            }
        }
    }

    /**
     * returns if this goal is to be skipped
     * @return if the goal is configured to be skipped
     */
    public final boolean isSkipped()
    {
        return skipped;
    }

    /**
     * Set this if you are using make-maven-plugin in a set lifecycle and you want to skip a goal
     * @param skipped Set this if you are using make-maven-plugin in a set lifecycle and you want to skip a goal
     */
    public final void setSkipped( boolean skipped )
    {
        this.skipped = skipped;
    }

    /**
     * Read if make-maven-plugin is configureed to resolve <code>@pathOf(..)@</code> expressions or not
     * @return boolean if make-maven-plugin is configured to resolve <code>@pathOf(..)@</code>
     *        expressions or not
     */
    public final boolean isOptionsResolved()
    {
        return resolveOptions;
    }

    /**
     * Configure make-maven-plugin to resolve <code>@pathOf(..)@</code> expressions or not
     * @param resolveOpts boolean to configure make-maven-plugin to resolve <code>@pathOf(..)@</code>
     *        expressions or not
     */
    public final void setOptionsResolved( boolean resolveOpts )
    {
        this.resolveOptions = resolveOpts;
    }

    /**
     * Gets boolean value if make-maven-mojo is configured to search for executable prior to execution
     * @return returns boolean value if make-maven-mojo is configured to search for executable prior to execution
     */
    public boolean isSearchForExecutable()
    {
        return searchForExecutable;
    }

    /**
     * Sets boolean value to tell make-maven-mojo to search for executable prior to execution
     * @param searchForExecutable boolean value to tell make-maven-mojo to search for executable prior to execution
     */
    public void setSearchForExecutable( boolean searchForExecutable )
    {
        this.searchForExecutable = searchForExecutable;
    }

    /**
     * Takes a list of java properties and saves them for use as environment variables used when the 
     * make operations are executed
     * 
     * @param environment list of environment variables stored in a java Properties
     */
    protected final void setEnvironment( Properties environment )
    {
        this.environment = environment;
    }

    /**
     * Get the environment variables for the object
     * @return environment variables returned as java Properties
     */
    protected final Properties getEnvironment()
    {
        return environment;
    }

}
