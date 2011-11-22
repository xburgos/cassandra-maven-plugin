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
  * $HeadURL$
  * $Id$
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Invokes the JMeter gui.<br />
 * The purpose is to create a testplan with the artifacts of the current project in the classpath.<br />
 * This is necessary if the testplan should contain unittests or javaclases from the project.
 *
 * @author ksr@lakeside.dk
 * @goal jmetergui
 * @execute phase="package"
 * @requiresDependencyResolution test
 */
public class JMeterGuiMojo
    extends JMeterMojo
{
    /**
     * The "current" maven project as executed by the package phase.<br />
     * We use this to find dependencies and paths.
     *
     * @parameter expression="${executedProject}"
     */
    protected MavenProject executedproject;

    public void execute()
        throws MojoExecutionException
    {
        ensureJMeter();

        JavaCommand java = getJavaLauncher();
        // Add Mac-specific property - should be ignored elsewhere (JMeter bug 47064)
        java.addSystemProperty( "apple.laf.useScreenMenuBar" , "true" );
        java.addArgument( "-jar" );
        String jmeterJar = getJmeterJar().getAbsolutePath();
        java.addArgument( jmeterJar );
        executeJmeter( java );
    }

    protected void appendGcArgs( JavaCommand java )
    {
        //Do nothing
    }

    protected final MavenProject getProject()
    {
        return executedproject;
    }
}
