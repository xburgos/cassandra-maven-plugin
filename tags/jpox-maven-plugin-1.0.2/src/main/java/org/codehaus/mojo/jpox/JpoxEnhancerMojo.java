package org.codehaus.mojo.jpox;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @goal enhance
 * @phase process-classes
 * @requiresDependencyResolution
 * @description Enhances the application data objects.
 */
public class JpoxEnhancerMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classes;

    /**
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     */
    private List classpathElements;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     */
    private List pluginArtifacts;

    public void execute()
        throws MojoExecutionException
    {
        List files = findMappingFiles();

        if ( files.size() == 0 )
        {
            System.out.println( "No files to enhance." );

            return;
        }

        URL log4jProperties = this.getClass().getResource( "/log4j.configuration" );

        try
        {
            enhance( classes, pluginArtifacts, log4jProperties, files );
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Error while executing the JPox enhancer tool.", e );
        }
    }

    private List findMappingFiles()
        throws MojoExecutionException
    {
        List files;

        try
        {
            files = FileUtils.getFiles( classes, "**/*.jdo", "" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while scanning for '**/*.jdo' in " +
                                              "'" + classes.getAbsolutePath() + "'.", e );
        }

        return files;
    }

    private void enhance( File classes, List pluginArtifacts, URL log4jProperties, List files )
        throws CommandLineException, MojoExecutionException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "java" );

        StringBuffer cpBuffer = new StringBuffer();

        for ( Iterator it = classpathElements.iterator(); it.hasNext(); )
        {
            cpBuffer.append( (String) it.next() );

            if ( it.hasNext() )
            {
                cpBuffer.append( File.pathSeparator );
            }
        }

        for ( Iterator it = pluginArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            try
            {
                cpBuffer.append( File.pathSeparator ).append( artifact.getFile().getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "Error while creating the canonical path for '" + artifact.getFile() + "'.", e );
            }
        }

        cl.createArgument().setValue( "-cp" );

        cl.createArgument().setValue( cpBuffer.toString() );

        cl.createArgument().setValue( "-Dlog4j.configuration=" + log4jProperties );

        cl.createArgument().setValue( "org.jpox.enhancer.JPOXEnhancer" );

        cl.createArgument().setValue( "-v" );

        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            File file = (File) it.next();

            cl.createArgument().setValue( file.getAbsolutePath() );
        }

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        System.err.println( "Executing command line:" );

        System.err.println( cl.toString() );

        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

        System.err.println( "exit code: " + exitCode );

        System.err.println( "--------------------" );
        System.err.println( " Standard output from the JPox enhancer tool:" );
        System.err.println( "--------------------" );
        System.err.println( stdout.getOutput() );
        System.err.println( "--------------------" );

        String stream = stderr.getOutput();

        if ( stream.trim().length() > 0 )
        {
            System.err.println( "--------------------" );
            System.err.println( " Standard error from the JPox enhancer tool:" );
            System.err.println( "--------------------" );
            System.err.println( stderr.getOutput() );
            System.err.println( "--------------------" );
        }

        if ( exitCode != 0 )
        {
            throw new MojoExecutionException( "The JPox enhancer tool exited with a non-null exit code." );
        }
    }
}
