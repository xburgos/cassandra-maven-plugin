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
import org.codehaus.mojo.chronos.common.model.ResponsetimeSamples;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilityclass to assist in generating charts.
 *
 * @author ksr@lakeside.dk
 */
public class ChartUtil
{

    private static final double GAP = 10D;

    private static final double MARGIN = 0.02D;

    public static void addDomainMarker( XYPlot xyplot, String label, double value )
    {
        xyplot.addDomainMarker( addValueMarker( label, value, true ) );
    }

    public static void addRangeMarker( XYPlot xyplot, String label, double value )
    {
        xyplot.addRangeMarker( addValueMarker( label, value, false ) );
    }

    public static List createDefaultPlugins( ResponsetimeSamples samples, GCSamples gcSamples )
    {
        List plugins = new ArrayList();
        plugins.add( new ChronosResponsetimePlugin( samples ) );
        plugins.add( new ChronosHistogramPlugin( samples ) );
        plugins.add( new ChronosThroughputPlugin( samples ) );
        plugins.add( new ChronosGCPlugin( gcSamples ) );
        return plugins;
    }

    public static XYPlot newPlot( TimeSeries timeSeries, String label, boolean forceIncludeZero )
    {
        XYDataset dataset = asDataset( timeSeries );
        return newPlot( label, forceIncludeZero, dataset );
    }

    /* Method merged from Atlassion */

    public static XYPlot setUpperBound( JFreeChart chart, double max )
    {
        XYPlot plot = chart.getXYPlot();

        ValueAxis axis = plot.getDomainAxis();
        axis.setUpperBound( max );
        return plot;
    }

    public static XYPlot setupXYPlot( JFreeChart chart, DateFormat dateFormat )
    {
        XYPlot plot = chart.getXYPlot();

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride( dateFormat );

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        return plot;
    }

    /* pp */

    static CombinedDomainXYPlot createCombinedPlot( DateAxis timeAxis, XYPlot xyplot1, XYPlot xyplot2 )
    {
        CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot( timeAxis );
        combineddomainxyplot.setGap( GAP );
        combineddomainxyplot.add( xyplot1, 2 );
        combineddomainxyplot.add( xyplot2, 1 );
        combineddomainxyplot.setOrientation( PlotOrientation.VERTICAL );
        return combineddomainxyplot;
    }

    /* pp */

    static DateAxis createTimeAxis( String label, SimpleDateFormat dateFormat )
    {
        DateAxis timeAxis = new DateAxis( label );
        timeAxis.setDateFormatOverride( dateFormat );
        timeAxis.setLowerMargin( MARGIN );
        timeAxis.setUpperMargin( MARGIN );
        return timeAxis;
    }

    /* pp */


    static XYPlot newPlot( String verticalLabel, boolean forceIncludeZero, XYDataset dataset )
    {
        StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
        NumberAxis numberaxis = new NumberAxis( verticalLabel );
        numberaxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        numberaxis.setAutoRangeIncludesZero( forceIncludeZero );
        return new XYPlot( dataset, null, numberaxis, standardxyitemrenderer );
    }

    /**
     * Generate a {@link ValueMarker}.
     */
    private static ValueMarker addValueMarker( String text, double x, boolean domain )
    {
        ValueMarker marker = new ValueMarker( x );
        marker.setPaint( Color.GRAY );
        marker.setLabel( text );
        if ( domain )
        {
            marker.setLabelAnchor( RectangleAnchor.TOP_LEFT );
            marker.setLabelTextAnchor( TextAnchor.TOP_RIGHT );
        }
        else
        {
            marker.setLabelAnchor( RectangleAnchor.TOP_RIGHT );
            marker.setLabelTextAnchor( TextAnchor.BOTTOM_RIGHT );
        }
        return marker;
    }

    private static XYDataset asDataset( TimeSeries series )
    {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries( series );
        return dataset;
    }
}