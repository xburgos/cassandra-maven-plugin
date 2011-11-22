package org.codehaus.mojo.shell;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;

/**
 * Mojo used to embed a shell script inside the POM.
 * This class handles construction of the command-line and monitoring of the check file,
 * if specified. It will also handle chmod'ing the given shell command, if required.
 * 
 * @goal shell
 * @requiresDependencyResolution test
 */
public class ShellExecMojo
    extends AbstractMojo
{

    /**
     * Whether to keep the script file generated by this invocation.
     * 
     * @parameter default-value="false" expression="${shell.keepScriptFile}"
     */
    private boolean keepScriptFile;

    /**
     * MavenProject instance used to resolve property expressions from within Ant.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The dependency artifacts of this project, for resolving @pathOf(..)@ expressions.
     * These are of type org.apache.maven.artifact.Artifact, and are keyed by groupId:artifactId, 
     * using org.apache.maven.artifact.ArtifactUtils.versionlessKey(..) for consistent formatting.
     * 
     * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
     */
    private Set < Artifact > testArtifacts;

    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter expression="${workDir}" default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File workDir;

    /**
     * If chmod is used
     * 
     * @parameter expression="${chmod}" default-value="false"
     */
    private boolean chmod;

    /**
     * The contents of the shell script.
     * 
     * @parameter expression="${script}"
     * @required
     */
    private String script;

    /**
     * Result of search for the configuration script, so we don't have to re-search.
     */
    private File executable;

    /**
     * Whether to execute String.trim() on the script parameter before using it
     * to generate the script file. This can be important in environments like Bash,
     * where '#!/bin/sh' must be on the first line.
     * 
     * @parameter expression="${trimScript}" default-value="true"
     */
    private boolean trimScript;

    /**
     * The script-file extension.
     * 
     * @parameter expression="${extension}" default-value=".sh"
     */
    private String extension;

    /**
     * @parameter expression="${debug}" default-value="false"
     */
    private boolean debug;

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
     * @parameter default-value="false"
     */
    private boolean skipPomProjects;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * 1. Create a temporary file containing the script. This is the executable.
     * 
     * 3. If chmodUsed == true, then we'll set the executable bit on the executable file using chmod.
     * 
     * 4. Construct the Ant Exec task using the supplied command, any command-line options, an optional
     *    Make target, working directory, and overrides for failure conditions. 
     *    
     * 5. Next, execute the resulting Exec task.
     * 
     * @throws MojoExecutionException thrown when depedency resolution or execution failures
     */
    public void execute() throws MojoExecutionException
    {
        if ( skipPomProjects && "pom".equals( project.getPackaging() ) )
        {
            getLog().info( "Skipping POM project, per configuration." );
            return;
        }

        workDir.mkdirs();

        try
        {
            createExecutable();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create shell script. Reason: " + e.getMessage(), e );
        }

        if ( chmod )
        {
            // add the task to make the configure script executable
            doChmod();
        }

        Commandline exec = new Commandline();
        exec.setShell( new BourneShell( true ) );

        exec.setWorkingDirectory( workDir.getAbsolutePath() );

        exec.setExecutable( executable.getAbsolutePath() );

        try
        {
            StreamConsumer consumer = cliManager.newInfoStreamConsumer();
            
            int result = cliManager.execute( exec, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( 
                    "Script failed to execute (exit value != 0). Please see output above for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( 
                "Failed to execute embedded shell script. Reason: " + e.getMessage(), e );
        }
    }

    /**
     * Method used to set mojo behavior to set the executable prior to invocagtion
     * @param chmodUsed if set, executable will be set to exececutable prior to invocation
     */
    protected final void setChmodUsed( boolean chmodUsed )
    {
        this.chmod = chmodUsed;
    }

    /**
     * Generate the script file (to a temp file).
     * @throws IOException thrown on File access errors
     * @throws MojoExecutionException thrown if dependency resolution problems
     */
    private void createExecutable()
        throws IOException, MojoExecutionException
    {
        executable = File.createTempFile( "maven-shell-plugin-", extension );

        if ( debug || getLog().isDebugEnabled() || keepScriptFile )
        {
            getLog().info( "NOT deleting generated script file: " + executable.getAbsolutePath() );
        }
        else
        {
            executable.deleteOnExit();
        }

        Writer writer = null;

        String output = script;

        if ( trimScript )
        {
            output = script.trim();
        }

        try
        {
            writer = new FileWriter( executable );
            
            List < ArtifactRepository > repositories = new ArrayList < ArtifactRepository > ();
            repositories.addAll( remoteRepositories );
            repositories.addAll( remotePluginRepositories );
            
            ArtifactPathResolver pathResolver = new PrefixPropertyPathResolver( 
                projectBuilder, repositories, localRepository, artifactFactory, getLog() );
            
            DependencyPathResolver resolver = new DependencyPathResolver( testArtifacts, pathResolver, getLog() );
            output = resolver.resolveDependencyPaths( output );
            
            writer.write( output );
        }
        catch ( PathResolutionException e )
        {
            throw new MojoExecutionException( 
                "Error resolving dependency paths for: " + output + ". Reason: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * Add the Ant task used to make the command executable.
     * @throws MojoExecutionException thrown when chmod fails
     */
    private void doChmod() throws MojoExecutionException
    {
        Commandline chmodcmd = new Commandline();
        chmodcmd.setExecutable( "chmod" );

        chmodcmd.createArg().setLine( "+x" );

        chmodcmd.createArg().setLine( executable.getAbsolutePath() );

        StreamConsumer consumer = cliManager.newDebugStreamConsumer();

        try
        {
            int result = cliManager.execute( chmodcmd, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( 
                    "Failed to chmod script file (exit value != 0). Please see debug output for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to chmod +x: " + executable + ". Reason: " + e.getMessage(), e );
        }
    }


}
