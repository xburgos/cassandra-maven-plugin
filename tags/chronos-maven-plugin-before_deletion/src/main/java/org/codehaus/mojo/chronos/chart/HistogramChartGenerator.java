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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import java.util.ResourceBundle;

/**
 * This class is responsible for generating histograms.
 *
 * @author ksr@lakeside.dk
 */
public abstract class HistogramChartGenerator
    extends ChartUtil
{

    private static final int BINS = 100;

    private static final float FOREGROUND_ALPHA = 0.85F;

    protected final JFreeChart createHistogramChart( ResponsetimeSamples samples, String label, ResourceBundle bundle,
                                                     ReportConfig config )
    {
        HistogramDataset histogramdataset = new HistogramDataset();

        double[] sampleArray = samples.extractResponsetimes();
        histogramdataset.addSeries( label, sampleArray, BINS );
        JFreeChart chart = ChartFactory.createHistogram( bundle.getString( "chronos.label.histogram" ),
                                                         bundle.getString( "chronos.label.histogram.x" ),
                                                         bundle.getString( "chronos.label.histogram.y" ),
                                                         histogramdataset, PlotOrientation.VERTICAL, true, false,
                                                         false );
        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setForegroundAlpha( FOREGROUND_ALPHA );
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
        xybarrenderer.setDrawBarOutline( false );

        if ( config.isShowpercentile95() )
        {
            String label95 = bundle.getString( "chronos.label.percentile95.arrow" );
            double value = samples.getPercentile95();
            ChartUtil.addDomainMarker( xyplot, label95, value );
        }
        if ( config.isShowpercentile99() )
        {
            String label99 = bundle.getString( "chronos.label.percentile99.arrow" );
            double value = samples.getPercentile99();
            ChartUtil.addDomainMarker( xyplot, label99, value );
        }
        if ( config.isShowaverage() )
        {
            String label2 = bundle.getString( "chronos.label.average.arrow" );
            double value = samples.getAverage();
            ChartUtil.addDomainMarker( xyplot, label2, value );
        }
        return chart;
    }
}
