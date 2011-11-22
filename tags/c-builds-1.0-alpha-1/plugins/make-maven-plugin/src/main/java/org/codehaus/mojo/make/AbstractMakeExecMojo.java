package org.codehaus.mojo.make;

/*
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
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.project.extras.ArtifactPathResolver;
import org.codehaus.mojo.tools.project.extras.DependencyPathResolver;
import org.codehaus.mojo.tools.project.extras.PathResolutionException;
import org.codehaus.mojo.tools.project.extras.PrefixPropertyPathResolver;

/**
 * Abstract base class for managing invocation of a Make-ish build target from Maven.
 * This class handles construction of the command-line and monitoring of the check file,
 * if specified. It will also handle chmod'ing the given shell command, if required.
 */
public abstract class AbstractMakeExecMojo
    extends AbstractMojo
{

    /**
     * The classic Make DEST_DIR parameter
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

    // cwb >>>>
    //parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}" alias="workDir"
    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter expression="${workDir}" default-value="${project.build.directory}/${project.artifactId}-${project.version}"
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
    private Set artifacts;

    /**
     * The Ant messageLevel to use.
     * 
     * @parameter expression="${messageLevel}" default-value="info"
     */
    private String messageLevel;

    /**
     * The shell command to execute. 
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     */
    private String command = "make";

    /**
     * The command-line arguments to include in the command invocation. 
     * 
     * NOTE: This is NOT a direct parameter. We need to allow the concrete implementations 
     * to specify their own params for this, so that we can allow all mojos to be configured 
     * from the same section of the plugin declaration without worrying about naming collisions.
     */
    private List options;

    private boolean resolveOptions = true;

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
     * @parameter
     */
    private Properties environment;
    
    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;
    
    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;
    
    /**
     * @parameter default-value="${project.pluginArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remotePluginRepositories;
    
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
     * Result of search for the configuration script, so we don't have to re-search.
     */
    private File executable;

    private boolean searchForExecutable = false;

    private boolean ignoreFailures = false;

    private boolean ignoreErrors = false;

    private boolean chmodUsed = false;

    private boolean skipped = false;

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
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipped )
        {
            getLog().info( "Skipping this step of the Make process." );
            return;
        }
        
        if ( BuildAdvisor.isProjectBuildSkipped( project, session.getContainer().getContext() ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
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

        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );

        if ( messageLevel != null )
        {
            antCaller.setMessageLevel( messageLevel );
        }

        antCaller.setProjectBasedir( workDir );

        if ( searchForExecutable )
        {
            findExecutable();
        }
        
        if ( chmodUsed )
        {
            // add the task to make the configure script executable
            addChmodTask( antCaller );
        }

        ExecTask exec = new ExecTask();

        exec.setDir( workDir );

        exec.setFailIfExecutionFails( !ignoreFailures );
        exec.setFailonerror( !ignoreErrors );
        
        // make a stab at trimming the task name down a bit...
        String taskName = command;

        int lastSlash = taskName.lastIndexOf( File.separator );

        if ( lastSlash > -1 )
        {
            taskName = taskName.substring( lastSlash + 1 );
        }

        if ( target != null )
        {
            taskName += "->" + target;
        }

        exec.setTaskName( "exec:" + taskName );

        if ( executable != null )
        {
            exec.setExecutable( executable.getAbsolutePath() );
        }
        else
        {
            // assume the command is somewhere on the path...
            exec.setExecutable( command );
        }

        getLog().debug( "Executing command: " + command + " in work dir: " + workDir );

        List repos = new ArrayList();
        repos.addAll( remoteRepositories );
        repos.addAll( remotePluginRepositories );
        
        ArtifactPathResolver pathResolver = new PrefixPropertyPathResolver( projectBuilder, repos, localRepository, artifactFactory, getLog() );
        
        DependencyPathResolver resolver = new DependencyPathResolver( artifacts, pathResolver, getLog() );
        
        if ( options != null )
        {
            List resolvedPaths = new ArrayList();

            for ( Iterator it = options.iterator(); it.hasNext(); )
            {
                String option = (String) it.next();

                if ( resolveOptions )
                {
                    try
                    {
                        option = resolver.resolveDependencyPaths( option );
                        resolvedPaths.add( option );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error resolving option: \'" + option + "\'. Reason: "
                            + e.getMessage(), e );
                    }
                    catch ( PathResolutionException e )
                    {
                        throw new MojoExecutionException( "Error resolving dependency paths for: " + option + ". Reason: " + e.getMessage(), e );
                    }
                }

                exec.createArg().setLine( option );
            }

            getLog().debug( " Resolved ConfigOptions = " + resolvedPaths );
        }

        if ( environment != null && !environment.isEmpty() )
        {
            for ( Iterator it = environment.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                
                Environment.Variable var = new Environment.Variable();
                var.setKey( key );
                
                if ( resolveOptions )
                {
                    try
                    {
                        val = resolver.resolveDependencyPaths( val );
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error resolving environment variable: \'" + val + "\'. Reason: "
                            + e.getMessage(), e );
                    }
                    catch ( PathResolutionException e )
                    {
                        throw new MojoExecutionException( "Error resolving dependency paths for: " + val + ". Reason: " + e.getMessage(), e );
                    }
                }
                
                var.setValue( val );
                
                exec.addEnv( var );
                
                getLog().info( "Setting envar: " + key + "=" + val );
            }                                           
        }

        if ( target != null && target.trim().length() > 0 )
        {
            exec.createArg().setLine( target );
        }

        antCaller.addTask( exec );

        try
        {
            antCaller.executeTasks( project );
        }
        catch ( AntExecutionException e )
        {
            Throwable cause = e.getCause();

            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error executing make target. Reason: " + cause.getMessage(), cause );

                throw new MojoExecutionException( "Failed to execute make target. Reason: " + cause.getMessage(), cause );
            }
            else
            {
                throw new MojoExecutionException( "Failed to execute make target. Reason: " + e.getMessage(), e );
            }
        }

        if ( check != null && check.lastModified() <= lastMod )
        {
            throw new MojoExecutionException( "Check file: " + check + " not changed." );
        }
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

    protected final boolean isChmodUsed()
    {
        return chmodUsed;
    }

    protected final void setChmodUsed( boolean chmodUsed )
    {
        this.chmodUsed = chmodUsed;
    }

    protected final String getCheckFile()
    {
        return checkFile;
    }

    protected final void setCheckFile( String checkFile )
    {
        this.checkFile = checkFile;
    }

    protected final String getCommand()
    {
        return command;
    }

    protected final void setCommand( String command )
    {
        this.command = command;
    }

    protected final List getOptions()
    {
        return options;
    }

    protected final void setOptions( List options )
    {
        this.options = options;
    }

    protected final MavenProject getProject()
    {
        return project;
    }

    protected final void setProject( MavenProject project )
    {
        this.project = project;
    }

    protected final String getTarget()
    {
        return target;
    }

    protected final void setTarget( String target )
    {
        this.target = target;
    }

    protected final void setIgnoreFailures( boolean ignoreFailures )
    {
        this.ignoreFailures = ignoreFailures;
    }

    protected final boolean ignoreFailures()
    {
        return ignoreFailures;
    }

    protected final void setIgnoreErrors( boolean ignoreErrors )
    {
        this.ignoreErrors = ignoreErrors;
    }

    protected final boolean ignoreErrors()
    {
        return ignoreErrors;
    }

    protected String getDestDir()
    {
        return destDir;
    }
    
    protected void setWorkDir( File workDir )
    {
        this.workDir = workDir;
    }

    // cwb >>>
    protected File getWorkDir()
    {
        return workDir;
    }

    //<<<

    /**
     * Locate the command, and cache the resulting File for later reference.
     */
    private void findExecutable()
        throws MojoExecutionException
    {
        // 1. check for the configuration script as-is, in case it's fully qualified.
        executable = new File( command );

        if ( !executable.exists() )
        {
            getLog().warn( "Cannot find command executable file: \'" + command + "\'. Checking work directory." );

            // 2. Check for the configuration script inside the working directory.
            executable = new File( workDir, command );

            // If we can't find it in #1 or #2, then die.
            if ( !executable.exists() )
            {
                executable = null;

                getLog().warn(
                               "Cannot find command executable file: \'" + command + "\' in work directory: \'"
                                   + workDir + "\'. We will assume this command is on the system path." );
            }
        }
    }

    /**
     * Add the Ant task used to make the command executable.
     */
    private void addChmodTask( AntCaller antCaller )
    {
        if( executable != null )
        {
            ExecTask chmod = new ExecTask();

            chmod.setTaskName( "exec:chmod" );

            chmod.setExecutable( "chmod" );

            chmod.createArg().setLine( "+x" );

            chmod.createArg().setLine( executable.getAbsolutePath() );

            antCaller.addTask( chmod );
        }
    }

    public final boolean isSkipped()
    {
        return skipped;
    }

    public final void setSkipped( boolean skipped )
    {
        this.skipped = skipped;
    }

    public final boolean isOptionsResolved()
    {
        return resolveOptions;
    }

    public final void setOptionsResolved( boolean resolveOptions )
    {
        this.resolveOptions = resolveOptions;
    }

    public boolean isSearchForExecutable()
    {
        return searchForExecutable;
    }

    public void setSearchForExecutable( boolean searchForExecutable )
    {
        this.searchForExecutable = searchForExecutable;
    }

}
