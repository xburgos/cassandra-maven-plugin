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

import org.codehaus.mojo.chronos.common.model.ResponsetimeSamples;
import org.codehaus.mojo.chronos.report.ReportConfig;
import org.jfree.chart.JFreeChart;

import java.util.ResourceBundle;

/**
 * This class is responsible for generating responsetime charts (aggregated for all tests).
 *
 * @author ksr@lakeside.dk
 */
public final class SummaryResponsetimeChartSource
    extends ResponseChartGenerator
    implements ChartSource
{
    private ResponsetimeSamples samples;

    public SummaryResponsetimeChartSource( ResponsetimeSamples samples )
    {
        this.samples = samples;
    }

    public JFreeChart getChart( ResourceBundle bundle, ReportConfig config )
    {
        return createResponseChart( bundle.getString( "chronos.label.testcases" ), samples, bundle, config );
    }

    public String getFileName( ReportConfig config )
    {
        return "response-summary-" + config.getId();
    }

    public boolean isEnabled( ReportConfig config )
    {
        return config.isShowsummary() && config.isShowresponse() && !config.isShowdetails();
    }

}
