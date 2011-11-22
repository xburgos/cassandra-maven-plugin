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
 * Execute a Make (or similar) target to test the compiled binary.
 * 
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class TestExecMojo
    extends AbstractMakeExecMojo
{
    
    /**
     * The command we should invoke to run the tests.
     * 
     * @parameter
     */
    private String testCommand = "make";
    
    /**
     * Command-line options for use in the test command invocation. 
     * No expressions are currently supported here, other than those resolved inside the pom.xml.
     * 
     * @parameter
     */
    private List testOptions;
    
    /**
     * The Make test target to execute.
     * 
     * @parameter
     */
    private String testTarget = "check";
    
    /**
     * A file which should be modified during the tests...if this is set and not updated,
     * the mojo will fail.
     * 
     * @parameter
     */
    private String testCheckFile;
    
    /**
     * Whether we should prevent the test mojo from failing if there are test failures.
     * 
     * @parameter
     */
    private boolean ignoreTestFailures = false;
    
    /**
     * Whether we should skip testing this project. This is merely a short-circuit mechanism, 
     * since this mojo will be included in a standard lifecycle mapping.
     * 
     * @parameter expression="${skipTest}" default-value="false" alias="maven.test.skip"
     */
    private boolean skipTest = false;
    
    /**
     * Whether to set the Executable bit for the test command.
     * 
     * @parameter alias="chmod.test"
     */
    private boolean chmodTestCommand = false;
    
    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter
     */
    private File makeTestWorkDir;
    
    /**
     * Execute the tests for this binary, using the command, target, and options supplied.
     * If ignoreTestFailures == true, then succeed even if the make target fails. If
     * a test check file is set, then make sure that check file has been modified by the
     * testing process before allowing the execution to succeed.
     */
    public void execute() throws MojoExecutionException
    {
        setCommand( testCommand );
        setOptions( testOptions );
        setTarget( testTarget );
        setCheckFile( testCheckFile );
        setChmodUsed( chmodTestCommand );
        setSkipped( skipTest );
        
        if ( makeTestWorkDir != null )
        {
            setWorkDir( makeTestWorkDir );
        }        
        
        try
        {
            setIgnoreFailures( ignoreTestFailures );
            setIgnoreErrors( ignoreTestFailures );
            
            super.execute();
        }
        catch ( MojoExecutionException e )
        {
            Throwable cause = e.getCause();
            
            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error running tests", cause );
                
                throw new MojoExecutionException( "There were some test failures." );
            }
            else
            {
                throw e;
            }
        }
    }
    
}
