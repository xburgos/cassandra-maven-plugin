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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Execute the configure (or similar) script to prepare the project
 * directory for Make execution (or similar).
 *
 * @goal configure
 * @phase process-resources
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
     * @parameter default-value="false"
     */
    private boolean skipConfigure = false;

    /**
     * The command path. The default is "./configure" in the work directory.
     *
     * @parameter default-value="./configure"
     */
    private String configureCommand;

    /**
     * The prefix option. The default is "--prefix".
     *
     * @parameter default-value="--prefix"
     */
    private String prefixOption;

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
    private List < String > configureOptions;
    
    /**
     * These are environment variables you want added to the configure command's execution shell.
     * They will be resolved in the same manner as configureOptions, where "@pathOf(groupId:artifactId)@"
     * is resolved to the prefix of that dependency.
     * 
     * @parameter
     */
    private Properties configureEnvironment;

    /**
     * This is a file location checked before and after the configure execution, if specified.
     * If the file is not touched during configuration, this mojo will fail.
     *
     * @parameter
     */
    private String configureCheckFile;

    /**
     * Whether to set the Executable bit for the configure script on the filesystem.
     *
     * @parameter default-value="true"
     */
    private boolean chmodConfigureCommand;
    
    /**
     * Whether the configure command should be referenced as an absolute path or not. Default is false.
     * 
     * @parameter default-value="false"
     */
    private boolean absoluteConfigureCommand;

    /**
     * Whether the workDir and other locations should be searched for the configure executable.
     *
     * @parameter default-value="true"
     */
    private boolean searchForConfigureCommand;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     *
     * @parameter
     */
    private File configureWorkDir;

    /**
     * The configure prefix, to let the RPM harvester know how to build the dir structure.
     *
     * @parameter
     * @required
     */
    private String prefix;

    /**
     * Locate the configuration script, make it executable, build up the command-line args, and
     * execute it. This will prepare the project sources for Make to run.
     * 
     * @throws MojoExecutionException thrown when there is an error encountered by configure
     */
    public void execute() throws MojoExecutionException
    {
        getLog().debug( "In ConfigExecMojo.execute:: workDir= " + getWorkDir() );

        if ( "pom".equals( getProject().getPackaging() ) )
        {
            getLog().info( "Skipping POM project." );
            return;
        }

        setCommand( configureCommand );
        setTarget( null );
        setSkipped( skipConfigure );
        setSearchForExecutable( searchForConfigureCommand );

        if ( configureWorkDir != null )
        {
            setWorkDir( configureWorkDir );
        }

        List < String > options = new ArrayList < String > ();

        getLog().debug( "config prefix set to: \'" + prefix + "\'" );

        if ( prefixOption != null )
        {
          options.add( prefixOption + "=" + prefix );
        }

        if ( configureOptions != null && !configureOptions.isEmpty() )
        {
            options.addAll( configureOptions );
        }
        
        setOptions( options );

        if ( configureEnvironment != null && !configureEnvironment.isEmpty() )
        {
            setEnvironment( configureEnvironment );
        }

        setCheckFile( configureCheckFile );
        setChmodUsed( chmodConfigureCommand );
        setAbsoluteCommandPathUsed( absoluteConfigureCommand );

        // perform the project configuration
        super.execute();
    }

}
