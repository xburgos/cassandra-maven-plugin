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

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.report.chart.ChartRenderer;
import org.codehaus.mojo.chronos.report.chart.ChartRendererImpl;
import org.codehaus.mojo.chronos.report.chart.ChartUtil;
import org.codehaus.mojo.chronos.report.chart.GraphGenerator;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Creates a report of the currently executed performancetest in html format.
 *
 * @author ksr@lakeside.dk
 * @goal report
 */
// Line merged from Atlession
// Removed the @execution phase=verify descriptor to prevent double lifecycle execution
// * @execute phase=verify
public class ReportMojo
    extends AbstractMavenReport
{

    private static final int DEFAULT_DURATION = 20000;

    /**
     * Location (directory) where generated html will be created.
     *
     * @parameter expression="${project.build.directory}/site "
     */
    protected String outputDirectory;

    /**
     * Doxia Site Renderer.
     *
     * @component role="org.codehaus.doxia.site.renderer.SiteRenderer"
     * @required
     * @readonly
     */
    protected SiteRenderer siteRenderer;

    /**
     * Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    public MavenProject project;

    /**
     * The id of the report and the name of the generated html-file. If no id is defined, the dataid is used
     *
     * @parameter
     */
    protected String reportid;

    /**
     * The id of the data, to create a report from.
     *
     * @parameter default-value = "performancetest"
     */
    protected String dataid;

    /**
     * The title of the generated report.
     *
     * @parameter
     */
    protected String title;

    /**
     * The description of the generated report.
     *
     * @parameter
     */
    protected String description;

    /**
     * responsetimeDivider may be used when the response time of a single request is so low that the granularity of the
     * system timer corrupts the response time measured.
     *
     * @parameter default-value = 1
     */
    protected int responsetimedivider = 1;

    /**
     * The timeinterval (in millis) to base moving average calculations on.
     *
     * @parameter default-value = 20000
     */
    protected int averageduration = DEFAULT_DURATION; // 20 seconds

    /**
     * The timeinterval (in millis) to count threads within.
     *
     * @parameter default-value = 20000
     */
    protected int threadcountduration = DEFAULT_DURATION; // 20 seconds

    /**
     * Should a summary of the tests taken together be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showsummary = true;

    /**
     * Should a summary of the tests include graphs?
     *
     * @parameter default-value=true
     */
    protected boolean showsummarycharts = true;

    /**
     * Should details of each individual test be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showdetails = true;

    /**
     * Should responsetimes be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showresponse = true;

    /**
     * Should a histogram be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showhistogram = true;

    /**
     * Should a graph of throughput be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showthroughput = true;

    /**
     * Will information tables be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showinfotable = true;

    /**
     * Will the information tables contain timing info?
     *
     * @parameter default-value=true
     */
    protected boolean showtimeinfo = true;

    /**
     * Will graphs of responsetimes and histogram show 95 percentiles?
     *
     * @parameter
     */
    protected Boolean showpercentile;

    /**
     * Will graphs of responsetimes and histogram show 95 percentiles?
     *
     * @parameter default-value=true
     */
    protected boolean showpercentile95 = true;

    /**
     * Will graphs of responsetimes and histogram show 95 percentiles?
     *
     * @parameter default-value=false
     */
    protected boolean showpercentile99 = false;

    /**
     * Will graphs of responsetimes and histogram show the average?
     *
     * @parameter default-value=true
     */
    protected boolean showaverage = true;

    /**
     * Will garbage collections be shown?
     *
     * @parameter default-value=true
     */
    protected boolean showgc = true;

    /**
     * Set the history chart upper bound
     *
     * @parameter default-value=0
     */
    /* Merged from Atlassion */
    protected double historychartupperbound = 0;

    /**
     * Points to a simple text file containing meta data about the build.<br />
     * The information will be added to the reports under <i>Additional build info</i>.<br />
     * The file is read line for line and added the report.<br />
     * The readed expects the <code>tab</code> character to seperate keys and values:
     * <p/>
     * <pre>
     * Build no.&lt;tab&gt;567
     * Svn tag&lt;tab&gt;Test
     * </pre>
     *
     * @parameter default-value=null
     */
    protected String metadata;

    /**
     * @param locale
     * @throws MavenReportException
     * @see AbstractMavenReport#executeReport(Locale)
     */
    public void executeReport( Locale locale )
        throws MavenReportException
    {
        String dataId = getDataId();

        if ( showpercentile != null )
        {
            getLog().warn(
                "Property showPercentile is deprecated. Value is ignored. Use showPercentile95 og showPercentile99 instead" );
        }

        try
        {
            final ProjectBaseDir projectBaseDir = new ProjectBaseDir( project.getBasedir() );
            // parse logs
            final TestDataDirectory testDataDirectory = projectBaseDir.getDataDirectory( dataId );
            GroupedResponsetimeSamples jmeterSamples = testDataDirectory.readResponsetimeSamples();
            if ( jmeterSamples.size() == 0 )
            {
                throw new MavenReportException( "Response time samples not found for " + dataId );
            }

            getLog().info( "  tests: " + jmeterSamples.getSampleGroups().size() );
            getLog().info( "  jmeter samples: " + jmeterSamples.size() );

            // charts
            getLog().info( " generating charts..." );

            GCSamples gcSamples = testDataDirectory.readGCSamples();
            List defaultPlugins = ChartUtil.createDefaultPlugins( jmeterSamples, gcSamples );
            GraphGenerator graphGenerator = new GraphGenerator( defaultPlugins );
            ChartRenderer renderer = new ChartRendererImpl( getOutputDirectory() );
            graphGenerator.generateGraphs( renderer, getBundle( locale ), getConfig() );

            // report
            ReportGenerator reportGenerator = new ReportGenerator( getBundle( locale ), getConfig(), graphGenerator );
            getLog().info( " generating report..." );
            Sink sink = getSink();
            reportGenerator.doGenerateReport( sink, jmeterSamples );

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

    private String getDataId()
    {
        return dataid;
    }

    /**
     * @param locale
     * @see MavenReport#getName(Locale)
     */
    public String getName( Locale locale )
    {
        return getOutputName();
    }

    /**
     * @param locale
     * @see MavenReport#getDescription(Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "chronos.description" );
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return getConfig().getId();
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getReportOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    ResourceBundle getBundle( Locale locale )
    {
        return Utils.getBundle( locale );
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
     */
    public boolean canGenerateReport()
    {
        // Only execute reports for java projects
        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        return "java".equals( artifactHandler.getLanguage() );
    }

    /**
     * @return Returns the report.
     */
    protected ReportConfig getConfig()
    {
        return new ReportConfig()
        {
            public String getId()
            {
                return reportid != null ? reportid : dataid;
            }

            public String getTitle()
            {
                return title;
            }

            public String getDescription()
            {
                return description;
            }

            public int getAverageduration()
            {
                return averageduration;
            }

            public long getThreadcountduration()
            {
                return threadcountduration;
            }

            /* Merged from Atlassion */
            public double getHistoryChartUpperBound()
            {
                return historychartupperbound;
            }

            public boolean isShowaverage()
            {
                return showaverage;
            }

            public boolean isShowdetails()
            {
                return showdetails;
            }

            public boolean isShowgc()
            {
                return showgc;
            }

            public boolean isShowhistogram()
            {
                return showhistogram;
            }

            public boolean isShowinfotable()
            {
                return showinfotable;
            }

            public boolean isShowpercentile95()
            {
                return showpercentile95;
            }

            public boolean isShowpercentile99()
            {
                return showpercentile99;
            }

            public boolean isShowresponse()
            {
                return showresponse;
            }

            public boolean isShowsummary()
            {
                return showsummary;
            }

            public boolean isShowsummarycharts()
            {
                return showsummarycharts;
            }

            public boolean isShowthroughput()
            {
                return showthroughput;
            }

            public boolean isShowtimeinfo()
            {
                return showtimeinfo;
            }

            public String getMetadata()
            {
                return metadata;
            }
        };
    }
}
