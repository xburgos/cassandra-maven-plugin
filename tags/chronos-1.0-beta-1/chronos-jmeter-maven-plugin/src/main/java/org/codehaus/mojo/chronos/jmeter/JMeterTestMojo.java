/*
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
  *
  * Copyright (c) to all contributors
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
  *
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterTestMojo.java $
  * $Id: JMeterTestMojo.java 14241 2011-06-30 13:23:24Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.IOUtil;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Invokes JMeter.<br />
 * JMeter is invoked by spawning a separate process to make it possible to control startup parameters. Can also be used
 * by specifying a .jtl file as input and (possibly) a garbage collection logfile.
 *
 * @author ksr@lakeside.dk
 * @goal jmeter
 * @phase integration-test
 * @requiresDependencyResolution test
 */
public class JMeterTestMojo
    extends JMeterMojo
{
    /**
     * The current maven project.
     *
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * The inputfile. This could either be a .jmx file or a folder containing .jmx files.
     * jmeter is invoked, and corresponding .jtl file(s) generated.
     *
     * @parameter
     * @required
     */
    private File input;

    /**
     * The id of the jmeter invocation.
     *
     * @parameter default-value="performancetest"
     */
    private String dataid;

    /**
     * Will garbage collections be logged? Note that this is really only relevant if your tests are junitsamples in
     * jmeter.
     *
     * @parameter default-value=true
     */
    private boolean loggc;

    /**
     * The name of an (optional) garbage collection logfile. Only used when loggc is set to true.
     *
     * @parameter
     */
    private File gclogfile;

    /**
     * Configuration parameters used for configurating the .
     *
     * @parameter
     */
    private List gcargs;

    /**
     * Clasname of an (optional) bootstrapperclass. The purpose is to allow bootstrapping the proces eg. by initializing
     * testdata in a relational database without measuring the time.
     *
     * @parameter
     */
    private String bootstrapper;

    /**
     * Additional arguments supplied to jmeter.
     * like invoking the jmeter startup script with
     * using jmeterargumensts like -Jvariable=value
     *
     * @parameter
     */
    private Properties jmeterVariables;

    /**
     * Set/Update the data input file of the <code>JMeterTestMojo</code>.
     *
     * @param input The new data input file.
     */
    public void setInput( File input )
    {
        this.input = input;
    }

    /**
     * Set/update the data id of the generated data.
     *
     * @param dataId The new data id.
     */
    public void setDataid( String dataId )
    {
        this.dataid = dataId;
    }

    /**
     * Set/update whether GC should be logged during the JMeter run.<br />
     * If <code>true</code> GC arguments will be added to launcher for JMeter. <br/>
     * If no specified output file has been specified, a new location/name will be generated.<br />
     * If <code>false</code> then JMeter will run without any GC arguments.
     *
     * @param loggc <code>true</code> to include GC logs - otherwise <code>false</code>.
     */
    public void setLoggc( boolean loggc )
    {
        this.loggc = loggc;
    }

    /**
     * Set the location of the generated GC output file.
     *
     * @param gclogfile The new GC output file location.
     */
    public void setGclogfile( File gclogfile )
    {
        this.gclogfile = gclogfile;
    }

    public void execute()
        throws MojoExecutionException
    {
        if ( !input.exists() )
        {
            throw new MojoExecutionException( "Invalid argument 'input', " + input.getPath() + " does not exist." );
        }

        final ProjectBaseDir projectBaseDir = new ProjectBaseDir(project);
        TestDataDirectory dir = projectBaseDir.getDataDirectory( dataid ).ensure();
        if ( input.isDirectory() )
        {
            // input is a jmx file
            File[] jmxFiles = IOUtil.listFilesWithExtension( input, "jmx" );
            for ( int i = 0; i < jmxFiles.length; i++ )
            {
                final File outputJtlFile = getOutputJtlFile(dir, jmxFiles[i]);
                executeJMeterIfNecessary( jmxFiles[i], outputJtlFile );
            }
        }
        else if ( isJmxFile( input ) )
        {
            // input is a jmx file
            final File outputJtlFile = getOutputJtlFile(dir, input);
            executeJMeterIfNecessary( input, outputJtlFile );
        }
        else
        {
            // exists due to previous check
            getLog().info( "jtl file " + input.getAbsolutePath() + " specified as input. Skipping jmeter..." );
        }
    }

    private void executeJMeterIfNecessary( File inputJmxFile, File outputJtlFile )
        throws MojoExecutionException
    {
        if ( outputJtlFile.exists() && inputJmxFile.lastModified() > outputJtlFile.lastModified() )
        {
            getLog().info( "clearing old testlog" );
            outputJtlFile.delete();
        }
        if ( !outputJtlFile.exists() )
        {
            ensureJMeter();
            if ( bootstrapper != null )
            {
                getLog().info( "Launching bootstrapClassName " + bootstrapper );
                getBootstrapLauncher( bootstrapper ).exe();
            }
            else
            {
                getLog().info( "No bootstrapper class found" );
            }
            getLog().info( "Excuting test " + inputJmxFile.getPath() );
            getJMeterLauncher( inputJmxFile, outputJtlFile ).exe();
        }
        else
        {
            getLog().info( "jtl file " + outputJtlFile.getAbsolutePath() + " up-to-date, skipping..." );
        }
        final ProjectBaseDir projectBaseDir = new ProjectBaseDir( project );
        final TestDataDirectory testDataDirectory = projectBaseDir.getDataDirectory(dataid);
        JMeterOutputMojo.parseJMeterOutput(testDataDirectory, outputJtlFile);
        JMeterOutputMojo.parseGCLog(testDataDirectory, gclogfile);
    }

    interface CommandLauncher
    {
        public void exe()
            throws MojoExecutionException;
    }

    private CommandLauncher getBootstrapLauncher( final String bootstrapClassName )
    {
        return new CommandLauncher()
        {
            public void exe()
                throws MojoExecutionException
            {
                JavaCommand bootstrapCmd = new JavaCommand( project.getBasedir().getAbsolutePath(), getLog() );
                bootstrapCmd.addArgument( "-cp" );
                StringBuffer classPath = new StringBuffer();
                Iterator it = getDependencyUtil().getDependencies( project ).iterator();
                while ( it.hasNext() )
                {
                    Artifact artifact = (Artifact) it.next();
                    classPath.append( artifact.getFile() );
                    if ( it.hasNext() )
                    {
                        classPath.append( File.pathSeparatorChar );
                    }
                }
                bootstrapCmd.addArgument( classPath.toString() );
                bootstrapCmd.addArgument( bootstrapClassName );

                try
                {
                    int result = bootstrapCmd.execute();
                    if ( result != 0 )
                    {
                        throw new MojoExecutionException(
                            "Result of " + bootstrapCmd + " execution is: '" + result + "'." );
                    }
                }
                catch ( CommandLineException e )
                {
                    throw new MojoExecutionException( "Could not create bootstrapClassName", e );
                }
            }
        };
    }

    private CommandLauncher getJMeterLauncher( final File inputJmxFile, final File outputJtlFile )
    {
        return new CommandLauncher()
        {
            public void exe()                           
                throws MojoExecutionException
            {
                JavaCommand java = getJavaLauncher();
                java.addArgument( "-jar" );
                String jmeterJar = getJmeterJar().getAbsolutePath();
                java.addArgument( jmeterJar );
                // non-gui
                java.addArgument( "-n" );
                // testplan inside this file
                java.addArgument( "-t" );
                java.addArgument( inputJmxFile.getAbsolutePath() );
                // output jtl
                java.addArgument( "-l" );
                java.addArgument( outputJtlFile.getAbsolutePath() );
                java.addArgument( "-j" );
                java.addArgument( "target/jmeter.log" );

                if (jmeterVariables != null) {
                    Enumeration variableNames = jmeterVariables.propertyNames();
                    while ( variableNames.hasMoreElements() )
                    {
                        String name = (String) variableNames.nextElement();
                        String value = jmeterVariables.getProperty( name );
                        java.addNameValue( "-J"+name, value );
                    }
                }

                executeJmeter( java );
            }
        };
    }

    private boolean isJmxFile( File input )
    {
        return input.getName().endsWith( ".jmx" );
    }

    private File getOutputJtlFile(TestDataDirectory dir, File inputFile) {
        final String inputName = IOUtil.removeExtension( inputFile.getName() );
        return new File( dir.getDirectory(), inputName + ".jtl" );
    }

    private final File getGcLogFile()
    {
        if ( !loggc )
        {
            return null;
        }
        if ( gclogfile != null )
        {
            return gclogfile;
        }
        File chronosDir = new ProjectBaseDir(getProject().getBasedir()).getChronosDir();
        return new File( chronosDir, "gclog-" + dataid + ".txt" );
    }

    protected final MavenProject getProject()
    {
        return project;
    }

    protected final void appendGcArgs( JavaCommand java )
    {
        File gclog = getGcLogFile();
        if ( gclog != null )
        {
            if ( isSunVm() )
            {
                java.addArgument( "-verbose:gc" );
                java.addArgument( "-Xloggc:" + gclog.getAbsolutePath() );
            }
            else
            {
                if ( gcargs != null )
                {
                    for ( Iterator iterator = gcargs.iterator(); iterator.hasNext(); )
                    {
                        java.addArgument( (String) iterator.next() );
                    }
                }
            }
        }
    }

    private boolean isSunVm()
    {
        return System.getProperty( "java.vendor" ).startsWith( "Sun " );
    }
}
