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
package org.codehaus.mojo.chronos.history;

import junit.framework.TestCase;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.IOUtil;

import java.io.File;
import java.io.IOException;

/**
 * Test case for the <code>SaveHistoryMojo</code> class.
 * 
 * @author ads
 */
public class SaveHistoryMojoTest extends TestCase {

    /**
     * Simple test
     * 
     * @throws Exception
     *             Thrown if things goes wrong
     */
    public void testHistory() throws Exception {
        final String dataId = "SaveHistoryTest";

        removeDataDir(dataId);
        // This is normally done by the JMeterTestMojo.

        prepareJmeterLog(dataId, "src/test/resources/combinedtest-jtl22-summaryreport.xml");

        SaveHistoryMojo shmojo = new SaveHistoryMojo();
        shmojo.historydir = new File("target/chronos/");
        shmojo.dataid = dataId;
        shmojo.project = newMavenProject();
        shmojo.execute();
    }

    /**
     * Creates a new maven project for use in unit tests.
     *
     * @return The new <code>MavenProject</code> instance.
     */
    public static MavenProject newMavenProject()
    {
        Model model = new Model();
        model.setName("test");
        model.setUrl("url");

        MavenProject project = new MavenProject(model);
        project.setFile(new File(".", "pom.xml"));
        return project;
    }

    public static void prepareJmeterLog(String dataId, String responsetimeXmlPath) throws MojoExecutionException,
            IOException
        {
        File dir = new File("target/chronos/" + dataId);
        IOUtil.ensureDir( dir );
        File input = new File(responsetimeXmlPath);
        File output = new File(dir, "perf-test.xml");
        IOUtil.copyFile(input, output);
    }

    public static void removeDataDir(String dataId) {
        File file = new File("target/chronos/" + dataId);
        if(file.exists()) {
            deleteRecursive(file);
        }
    }

    private static void deleteRecursive(File file)
    {
        if(file.isDirectory()) {
            File[] elements = file.listFiles();
            for (int i = 0; i < elements.length; i++) {
                deleteRecursive(elements[i]);
            }
        }
        file.delete();
    }
}