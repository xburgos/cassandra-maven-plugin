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

import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.chronos.history.HistoricSamples;

import java.util.ResourceBundle;

/**
 * Helper class doing th eheavy listing when generating historic reports.
 *
 * @author ksr@lakeside.dk
 */
public final class HistoryReportGenerator
{
    private static final String IMG_EXT = ".png";

    private String dataId;

    private ResourceBundle bundle;

    private ReportSink reportSink;

    private String title;

    private String description;

    private Sink sink;

    /**
     * Constructor for the <code>HistoryReportGenerator</code> class.
     *
     * @param dataId      The id of the JMeter test.
     * @param bundle      <code>ResourceBundle</code> containing locale texts.
     * @param title       The report title.
     * @param description The report description.
     */
    public HistoryReportGenerator( String dataId, ResourceBundle bundle, String title, String description )
    {
        this.dataId = dataId;
        this.bundle = bundle;
        this.title = title;
        this.description = description;
    }

    /**
     * Generate a report (as an html page).
     *
     * @param aSink   The {@link Sink} to output the report content to
     * @param samples The {@link org.codehaus.mojo.chronos.responsetime.ResponsetimeSamples} to create a report from
     * @param showgc  <code>boolean</code> specifying the GSS graphs is to be included in the report.
     */
    public void doGenerateReport( Sink aSink, HistoricSamples samples, boolean showgc )
    {
        this.reportSink = new ReportSink( bundle, aSink );
        this.sink = aSink;

        aSink.head();
        aSink.text( bundle.getString( "chronos.description" ) );
        aSink.head_();
        aSink.body();

        String anchor = "Report" + dataId;
        reportSink.constructHeaderSection( title, description, anchor );
        String text = bundle.getString( "chronos.label.summary" );
        String anchor1 = "Summary" + dataId;
        reportSink.title2( text, anchor1 );
        constructReportHotLinks();
        reportSink.graphics( "history-response-summary-" + dataId + IMG_EXT );
        reportSink.graphics( "history-throughput-" + dataId + IMG_EXT );
        if ( showgc )
        {
            reportSink.graphics( "history-gc-" + dataId + IMG_EXT );
        }
        reportSink.title2( bundle.getString( "chronos.label.testcases" ), "Test_Cases" + dataId );
        constructReportHotLinks();
        String[] groupNames = samples.getGroupNames();
        for ( int i = 0; i < groupNames.length; i++ )
        {
            reportSink.title3( groupNames[i], i + dataId );
            reportSink.graphics( "history-response-" + i + "-" + dataId + IMG_EXT );
        }
        reportSink.sinkLineBreak();
        aSink.body_();
        aSink.flush();
        aSink.close();
    }

    private void constructReportHotLinks()
    {
        sink.section3();
        reportSink.sinkLink( bundle.getString( "chronos.label.summary" ), "Summary" + dataId );
        reportSink.sinkLink( bundle.getString( "chronos.label.testcases" ), "Test_Cases" + dataId );
        sink.section3_();
    }

}
