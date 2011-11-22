package org.codehaus.mojo.autotools;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;

/**
 * Execute autoreconf with command line arguments.
 * 
 * @goal autoreconf
 * @phase process-resources
 * @requiresProject false
 */
public class AutoreconfExecMojo
    extends AbstractMojo
{
    
    /**
     * The actual shell command to run.
     */
    private String command = "autoreconf";
    
    private File executable;
    
   /**
    * @parameter
    */
   private List arguments;
   
   /**
    * @parameter
    */
   private Properties environment;
   
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
    * @parameter expression="${workDir}" default-value="${project.build.directory}"
    */
   private File workDir;
   
   /**
    * @parameter
    */
   private boolean ignoreFailures = false;
   
   /**
    * @parameter
    */
   private boolean ignoreErrors = false;
    
    /**
     * Setup the command-line instruction and arguments, then execute it.
     */
    public void execute() throws MojoExecutionException
    {
        workDir.mkdirs();
        
        ExecTask exec = new ExecTask();
        exec.setDir( workDir );
        exec.setFailIfExecutionFails( !ignoreFailures );
        exec.setFailonerror( !ignoreErrors );

        String taskName = command;
        int lastSlash = taskName.lastIndexOf( File.separator );
        if ( lastSlash > -1 )
        {
            taskName = taskName.substring( lastSlash + 1 );
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
        
        if ( arguments != null )
        {
            for ( Iterator it = arguments.iterator(); it.hasNext(); )
            {
                String option = (String) it.next();
                exec.createArg().setLine( option );
            }
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
                var.setValue( val );

                exec.addEnv( var );
                
                getLog().info( "Setting envar: " + key + "=" + val );
            }                                           
        }
        
        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );
        antCaller.setProjectBasedir( workDir );
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
    }
    
}
