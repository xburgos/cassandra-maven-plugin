package org.codehaus.mojo.idlj;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * This class implement the <code>CompilerTranslator</code> for the
 * Sun idlj IDL compiler
 * 
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id$
 */
public class IdljTranslator implements CompilerTranslator
{

    /**
     * enable/disable debug messages 
     */
    private final boolean debug;

    /**
     * the <code>Log</code> that will used for the messages
     */
    private Log log;

    /**
     * Create an <code>IdljTranslator</code> object with the specified <code>Log</code> and
     * enable or disable the debugging
     * 
     * @param d <code>true</code> to enable debug logging
     * @param l <code>Log</code> to use for logging the activity
     */    
    public IdljTranslator( boolean d, Log l )
    {
        debug = d;
        log = l;
    }

    /**
     * This method it's used to invoke the compiler
     * 
     * @param l the<code>Log</code> to use for log messages
     * @param sourceDirectory the path to the sources
     * @param includeDirs the <code>List</code> of directories where to find the includes
     * @param targetDirectory the path to the destination of the compilation
     * @param idlFile the path to the file to compile
     * @param source //TODO ???
     * @throws MojoExecutionException the exeception is thrown whenever the compilation fails or crashes
     * 
     * @see CompilerTranslator#invokeCompiler(Log, String, List, String, String, Source)
     */    
    public void invokeCompiler( Log l, String sourceDirectory, List includeDirs, String targetDirectory,
            String idlFile, Source source ) throws MojoExecutionException
    {
        log = l;
        List args = new ArrayList();
        args.add( "-i" );
        args.add( sourceDirectory );

        // add idl files from other directories as well
        if ( includeDirs != null )
        {
            Iterator iter = includeDirs.iterator();
            while ( iter.hasNext() )
            {
                String includeDir = (String) iter.next();
                args.add( "-i" );
                args.add( includeDir );
            }
        }

        args.add( "-td" );
        args.add( targetDirectory );

        if ( source.getPackagePrefix() != null )
        {
            throw new MojoExecutionException( "idlj compiler does not support packagePrefix" );
        }

        if ( source.getPackagePrefixes() != null )
        {
            for ( Iterator prefixes = source.getPackagePrefixes().iterator(); prefixes.hasNext(); )
            {
                PackagePrefix prefix = (PackagePrefix) prefixes.next();
                args.add( "-pkgPrefix" );
                args.add( prefix.getType() );
                args.add( prefix.getPrefix() );
            }
        }

        if ( source.getDefines() != null )
        {
            for ( Iterator defs = source.getDefines().iterator(); defs.hasNext(); )
            {
                Define define = (Define) defs.next();
                if ( define.getValue() != null )
                {
                    throw new MojoExecutionException( "idlj compiler unable to define symbol values" );
                }
                args.add( "-d" );
                args.add( define.getSymbol() );
            }
        }

        if ( source.emitStubs() != null && source.emitStubs().booleanValue() )
        {
            if ( source.emitSkeletons().booleanValue() )
            {
                args.add( "-fall" );
            }
            else
            {
                args.add( "-fclient" );
            }
        }
        else
        {
            if ( source.emitSkeletons() != null && source.emitSkeletons().booleanValue() )
            {
                args.add( "-fserver" );
            }
            else
            {
                args.add( "-fserverTIE" );
            }
        }

        if ( source.compatible() != null && source.compatible().booleanValue() )
        {
            String version = System.getProperty( "java.specification.version" );
            log.debug( "JDK Version:" + version );
            // TODO A compiled REGEX should be used instead of the matches()
            // method
            if ( version.matches( "^[0-1]\\.[0-3]" ) )
            {
                log.debug( "OPTION IGNORED: compatible" );
            }
            else
            {
                args.add( "-oldImplBase" );
            }
        }

        if ( source.getAdditionalArguments() != null )
        {
            for ( Iterator it = source.getAdditionalArguments().iterator(); it.hasNext(); )
            {
                args.add( it.next() );
            }
        }

        args.add( idlFile );

        Class compilerClass = getCompilerClass();
        invokeCompiler( compilerClass, args );
    }

    /**
     * @return the <code>Class</code> that implements the idlj compiler
     * @throws MojoExecutionException if the search for the class fails
     */
    private Class getCompilerClass() throws MojoExecutionException
    {
        ClassLoader cl = this.getClass().getClassLoader();
        Class idljCompiler;
        try
        {
            idljCompiler = Class.forName( getIDLCompilerClass() );
        }
        catch ( ClassNotFoundException e )
        {
            try
            {
                File javaHome = new File( System.getProperty( "java.home" ) );
                File toolsJar = new File( javaHome, "../lib/tools.jar" );
                URL toolsJarUrl = toolsJar.toURL();
                URLClassLoader urlLoader = new URLClassLoader( new URL[] { toolsJarUrl }, cl );

                // Unfortunately the idlj compiler reads messages using the
                // system class path.
                // Therefore this really nasty hack is required.
                System.setProperty( "java.class.path", System.getProperty( "java.class.path" )
                        + System.getProperty( "path.separator" ) + toolsJar.getAbsolutePath() );
                if ( System.getProperty( "java.vm.name" ).indexOf( "HotSpot" ) != -1 )
                {
                    urlLoader.loadClass( "com.sun.tools.corba.se.idl.som.cff.FileLocator" );
                }
                idljCompiler = urlLoader.loadClass( getIDLCompilerClass() );
            }
            catch ( Exception notUsed )
            {
                throw new MojoExecutionException( " IDL compiler not available", e );
            }
        }
        return idljCompiler;
    }

    /**
     * @return the class name of the clas that implements the compiler
     */
    private String getIDLCompilerClass()
    {
        String vendor = System.getProperty( "java.vm.name" );

        if ( vendor.indexOf( "IBM" ) != -1 )
        {
            return "com.ibm.idl.toJavaPortable.Compile";
        }
        return "com.sun.tools.corba.se.idl.toJavaPortable.Compile";
    }

    /**
     * 
     * Invoke the specified compiler with a set of arguments
     * 
     * @param compilerClass the <code>Class</code> that implemtns the compiler
     * @param args a <code>List</code> that contains the arguments to use for the compiler
     * @throws MojoExecutionException if the compilation fail or the compiler crashes
     */
    private void invokeCompiler( Class compilerClass, List args ) throws MojoExecutionException
    {
        Method compilerMainMethod;
        String arguments[];

        if ( debug )
        {
            args.add( 0, "-verbose" );
            arguments = (String[]) args.toArray( new String[args.size()] );
            String command = compilerClass.getName();
            for ( int i = 0; i < arguments.length; i++ )
            {
                command += " " + arguments[i];
            }
            log.info( command );
        }
        else
        {
            arguments = (String[]) args.toArray( new String[args.size()] );
        }

        try
        {
            compilerMainMethod = compilerClass.getMethod( "main", new Class[] { String[].class } );
        }
        catch ( NoSuchMethodException e1 )
        {
            throw new MojoExecutionException( "Error: Compiler had no main method" );
        }

        try
        {
            compilerMainMethod.invoke( compilerClass, new Object[] { arguments } );
        }
        catch ( InvocationTargetException e )
        {
            throw new MojoExecutionException( "IDL compilation failed", e.getTargetException() );
        }
        catch ( Throwable e )
        {
            throw new MojoExecutionException( "IDL compilation failed", e );
        }
    }

}
