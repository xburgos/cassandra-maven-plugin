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
package org.codehaus.mojo.chronos.report;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.doxia.site.renderer.DefaultSiteRenderer;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.common.IOUtil;

import java.io.*;

/**
 * Helper class used for unit testing
 * 
 * @author ads
 */
public class TestHelper {

    public static void performReport(String responsetimeXmlPath, String gcFileXmlPath, final String id) throws Exception
    {
        performReport(newMavenProject(), new DefaultSiteRenderer(), responsetimeXmlPath, gcFileXmlPath, id);
    }

    public static void performReport(MavenProject project, SiteRenderer siteRenderer, String responsetimeXmlPath, String gcFileXmlPath,
            final String dataId) throws Exception
    {
        removeDataDir(dataId);

        prepareJmeterLog(dataId, responsetimeXmlPath);

        if(gcFileXmlPath != null) {
            prepareGcLog(dataId, gcFileXmlPath);
        }

        ReportMojo mojo = new ReportMojo();
        mojo.dataid = dataId;
        mojo.title = "title";
        mojo.description = "here is my description";
        mojo.siteRenderer = siteRenderer;
        mojo.outputDirectory = "target/chronos-site/" + dataId;
        mojo.project = project;
        mojo.showdetails = false;
        mojo.showhistogram = false;
        mojo.showresponse = false;
        mojo.metadata = "src/test/resources/metadata.properties";
        mojo.execute();
    }

    public static void prepareJmeterLog(String dataId, String responsetimeXmlPath) throws MojoExecutionException, IOException {
        File dir = new File("target/chronos/" + dataId);
        IOUtil.ensureDir(dir);
        File input = new File(responsetimeXmlPath);
        File output = new File(dir, "perf-test.xml");
        IOUtil.copyFile( input, output );
    }

    private static void prepareGcLog(String dataId, String gcFilePath) throws MojoExecutionException, IOException {
        File dir = new File("target/chronos/" + dataId);
        IOUtil.ensureDir(dir);
        File input = new File(gcFilePath);
        File output = new File(dir, "gc-test.xml");
        IOUtil.copyFile( input, output );
        IOUtil.copyDTDToDir("chronos-gc.dtd", dir);
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
}