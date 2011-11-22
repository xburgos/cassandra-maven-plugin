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
package org.codehaus.mojo.chronos.report.chart;

import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.report.ReportConfig;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * Source for garbage collection charts.
 *
 * @author ksr@lakeside.dk
 */
public final class SummaryGCChartSource
    implements ChartSource
{
    private GCSamples samples;

    public SummaryGCChartSource( GCSamples samples )
    {
        this.samples = samples;
    }

    public boolean isEnabled( ReportConfig config )
    {
        return config.isShowgc() && config.isShowsummary() && samples.getSampleCount() > 0;
    }

    public String getFileName( ReportConfig config )
    {
        return "gc-" + config.getId();
    }

    public JFreeChart getChart( ResourceBundle bundle, ReportConfig config )
    {
        String beforeLabel = bundle.getString( "chronos.label.gc.before" );
        String afterLabel = bundle.getString( "chronos.label.gc.after" );
        TimeSeriesCollection dataset1 = new TimeSeriesCollection();
        TimeSeries heapBeforeSeries = new TimeSeries( beforeLabel, Millisecond.class );
        samples.extractHeapBefore( heapBeforeSeries );
        TimeSeries heapAfterSeries = new TimeSeries( afterLabel, Millisecond.class );
        samples.extractHeapAfter( heapAfterSeries );

        dataset1.addSeries( heapBeforeSeries );
        dataset1.addSeries( heapAfterSeries );
        TimeSeriesCollection dataset = dataset1;

        String title = bundle.getString( "chronos.label.gc" );
        String timeLabel = bundle.getString( "chronos.label.gc.time" );
        String valueLabel = bundle.getString( "chronos.label.gc.mem" );
        JFreeChart chart =
            ChartFactory.createTimeSeriesChart( title, timeLabel, valueLabel, dataset, true, true, false );
        ChartUtil.setupXYPlot( chart, new SimpleDateFormat( "HH:mm:ss" ) );
        return chart;
    }

}
