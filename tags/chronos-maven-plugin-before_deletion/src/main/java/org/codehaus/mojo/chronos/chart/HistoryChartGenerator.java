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

import org.codehaus.mojo.chronos.history.HistoricSamples;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * Responsible for generating charts showing historic results.
 *
 * @author ksr@lakeside.dk
 */
public final class HistoryChartGenerator
{
    private ChartRenderer renderer;

    private ResourceBundle bundle;

    public HistoryChartGenerator( ChartRenderer renderer, ResourceBundle bundle )
    {
        this.renderer = renderer;
        this.bundle = bundle;
    }

    /**
     * create a summary chart of all samples together.
     *
     * @param samples the {@link HistoricSamples} to chart
     * @param dataId  an id of the current report
     * @param max     an upper bound for the responsetime
     * @throws IOException If the chart cannot be written to the filesystem
     */
    public void createResponseSummaryChart( HistoricSamples samples, String dataId, double max )
        throws IOException
    {
        String label1 = bundle.getString( "chronos.label.average.arrow" );
        TimeSeries averageSeries = samples.getAverageTime( label1 );
        String label2 = bundle.getString( "chronos.label.percentile95.arrow" );
        TimeSeries percentileseries = samples.getpercentile95( label2 );
        XYDataset dataset = getResponseDataset( averageSeries, percentileseries );
        /* Merged from Atlassion */
        // renderResponseChart(dataset, "history-response-summary-" + dataId);
        renderResponseChart( dataset, "history-response-summary-" + dataId, max );
    }

    public void createThroughputChart( HistoricSamples samples, String dataId )
        throws IOException
    {
        XYPlot xyplot = newPlot( samples.getThroughput( dataId ), "chronos.label.throughput.requests", true );
        xyplot.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );
        xyplot.getRenderer().setSeriesPaint( 0, Color.GREEN );

        String timeLabel = bundle.getString( "chronos.label.throughput.historytime" );
        DateAxis timeAxis = ChartUtil.createTimeAxis( timeLabel, new SimpleDateFormat() );
        xyplot.setDomainAxis( timeAxis );
        JFreeChart chart = new JFreeChart( bundle.getString( "chronos.label.throughput" ), xyplot );
        renderer.renderChart( "history-throughput-" + dataId, chart );
    }

    public void createGcChart( HistoricSamples samples, String dataId )
        throws IOException
    {
        XYPlot xyplot1 = newPlot( samples.getGcRatio( dataId ), "chronos.label.gc.ratio", true );
        xyplot1.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );
        xyplot1.getRenderer().setSeriesPaint( 0, Color.GREEN );
        xyplot1.getRangeAxis().setStandardTickUnits( NumberAxis.createStandardTickUnits() );

        XYPlot xyplot2 = newPlot( samples.getKbCollectedPrSecond( dataId ), "chronos.label.gc.kbpersec", true );
        xyplot2.setRangeAxisLocation( AxisLocation.TOP_OR_LEFT );
        xyplot2.getRenderer().setSeriesPaint( 0, Color.GRAY );
        xyplot2.getRangeAxis().setStandardTickUnits( NumberAxis.createStandardTickUnits() );

        String timeLabel = bundle.getString( "chronos.label.gc.historytime" );
        DateAxis timeAxis = ChartUtil.createTimeAxis( timeLabel, new SimpleDateFormat() );
        XYPlot combinedPlot = ChartUtil.createCombinedPlot( timeAxis, xyplot1, xyplot2 );
        // xyplot1.setDomainAxis( timeAxis );
        // XYPlot combinedPlot = xyplot1;
        JFreeChart chart = new JFreeChart( bundle.getString( "chronos.label.gc" ), combinedPlot );
        renderer.renderChart( "history-gc-" + dataId, chart );
    }

    private XYPlot newPlot( TimeSeries timeSeries, String label, boolean forceIncludeZero )
    {
        return ChartUtil.newPlot( timeSeries, bundle.getString( label ), forceIncludeZero );
    }

    /**
     * create the response charts for the individual samples.
     *
     * @param samples the {@link HistoricSamples} to chart
     * @param dataId  an id of the current report
     * @throws IOException if the chart cannot be written to the filesystem
     */
    public void createResponseDetailsChart( HistoricSamples samples, String dataId )
        throws IOException
    {
        // Merged from Atlassion
        double maxgraphupperbound = 0;
        String[] groupNames = samples.getGroupNames();
        for ( int i = 0; i < groupNames.length; i++ )
        {
            String label1 = bundle.getString( "chronos.label.average.arrow" );
            TimeSeries averageSeries = samples.getAverageTime( label1, groupNames[i] );
            String label2 = bundle.getString( "chronos.label.percentile95.arrow" );
            TimeSeries percentileseries = samples.getPercentile95( label2, groupNames[i] );
            XYDataset dataset = getResponseDataset( averageSeries, percentileseries );
            // Merged from Atlassion
            // renderResponseChart(dataset, "history-response-" + i + "-" + dataId);
            renderResponseChart( dataset, "history-response-" + i + "-" + dataId, maxgraphupperbound );
        }
    }

    /* Merged from Atlassion */
    // private void renderResponseChart(XYDataset dataset, String name) throws IOException {

    private void renderResponseChart( XYDataset dataset, String name, double max )
        throws IOException
    {
        String title = bundle.getString( "chronos.label.responsetimes" );
        String xLabel = bundle.getString( "chronos.label.responsetimes.historytime" );
        String yLabel = bundle.getString( "chronos.label.responsetimes.responsetime" );
        JFreeChart chart = ChartFactory.createTimeSeriesChart( title, xLabel, yLabel, dataset, true, true, false );
        /* Merged from Atlassion */
        if ( max > 0 )
        {
            ChartUtil.setUpperBound( chart, max );
        }
        ChartUtil.setupXYPlot( chart, new SimpleDateFormat() );
        renderer.renderChart( name, chart );
    }

    private XYDataset getResponseDataset( TimeSeries averageSeries, TimeSeries percentileseries )
    {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries( averageSeries );
        dataset.addSeries( percentileseries );
        return dataset;
    }
}
