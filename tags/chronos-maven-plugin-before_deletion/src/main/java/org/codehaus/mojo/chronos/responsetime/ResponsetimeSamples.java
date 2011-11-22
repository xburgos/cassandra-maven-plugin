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
package org.codehaus.mojo.chronos.responsetime;

import org.apache.commons.math.stat.StatUtils;
import org.codehaus.mojo.chronos.ReportMojo;
import org.codehaus.mojo.chronos.Utils;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Contains info from a jmeter jtl file.
 *
 * @author ksr@lakeside.dk
 */
public abstract class ResponsetimeSamples
    implements Serializable
{

    private static final int PERCENTILE_95 = 95;
    private static final int PERCENTILE_99 = 99;

    private static final long serialVersionUID = 4056724466498233661L;

    /**
     * <code>List</code> containing loaded samples.
     */
    protected final List samples = new ArrayList();

    /**
     * <code>int</code> representing the number of succeeded samples.
     */
    protected int succeeded;

    /**
     * Add all <code>ResponsetimeSample</code> instances from the supplied <code>ResponsetimeSamples</code> object to
     * <code>this</code> instance.
     *
     * @param other The <code>ResponsetimeSamples</code> to add from.
     */
    public final void addAll( ResponsetimeSamples other )
    {
        Iterator it = other.samples.iterator();
        while ( it.hasNext() )
        {
            ResponsetimeSample sample = (ResponsetimeSample) it.next();
            add( sample );
        }
    }

    /**
     * add a (hopefully successful) sample.
     *
     * @param sample <code>JMeterSample</code> to add.
     */
    public final void add( ResponsetimeSample sample )
    {
        samples.add( sample );
        if ( sample.isSuccess() )
        {
            succeeded++;
        }
    }

    /**
     * Retrieve the number of samples contained by this <code>ResponsetimeSample</code> instance.
     *
     * @return the number of samples
     */
    public final int size()
    {
        return samples.size();
    }

    /**
     * Retrieve the success rate of the contained <code>ResponsetimeSample</code> instances.
     *
     * @return the successrate (in percentage)
     */
    public final double getSuccessrate()
    {
        return 100 * ( (double) getSucceeded() ) / samples.size();
    }

    /**
     * Retrieve the number of failed <code>ResponsetimeSample</code> instances.
     *
     * @return the number of failed samples
     */
    public final int getFailed()
    {
        return samples.size() - getSucceeded();
    }

    /**
     * Retrieve the number of succeeded <code>ResponsetimeSample</code> instances.
     *
     * @return the number of succeeded samples
     */
    public final int getSucceeded()
    {
        return succeeded;
    }

    /**
     * Retrieve average response time of the <code>ResponsetimeSample</code> instances.
     *
     * @return the average responsetime of all samples
     */
    public final double getAverage()
    {
        return StatUtils.mean( extractResponsetimes() );
    }

    /**
     * Retrieve the minimum response time of the <code>ResponsetimeSample</code> instances.
     *
     * @return the minimum responsetime of all samples
     */
    public final double getMin()
    {
        return StatUtils.min( extractResponsetimes() );
    }

    /**
     * Retrieve the maximum response time of the <code>ResponsetimeSample</code> instances.
     *
     * @return the maximum responsetime of all samples
     */
    public final double getMax()
    {
        return StatUtils.max( extractResponsetimes() );
    }

    /**
     * Calculate the 95 % fractile of the <code>ResponsetimeSample</code> instances.
     *
     * @return the 95% fractile of all responsetimes
     */
    public final double getPercentile95()
    {
        return StatUtils.percentile( extractResponsetimes(), PERCENTILE_95 );
    }

    /**
     * Calculate the 99 % fractile of the <code>ResponsetimeSample</code> instances.
     *
     * @return the 99% fractile of all responsetimes
     */
    public final double getPercentile99()
    {
        return StatUtils.percentile( extractResponsetimes(), PERCENTILE_99 );
    }

    /**
     * Note that the samples is ordered by timestamp, so the first one has the lowest timestamp.
     *
     * @return the first timestamp of all samples
     */
    public final long getFirstTimestamp()
    {
        if ( samples.isEmpty() )
        {
            return System.currentTimeMillis();
        }
        ResponsetimeSample responsetimeSample = (ResponsetimeSample) samples.get( 0 );
        return responsetimeSample.getTimestamp();
    }

    /**
     * Extracts all responsetimes.
     * <p/>
     * See {@link ReportMojo#responsetimedivider}
     *
     * @return the responsetimes as an array
     */
    public final double[] extractResponsetimes()
    {
        double[] responsetimes = new double[samples.size()];
        int i = 0;
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            ResponsetimeSample sample = (ResponsetimeSample) it.next();
            responsetimes[i++] = sample.getResponsetime();
        }
        return responsetimes;
    }

    public final void appendResponsetimes( TimeSeries series )
    {
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            ResponsetimeSample sample = (ResponsetimeSample) it.next();
            long delta = getFirstTimestamp();
            Millisecond timestamp = Utils.createMS( sample.getTimestamp() - delta );
            double responseTime = sample.getResponsetime();
            series.addOrUpdate( timestamp, responseTime );
        }
    }

    public final void appendThreadCounts( TimeSeries series, long threadCountDuration )
    {
        if ( samples.size() > 0 )
        {
            long firstSerial = getFirstTimestamp();
            Map activeThreads = new HashMap();
            for ( Iterator it = samples.iterator(); it.hasNext(); )
            {
                ResponsetimeSample sample = (ResponsetimeSample) it.next();
                int threadCount = activeThreads.size();
                for ( Iterator it2 = activeThreads.keySet().iterator(); it2.hasNext(); )
                {
                    String key = (String) it2.next();
                    if ( sample.getTimestamp()
                        > ( (ResponsetimeSample) activeThreads.get( key ) ).getTimestamp() + threadCountDuration )
                    {
                        it2.remove();
                    }
                }
                activeThreads.put( sample.getThreadId(), sample );

                if ( threadCount != activeThreads.size() )
                {
                    series.addOrUpdate( Utils.createMS( sample.getTimestamp() - firstSerial ), threadCount );
                    series.addOrUpdate( Utils.createMS( sample.getTimestamp() - firstSerial + 1 ),
                                        activeThreads.size() );
                }
            }
            ResponsetimeSample sample = (ResponsetimeSample) samples.get( samples.size() - 1 );
            series.addOrUpdate( Utils.createMS( sample.getTimestamp() - firstSerial + 1 ), activeThreads.size() );
        }
    }

    public final TimeSeries createMovingThroughput( String name )
    {
        TimeSeries series = new TimeSeries( name, Millisecond.class );
        if ( samples.isEmpty() )
        {
            return series;
        }

        Collections.sort( samples, new Comparator()
        {
            public int compare( Object arg1, Object arg2 )
            {
                ResponsetimeSample sample1 = (ResponsetimeSample) arg1;
                ResponsetimeSample sample2 = (ResponsetimeSample) arg2;
                long endtime1 = sample1.getTimestamp() + (long) sample1.getResponsetime();
                long endtime2 = sample2.getTimestamp() + (long) sample2.getResponsetime();
                return (int) ( endtime1 - endtime2 );
            }
        } );

        int periodLength = 1000;
        long rampUpTime = 0;
        int measurements = 0;
        final long firstAllowedTimestamp = getFirstTimestamp() + rampUpTime;
        long periodStart = firstAllowedTimestamp;
        long periodEnd = periodStart + periodLength;
        for ( int i = 0; i < samples.size(); i++ )
        {
            ResponsetimeSample sample = (ResponsetimeSample) samples.get( i );
            long sampleEndTime = sample.getTimestamp() + sample.getResponsetime();
            if ( sampleEndTime < periodStart )
            {
                continue;
            }
            if ( sampleEndTime <= periodEnd )
            {
                measurements++;
            }
            else
            {
                if ( measurements > 0 )
                {
                    series.addOrUpdate( Utils.createMS( periodEnd - firstAllowedTimestamp ), measurements );
                }
                else
                {
                    series.addOrUpdate( Utils.createMS( periodEnd - firstAllowedTimestamp ), null );
                }
                measurements = 1;
                periodStart = periodEnd;
                periodEnd = periodStart + periodLength;
            }
        }
        return series;
    }

    /**
     * Retrieve the maximum average throughput of the <code>ResponsetimeSample</code> instances.
     *
     * @return the maximum of a moving average of the throughput
     */
    public final double getMaxAverageThroughput( int averageduration )
    {
        TimeSeries series = createMovingThroughput( "" );
        TimeSeries averageseries = MovingAverage.createMovingAverage( series, "", averageduration, 0 );
        double max = 0;
        for ( Iterator it = averageseries.getItems().iterator(); it.hasNext(); )
        {
            TimeSeriesDataItem item = (TimeSeriesDataItem) it.next();
            if ( item.getValue() != null )
            {
                max = Math.max( max, item.getValue().doubleValue() );
            }
        }
        return max;
    }

    /**
     * Retrieve the total execution time of the <code>ResponsetimeSample</code> instances.
     *
     * @return Total execution time in milliseconds.
     */
    public final long getTotalTime()
    {
        ResponsetimeSample first = (ResponsetimeSample) samples.get( 0 );
        ResponsetimeSample last = (ResponsetimeSample) samples.get( samples.size() - 1 );
        return last.getTimestamp() + last.getResponsetime() - first.getTimestamp();
    }
}