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
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * Execute a Make-ish install target to assemble the compiled binaries
 * into a temporary destination directory (work dir) for harvesting by
 * a packager of some sort (like RPM).
 * 
 * @goal make-install
 * @phase package
 * @requiresDependencyResolution test
 */
public class MakeInstallExecMojo
    extends AbstractMakeExecMojo
{
    /**
     * Whether we should skip the install target of the Make process for this project. 
     * This is merely a short-circuit mechanism, 
     * since this mojo will be included in a standard lifecycle mapping.
     * 
     * @parameter expression="${skipInstall}" default-value="false" alias="make.install.skip"
     */
    private boolean skipInstall = false;
    
    
    /**
     * Shell command used to install the project binaries into the work directory.
     * 
     * @parameter
     */
    private String installCommand = "make";
    
    /**
     * Command-line options used in the install command invocation.
     * No expressions are supported here, other than those resolved wihtin the pom.xml.
     * 
     * @parameter
     */
    private List installOptions;
    
    /**
     * The Make target which should be executed; default is 'install'
     * 
     * @parameter
     */
    private String installTarget = "install";
    
    /**
     * An optional check file to monitor before and after the installation process.
     * If this file is specified and is not changed during installation to the work
     * directory, the mojo fails.
     * 
     * @parameter
     */
    private String installCheckFile;
    
    /**
     * Whether to set the Executable bit for the install command.
     * 
     * @parameter alias="chmod.install"
     */
    private boolean chmodInstallCommand = false;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter
     */
    private File makeInstallWorkDir;
    
    /**
     * Setup the command-line script, target, and arguments; then, execute the install.
     * If the check file is specified, it will be checked before and after installation
     * to verify that this file was modified in the process...and fail if it wasn't.
     */
    public void execute() throws MojoExecutionException
    {
        setCommand( installCommand );
        setOptions( installOptions );
        setTarget( installTarget );
        setCheckFile( installCheckFile );
        setChmodUsed( chmodInstallCommand );
        setSkipped( skipInstall );
        
        if ( makeInstallWorkDir != null )
        {
            setWorkDir( makeInstallWorkDir );
        }        
        
        if ( getOptions() == null || getOptions().isEmpty() )
        {
	    // cwb >>>>>
            getLog().info( "No Options provided: setting DESTDIR= " + getDestDir() );

            setOptions( Collections.singletonList( "DESTDIR=" + getDestDir() ) );

	    /***
	    List opts = new ArrayList();
	    opts.add( "DESTDIR=" + getDestDir() );
	    opts.add( "INSTALL_PREFIX=" + buildDir );
		
	    setOptions( opts );
	    ***/
	    //<<<<<
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
                
                throw new MojoExecutionException( "Make-install failed." );
            }
            else
            {
                throw e;
            }
        }
    }

}
