package org.codehaus.mojo.gwt;

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
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Goal which generate Asyn interface.
 * 
 * @goal generateAsync
 * @phase generate-sources
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class GenerateAsyncMojo
    extends AbstractGwtMojo
{
    /**
     * Pattern for GWT service interface
     * 
     * @parameter default-value="**\/*Service.java"
     */
    private String servicePattern;

    /**
     * Extension for GWT-RPC. May be set to "rpc" if you want to map GWT-RPC calls to "*.rpc" in web.xml, for example
     * when using Spring dispatch servlet to handle RPC requests.
     * 
     * @parameter default-value="" expression="${gwt.rpcExtension}"
     */
    private String rpcExtension;

    /**
     * Stop the build on error
     * 
     * @parameter default-value="false" expression="${maven.gwt.failOnError}"
     */
    private boolean failOnError;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException
    {
        getLog().debug( "GenerateAsyncMojo#execute()" );

        List < String > sourceRoots = getProject().getCompileSourceRoots();
        boolean generated = false;
        for ( String sourceRoot : sourceRoots )
        {
            try
            {
                generated |= scanAndGenerateAsync( new File( sourceRoot ) );
            }
            catch ( Throwable e )
            {
                getLog().error( "Failed to generate Async interface", e );
                if ( failOnError )
                {
                    throw new MojoExecutionException( "Failed to generate Async interface", e );
                }
            }
        }
        if ( generated )
        {
            addCompileSourceRoot( generateDirectory );
        }
    }

    /**
     * @param file the base directory to scan for RPC services
     * @return true if some file have been generated
     * @throws Exception generation failure
     */
    private boolean scanAndGenerateAsync( File file )
        throws Exception
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( file );
        scanner.setIncludes( new String[] { servicePattern } );
        scanner.scan();
        String[] sources = scanner.getIncludedFiles();
        if ( sources.length == 0 )
        {
            return false;
        }
        for ( String source : sources )
        {
            generateAsync( new File( file, source ), source );
        }
        return true;
    }

    /**
     * @param source the RPC service java source file
     * @param name the service name
     * @throws Exception generation failure
     */
    private void generateAsync( File source, String name )
        throws Exception
    {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.getClassLibrary().addClassLoader( getProjectClassLoader() );
        builder.addSource( new FileReader( source ) );
        name = name.substring( 0, name.length() - 5 ) + "Async";

        JavaClass clazz = builder.getClasses()[0];
        JavaClass[] implemented = clazz.getImplementedInterfaces();
        boolean isRemoteService = false;
        for ( JavaClass implement : implemented )
        {
            if ( "com.google.gwt.user.client.rpc.RemoteService".equals( implement.getFullyQualifiedName() ) )
            {
                isRemoteService = true;
                break;
            }
        }
        if ( !isRemoteService )
        {
            return;
        }

        File out = new File( generateDirectory, name + ".java" );
        out.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter( out );

        JavaSource javaSource = builder.getSources()[0];
        writer.println( "package " + javaSource.getPackage() + ";" );
        writer.println();
        String[] imports = javaSource.getImports();
        for ( String string : imports )
        {
            if ( !"com.google.gwt.user.client.rpc.RemoteService".equals( string ) )
            {
                writer.println( "import " + string + ";" );
            }
        }
        writer.println( "import com.google.gwt.core.client.GWT;" );
        writer.println( "import com.google.gwt.user.client.rpc.AsyncCallback;" );
        writer.println( "import com.google.gwt.user.client.rpc.ServiceDefTarget;" );

        writer.println();
        String className = clazz.getName();
        writer.println( "public interface " + className + "Async" );
        writer.println( "{" );

        JavaMethod[] methods = clazz.getMethods();
        for ( JavaMethod method : methods )
        {
            writer.println( "" );
            writer.println( "    /**" );
            writer.println( "     * GWT-RPC service  asynchronous (client-side) interface" );
            writer.println( "     * @see " + clazz.getFullyQualifiedName() );
            writer.println( "     */" );
            writer.print( "    void " + method.getName() + "( " );
            JavaParameter[] params = method.getParameters();
            for ( int j = 0; j < params.length; j++ )
            {
                JavaParameter param = params[j];
                if ( j > 0 )
                {
                    writer.print( ", " );
                }
                writer.print( param.getType().getJavaClass().getName() + " " + param.getName() );
            }
            if ( params.length > 0 )
            {
                writer.print( ", " );
            }
            if ( method.getReturns().isVoid() )
            {
                writer.println( "AsyncCallback<Void> callback );" );
            }
            else
            {
                writer.println( "AsyncCallback<" + method.getReturns().getJavaClass().getName() + "> callback );" );
            }
            writer.println();
        }

        writer.println();

        writer.println( "    /**" );
        writer.println( "     * Utility class to get the RPC Async interface from client-side code" );
        writer.println( "     */" );
        writer.println( "    public static class Util " );
        writer.println( "    { " );
        writer.println( "        private static " + className + "Async instance;" );
        writer.println();
        writer.println( "        public static " + className + "Async getInstance()" );
        writer.println( "        {" );
        writer.println( "            if ( instance == null )" );
        writer.println( "            {" );
        writer.println( "                instance = (" + className + "Async) GWT.create( " + className + ".class );" );
        writer.println( "                ServiceDefTarget target = (ServiceDefTarget) instance;" );
        writer.print( "                target.setServiceEntryPoint( GWT.getModuleBaseURL() + \"" + className + "\"" );
        if ( rpcExtension != null && rpcExtension.length() > 0 )
        {
            writer.print( " + " + rpcExtension );
        }
        writer.println( " );" );
        writer.println( "            }" );
        writer.println( "            return instance;" );
        writer.println( "        }" );
        writer.println( "    }" );

        writer.println( "}" );
        writer.close();
    }
}