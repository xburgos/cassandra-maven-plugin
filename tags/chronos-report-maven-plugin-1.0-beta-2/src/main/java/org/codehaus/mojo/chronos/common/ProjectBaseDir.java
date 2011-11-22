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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/responsetime/ResponsetimeSamples.java $
  * $Id: ResponsetimeSamples.java 14459 2011-08-12 13:41:52Z soelvpil $
  */
package org.codehaus.mojo.chronos.common;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * representation of the basedir for a project.
 * Primarily as a factory to find directories for a single performancetst
 */
public class ProjectBaseDir
{
    private File baseDir;
    private Log log;

    public ProjectBaseDir( MavenProject project, Log log )
    {
        this( project.getBasedir(), log );
    }

    public ProjectBaseDir(File baseDirectory, Log log)
    {
        this.baseDir = baseDirectory;
        this.log = log;
    }

    /**
     * Based on the specied base directory - return the chronos directory of the running build.<br />
     * If the chronos directory does not exits - it will be created.
     *
     * @return <code>File</code> pointing on the chronos directory.
     */
    public File getChronosDir()
    {
        File target = new File( baseDir, "target" );
        File chronos = new File( target, "chronos" );
        return IOUtil.ensureDir( chronos );
    }

    public TestDataDirectory getDataDirectory( String dataId )
    {
        return new TestDataDirectory( getChronosDir(), dataId, log );
    }

}
