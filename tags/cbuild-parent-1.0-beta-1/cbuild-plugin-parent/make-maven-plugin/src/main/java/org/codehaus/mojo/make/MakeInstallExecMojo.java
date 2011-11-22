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
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Execute a Make-ish install target to assemble the compiled binaries
 * into a temporary destination directory (work dir) for harvesting by
 * a packager of some sort (like RPM).
 * 
 * @goal make-install
 * @phase test
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
     * 
     * @parameter
     */
    private List < String > installOptions;
    
    /**
     * Shell environment variables used in the install command invocation.
     * 
     * @parameter
     */
    private Properties installEnvironment;
    
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
     * 
     * @throws MojoExecutionException thrown if install encounters a problem
     */
    public void execute() throws MojoExecutionException
    {
        setCommand( installCommand );
        setTarget( installTarget );
        setCheckFile( installCheckFile );
        setChmodUsed( chmodInstallCommand );
        setSkipped( skipInstall );

        if ( makeInstallWorkDir != null )
        {
            setWorkDir( makeInstallWorkDir );
        }

        String destDir = null;

        if ( installEnvironment != null )
        {
            destDir = (String) installEnvironment.get( "DESTDIR" );
        }

        if ( ( destDir == null ) && ( getDestDir() != null ) )
        {
            if ( installEnvironment == null )
            {
                installEnvironment = new Properties();
            }

            getLog().debug( "Setting DESTDIR envar to: " + getDestDir() );
            installEnvironment.put( "DESTDIR", getDestDir() );
        }

        setEnvironment( installEnvironment );

        // http://jira.codehaus.org/browse/CBUILDS-24
        if ( getDestDir() != null )
        {
            if ( installOptions == null )
            {
                installOptions = new ArrayList < String > ();
            }
            installOptions.add( "DESTDIR=" + getDestDir() );
        }
        setOptions( installOptions );

        super.execute();
    }

}
