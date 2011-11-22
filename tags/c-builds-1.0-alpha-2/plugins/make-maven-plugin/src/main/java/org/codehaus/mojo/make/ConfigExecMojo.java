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
import java.util.ArrayList;
import java.util.List;

/**
 * Execute the configure (or similar) script to prepare the project
 * directory for Make execution (or similar).
 *
 * @goal configure
 * @phase process-sources
 * @requiresDependencyResolution test
 */
public class ConfigExecMojo
    extends AbstractMakeExecMojo
{

    /**
     * Whether we should skip the configuration step of the Make process for this project.
     * This is merely a short-circuit mechanism,
     * since this mojo will be included in a standard lifecycle mapping.
     *
     * @parameter expression="${skipConfig}" default-value="false" alias="make.config.skip"
     */
    private boolean skipConfig = false;

    /**
     * The command path. The default is "./configure" in the work directory.
     *
     * @parameter default-value="./configure"
     */
    private String configCommand;

    /**
     * The list of command-line arguments to the configuration script. An example might be:
     * <p/>
     * "--with-ssl=/path/to/openssl"
     * <p/>
     * NOTE: The expressions "@destDir@" and "@pathOf(groupId:artifactId)@" are available for use here.
     * The first will substitute the value of the destDir parameter (described below) in place of the token,
     * and the second will substitute the systemPath of the dependency referenced by groupId:artifactId for
     * the expression. For example:
     * <p/>
     * "--ssldir=@destDir@"
     * "--with-ssl=@pathOf(org.openssl:openssl)@"
     *
     * @parameter
     */
    private List configOptions;

    /**
     * This is a file location checked before and after the configure execution, if specified.
     * If the file is not touched during configuration, this mojo will fail.
     *
     * @parameter
     */
    private String configCheckFile;

    /**
     * Whether to set the Executable bit for the configure script on the filesystem.
     *
     * @parameter alias="chmod.config"
     */
    private boolean chmodConfigCommand = true;

    /**
     * Whether the workDir and other locations should be searched for the configure executable.
     *
     * @parameter default-value="true"
     */
    private boolean searchForConfigCommand;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     *
     * @parameter
     */
    private File configWorkDir;

    /**
     * The configure prefix, to let the RPM harvester know how to build the dir structure.
     *
     * @parameter
     */
    private String prefix;

    /**
     * Locate the configuration script, make it executable, build up the command-line args, and
     * execute it. This will prepare the project sources for Make to run.
     */
    public void execute()
        throws MojoExecutionException
    {
        getLog().debug( "In ConfigExecMojo.execute:: workDir= " + getWorkDir() );

        if ( "pom".equals( getProject().getPackaging() ) )
        {
            getLog().info( "Skipping POM project." );
            return;
        }

        setCommand( configCommand );
        setTarget( null );
        setSkipped( skipConfig );
        setSearchForExecutable( searchForConfigCommand );

        if ( configWorkDir != null )
        {
            setWorkDir( configWorkDir );
        }

        List options = new ArrayList();

        getLog().debug( "config prefix set to: \'" + prefix + "\'" );

        options.add( "--prefix=" + prefix );

        if ( configOptions != null && !configOptions.isEmpty() )
        {
            options.addAll( configOptions );
        }

        setOptions( options );

        setCheckFile( configCheckFile );
        setChmodUsed( chmodConfigCommand );

        try
        {
            // perform the project configuration
            super.execute();
        }
        catch ( MojoExecutionException e )
        {
            Throwable cause = e.getCause();

            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error configuring build.", cause );

                throw new MojoExecutionException( "Failed to configure project for build." );
            }
            else
            {
                throw new MojoExecutionException( "Failed to configure project for build.", e );
            }
        }
    }

}
