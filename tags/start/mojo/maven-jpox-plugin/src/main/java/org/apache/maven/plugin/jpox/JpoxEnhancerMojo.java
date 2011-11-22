package org.apache.maven.plugin.jpox;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractPlugin;
import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * @goal enhance
 *
 * @requiresDependencyResolution
 *
 * @description Enhances the application data objects.
 *
 * @parameter
 * name="basedir"
 * type="java.lang.String"
 * required="true"
 * validator=""
 * expression="#basedir"
 * description=""
 *
 * @parameter
 *  name="classes"
 *  type="String"
 *  required="true"
 *  validator=""
 *  expression="#project.build.output"
 *  description=""
 *
 * @parameter
 *  name="output"
 *  type="String"
 *  required="true"
 *  validator=""
 *  expression="#project.build.output"
 *  description=""
 *
 * @parameter
 *  name="classpathElements"
 *  type="String"
 *  required="true"
 *  validator=""
 *  expression="#project.classpathElements"
 *  description=""
 */
public class JpoxEnhancerMojo
    extends AbstractPlugin
{
    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {
        String basedir = (String) request.getParameter( "basedir" );

        String classes = (String) request.getParameter( "classes" );

        String output = (String) request.getParameter( "output" );

        String[] classpathElements = (String[]) request.getParameter( "classpathElements" );

        List files = FileUtils.getFiles( new File( basedir ), "**/*.jdo", "" );

        if ( files.size() == 0 )
        {
            System.out.println( "No files to enhance." );

            return;
        }

        File log4jProperties = File.createTempFile( "jpox-enhancer", "tmp" );

        InputStream configuration = this.getClass().getResourceAsStream( "/log4j.configuration" );

        IOUtil.copy( configuration, new FileOutputStream( log4jProperties ) );

        System.out.println( "Enhancing files " + files.size() + "." );
        System.out.println( " Classes directory: " + classes );
        System.out.println( " Output directory: " + output );

        Commandline cl = new Commandline();

        cl.setExecutable( "java" );

        String cp = classes;

        for ( int i = 0; i < classpathElements.length; i++ )
        {
            String classpathElement = classpathElements[ i ];

            cp += ":" + classpathElement;
        }

        cl.createArgument().setValue( "-cp" );

        cl.createArgument().setValue( cp );

        cl.createArgument().setValue( "-Dlog4j.configuration=" + log4jProperties.toURL() );

        cl.createArgument().setValue( "org.jpox.enhancer.JPOXEnhancer" );

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( output );

        cl.createArgument().setValue( "-verify" );

        cl.createArgument().setValue( "-check" );

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
        System.err.println( " Standard output" );
        System.err.println( "--------------------" );
        System.err.println( stdout.getOutput() );
        System.err.println( "--------------------" );

        System.err.println( "--------------------" );
        System.err.println( " Standard error" );
        System.err.println( "--------------------" );
        System.err.println( stderr.getOutput() );
        System.err.println( "--------------------" );

        log4jProperties.deleteOnExit();

        log4jProperties.delete();
    }
}
