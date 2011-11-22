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
import org.apache.maven.project.MavenProject;
import org.codehaus.doxia.site.renderer.DefaultSiteRenderer;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.TestHelper;

import java.io.File;

/**
 * Test case for the <code>HistoryReportMojo</code> class.
 * 
 * @author ads
 */
public class HistoryReportMojoTest extends TestCase {

    private static final String HISTORY_ID = "HistoryTest";
    private static final File HISTORY_FOLDER = new File("target/chronos/history");
    private static final String BASE_RES = "src/test/resources/";

    /**
     * Simple mojo test.
     * 
     * @throws Exception
     *             Thrown if things goes wrong.
     */
    public void test() throws Exception {
        MavenProject mavenProject = TestHelper.newMavenProject();
        SiteRenderer siteRenderer = new DefaultSiteRenderer();

        TestHelper.performReport(mavenProject, siteRenderer, BASE_RES + "test1-junitsamples.jtl", BASE_RES
                + "test1-gc.txt", HISTORY_ID);

        saveHistory(mavenProject, HISTORY_ID);

        TestHelper.performReport(mavenProject, siteRenderer, BASE_RES + "combinedtest-jtl22-summaryreport.jtl", null,
                HISTORY_ID);
        saveHistory(mavenProject, HISTORY_ID);

        HistoryReportMojo hrmojo = new HistoryReportMojo();
        hrmojo.dataid = HISTORY_ID;
        hrmojo.historydir = HISTORY_FOLDER;
        hrmojo.project = mavenProject;
        hrmojo.siteRenderer = new DefaultSiteRenderer();
        hrmojo.outputDirectory = HISTORY_FOLDER.getParentFile().getAbsolutePath();
        hrmojo.execute();
    }

    private void saveHistory(MavenProject mavenProject, String id) throws Exception {
        SaveHistoryMojo shmojo = new SaveHistoryMojo();
        shmojo.historydir = HISTORY_FOLDER;
        shmojo.dataid = id;
        shmojo.project = mavenProject;
        shmojo.execute();
    }
}