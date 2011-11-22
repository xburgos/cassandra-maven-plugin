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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 * This class is responsible for generating responsetime charts.
 *
 * @author ksr@lakeside.dk
 */
public abstract class ResponseChartGenerator
{
    protected final JFreeChart createResponseChart( String datasetName, ResponsetimeSamples samples,
                                                    ResourceBundle bundle, ReportConfig config )
    {
        XYDataset dataset = createResponseDataset( datasetName, samples, bundle, config );

        String title = bundle.getString( "chronos.label.responsetimes" );
        String timeAxisLabel = bundle.getString( "chronos.label.responsetimes.time" );
        String valueAxisLabel = bundle.getString( "chronos.label.responsetimes.responsetime" );
        JFreeChart chart =
            ChartFactory.createTimeSeriesChart( title, timeAxisLabel, valueAxisLabel, dataset, true, true, false );
        ChartUtil.setupXYPlot( chart, new SimpleDateFormat( "HH:mm:ss" ) );
        // change rendering order - so average is in front
        chart.getXYPlot().setSeriesRenderingOrder( SeriesRenderingOrder.FORWARD );

        if ( config.isShowpercentile95() )
        {
            String text = bundle.getString( "chronos.label.percentile95.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getPercentile95() );
        }
        if ( config.isShowpercentile99() )
        {
            String text = bundle.getString( "chronos.label.percentile99.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getPercentile99() );
        }
        if ( config.isShowaverage() )
        {
            String text = bundle.getString( "chronos.label.average.arrow" );
            ChartUtil.addRangeMarker( chart.getXYPlot(), text, samples.getAverage() );
        }
        return chart;
    }

    private TimeSeriesCollection createResponseDataset( String name, ResponsetimeSamples samples, ResourceBundle bundle,
                                                        ReportConfig config )
    {
        TimeSeries series = new TimeSeries( name, Millisecond.class );

        samples.appendResponsetimes( series );
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries( series );

        String averageLabel = bundle.getString( "chronos.label.average" );
        TimeSeries averageseries =
            MovingAverage.createMovingAverage( series, averageLabel, config.getAverageduration(), 0 );
        dataset.addSeries( averageseries );
        return dataset;
    }
}
