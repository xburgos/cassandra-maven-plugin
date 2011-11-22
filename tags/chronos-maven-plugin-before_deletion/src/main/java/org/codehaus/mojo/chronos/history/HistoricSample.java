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
package org.codehaus.mojo.chronos.history;

import org.codehaus.mojo.chronos.gc.GCSamples;
import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSampleGroup;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is a historic sample representing the statistics from a previous run.
 *
 * @author ksr@lakeside.dk
 */
public class HistoricSample
    implements Serializable
{
    private static final int DEFAULT_DURATION = 20000;

    private static final long serialVersionUID = 8492792243093456318L;

    private long timestamp;

    private double gcRatio = -1d;

    private double collectedPrSecond = -1d;

    private double responsetimeAverage = -1d;

    private double responsetime95Percentile = -1d;

    private HashMap individualPercentiles;

    private HashMap individualAverages;

    private double maxAverageThroughput = -1d;

    public HistoricSample( GroupedResponsetimeSamples responseSamples, GCSamples gcSamples )
    {
        timestamp = responseSamples.getFirstTimestamp();
        responsetimeAverage = responseSamples.getAverage();
        responsetime95Percentile = responseSamples.getPercentile95();
        individualAverages = new HashMap();
        individualPercentiles = new HashMap();
        Iterator it = responseSamples.getSampleGroups().iterator();
        while ( it.hasNext() )
        {
            ResponsetimeSampleGroup group = (ResponsetimeSampleGroup) it.next();
            individualAverages.put( group.getName(), new Double( group.getAverage() ) );
            individualPercentiles.put( group.getName(), new Double( group.getPercentile95() ) );
        }
        if ( gcSamples != null )
        {
            gcRatio = gcSamples.getGarbageCollectionRatio( responseSamples.getTotalTime() );
            collectedPrSecond = gcSamples.getCollectedKBPerSecond( responseSamples.getTotalTime() );
        }
        int averageDuration = Math.max( DEFAULT_DURATION, (int) responsetime95Percentile );
        maxAverageThroughput = responseSamples.getMaxAverageThroughput( averageDuration );
    }

    private HistoricSample()
    {
        // Do nothing
    }

    /**
     * @return Returns the timestamp.
     */
    public final long getTimestamp()
    {
        return timestamp;
    }

    /**
     * @return Returns the gcRatio.
     */
    public final double getGcRatio()
    {
        return gcRatio;
    }

    /**
     * @return Returns the collectedPrSecond.
     */
    public final double getCollectedPrSecond()
    {
        return collectedPrSecond;
    }

    /**
     * @return Returns the responsetimeAverage.
     */
    public final double getResponsetimeAverage()
    {
        return responsetimeAverage;
    }

    /**
     * @return Returns the responsetime95Percrntile.
     */
    public final double getResponsetime95Percentile()
    {
        return responsetime95Percentile;
    }

    public final Set getGroupNames()
    {
        return individualAverages.keySet();
    }

    public final double getResponsetimeAverage( String groupName )
    {
        return ( (Double) individualAverages.get( groupName ) ).doubleValue();
    }

    public final double getResponsetimePercentiles( String groupName )
    {
        return ( (Double) individualPercentiles.get( groupName ) ).doubleValue();
    }

    /**
     * @return Returns the maxAverageThroughput.
     */
    public final double getMaxAverageThroughput()
    {
        return maxAverageThroughput;
    }

    public final Element toXML()
    {
        Element historyXML = new Element( "history" );

        historyXML.setAttribute( "timestamp", Long.toString( timestamp ) );
        historyXML.setAttribute( "gcRatio", Double.toString( gcRatio ) );
        historyXML.setAttribute( "collectedPrSecond", Double.toString( collectedPrSecond ) );

        historyXML.setAttribute( "responsetimeAverage", Double.toString( responsetimeAverage ) );
        historyXML.setAttribute( "responsetime95Percentile", Double.toString( responsetime95Percentile ) );
        historyXML.setAttribute( "maxAverageThroughput", Double.toString( maxAverageThroughput ) );

        Element individualPercentilesXML = new Element( "individualPercentiles" );
        for ( Iterator iterator = individualPercentiles.entrySet().iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            individualPercentilesXML.addContent(
                new Element( "entry" ).setAttribute( "key", (String) entry.getKey() ).setAttribute( "value",
                                                                                                    ( (Double) entry.getValue() ).toString() ) );
        }
        historyXML.addContent( individualPercentilesXML );

        Element individualAveragesXML = new Element( "individualAverages" );
        for ( Iterator iterator = individualAverages.entrySet().iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            individualAveragesXML.addContent(
                new Element( "entry" ).setAttribute( "key", (String) entry.getKey() ).setAttribute( "value",
                                                                                                    ( (Double) entry.getValue() ).toString() ) );
        }
        historyXML.addContent( individualAveragesXML );

        return historyXML;
    }

    public static HistoricSample fromXML( File file )
        throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );

        Element historyXML = document.getRootElement();
        if ( !"history".equals( historyXML.getName() ) )
        {
            throw new JDOMException( "Invalid XML structure - history tag expected, but was " + historyXML.getName() );
        }

        HistoricSample hs = new HistoricSample();
        hs.timestamp = Long.parseLong( historyXML.getAttributeValue( "timestamp" ) );
        hs.gcRatio = Double.parseDouble( historyXML.getAttributeValue( "gcRatio" ) );
        hs.collectedPrSecond = Double.parseDouble( historyXML.getAttributeValue( "collectedPrSecond" ) );

        hs.responsetimeAverage = Double.parseDouble( historyXML.getAttributeValue( "responsetimeAverage" ) );
        hs.responsetime95Percentile = Double.parseDouble( historyXML.getAttributeValue( "responsetime95Percentile" ) );
        hs.maxAverageThroughput = Double.parseDouble( historyXML.getAttributeValue( "maxAverageThroughput" ) );

        hs.individualPercentiles = populateMap( historyXML.getChild( "individualPercentiles" ) );
        hs.individualAverages = populateMap( historyXML.getChild( "individualAverages" ) );

        return hs;
    }

    private static HashMap populateMap( Element xml )
    {
        HashMap res = new HashMap();

        for ( Iterator iterator = xml.getChildren().iterator(); iterator.hasNext(); )
        {
            Element entryXML = (Element) iterator.next();

            res.put( entryXML.getAttributeValue( "key" ), Double.valueOf( entryXML.getAttributeValue( "value" ) ) );
        }
        return res;
    }
}
