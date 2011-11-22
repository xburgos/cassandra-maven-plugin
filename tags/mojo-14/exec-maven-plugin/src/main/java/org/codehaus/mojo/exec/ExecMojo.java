package org.codehaus.mojo.exec;

/*
 * Copyright 2005-2006 The Codehaus.
 *
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
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A Plugin for executing external programs.
 *
 * @author Jerome Lacoste <jerome@coffeebreaks.org>
 * @version $Id$
 * @goal exec
 * @requiresDependencyResolution
 * @description Program Execution plugin
 */
public class ExecMojo
    extends AbstractExecMojo
{
    /**
     * @parameter expression="${skip}" default-value="false"
     */
    private boolean skip;

    /**
     * @parameter expression="${exec.executable}"
     * @required
     */
    private String executable;

    /**
     * @parameter expression="${exec.workingdir}
     */
    private File workingDirectory;

    /**
     * Can be of type <code>&lt;argument&gt;</code> or <code>&lt;classpath&gt;</code>
     * Can be overriden using "exec.args" env. variable
     *
     * @parameter
     */
    private List arguments;

    /**
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;
    
    /**
     * if exec.args expression is used when invokign the exec:exec goal,
     * any occurence of %classpath argument is replaced by the actual project dependency classpath.
     */ 
    public static final String CLASSPATH_TOKEN = "%classpath"; 

    /**
     * priority in the execute method will be to use System properties arguments over the pom specification.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "skipping execute as per configuraion" );
            return;
        }

        if ( basedir == null )
        {
            throw new IllegalStateException( "basedir is null. Should not be possible." );
        }

        String argsProp = getSystemProperty( "exec.args" );

        List commandArguments = new ArrayList();

        if ( hasCommandlineArgs() )
        {
            String[] args = parseCommandlineArgs();
            for ( int i = 0; i < args.length; i++ ) 
            {
                if ( CLASSPATH_TOKEN.equals( args[i] ) ) 
                {
                    Collection artifacts = project.getArtifacts();
                    commandArguments.add( computeClasspath( artifacts ) );
                } 
                else 
                {
                    commandArguments.add( args[i] );
                }
            }
        }
        else if ( !isEmpty( argsProp ) )
        {
            getLog().debug( "got arguments from system properties: " + argsProp );

            StringTokenizer strtok = new StringTokenizer( argsProp, " " );
            while ( strtok.hasMoreTokens() )
            {
                commandArguments.add( strtok.nextToken() );
            }
        }
        else
        {
            if ( arguments != null )
            {
                for ( int i = 0; i < arguments.size(); i++ )
                {
                    Object argument = arguments.get( i );
                    String arg;
                    if ( argument == null )
                    {
                        throw new MojoExecutionException(
                            "Misconfigured argument, value is null. Set the argument to an empty value"
                            + " if this is the required behaviour." );
                    }
                    else if ( argument instanceof Classpath )
                    {
                        Classpath classpath = (Classpath) argument;
                        Collection artifacts = project.getArtifacts();
                        if ( classpath.getDependencies() != null )
                        {
                            artifacts = filterArtifacts( artifacts, classpath.getDependencies() );
                        }
                        arg = computeClasspath( artifacts );
                    }
                    else
                    {
                        arg = argument.toString();
                    }
                    commandArguments.add( arg );
                }
            }
        }

        Commandline commandLine = new Commandline();

        commandLine.setExecutable( getExecutablePath() );

        for ( Iterator it = commandArguments.iterator(); it.hasNext(); )
        {
            commandLine.createArgument().setValue( it.next().toString() );
        }

        if ( workingDirectory == null )
        {
            workingDirectory = basedir;
        }

        if ( !workingDirectory.exists() )
        {
            getLog().debug( "Making working directory '" + workingDirectory.getAbsolutePath() + "'." );
            if ( !workingDirectory.mkdirs() )
            {
                throw new MojoExecutionException(
                    "Could not make working directory: '" + workingDirectory.getAbsolutePath() + "'" );
            }
        }

        commandLine.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        // FIXME what about redirecting the output to getLog() ??
        // what about consuming the input just to be on the safe side ?
        // what about allowing parametrization of the class name that acts as consumer?
        StreamConsumer consumer = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLog().info( line );
            }
        };

        try
        {
            int result = executeCommandLine( commandLine, consumer, consumer );

            if ( result != 0 )
            {
                throw new MojoExecutionException( "Result of " + commandLine + " execution is: '" + result + "'." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Command execution failed.", e );
        }

        registerSourceRoots();
    }

    private String computeClasspath( Collection artifacts )
    {
        StringBuffer theClasspath = new StringBuffer();

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            if ( theClasspath.length() > 0 )
            {
                theClasspath.append( File.pathSeparator );
            }
            Artifact artifact = (Artifact) it.next();
            getLog().debug( "dealing with " + artifact );
            theClasspath.append( artifact.getFile().getAbsolutePath() );
        }
        // FIXME check project current phase?
        // we should probably add the output and testoutput dirs based on the Project's phase
        if ( true )
        {
            if ( theClasspath.length() > 0 )
            {
                theClasspath.append( File.pathSeparator );
            }
            theClasspath.append( project.getBuild().getOutputDirectory() );
        }
        if ( false )
        {
            if ( theClasspath.length() > 0 )
            {
                theClasspath.append( File.pathSeparator );
            }
            theClasspath.append( project.getBuild().getTestOutputDirectory() );
        }

        return theClasspath.toString();
    }

    private Collection filterArtifacts( Collection artifacts, Collection dependencies )
    {
        AndArtifactFilter filter = new AndArtifactFilter();

        filter.add( new IncludesArtifactFilter( new ArrayList( dependencies ) ) ); // gosh

        StringBuffer theClasspath = new StringBuffer();

        List filteredArtifacts = new ArrayList();
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();
            if ( filter.include( artifact ) )
            {
                getLog().debug( "filtering in " + artifact );
                filteredArtifacts.add( artifact );
            }
        }
        return filteredArtifacts;
    }

    String getExecutablePath()
    {
        File execFile = new File( executable );
        // if the file doesn't exist, the exec is probably in the PATH...
        // we should probably also test for isFile and canExecute, but the second one is only
        // available in SDK 6.
        if ( execFile.exists() )
        {
            return execFile.getAbsolutePath();
        }
        else
        {
            getLog().debug( "executable " + executable + " not found in place, assuming it is in the PATH." );
            return executable;
        }
    }


    private static boolean isEmpty( String string )
    {
        return string == null || string.length() == 0;
    }

    //
    // methods used for tests purposes - allow mocking and simulate automatic setters
    //

    protected int executeCommandLine( Commandline commandLine, StreamConsumer stream1, StreamConsumer stream2 )
        throws CommandLineException
    {
        return CommandLineUtils.executeCommandLine( commandLine, stream1, stream2 );
    }

    void setExecutable( String executable )
    {
        this.executable = executable;
    }

    String getExecutable()
    {
        return executable;
    }

    void setWorkingDirectory( String workingDir )
    {
        setWorkingDirectory( new File( workingDir ) );
    }

    void setWorkingDirectory( File workingDir )
    {
        this.workingDirectory = workingDir;
    }

    void setArguments( List arguments )
    {
        this.arguments = arguments;
    }

    void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    void setProject( MavenProject project )
    {
        this.project = project;
    }

    protected String getSystemProperty( String key )
    {
        return System.getProperty( key );
    }
}
