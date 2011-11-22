package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;

/**
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @version $Id: $
 */
public abstract class AbstractAPTMojo extends PreAbstractAPTMojo
{

    /**
     * Integer returned by the Apt compiler to indicate success.
     */
    private static final int APT_COMPILER_SUCCESS = 0;

    /**
     * class in tools.jar that implements APT
     */
    private static final String APT_ENTRY_POINT = "com.sun.tools.apt.Main";

    /**
     * method used to compile.
     */
    private static final String APT_METHOD_NAME = "process";

    /**
     * old method used for apt.
     */
    private static final String APT_METHOD_NAME_OLD = "compile";

    /**
     * store info about modification of system classpath for Apt compiler
     */
    private static boolean isClasspathModified;

    /**
     * Force apt call without staleness checking.
     *
     * @parameter default-value="false"
     */
    private boolean force;

    /**
     * targetPath for generated resources
     *
     * @parameter
     */
    private String resourceTargetPath;

    /**
     * enables resource filtering for generated resources
     *
     * @parameter    default-value="false"
     */
    private boolean resourceFiltering;


    /**
     * A List of targetFiles for SingleSourceTargetMapping
     *
     * @parameter
     */
    private List targetFiles;

    /**
     * The extra source directories containing the sources to be processed.
     *
     * @parameter
     */
    private List aptSourceRoots;

    /**
     * Whether to include debugging information in the compiled class files. The default value is true.
     *
     * @parameter expression="${maven.compiler.debug}" default-value="true"
     * @readonly
     */
    protected boolean debug;

    /**
     * Output source locations where deprecated APIs are used
     *
     * @parameter
     */
    protected boolean showDeprecation;

    /**
     * Output warnings
     *
     * @parameter default-value="true"
     */
    protected boolean showWarnings;

    /**
     * The -encoding argument for the Apt
     *
     * @parameter
     */
    protected String encoding;

    /**
     * run Apt in verbode mode
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    protected boolean verbose;

    /**
     * The -nocompile argument for the Apt
     *
     * @parameter default-value="true"
     */
    protected boolean nocompile;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    protected int staleMillis;

    /**
     *
     * @return list of compilation source roots
     */
    protected abstract List getCompileSourceRoots();

    /**
     *
     * @return output directory
     */
    protected abstract File getOutputDirectory();

    /**
     * Force apt call without staleness checking.
     * @return force
     */
    public boolean isForce()
    {
        return force;
    }

    /**
     * enables resource filtering for generated resources
     * @return resourceFiltering
     */
    protected boolean isResourceFiltering()
    {
        return resourceFiltering;
    }

    protected List getAptSourceRoots()
    {
        return aptSourceRoots;
    }

    /**
     * targetPath for generated resources
     *
     * @return resouceTargetPath
     */
    protected String getResourceTargetPath()
    {
        return resourceTargetPath;
    }

    /**
     * A List of targetFiles for SingleSourceTargetMapping
     *
     * @return a List of TargetFiles
     */
    protected List getTargetFiles()
    {
        return targetFiles;
    }

    protected SourceInclusionScanner getSourceInclusionScanner()
    {
        SourceInclusionScanner scanner = null;

        Set includeSet = new HashSet();
        Set excludeSet = new HashSet();

        if ( getIncludes() == null )
        {
            includeSet.add( "**/*.java" );
        }
        else
        {
            includeSet.addAll( Arrays.asList( getIncludes() ) );
        }
        if ( getExcludes() != null )
        {
            excludeSet.addAll( Arrays.asList( getExcludes() ) );
        }
        if ( isForce() )
        {
            scanner = new SimpleSourceInclusionScanner( includeSet, excludeSet );
        }
        else
        {
            scanner = new StaleSourceScanner( staleMillis, includeSet, excludeSet );
        }

        if ( getTargetFiles() != null && getTargetFiles().size() > 0 )
        {
            for ( Iterator it = getTargetFiles().iterator(); it.hasNext(); )
            {
                String file = (String) it.next();
                scanner.addSourceMapping( new SingleTargetSourceMapping( ".java", file ) );
            }
        }
        else
        {
            scanner.addSourceMapping( new SuffixMapping( ".java", ".class" ) );
        }
        return scanner;
    }

    public void execute() throws MojoExecutionException
    {
        getLog().debug( "Using apt compiler" );
        List cmd = new LinkedList();

        int result = APT_COMPILER_SUCCESS;
        StringWriter writer = new StringWriter();
        // finally invoke APT
        // Use reflection to be able to build on all JDKs:
        try
        {
            // we need to have tools.jar in lasspath
            // due to bug in Apt compiler, system classpath must be modified but
            // in future:
            // TODO try separate ClassLoader (see Plexus compiler api)
            if ( !isClasspathModified )
            {
                URL[] urls = ((URLClassLoader)this.getClass().getClassLoader().getSystemClassLoader()).getURLs();
                for(int i = 0; i < urls.length; i++) {
                    if(urls[i].getPath().endsWith("tools.jar")) {
                        isClasspathModified = true;
                        break;
                    }
                }
                if( !isClasspathModified ) {
                    URL toolsJar = new File( System.getProperty( "java.home" ), "../lib/tools.jar" ).toURL();
                    Method m = URLClassLoader.class.getDeclaredMethod( "addURL", new Class[] { URL.class } );
                    m.setAccessible( true );
                    m.invoke( this.getClass().getClassLoader().getSystemClassLoader(), new Object[] { toolsJar } );
                    isClasspathModified = true;
                }
            }
            // init comand line
            setAptCommandlineSwitches( cmd );
            setAptSpecifics( cmd );
            setStandards( cmd );
            setClassPath( cmd );
            if ( !setSourcepath( cmd ) )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "there are not stale sources." );
                }
                return;
            }
            if ( getLog().isDebugEnabled() )
            {
                for ( Iterator it = cmd.iterator(); it.hasNext(); )
                {
                    getLog().info( it.next() + "\n" );
                }
            }
            Class c = null;
            try {
                c = AbstractAPTMojo.class.forName( APT_ENTRY_POINT ); // getAptCompilerClass();    
            } catch (ClassNotFoundException ex) {
                c = AbstractAPTMojo.class.getClassLoader().getSystemClassLoader().loadClass(APT_ENTRY_POINT);    
            }
            Object compiler = c.newInstance();

            try
            {
                Method compile =
                    //c.getMethod( APT_METHOD_NAME, new Class[] { PrintWriter.class, ( new String[] {} ).getClass() } );
                    c.getMethod( APT_METHOD_NAME, new Class[] {( new String[] {} ).getClass() } );
                result =
                    //( (Integer) compile.invoke( compiler, new Object[] { new PrintWriter( writer ),
                   //     cmd.toArray( new String[cmd.size()] ) } ) ).intValue();
                ( (Integer) compile.invoke( compiler, new Object[] { cmd.toArray( new String[cmd.size()] ) } ) ).intValue();
                
            }
            catch ( NoSuchMethodException e )
            {
                // we need try old method
                Method compile =
                    c.getMethod( APT_METHOD_NAME_OLD,
                        new Class[] { ( new String[] {} ).getClass(), PrintWriter.class } );
                result = ( (Integer) //
                    compile.invoke( compiler, new Object[] { cmd.toArray( new String[cmd.size()] ),
                        new PrintWriter( writer ) } ) ).intValue();
            }

        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( "Error starting apt compiler", ex );
        }
        finally
        {
            if ( result != APT_COMPILER_SUCCESS )
            {
                throw new MojoExecutionException( this, "Compilation error.", writer.getBuffer().toString() );
            }
            if ( getLog().isDebugEnabled() )
            {
                String r = writer.getBuffer().toString();
                if ( 0 != r.length() )
                {
                    getLog().debug( r );
                }
                getLog().debug( "Apt finished." );
            }
        }
    }

    private void setClassPath( List cmd ) throws MojoExecutionException
    {
        cmdAdd( cmd, "-classpath", toStringPath( getClasspathElements() ) );
    }

    private String toStringPath( Collection path )
    {
        StringBuffer pathBuffer = new StringBuffer();
        for ( Iterator it = path.iterator(); it.hasNext(); )
        {
            pathBuffer.append( (String) it.next() );
            if ( it.hasNext() )
            {
                pathBuffer.append( PATH_SEPARATOR );
            }
        }
        return pathBuffer.toString();
    }

    private void setAptCommandlineSwitches( List cmd )
    {
        String[] options = getOptions();

        if ( null == options )
        {
            return;
        }
        for ( int i = 0; i < options.length; i++ )
        {
            cmdAdd( cmd, "-A" + options[i].trim() );
        }
    }

    private void setAptSpecifics( List cmd ) throws MojoExecutionException
    {
        try
        {
            String g = getGeneratedFinalDir();
            File generatedDir = new File( g );
            cmdAdd( cmd, "-s", generatedDir.getCanonicalPath() );
            if ( !generatedDir.exists() )
            {
                generatedDir.mkdirs();
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Generated directory is invalid.", e );
        }
        if ( nocompile )
        {
            cmdAdd( cmd, "-nocompile" );
        }
        if ( null != factory && 0 != factory.length() )
        {
            cmdAdd( cmd, "-factory", factory );
        }
    }

    private void setStandards( List cmd ) throws MojoExecutionException
    {
        if ( debug )
        {
            cmdAdd( cmd, "-g" );
        }
        if ( !showWarnings )
        {
            cmdAdd( cmd, "-nowarn" );
        }
        if ( showDeprecation )
        {
            cmdAdd( cmd, "-depecation" );
        }
        if ( null != encoding )
        {
            cmdAdd( cmd, "-encoding", encoding );
        }
        if ( verbose )
        {
            cmdAdd( cmd, "-verbose" );
        }
        // add output directory
        try
        {
            if ( !getOutputDirectory().exists() )
            {
                getOutputDirectory().mkdirs();
            }
            cmdAdd( cmd, "-d", getOutputDirectory().getCanonicalPath() );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( "Output directory is invalid.", ex );
        }
    }

    private boolean setSourcepath( List cmd ) throws MojoExecutionException
    {
        boolean has = false;
        // sources ....
        List sourceRoots = getCompileSourceRoots();
        has = addSources( sourceRoots, cmd, has );
        List aptRoots = getAptSourceRoots();
        if ( aptRoots != null )
        {
            has = addSources( aptRoots, cmd, has );
        }
        return has;
    }

    private boolean addSources( List sourceRoots, List cmd, boolean has ) throws MojoExecutionException
    {
        Iterator it = sourceRoots.iterator();
        while ( it.hasNext() )
        {
            File srcFile = new File( (String) it.next() );
            if ( srcFile.isDirectory() )
            {
                Collection sources = null;
                try
                {
                    sources = getSourceInclusionScanner().getIncludedSources( srcFile, getOutputDirectory() );
                }
                catch ( Exception ex )
                {
                    throw new MojoExecutionException( "Can't agregate sources.", ex );
                }
                if ( getLog().isDebugEnabled() )
                {
                    getLog().debug( "sources from: " + srcFile.getAbsolutePath() );
                    String s = "";
                    for ( Iterator jt = sources.iterator(); jt.hasNext(); )
                    {
                        s += jt.next() + "\n";
                    }
                    getLog().debug( s );
                }
                Iterator jt = sources.iterator();
                while ( jt.hasNext() )
                {
                    File src = (File) jt.next();
                    cmd.add( src.getAbsolutePath() );
                    has = true;
                }
            }
        }
        return has;
    }

    private void cmdAdd( List cmd, String arg )
    {
        /**
         * OBSOLETE if( true == getLog().isDebugEnabled() ) { getLog().debug( arg ); }
         */
        cmd.add( arg );
    }

    private void cmdAdd( List cmd, String arg1, String arg2 )
    {
        /**
         * OBSOLETE if( true == getLog().isDebugEnabled() ) { getLog().debug( arg1 + " " + arg2 ); }
         */
        cmd.add( arg1 );
        cmd.add( arg2 );
    }
}
