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
package org.codehaus.mojo.chronos.common.model;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holder to handle historic results and calculate statistics.
 *
 * @author ksr@lakeside.dk
 */
public final class HistoricSamples
{

    private Set groupNames = new LinkedHashSet();

    private List samples = new ArrayList();

    public void addHistoricSample( HistoricSample sample )
    {
        samples.add( sample );
        groupNames.addAll( sample.getGroupNames() );
    }

    public String[] getGroupNames()
    {
        return (String[]) groupNames.toArray( new String[groupNames.size()] );
    }

    public TimeSeries getAverageTime( String name )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getResponsetimeAverage();
            }

            public boolean accept( HistoricSample sample )
            {
                return true;
            }
        } );
    }

    public TimeSeries getAverageTime( String name, final String groupName )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getResponsetimeAverage( groupName );
            }

            public boolean accept( HistoricSample sample )
            {
                return sample.getGroupNames().contains( groupName );
            }
        } );
    }

    public TimeSeries getpercentile95( String name )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getResponsetime95Percentile();
            }

            public boolean accept( HistoricSample sample )
            {
                return true;
            }
        } );
    }

    public TimeSeries getPercentile95( String name, final String groupName )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getResponsetimePercentiles( groupName );
            }

            public boolean accept( HistoricSample sample )
            {
                return sample.getGroupNames().contains( groupName );
            }
        } );
    }

    public TimeSeries getThroughput( String name )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getMaxAverageThroughput();
            }

            public boolean accept( HistoricSample sample )
            {
                return true;
            }
        } );
    }

    public TimeSeries getGcRatio( String name )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getGcRatio();
            }

            public boolean accept( HistoricSample sample )
            {
                return true;
            }
        } );
    }

    public TimeSeries getKbCollectedPrSecond( String name )
    {
        return visitAll( name, new HistoricSampleExtractor()
        {
            public double extract( HistoricSample sample )
            {
                return sample.getCollectedPrSecond();
            }

            public boolean accept( HistoricSample sample )
            {
                return true;
            }
        } );
    }

    private TimeSeries visitAll( String name, HistoricSampleExtractor visitor )
    {
        TimeSeries series = new TimeSeries( name );
        Iterator it = samples.iterator();
        while ( it.hasNext() )
        {
            HistoricSample sample = (HistoricSample) it.next();
            if ( visitor.accept( sample ) )
            {
                Millisecond timestamp = new Millisecond( new Date( sample.getTimestamp() ) );
                double value = visitor.extract( sample );
                series.addOrUpdate( timestamp, value );
            }
        }
        return series;
    }

    /**
     * Base interface for extracting statistics from historic results.
     *
     * @author kent (creator)
     * @author $LastChangedBy$ $LastChangedDate$
     * @version $Revision$
     */
    interface HistoricSampleExtractor
    {
        double extract( HistoricSample sample );

        boolean accept( HistoricSample sample );
    }
}
