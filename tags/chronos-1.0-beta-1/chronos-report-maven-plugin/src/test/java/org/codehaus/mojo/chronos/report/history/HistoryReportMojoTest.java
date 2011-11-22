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
package org.codehaus.mojo.chronos.report.history;

import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.doxia.site.renderer.DefaultSiteRenderer;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.common.HistoricDataDirectory;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.common.model.HistoricSample;
import org.codehaus.mojo.chronos.report.TestHelper;

import java.io.File;

/**
 * Test case for the <code>HistoryReportMojo</code> class.
 * 
 * @author ads
 */
public class HistoryReportMojoTest extends TestCase {

    /**
     * Simple check test.
     * 
     * @throws Exception
     *             Thrown if things goes wrong.
     */
    public void test() throws Exception {
        MavenProject mavenProject = TestHelper.newMavenProject();
        SiteRenderer siteRenderer = new DefaultSiteRenderer();

        TestHelper.performReport(mavenProject, siteRenderer, "src/test/resources/test1-junitsamples.xml", "src/test/resources/test1-gc.xml", "HistoryTest");

        saveHistory(mavenProject, "HistoryTest");

        TestHelper.performReport(mavenProject, siteRenderer, "src/test/resources/combinedtest-jtl22-summaryreport.xml", null, "HistoryTest");
        saveHistory(mavenProject, "HistoryTest");

        HistoryReportMojo hrmojo = new HistoryReportMojo();
        hrmojo.dataid = "HistoryTest";
        hrmojo.historydir = new File("target/chronos-history");
        hrmojo.project = mavenProject;
        hrmojo.siteRenderer = new DefaultSiteRenderer();
        hrmojo.outputDirectory = new File("target/chronos-history").getParentFile().getAbsolutePath();
        hrmojo.execute();
    }

    private void saveHistory(MavenProject mavenProject, String dataid) throws Exception {
        HistoricDataDirectory historicDataDirectory = new HistoricDataDirectory( new File("target/chronos-history"), dataid );
        final ProjectBaseDir projectBaseDir = new ProjectBaseDir( mavenProject );
        final TestDataDirectory testDataDirectory = projectBaseDir.getDataDirectory(dataid);

        GroupedResponsetimeSamples responseSamples = testDataDirectory.readResponsetimeSamples();
        if ( responseSamples.size() == 0 )
        {
            throw new MojoExecutionException( "Response time samples not found for " + dataid );
        }

        GCSamples gcSamples = testDataDirectory.readGCSamples();
        HistoricSample history = new HistoricSample( responseSamples, gcSamples );
        historicDataDirectory.writeHistorySample( history );
    }
}