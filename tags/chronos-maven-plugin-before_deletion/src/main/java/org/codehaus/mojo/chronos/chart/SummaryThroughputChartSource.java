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
package org.codehaus.mojo.chronos.chart;

import org.codehaus.mojo.chronos.ReportConfig;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSamples;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * This class is responsible for generating throughput charts.
 *
 * @author ksr@lakeside.dk
 */
public final class SummaryThroughputChartSource
    implements ChartSource
{

    private ResponsetimeSamples samples;

    public SummaryThroughputChartSource( ResponsetimeSamples samples )
    {
        this.samples = samples;
    }

    public boolean isEnabled( ReportConfig config )
    {
        return config.isShowthroughput() && config.isShowsummary();
    }

    public String getFileName( ReportConfig config )
    {
        return "throughput-" + config.getId();
    }

    public JFreeChart getChart( ResourceBundle bundle, ReportConfig config )
    {
        XYPlot throughputPlot = createThroughputPlot( bundle, config );
        XYPlot threadCountPlot = createThreadCountPlot( bundle, config );

        String label = bundle.getString( "chronos.label.throughput.time" );
        SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );
        DateAxis timeAxis = ChartUtil.createTimeAxis( label, dateFormat );
        CombinedDomainXYPlot combineddomainxyplot =
            ChartUtil.createCombinedPlot( timeAxis, throughputPlot, threadCountPlot );
        return new JFreeChart( bundle.getString( "chronos.label.throughput" ), combineddomainxyplot );
    }

    private XYPlot createThroughputPlot( ResourceBundle bundle, ReportConfig config )
    {
        TimeSeriesCollection dataset1 = createThroughputDataset( bundle, config );
        XYPlot throughputPlot =
            ChartUtil.newPlot( dataset1, bundle.getString( "chronos.label.throughput.requests" ), true );
        throughputPlot.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );
        throughputPlot.getRenderer().setSeriesPaint( 0, Color.GREEN );
        throughputPlot.getRenderer().setSeriesPaint( 1, Color.BLUE );
        throughputPlot.setSeriesRenderingOrder( SeriesRenderingOrder.FORWARD );

        double maxAvgThroughput = samples.getMaxAverageThroughput( config.getAverageduration() );
        String maxThroughputLabel = bundle.getString( "chronos.label.maxaveragethroughput" );
        ChartUtil.addRangeMarker( throughputPlot, maxThroughputLabel, maxAvgThroughput );
        return throughputPlot;
    }

    private TimeSeriesCollection createThroughputDataset( ResourceBundle bundle, ReportConfig config )
    {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series = samples.createMovingThroughput( bundle.getString( "chronos.label.throughput" ) );
        dataset.addSeries( series );
        int avgDuration = config.getAverageduration();
        String label = bundle.getString( "chronos.label.average" );
        TimeSeries averageseries = MovingAverage.createMovingAverage( series, label, avgDuration, 0 );
        dataset.addSeries( averageseries );
        return dataset;
    }

    private XYPlot createThreadCountPlot( ResourceBundle bundle, ReportConfig config )
    {
        TimeSeriesCollection dataset2 = createThreadCountdataset( bundle, config );
        String label = bundle.getString( "chronos.label.threadcount.y" );
        XYPlot threadCountPlot = ChartUtil.newPlot( dataset2, label, false );
        threadCountPlot.setRangeAxisLocation( AxisLocation.TOP_OR_LEFT );
        threadCountPlot.getRenderer().setSeriesPaint( 0, Color.GRAY );
        return threadCountPlot;
    }

    private TimeSeriesCollection createThreadCountdataset( ResourceBundle bundle, ReportConfig config )
    {
        String label = bundle.getString( "chronos.label.threadcount" );
        TimeSeries series = new TimeSeries( label, Millisecond.class );
        samples.appendThreadCounts( series, config.getThreadcountduration() );
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries( series );
        return dataset;
    }

}
