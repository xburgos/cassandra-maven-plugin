package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.StringUtils;

/**
 * Base mojo for executing apt.
 * 
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 */
public abstract class AbstractAptMojo extends AbstractMojo
{
    // read-only parameters ---------------------------------------------------

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The plugin's artifacts.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginArtifacts;

    /**
     * The directory to run apt from when forked.
     * 
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File workingDirectory;

    // configurable parameters ------------------------------------------------

    /**
     * Whether to run apt in a separate process.
     * 
     * @parameter default-value="false"
     */
    private boolean fork;

    /**
     * The apt executable to use when forked.
     * 
     * @parameter expression="${maven.apt.executable}" default-value="apt"
     */
    private String executable;

    /**
     * The initial size of the memory allocation pool when forked, for example <code>64m</code>.
     * 
     * @parameter
     */
    private String meminitial;

    /**
     * The maximum size of the memory allocation pool when forked, for example <code>128m</code>.
     * 
     * @parameter
     */
    private String maxmem;

    /**
     * Whether to show apt warnings. This is opposite to the <code>-nowarn</code> argument for apt.
     * 
     * @parameter expression="${maven.apt.showWarnings}" default-value="false"
     */
    private boolean showWarnings;

    /**
     * The source file encoding name, such as <code>EUC-JP</code> and <code>UTF-8</code>. If encoding is not
     * specified, the encoding <code>ISO-8859-1</code> is used rather than the platform default for reproducibility
     * reasons. This is equivalent to the <code>-encoding</code> argument for apt.
     * 
     * @parameter expression="${maven.apt.encoding}" default-value="ISO-8859-1"
     */
    private String encoding;

    /**
     * Whether to output information about each class loaded and each source file processed. This is equivalent to the
     * <code>-verbose</code> argument for apt.
     * 
     * @parameter expression="${maven.apt.verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Options to pass to annotation processors. These are equivalent to multiple <code>-A</code> arguments for apt.
     * 
     * @parameter
     */
    private String[] options;

    /**
     * Name of <code>AnnotationProcessorFactory</code> to use; bypasses default discovery process. This is equivalent
     * to the <code>-factory</code> argument for apt.
     * 
     * @parameter expression="${maven.apt.factory}"
     */
    private String factory;

    /**
     * The source directories containing any additional sources to be processed.
     * 
     * @parameter
     */
    private List additionalSourceRoots;

    /**
     * The path for processor-generated resources.
     * 
     * @parameter
     */
    private String resourceTargetPath;

    /**
     * Whether resource filtering is enabled for processor-generated resources.
     * 
     * @parameter default-value="false"
     */
    private boolean resourceFiltering;

    /**
     * Force apt processing without staleness checking. When <code>false</code>, use <code>outputFiles</code> or
     * <code>outputFileEndings</code> to control computing staleness.
     * 
     * @parameter default-value="false"
     */
    private boolean force;
    
    /**
     * The filenames of processor-generated files to examine when computing staleness. For example,
     * <code>generated.xml</code> would specify that the processor creates the aforementioned single file from all
     * <code>.java</code> source files. When this parameter is not specified, <code>outputFileEndings</code> is used
     * instead.
     * 
     * @parameter
     */
    private Set outputFiles;

    /**
     * The filename endings of processor-generated files to examine when computing staleness. For example,
     * <code>.txt</code> would specify that the processor creates a corresponding <code>.txt</code> file for every
     * <code>.java</code> source file. Default value is <code>.java</code>. Note that this parameter has no effect if
     * <code>outputFiles</code> is specified.
     * 
     * @parameter
     */
    private Set outputFileEndings;

    /**
     * Sets the granularity in milliseconds of the last modification date for testing whether a source needs
     * processing.
     * 
     * @parameter expression="${maven.apt.staleMillis}" default-value="0"
     */
    private int staleMillis;

    /**
     * Whether to bypass running apt.
     * 
     * @parameter expression="${maven.apt.skip}" default-value="false"
     */
    private boolean skip;
    
    // fields -----------------------------------------------------------------
    
    private Set includes;
    
    private Set excludes;

    // Mojo methods -----------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public final void execute() throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping apt" );
        }
        else
        {
            executeImpl();
        }
    }

    // protected methods ------------------------------------------------------

    protected void executeImpl() throws MojoExecutionException
    {
        // apply defaults
        
        includes = defaultSet( getIncludes(), Collections.singleton( "**/*.java" ) );
        excludes = defaultSet( getExcludes() );
        
        // invoke apt
        
        List staleSourceFiles = getSourceFiles( getStaleScanner(), "stale sources" );

        if ( staleSourceFiles.isEmpty() )
        {
            getLog().info( "Nothing to process - all processor-generated files are up to date" );
        }
        else
        {
            executeApt();
        }
        
        // add source root

        getCompileSourceRoots().add( getOutputDirectory().getPath() );
        
        // add resource

        Resource resource = new Resource();

        resource.setDirectory( getOutputDirectory().getPath() );
        resource.addExclude( "**/*.java" );
        resource.setFiltering( resourceFiltering );

        if ( resourceTargetPath != null )
        {
            resource.setTargetPath( resourceTargetPath );
        }

        getResources().add( resource );
    }

    /**
     * Gets the Maven project.
     * 
     * @return the project
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Gets the options to pass to annotation processors.
     * 
     * @return an array of options to pass to annotation processors
     */
    protected String[] getOptions()
    {
        return options;
    }

    /**
     * The source directories containing the sources to be processed.
     * 
     * @return list of compilation source roots
     */
    protected abstract List getCompileSourceRoots();

    /**
     * Gets the project's resources.
     * 
     * @return the project's resources
     */
    protected abstract List getResources();

    /**
     * Gets the project's classpath.
     * 
     * @return a list of classpath elements
     */
    protected abstract List getClasspathElements();

    /**
     * Gets a set of inclusion filters for apt.
     * 
     * @return a set of inclusion filters
     */
    protected abstract Set getIncludes();

    /**
     * Gets a set of exclusion filters for apt.
     * 
     * @return a set of exclusion filters
     */
    protected abstract Set getExcludes();

    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace.
     * 
     * @return the directory root under which processor-generated source files will be placed
     */
    protected abstract File getOutputDirectory();
    
    // private methods --------------------------------------------------------
    
    private void executeApt() throws MojoExecutionException
    {
        List sourceFiles = getSourceFiles( getSourceScanner(), "sources" );
        
        if ( getLog().isInfoEnabled() )
        {
            int count = sourceFiles.size();
            
            getLog().info( "Processing " + count + " source file" + ( count == 1 ? "" : "s" ) + " to "
                           + getOutputDirectory().getAbsolutePath() );
        }

        List args = createArgs( sourceFiles );

        boolean success;

        if ( fork )
        {
            success = AptUtils.invokeForked( getLog(), workingDirectory, executable, meminitial, maxmem, args );
        }
        else
        {
            success = AptUtils.invoke( getLog(), args );
        }

        if ( !success )
        {
            throw new MojoExecutionException( "Apt failed" );
        }
    }

    private List createArgs( List sourceFiles ) throws MojoExecutionException
    {
        List args = new ArrayList();

        // javac arguments

        Set classpathElements = new LinkedHashSet();
        classpathElements.addAll( getPluginClasspathElements() );
        classpathElements.addAll( getClasspathElements() );

        if ( !classpathElements.isEmpty() )
        {
            args.add( "-classpath" );
            args.add( toPath( classpathElements ) );
        }

        List sourcePaths = getSourcePaths();

        if ( !sourcePaths.isEmpty() )
        {
            args.add( "-sourcepath" );
            args.add( toPath( sourcePaths ) );
        }

        if ( !showWarnings )
        {
            args.add( "-nowarn" );
        }

        if ( encoding != null )
        {
            args.add( "-encoding" );
            args.add( encoding );
        }

        if ( verbose )
        {
            args.add( "-verbose" );
        }

        // apt arguments

        args.add( "-s" );
        args.add( getOutputDirectory().getAbsolutePath() );

        // never compile
        args.add( "-nocompile" );

        if ( options != null )
        {
            for ( int i = 0; i < options.length; i++ )
            {
                args.add( "-A" + options[i].trim() );
            }
        }

        if ( StringUtils.isNotEmpty( factory ) )
        {
            args.add( "-factory" );
            args.add( factory );
        }

        // source files

        for ( Iterator iterator = sourceFiles.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();

            args.add( file.getAbsolutePath() );
        }

        return args;
    }

    private static String toPath( Collection paths )
    {
        StringBuffer buffer = new StringBuffer();

        for ( Iterator iterator = paths.iterator(); iterator.hasNext(); )
        {
            buffer.append( (String) iterator.next() );

            if ( iterator.hasNext() )
            {
                buffer.append( File.pathSeparator );
            }
        }

        return buffer.toString();
    }

    private List getPluginClasspathElements() throws MojoExecutionException
    {
        try
        {
            return MavenProjectUtils.getClasspathElements( project, pluginArtifacts );
        }
        catch ( DependencyResolutionRequiredException exception )
        {
            throw new MojoExecutionException( "Cannot get plugin classpath elements", exception );
        }
    }

    private List getSourcePaths()
    {
        List sourcePaths = new ArrayList();

        sourcePaths.addAll( getCompileSourceRoots() );

        if ( additionalSourceRoots != null )
        {
            sourcePaths.addAll( additionalSourceRoots );
        }

        return sourcePaths;
    }

    private List getSourceFiles( SourceInclusionScanner scanner, String name ) throws MojoExecutionException
    {
        List sourceFiles = new ArrayList();

        for ( Iterator iterator = getSourcePaths().iterator(); iterator.hasNext(); )
        {
            File sourceDir = new File( (String) iterator.next() );

            sourceFiles.addAll( getSourceFiles( scanner, name, sourceDir ) );
        }

        return sourceFiles;
    }

    private Set getSourceFiles( SourceInclusionScanner scanner, String name, File sourceDir )
        throws MojoExecutionException
    {
        Set sources;

        if ( sourceDir.isDirectory() )
        {
            try
            {
                sources = scanner.getIncludedSources( sourceDir, getOutputDirectory() );
            }
            catch ( InclusionScanException exception )
            {
                throw new MojoExecutionException( "Error scanning source directory: " + sourceDir, exception );
            }
        }
        else
        {
            sources = Collections.EMPTY_SET;
        }

        if ( getLog().isDebugEnabled() )
        {
            if ( sources.isEmpty() )
            {
                getLog().debug( "No " + name + " found in " + sourceDir );
            }
            else
            {
                getLog().debug( StringUtils.capitalizeFirstLetter( name ) + " found in " + sourceDir + ":" );

                LogUtils.log( getLog(), LogUtils.LEVEL_DEBUG, sources, "  " );
            }
        }

        return sources;
    }

    private SourceInclusionScanner getStaleScanner()
    {
        // create scanner
        
        SourceInclusionScanner scanner;
        
        if ( force )
        {
            if ( !isEmpty( outputFiles ) || !isEmpty( outputFileEndings ) )
            {
                getLog().warn( "Not using staleness checking - ignoring outputFiles and outputFileEndings" );
            }
            
            getLog().debug( "Processing all source files" );
            
            scanner = createSimpleScanner();
        }
        else
        {
            scanner = new StaleSourceScanner( staleMillis, includes, excludes );
            
            if ( !isEmpty( outputFiles ) )
            {
                if ( !isEmpty( outputFileEndings ) )
                {
                    getLog().warn( "Both outputFiles and outputFileEndings specified - using outputFiles" );
                }
                
                getLog().debug( "Computing stale sources against target files " + outputFiles );
                
                for ( Iterator iterator = outputFiles.iterator(); iterator.hasNext(); )
                {
                    String file = (String) iterator.next();
                    
                    scanner.addSourceMapping( new SingleTargetSourceMapping( ".java", file ) );
                }
            }
            else
            {
                Set suffixes = defaultSet( outputFileEndings, Collections.singleton( ".java" ) );
                
                getLog().debug( "Computing stale sources against target file endings " + suffixes );
                
                scanner.addSourceMapping( new SuffixMapping( ".java", suffixes ) );
            }
        }
        
        return scanner;
    }
    
    private SourceInclusionScanner getSourceScanner()
    {
        SourceInclusionScanner scanner;
        
        if ( force || isEmpty( outputFiles ) )
        {
            scanner = getStaleScanner();
        }
        else
        {
            scanner = createSimpleScanner();
        }
        
        return scanner;
    }
    
    private SourceInclusionScanner createSimpleScanner()
    {
        SourceInclusionScanner scanner = new SimpleSourceInclusionScanner( includes, excludes );
        
        // dummy mapping required to function
        scanner.addSourceMapping( new SuffixMapping( "", "" ) );
        
        return scanner;
    }
    
    private static Set defaultSet( Set set )
    {
        return defaultSet( set, Collections.EMPTY_SET );
    }
    
    private static Set defaultSet( Set set, Set defaultSet )
    {
        return isEmpty( set ) ? defaultSet : set;
    }
    
    private static boolean isEmpty( Collection collection )
    {
        return ( collection == null ) || collection.isEmpty();
    }
}