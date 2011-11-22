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
import java.awt.Font;
import java.awt.Paint;

import org.codehaus.mojo.dashboard.report.plugin.utils.ChartUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;


/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 *
 */
public class StackedBarChartRenderer extends AbstractChartRenderer
{
    /**
     * 
     * @param bundle
     * @param dashboardReport
     * @param strategy
     */
    public StackedBarChartRenderer( IChartStrategy strategy )
    {
        super( strategy );
    }
    /**
     * 
     * @param bundle
     * @param dashboardReport
     * @param strategy
     * @param width
     * @param height
     */
    public StackedBarChartRenderer( IChartStrategy strategy, int width, int height )
    {
        super( strategy, width, height );
    }
    /**
     * 
     */
    protected void createChart()
    {

        CategoryDataset categorydataset = (CategoryDataset) this.datasetStrategy.getDataset();
        report =
            ChartFactory.createStackedBarChart( this.datasetStrategy.getTitle(), this.datasetStrategy.getYAxisLabel(),
                                                this.datasetStrategy.getXAxisLabel(), categorydataset,
                                                PlotOrientation.HORIZONTAL, true, true, false );
        report.setBackgroundPaint( Color.lightGray );
        report.setAntiAlias( false );
        CategoryPlot categoryplot = (CategoryPlot) report.getPlot();
        categoryplot.setBackgroundPaint( Color.white );
        categoryplot.setRangeGridlinePaint( Color.lightGray );
        StackedBarRenderer stackedbarrenderer = (StackedBarRenderer) categoryplot.getRenderer();
        stackedbarrenderer.setDrawBarOutline( false );
        stackedbarrenderer.setItemLabelsVisible( true );
        stackedbarrenderer.setItemLabelGenerator( new StandardCategoryItemLabelGenerator() );
        stackedbarrenderer.setItemLabelFont( StackedBarRenderer.DEFAULT_VALUE_LABEL_FONT.deriveFont( Font.BOLD ) );
        int height =
            ( categorydataset.getColumnCount() * ChartUtils.STANDARD_BARCHART_ENTRY_HEIGHT * 2 )
                            + ChartUtils.STANDARD_BARCHART_ADDITIONAL_HEIGHT;
        if( height > ChartUtils.MINIMUM_HEIGHT)
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
            stackedbarrenderer.setSeriesPaint( i, paints[i] );
        }

    }
}
