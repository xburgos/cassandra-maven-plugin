package org.codehaus.mojo.dashboard.report.plugin.chart;

/*
 * Copyright 2006 David Vicente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.Locale;

import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class BarChartRenderer extends AbstractChartRenderer
{

    private static final double NUMBER_AXIS_RANGE = 1.0D;

    public BarChartRenderer( IDashBoardReportBean dashboardReport, IDatasetStrategy strategy )
    {
        super( dashboardReport, strategy );
    }

    public BarChartRenderer( IDashBoardReportBean dashboardReport, IDatasetStrategy strategy, int width, int height )
    {
        super( dashboardReport, strategy, width, height );
    }

    protected void createChart( IDashBoardReportBean dashboardReport )
    {
        CategoryDataset categorydataset = (CategoryDataset) this.datasetStrategy.createDataset( dashboardReport );
        report =
            ChartFactory.createBarChart( dashboardReport.getProjectName(), this.datasetStrategy.getYAxisLabel(),
                                         this.datasetStrategy.getXAxisLabel(), categorydataset,
                                         PlotOrientation.HORIZONTAL, true, true, false );
        report.setBackgroundPaint( Color.lightGray );
        CategoryPlot categoryplot = (CategoryPlot) report.getPlot();
        categoryplot.setBackgroundPaint( Color.white );
        categoryplot.setRangeGridlinePaint( Color.lightGray );
        categoryplot.setDomainGridlinePaint( Color.lightGray );
        categoryplot.setRangeAxisLocation( AxisLocation.BOTTOM_OR_LEFT );
        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        if ( datasetStrategy instanceof CoberturaBarChartStrategy )
        {
            numberaxis.setRange( 0.0D, BarChartRenderer.NUMBER_AXIS_RANGE );
            numberaxis.setNumberFormatOverride( NumberFormat.getPercentInstance() );
        }
        else
        {
            numberaxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        }
        BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
        barrenderer.setDrawBarOutline( false );
        barrenderer.setBaseItemLabelsVisible( true );
        if ( datasetStrategy instanceof CoberturaBarChartStrategy )
        {
            barrenderer.setBaseItemLabelGenerator( new StandardCategoryItemLabelGenerator(
                                                                                           "{2}",
                                                                                           NumberFormat.getPercentInstance( Locale.getDefault() ) ) );
        }
        else
        {
            barrenderer.setBaseItemLabelGenerator( new StandardCategoryItemLabelGenerator() );
        }
        
        int height =
            ( categorydataset.getColumnCount() * ChartUtils.STANDARD_BARCHART_ENTRY_HEIGHT * categorydataset.getRowCount() )
                            + ChartUtils.STANDARD_BARCHART_ADDITIONAL_HEIGHT;
        if ( height > ChartUtils.MINIMUM_HEIGHT )
        {
            super.setHeight( height );
        }
        else
        {
            super.setHeight( ChartUtils.MINIMUM_HEIGHT );
        }

        Paint[] paints = this.datasetStrategy.getPaintColor();

        for ( int i = 0; i < categorydataset.getRowCount() && i < paints.length; i++ )
        {
            barrenderer.setSeriesPaint( i, paints[i] );
        }

    }

}
