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

import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSampleGroup;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSamples;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plugin adding histogram charts to the reports.
 *
 * @author ksr@lakeside.dk
 */
public final class ChronosHistogramPlugin
    implements ChronosReportPlugin
{
    private ResponsetimeSamples samples;

    public ChronosHistogramPlugin( ResponsetimeSamples samples )
    {
        this.samples = samples;
    }

    public ChartSource getSummaryChartSource()
    {
        return new SummaryHistogramChartSource( samples );
    }

    public Map getDetailChartSources()
    {
        Map testname2ChartSource = new LinkedHashMap();
        if ( !( samples instanceof GroupedResponsetimeSamples ) )
        {
            return testname2ChartSource;
        }
        GroupedResponsetimeSamples groupedSamples = (GroupedResponsetimeSamples) samples;
        for ( Iterator it2 = groupedSamples.getSampleGroups().iterator(); it2.hasNext(); )
        {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup) it2.next();
            testname2ChartSource.put( sampleGroup.getName(), new DetailsHistogramChartSource( sampleGroup ) );
        }
        return testname2ChartSource;
    }
}
