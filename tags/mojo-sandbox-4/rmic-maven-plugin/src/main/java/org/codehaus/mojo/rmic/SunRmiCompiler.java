package org.codehaus.mojo.rmic;

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
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SunRmiCompiler.java 4315 2007-06-07 22:54:03Z kismet $
 */
public class SunRmiCompiler
    extends AbstractLogEnabled
    implements RmiCompiler
{
    // ----------------------------------------------------------------------
    // RmiCompiler Implementation
    // ----------------------------------------------------------------------

    public void execute( File[] path, List remoteClasses, File outputClasses )
        throws RmiCompilerException
    {
        // ----------------------------------------------------------------------
        // Construct the RMI Compiler's class path.
        // ----------------------------------------------------------------------

        IsolatedClassLoader classLoader = new IsolatedClassLoader();

        File toolsJar = new File( System.getProperty( "java.home" ), "../lib/tools.jar" );

        if ( toolsJar.isFile() )
        {
            try
            {
                classLoader.addURL( toolsJar.toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new RmiCompilerException(
                    "Error while converting '" + toolsJar.getAbsolutePath() + "' to a URL." );
            }
        }
        else
        {
            getLogger().warn( "tools.jar doesn't exist: " + toolsJar.getAbsolutePath() );
        }

        Class clazz = null;

        // ----------------------------------------------------------------------
        // Try to load the rmic class
        // ----------------------------------------------------------------------

        String[] classes = {"sun.rmi.rmic.Main",};

        for ( int i = 0; i < classes.length; i++ )
        {
            String className = classes[i];

            try
            {
                clazz = classLoader.loadClass( className );

                break;
            }
            catch ( ClassNotFoundException e )
            {
                // continue
            }
        }

        if ( clazz == null )
        {
            getLogger().info( "Looked for these classes:" );

            for ( int i = 0; i < classes.length; i++ )
            {
                String className = classes[i];

                getLogger().info( " * " + className );
            }

            getLogger().info( "Within this classpath:" );

            for ( int it = 0; it < classLoader.getURLs().length; it++ )
            {
                URL url = classLoader.getURLs()[it];

                getLogger().info( " * " + url.toExternalForm() );
            }

            throw new RmiCompilerException( "Could not find any of the classes required for executing rmic." );
        }

        // ----------------------------------------------------------------------
        // Build the argument list
        // ----------------------------------------------------------------------

        List arguments = new ArrayList();

        if ( path.length > 0 )
        {
            String c = "";
	    
            for ( int i = 0; i < path.length; i++ )
            {
                File file = path[i];

                c += file.getAbsolutePath() + File.pathSeparator;
            }

            arguments.add( "-classpath" );

            arguments.add( c );
        }

        arguments.add( "-d" );

        arguments.add( outputClasses.getAbsolutePath() );

        if ( getLogger().isDebugEnabled() )
        {
            arguments.add( "-verbose" );
        }

        for ( Iterator it = remoteClasses.iterator(); it.hasNext(); )
        {
            String remoteClass = (String) it.next();

            arguments.add( remoteClass );
        }

        String[] args = (String[]) arguments.toArray( new String[ arguments.size() ] );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "rmic arguments: " );

            for ( int i = 0; i < args.length; i++ )
            {
                String arg = args[i];

                getLogger().info( arg );
            }
        }

        // ----------------------------------------------------------------------
        // Execute it
        // ----------------------------------------------------------------------

        executeMain( clazz, args );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void executeMain( Class clazz, String[] args )
        throws RmiCompilerException
    {
        Method compile;

        Object main;

        try
        {
            Constructor constructor = clazz.getConstructor( new Class[]{OutputStream.class, String.class} );

            main = constructor.newInstance( new Object[]{System.out, "rmic"} );

            compile = clazz.getMethod( "compile", new Class[]{String[].class} );
        }
        catch ( NoSuchMethodException e )
        {
            throw new RmiCompilerException( "Error while initializing rmic.", e );
        }
        catch ( IllegalAccessException e )
        {
            throw new RmiCompilerException( "Error while initializing rmic.", e );
        }
        catch ( InvocationTargetException e )
        {
            throw new RmiCompilerException( "Error while initializing rmic.", e );
        }
        catch ( InstantiationException e )
        {
            throw new RmiCompilerException( "Error while initializing rmic.", e );
        }

        try
        {
            compile.invoke( main, new Object[]{args} );
        }
        catch ( IllegalAccessException e )
        {
            throw new RmiCompilerException( "Error while executing rmic.", e );
        }
        catch ( InvocationTargetException e )
        {
            throw new RmiCompilerException( "Error while executing rmic.", e );
        }
    }

    public static URL fileToURL( File file )
        throws RmiCompilerException
    {
        try
        {
            return file.toURL();
        }
        catch ( MalformedURLException e )
        {
            throw new RmiCompilerException( "Could not make a URL out of the class path element " + "'" + file.toString() + "'." );
        }
    }
}
