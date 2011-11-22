package org.codehaus.mojo.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.util.JRProperties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Compiles JasperReports xml definition files.
 * <p>Much of this was inspired by the JRAntCompileTask, while trying to make it
 * slightly cleaner and easier to use with Maven's mojo api.</p>
 *
 * @author gjoseph
 * @goal compile-reports
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class JasperReportsMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project}
     */
    private MavenProject project;

    /**
     * This is where the generated java sources are stored.
     *
     * @parameter expression="${project.build.directory}/jasperreports-generated-sources"
     */
    private File javaDir;

    /**
     * This is where the .jasper files are written.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File targetDirectory;

    /**
     * This is where the xml report design files should be.
     *
     * @parameter default-value="src/main/jasperreports"
     */
    private File sourceDirectory;

    /**
     * This is a regex used to select your report design files.
     * Selects all files with a .jrxml extension by default.
     *
     * @parameter default-value="(.*)\.jrxml"
     */
    private String sourcePattern;

    /**
     * This is a replacement pattern to name the serialized report
     * files. The defaut .jxrml extension is replaced by .jasper.
     * Should have a back reference to "$1" if using the default
     * sourcePattern.
     *
     * @parameter default-value="$1.jasper"
     */
    private String targetPattern;

    /**
     * Since the JasperReports compiler deletes the compiled classes,
     * one might want to set this to true, if they want to handle
     * the generated java source in their application.
     * Mind that this will not work if the mojo is bound to the compile
     * or any other later phase. (As one might need to do if they use
     * classes from their project in their report design)
     *
     * @parameter default-value="false"
     * @deprecated There seems to be an issue with the compiler plugin
     *             so don't expect this to work yet - the dependencies
     *             will have disappeared.
     */
    private boolean keepJava;

    /**
     * Not used for now - just a TODO - the idea being that one
     * might want to set this to false if they want to handle the
     * generated java source in their application.
     *
     * @parameter default-value="true"
     * @deprecated Not implemented
     */
    private boolean keepSerializedObject;

    /**
     * Wether the xml design files must be validated.
     *
     * @parameter default-value="true"
     */
    private boolean xmlValidation;

    /**
     * Uses the Javac compiler by default. This is different from
     * the original JasperReports ant task, which uses the JDT compiler by default.
     *
     * @parameter default-value="net.sf.jasperreports.engine.design.JRJavacCompiler"
     */
    private String compiler;

    /**
     * @parameter expression="${project.compileClasspathElements}"
     */
    private List classpathElements;

    /**
     * Any additional classpath entry you might want to add to
     * the JasperReports compiler. Not recommended for general use,
     * plugin dependencies should be used instead.
     *
     * @parameter
     */
    private String additionalClasspath;

    private Pattern sourceRegex;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "javaDir = " + javaDir );
        getLog().debug( "sourceDirectory = " + sourceDirectory );
        getLog().debug( "sourcePattern = " + sourcePattern );
        getLog().debug( "targetDirectory = " + targetDirectory );
        getLog().debug( "targetPattern = " + targetPattern );
        getLog().debug( "keepJava = " + keepJava );
        //getLog().debug("keepSerializedObject = " + keepSerializedObject);
        getLog().debug( "xmlValidation = " + xmlValidation );
        getLog().debug( "compiler = " + compiler );
        getLog().debug( "classpathElements = " + classpathElements );
        getLog().debug( "additionalClasspath = " + additionalClasspath );

        checkDir( javaDir, "Directory for generated java sources", true );
        checkDir( sourceDirectory, "Source directory", false );
        checkDir( targetDirectory, "Target directory", true );

        sourceRegex = Pattern.compile( sourcePattern );

        JRProperties.backupProperties();
        try
        {
            String classpath = buildClasspathString( classpathElements, additionalClasspath );
            getLog().debug( "buildClasspathString() = " + classpath );
            JRProperties.setProperty( JRProperties.COMPILER_CLASSPATH, classpath );
            JRProperties.setProperty( JRProperties.COMPILER_TEMP_DIR, javaDir.getAbsolutePath() );
            JRProperties.setProperty( JRProperties.COMPILER_KEEP_JAVA_FILE, keepJava );
            JRProperties.setProperty( JRProperties.COMPILER_CLASS, compiler );
            JRProperties.setProperty( JRProperties.COMPILER_XML_VALIDATION, xmlValidation );

            // scan source dir
            Map reportFilesMap = new HashMap();
            RegexFilenameFilter filter = new RegexFilenameFilter( sourceRegex );
            scanDir( reportFilesMap, sourceDirectory, filter );

            // actual compilation
            compile( reportFilesMap );

            if ( keepJava )
            {
                project.addCompileSourceRoot( javaDir.getAbsolutePath() );
            }
        }
        finally
        {
            JRProperties.restoreProperties();
        }
    }

    protected void compile( Map reportFilesMap )
        throws MojoFailureException, MojoExecutionException
    {
        Set files = reportFilesMap.keySet();
        getLog().info( "Compiling " + files.size() + " report design files." );
        Iterator it = files.iterator();
        while ( it.hasNext() )
        {
            File src = (File) it.next();
            File dest = (File) reportFilesMap.get( src );

            File destFileParent = dest.getParentFile();
            if ( !destFileParent.exists() )
            {
                if ( destFileParent.mkdirs() )
                {
                    getLog().debug( "Created directory " + destFileParent );
                }
                else
                {
                    throw new MojoExecutionException( "Could not create directory " + destFileParent );
                }
            }
            try
            {
                getLog().debug( "File : " + src + " ... " );
                JasperCompileManager.compileReportToFile( src.getAbsolutePath(), dest.getAbsolutePath() );
                getLog().debug( "OK." );
            }
            catch ( JRException e )
            {
                throw new MojoFailureException( this, "Error compiling report design : " + src,
                                                "Error compiling report design : " + src + " : " + e.getMessage() );
            }
        }
        getLog().info( "Compiled " + files.size() + " report design files." );
    }

    protected void scanDir( Map reportFilesMap, File sourceDirectory, RegexFilenameFilter filter )
        throws MojoExecutionException
    {
        File[] files = sourceDirectory.listFiles( filter );
        for ( int i = 0; i < files.length; i++ )
        {
            if ( files[i].isDirectory() )
            {
                scanDir( reportFilesMap, files[i], filter );
            }
            else
            {
                String pathRelativeToRoot = getPathRelativeToRoot( files[i] );
                String targetName = sourceRegex.matcher( pathRelativeToRoot ).replaceAll( targetPattern );
                File target = new File( targetDirectory, targetName );
                reportFilesMap.put( files[i], target );
            }

        }
    }

    private String getPathRelativeToRoot( File file )
        throws MojoExecutionException
    {
        try
        {
            String root = this.sourceDirectory.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            if ( !filePath.startsWith( root ) )
            {
                throw new MojoExecutionException( "File is not in source root ??? " + file );
            }
            return filePath.substring( root.length() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not getCanonicalPath from file " + file, e );
        }
    }

    protected String buildClasspathString( List classpathElements, String additionalClasspath )
    {
        StringBuffer classpath = new StringBuffer();
        Iterator it = classpathElements.iterator();
        while ( it.hasNext() )
        {
            String cpElement = (String) it.next();
            classpath.append( cpElement );
            if ( it.hasNext() )
            {
                classpath.append( File.pathSeparator );
            }
        }
        if ( additionalClasspath != null )
        {
            if ( classpath.length() > 0 )
            {
                classpath.append( File.pathSeparator );
            }
            classpath.append( additionalClasspath );

        }
        return classpath.toString();
    }

    private void checkDir( File dir, String desc, boolean isTarget )
        throws MojoExecutionException
    {
        if ( dir.exists() && !dir.isDirectory() )
        {
            throw new MojoExecutionException( desc + " is not a directory : " + dir );
        }
        else if ( !dir.exists() && isTarget && !dir.mkdirs() )
        {
            throw new MojoExecutionException( desc + " could not be created : " + dir );
        }

        if ( isTarget && !dir.canWrite() )
        {
            throw new MojoExecutionException( desc + " is not writable : " + dir );
        }
    }
}
