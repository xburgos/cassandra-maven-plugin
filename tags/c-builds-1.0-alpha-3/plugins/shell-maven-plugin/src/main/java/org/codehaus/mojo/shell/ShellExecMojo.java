package org.codehaus.mojo.shell;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.project.extras.ArtifactPathResolver;
import org.codehaus.mojo.tools.project.extras.DependencyPathResolver;
import org.codehaus.mojo.tools.project.extras.PathResolutionException;
import org.codehaus.mojo.tools.project.extras.PrefixPropertyPathResolver;
import org.codehaus.plexus.util.IOUtil;

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
     * @parameter expression="${project.testArtifacts}"
     * @required
     * @readonly
     */
    private List testArtifacts;
    
    /**
     * The temporary working directory where the project is actually built. By default, this is
     * within the '/target' directory.
     * 
     * @parameter expression="${workDir}" default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File workDir;

    /**
     * The Ant messageLevel to use.
     * 
     * @parameter expression="${messageLevel}" default-value="info"
     */
    private String messageLevel;

    /**
     * The Ant messageLevel to use.
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
     * 1. Create a temporary file containing the script. This is the executable.
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
        if ( skipPomProjects && "pom".equals( project.getPackaging() ) )
        {
            getLog().info( "Skipping POM project, per configuration." );
            return;
        }

        if ( BuildAdvisor.isProjectBuildSkipped( project, session.getContainer().getContext() ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );
        
        if ( messageLevel != null )
        {
            antCaller.setMessageLevel( messageLevel );
        }
        
        workDir.mkdirs();

        antCaller.setProjectBasedir( workDir );

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
            addChmodTask( antCaller );
        }

        ExecTask exec = new ExecTask();

        exec.setDir( workDir );

        exec.setTaskName( "exec:" + executable.getName() );

        exec.setExecutable( executable.getAbsolutePath() );
        
        exec.setFailIfExecutionFails( true );
        exec.setFailonerror( true );

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
                getLog().debug( "Error executing make target", cause );

                throw new MojoExecutionException( "Failed to execute embedded shell script. Reason: " + cause.getMessage(), cause );
            }
            else
            {
                throw new MojoExecutionException( "Failed to execute embedded shell script. Reason: " + e.getMessage(), e );
            }
        }
    }

    protected final void setChmodUsed( boolean chmodUsed )
    {
        this.chmod = chmodUsed;
    }

    protected final void setProject( MavenProject project )
    {
        this.project = project;
    }

    /**
     * Generate the script file (to a temp file).
     * @throws IOException 
     * @throws MojoExecutionException 
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
            
            List repositories = new ArrayList();
            repositories.addAll( remoteRepositories );
            repositories.addAll( remotePluginRepositories );
            
            ArtifactPathResolver pathResolver = new PrefixPropertyPathResolver( projectBuilder, repositories, localRepository, artifactFactory, getLog() );
            
            DependencyPathResolver resolver = new DependencyPathResolver( testArtifacts, pathResolver, getLog() );
            output = resolver.resolveDependencyPaths( output );
            
            writer.write( output );
        }
        catch ( PathResolutionException e )
        {
            throw new MojoExecutionException( "Error resolving dependency paths for: " + output + ". Reason: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * Add the Ant task used to make the command executable.
     */
    private void addChmodTask( AntCaller antCaller )
    {
        ExecTask chmod = new ExecTask();

        chmod.setTaskName( "exec:chmod" );

        chmod.setExecutable( "chmod" );

        chmod.createArg().setLine( "+x" );

        chmod.createArg().setLine( executable.getAbsolutePath() );

        antCaller.addTask( chmod );
    }


}
