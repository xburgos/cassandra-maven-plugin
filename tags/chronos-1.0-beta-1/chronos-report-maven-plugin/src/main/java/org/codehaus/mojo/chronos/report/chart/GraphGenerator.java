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

import org.codehaus.mojo.chronos.report.ReportConfig;
import org.jfree.chart.JFreeChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Generates the charts of the jmeter report.
 *
 * @author ksr@lakeside.dk
 */
public final class GraphGenerator
{
    private List<ChartSource> summaryChartSources = new ArrayList<ChartSource>();

    private Map detailsChartSources = new LinkedHashMap();

    public GraphGenerator( List plugins )
    {
        for ( Iterator iterator = plugins.iterator(); iterator.hasNext(); )
        {
            ChronosReportPlugin plugin = (ChronosReportPlugin) iterator.next();
            ChartSource summarySource = plugin.getSummaryChartSource();
            if ( summarySource != null )
            {
                summaryChartSources.add( summarySource );
            }
            Map detailsSources = plugin.getDetailChartSources();
            for ( Iterator iterator2 = detailsSources.keySet().iterator(); iterator2.hasNext(); )
            {
                String testName = (String) iterator2.next();
                List existing = (List) detailsChartSources.get( testName );
                if ( existing == null )
                {
                    existing = new ArrayList();
                    detailsChartSources.put( testName, existing );
                }
                existing.add( detailsSources.get( testName ) );
            }
        }
    }

    /**
     * Generates response, throughput, histogram and gc charts according to report parameters.
     *
     * @param renderer The <code>ChartRenderer</code> instance used for rendering.
     * @param bundle   The <code>ResourceBundle</code> instance used for localization.
     * @param config   The <code>ReportConfig</code> instance used for controlling the graph rendering.
     * @throws IOException Thrown if the operation fails.
     */
    public void generateGraphs( ChartRenderer renderer, ResourceBundle bundle, ReportConfig config )
        throws IOException
    {
        for ( ChartSource chartSource : getSummaryChartSources() )
        {
            if ( chartSource.isEnabled( config ) )
            {
                JFreeChart chart = chartSource.getChart( bundle, config );
                String fileName = chartSource.getFileName( config );
                renderer.renderChart( fileName, chart );
            }
        }
        for ( Iterator iterator = detailsChartSources.values().iterator(); iterator.hasNext(); )
        {
            List sources = (List) iterator.next();
            for ( Iterator iterator2 = sources.iterator(); iterator2.hasNext(); )
            {
                ChartSource source = (ChartSource) iterator2.next();
                if ( source.isEnabled( config ) )
                {
                    JFreeChart chart = source.getChart( bundle, config );
                    String fileName = source.getFileName( config );
                    renderer.renderChart( fileName, chart );
                }
            }
        }
    }

    public List<ChartSource> getSummaryChartSources()
    {
        return summaryChartSources;
    }

    public List getDetailsChartSources( String testName )
    {
        return (List) detailsChartSources.get( testName );
    }
}
