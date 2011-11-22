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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterMojo.java $
  * $Id: JMeterMojo.java 14221 2011-06-24 10:16:28Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Invokes JMeter. JMeter is invoked by spawning a separate process to make it possible to control startup parameters.
 *
 * @author ksr@lakeside.dk
 */
public abstract class JMeterMojo
    extends AbstractMojo
{
    /**
     * The path to the jmeter installation. The recommended way to set this is by specifying a property jmeter.home in
     * the pom.xml or settings.xml.<br />
     *
     * @parameter expression="${project.basedir}/jmeter"
     */
    public String jmeterhome;

    /**
     * The URL from where jMeter can be downloaded.
     *
     * @parameter default-value="http://www.eu.apache.org/dist/jakarta/jmeter/binaries/jakarta-jmeter-2.5.1.zip"
     */
    public String jMeterRemoteLocation;

    /**
     * The heapsize (both initial and max) of the spawned jvm invoking jmeter.
     *
     * @parameter default-value="256m"
     */
    private String heap = "256m";

    /**
     * Specifies the corresponding jvm option of the spawned jvm invoking jmeter.
     *
     * @parameter default-value="128m";
     */
    private String newsize = "128m";

    /**
     * Specifies the corresponding jvm option of the spawned jvm invoking jmeter.
     *
     * @parameter
     */
    private String survivorratio;

    /**
     * Specifies the corresponding jvm option of the spawned jvm invoking jmeter.
     *
     * @parameter
     */
    private String targetsurvivorratio;

    /**
     * Specifies the corresponding jvm option of the spawned jvm invoking jmeter.
     *
     * @parameter default-value="2"
     */
    private String maxtenuringthreshold = "2";

    /**
     * Specifies the corresponding jvm option of the spawned jvm invoking jmeter.
     *
     * @parameter
     */
    private String maxliveobjectevacuationratio;

    /**
     * How often will rmi garbage collections be performed? JVM option of the spawned jvm invoking jmeter.
     *
     * @parameter default-value = "600000";
     */
    private String rmigcinterval = "600000";

    /**
     * The size of the part of the spawned jmeter jvm's memory, where classes e.g. are stored.
     *
     * @parameter default-value = "64m";
     */
    private String permsize = "64m";

    /**
     * System-properties to the launched jvm.
     *
     * @parameter
     */
    private Properties sysproperties = new Properties();

    /**
     * <b>Optional</b> Miscellaneous configuration parameters used when launching JMeter
     *
     * @parameter
     */
    private List options;

    protected final void ensureJMeter()
        throws MojoExecutionException
    {
        File jMeterJar = getJmeterJar();
        if ( !jMeterJar.exists() )
        {
            try
            {
                new DownloadHelper( jMeterRemoteLocation, jmeterhome, getLog() ).downloadZipFile();
            }
            catch ( IOException ex )
            {
                throw new MojoExecutionException( "Error during jMeter download", ex );
            }
        }
    }

    protected final void executeJmeter( JavaCommand java )
        throws MojoExecutionException
    {
        DependencyUtil deps = getDependencyUtil();
        final List copied;
        try
        {
            copied = deps.copyDependencies( getProject() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Execution failed", e );
        }
        try
        {
            int result = java.execute();
            if ( result != 0 )
            {
                throw new MojoExecutionException( "Result of " + java + " execution is: '" + result + "'." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Execution failed", e );
        }
        finally
        {
            if ( copied != null )
            {
                deps.cleanUpDependencies( copied );
            }
        }
    }

    protected final DependencyUtil getDependencyUtil()
    {
        return new DependencyUtil( jmeterhome, getLog() );
    }

    protected final JavaCommand getJavaLauncher()
        throws MojoExecutionException
    {
        if ( jmeterhome == null )
        {
            throw new MojoExecutionException( "Missing jmeterhome. You must eithe define a property jmeter.home "
                + "or set the jmeterhome explicitly in your plugin execution" );
        }

        JavaCommand java = new JavaCommand( getProject().getBasedir().getAbsolutePath(), getLog() );
        java.addSystemProperty( "user.dir", jmeterhome + "/bin" );
        // Removed - it is only supported on SUN VM's, and only affects the HotSpot compiler.
        // And since the changes are only required for high throughput server applications.
        // It will be removed for the client launcher.
        // java.addArgument("-server");
        java.addArgument( "-Xms" + heap );
        java.addArgument( "-Xmx" + heap );
        java.addExtraJvmOption( ":NewSize", newsize );
        java.addExtraJvmOption( ":MaxNewSize", newsize );
        if ( survivorratio != null )
        {
            java.addExtraJvmOption( ":SurvivorRatio", survivorratio );
        }
        if ( targetsurvivorratio != null )
        {
            java.addExtraJvmOption( ":TargetSurvivorRatio", targetsurvivorratio );
        }
        java.addExtraJvmOption( ":MaxTenuringThreshold", maxtenuringthreshold );
        if ( maxliveobjectevacuationratio != null )
        {
            java.addExtraJvmOption( "MaxLiveObjectEvacuationRatio", maxliveobjectevacuationratio );
        }
        java.addExtraJvmOption( ":PermSize", permsize );
        java.addExtraJvmOption( ":MaxPermSize", permsize );
        appendGcArgs( java );
        if ( !sysproperties.containsKey( "sun.rmi.dgc.client.gcInterval" ) )
        {
            sysproperties.setProperty( "sun.rmi.dgc.client.gcInterval", rmigcinterval );
        }
        if ( !sysproperties.containsKey( "sun.rmi.dgc.server.gcInterval" ) )
        {
            sysproperties.setProperty( "sun.rmi.dgc.server.gcInterval", rmigcinterval );
        }
        Enumeration sysPropNames = sysproperties.propertyNames();
        while ( sysPropNames.hasMoreElements() )
        {
            String name = (String) sysPropNames.nextElement();
            String value = sysproperties.getProperty( name );
            java.addSystemProperty( name, value );
        }
        if ( options != null )
        {
            for ( Iterator it = options.iterator(); it.hasNext(); )
            {
                String option = (String) it.next();
                java.addArgument( option );
            }
        }
        return java;
    }

    protected abstract void appendGcArgs( JavaCommand java );

    protected final File getJmeterJar()
    {
        return new File( new File( jmeterhome, "bin" ), "ApacheJMeter.jar" );
    }

    protected abstract MavenProject getProject();
}