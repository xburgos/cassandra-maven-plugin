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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.taskdefs.ExecTask;

import java.io.File;
import java.util.List;

/**
 * Execute a Make-ish compile target to generate binaries from the project
 * source.
 * 
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution test
 */
public class CompileExecMojo
    extends AbstractMakeExecMojo
{
    
    /**
     * Whether we should skip the Make compile step for this project. This is merely a short-circuit mechanism, 
     * since this mojo will be included in a standard lifecycle mapping.
     * 
     * @parameter expression="${skipCompile}" default-value="false" alias="make.compile.skip"
     */
    private boolean skipCompile = false;
    
    /**
     * The actual shell command to run in order to compile the sources; defaults to 'make'.
     * 
     * @parameter
     */
    private String compileCommand = "make";
    
    /**
     * The list of command-line arguments to supply for the command invocation.
     * No expressions are currently allowed here, beyond those resolved inside the pom.xml.
     * 
     * @parameter
     */
    private List compileOptions;
    
    /**
     * The Make target to execute. In many cases, this doesn't have to be supplied, since the
     * default target commonly compiles the code.
     * 
     * @parameter
     */
    private String compileTarget;
    
    /**
     * A check file that will be monitored to verify modification before allowing the compile
     * execution to succeed. If empty, this option is not used.
     * 
     * @parameter
     */
    private String compileCheckFile;
    
    /**
     * Whether to set the Executable bit for the compile command.
     * 
     * @parameter alias="chmod.compile"
     */
    private boolean chmodCompileCommand = false;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter
     */
    private File makeCompileWorkDir;
    
    /**
     * Setup the command-line script, target, and arguments; then, execute the compile.
     * If the check file is specified, it will be checked before and after the compile
     * to verify that this file was modified in the process...and fail if it wasn't.
     */
    public void execute() throws MojoExecutionException
    {
        setCommand( compileCommand );
        setOptions( compileOptions );
        setTarget( compileTarget );
        setCheckFile( compileCheckFile );
        setChmodUsed( chmodCompileCommand );
        setSkipped( skipCompile );
        
        if ( makeCompileWorkDir != null )
        {
            setWorkDir( makeCompileWorkDir );
        }        
        
        try
        {
            super.execute();
        }
        catch ( MojoExecutionException e )
        {
            Throwable cause = e.getCause();
            
            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error compiling source", cause );
                
                throw new MojoExecutionException( "Compilation failed." );
            }
            else
            {
                throw e;
            }
        }
    }
    
}
