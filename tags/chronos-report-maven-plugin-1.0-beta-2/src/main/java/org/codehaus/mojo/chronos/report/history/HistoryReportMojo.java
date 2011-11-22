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

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.common.HistoricDataDirectory;
import org.codehaus.mojo.chronos.common.model.HistoricSamples;
import org.codehaus.mojo.chronos.report.HistoryReportGenerator;
import org.codehaus.mojo.chronos.report.Utils;
import org.codehaus.mojo.chronos.report.chart.ChartRenderer;
import org.codehaus.mojo.chronos.report.chart.ChartRendererImpl;
import org.codehaus.mojo.chronos.report.chart.HistoryChartGenerator;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Creates a historic report of performance test results.
 *
 * @author ksr@lakeside.dk
 * @goal historyreport
 * @execute phase=post-integration-test
 */
public class HistoryReportMojo
    extends AbstractMavenReport
{
    /**
     * Location where generated html will be created.
     *
     * @parameter expression="${project.build.directory}/site "
     * @required
     * @readonly
     */
    /*pp*/ String outputDirectory;

    /**
     * Doxia Site Renderer.
     *
     * @component role="org.codehaus.doxia.site.renderer.SiteRenderer"
     * @required
     * @readonly
     */
    /*pp*/ SiteRenderer siteRenderer;

    /**
     * Current Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    /*pp*/ MavenProject project;

    /**
     * The id of the data, to create a report from. Used to separate between several performancetest inside the same
     * maven project
     *
     * @parameter default-value = "performancetest"
     */
    /*pp*/ String dataid;

    /**
     * The directory where historic data are stored.
     *
     * @parameter expression="${basedir}/target/chronos-history"
     */
    /*pp*/ File historydir;

    /**
     * The title of the generated report
     *
     * @parameter default-value = ""
     */
    /*pp*/ String title;

    /**
     * The description of the generated report.
     *
     * @parameter default-value = ""
     */
    /*pp*/ String description;

    /**
     * Should the report contain garbage collections? Note that garbage collections are only relevant if they are from
     * the code being tested (if you use JMeter to test wbsites, the jmeter gc logs are totally irrelevant)!
     *
     * @parameter default-value=true
     */
    private boolean showgc;

    /**
     * This sets the default maximum value on the history report charts. This can be set to prevent "spikes" in the
     * charts which can throw the scale off.
     *
     * @parameter default-value=0
     */
    /* Merged from Atlassion */
    private double historychartupperbound;

    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        try
        {
            HistoricDataDirectory dataDirectory = new HistoricDataDirectory( historydir, dataid );

            HistoricSamples samples = dataDirectory.readHistoricSamples();
            ResourceBundle bundle = Utils.getBundle( locale );

            // charts
            getLog().info( " generating charts..." );
            ChartRenderer renderer = new ChartRendererImpl( getOutputDirectory() );
            HistoryChartGenerator charts = new HistoryChartGenerator( renderer, bundle );
            /* Merged from Atlassion */
            // charts.createResponseSummaryChart(samples, dataid);
            charts.createResponseSummaryChart( samples, dataid, historychartupperbound );
            charts.createThroughputChart( samples, dataid );
            if ( showgc )
            {
                charts.createGcChart( samples, dataid );
            }
            charts.createResponseDetailsChart( samples, dataid );

            HistoryReportGenerator reportgenerator = new HistoryReportGenerator( dataid, bundle, title, description );
            reportgenerator.doGenerateReport( getSink(), samples, showgc );
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "ReportGenerator failed", e );
        }
        catch ( JDOMException e )
        {
            throw new MavenReportException( "ReportGenerator failed", e );
        }
    }

    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    public String getDescription( Locale locale )
    {
        return description;
    }

    public String getName( Locale locale )
    {
        return getOutputName();
    }

    public String getOutputName()
    {
        return "history-" + dataid;
    }

    /**
     * We skip this report if no historical samples can be found...
     */
    public boolean canGenerateReport()
    {
        if ( !historydir.exists() )
        {
            logMissingDir(historydir);
            return false;
        }
        File dataDirectory = new File( historydir, dataid );
        if ( !dataDirectory.exists() )
        {
            logMissingDir(dataDirectory);
            return false;
        }
        return true;
    }

    private void logMissingDir(File dir)
    {
        getLog().info( "Directory with historic results " + dir + " not found, skipping historic report." );
    }

}
