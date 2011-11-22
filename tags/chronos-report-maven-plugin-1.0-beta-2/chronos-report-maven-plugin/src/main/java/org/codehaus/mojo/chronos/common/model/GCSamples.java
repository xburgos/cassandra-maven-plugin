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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Container for {@link GCSample}.
 *
 * @author ksr@lakeside.dk
 */
public class GCSamples
    implements Serializable
{

    private final List samples = new ArrayList();

    /**
     * adds a {@link GCSample} to the list.
     *
     * @param sample a {@link GCSample}
     */
    public final void add( GCSample sample )
    {
        samples.add( sample );
    }

    /**
     * @return the number of samples
     */
    public final int getSampleCount()
    {
        return samples.size();
    }

    public final double getTimeStampForSampleAt( int index )
    {
        return ( (GCSample) samples.get( index ) ).getTimestamp();
    }

    public final void extractHeapBefore( TimeSeries heapBeforeSeries )
    {
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            heapBeforeSeries.addOrUpdate( getTimestamp( sample ), sample.getHeapBefore() );
        }
    }

    public final void extractHeapAfter( TimeSeries heapAfterSeries )
    {
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            heapAfterSeries.addOrUpdate( getTimestamp( sample ), sample.getHeapAfter() );
        }
    }

    public final void extractHeapTotal( TimeSeries heapTotalSeries )
    {
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            heapTotalSeries.addOrUpdate( getTimestamp( sample ), sample.getHeapTotal() );
        }
    }

    public final void extractProcessingTime( TimeSeries series )
    {
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            series.addOrUpdate( getTimestamp( sample ), sample.getProcessingTime() );
        }
    }

    public final double getGarbageCollectionRatio( long totalTime )
    {
        double totalProcessing = 0.0d;
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            totalProcessing += sample.getProcessingTime();
        }
        return totalProcessing / totalTime;
    }

    public final double getCollectedKBPerSecond( long totalTime )
    {
        double totalCollected = 0.0d;
        for ( Iterator it = samples.iterator(); it.hasNext(); )
        {
            GCSample sample = (GCSample) it.next();
            totalCollected += ( sample.getHeapBefore() - sample.getHeapAfter() );
        }
        return ( totalCollected / 1000 ) / totalTime;
    }

    private Millisecond getTimestamp( GCSample sample )
    {
        int milliseconds = (int) ( sample.getTimestamp() * 1000 );
        return ModelUtil.createMillis( milliseconds );
    }

    public void addAll( GCSamples tmp )
    {
        this.samples.addAll( tmp.samples );
    }

    public static GCSamples fromXML( File file )
        throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );

        GCSamples gcsamples = new GCSamples();
        List gcSampleXMLs = document.getRootElement().getChildren();
        for ( Iterator iterator = gcSampleXMLs.iterator(); iterator.hasNext(); )
        {
            gcsamples.add( GCSample.fromXML( (Element) iterator.next() ) );
        }
        return gcsamples;
    }
}