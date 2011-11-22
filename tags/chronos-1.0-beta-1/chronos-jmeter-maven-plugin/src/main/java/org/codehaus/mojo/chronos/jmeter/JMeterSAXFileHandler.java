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
package org.codehaus.mojo.chronos.jmeter;

import org.jdom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * SAXHandler for JMeter xml logs.
 *
 * @author ksr@lakeside.dk
 */
public final class JMeterSAXFileHandler
    extends DefaultHandler
{
    private static final String JUNIT_SAMPLER_20 = "org.apache.jmeter.protocol.java.sampler.JUnitSampler";

    private final Collector collector = new Collector();

    private Properties sampleAttributes;

    private boolean inProperty = false;

    private boolean insideSample = false;

    private StringBuffer testMethodNameSB = new StringBuffer();

    private Properties parentSampleAttributes;

    /**
     * @param uri        See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param localName  See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param qName      See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @param attributes See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @throws SAXException See {@link DefaultHandler#startElement(String, String, String, Attributes)}
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        if ( "sampleResult".equals( qName ) )
        {
            // jtl20
            Properties props = new Properties();
            for ( int i = 0; i < attributes.getLength(); i++ )
            {
                props.put( attributes.getQName( i ), attributes.getValue( i ) );
            }
            sampleAttributes = props;
            insideSample = true;
        }
        else if ( "property".equals( qName ) )
        {
            // jtl20
            // TODO be sure that log cannot contain other types of character
            // data under junitSamples properties
            inProperty = true;
        }
        else if ( "httpSample".equals( qName ) || "sample".equals( qName ) )
        {
            // jtl21

            if ( insideSample )
            {
                parentSampleAttributes = sampleAttributes;
            }

            Properties props = new Properties();
            for ( int i = 0; i < attributes.getLength(); i++ )
            {
                props.put( attributes.getQName( i ), attributes.getValue( i ) );
            }
            sampleAttributes = props;
            insideSample = true;
        }
    }

    /**
     * this method can be called multiple times in one element if there's enough chars.
     *
     * @param ch     See {@link DefaultHandler#characters(char[], int, int)}
     * @param start  See {@link DefaultHandler#characters(char[], int, int)}
     * @param length See {@link DefaultHandler#characters(char[], int, int)}
     * @see DefaultHandler#characters(char[], int, int)
     */
    public void characters( char[] ch, int start, int length )
    {
        if ( insideSample && inProperty )
        {
            testMethodNameSB.append( new String( ch, start, length ) );
        }
    }

    /**
     * @param uri       See {@link DefaultHandler#endElement(String, String, String)}
     * @param localName See {@link DefaultHandler#endElement(String, String, String)}
     * @param qName     See {@link DefaultHandler#endElement(String, String, String)}
     * @throws SAXException See {@link DefaultHandler#endElement(String, String, String)}
     * @see DefaultHandler#endElement(String, String, String)
     */
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( "property".equals( qName ) )
        {
            inProperty = false;
        }
        else if ( "sampleResult".equals( qName ) )
        {
            // jtl20
            String embeddedPropertyValue = testMethodNameSB.toString();
            collectJtl20(embeddedPropertyValue, sampleAttributes);
            reset();
        }
        else if ( "httpSample".equals( qName ) || "sample".equals( qName ) )
        {
            // jtl21
            if ( !insideSample )
            {
                sampleAttributes = parentSampleAttributes;
            }

            collectJtl21(sampleAttributes);
            reset();
        }
    }

    private void collectJtl20(String embeddedPropertyValue, final Properties sampleAttributes) {
        final String sampleName;
        // it seems like when generated from Jmeter 2.1, the label will always
        // contain the String
        // 'org.apache.jmeter.protocol.java.sampler.JUnitSampler'
        String label = sampleAttributes.getProperty( "label" );
        if ( JUNIT_SAMPLER_20.equals( label ) && !"".equals(embeddedPropertyValue) )
        {
            sampleName = embeddedPropertyValue;
        }
        else
        {
            sampleName = label;
        }
        int responsetime = Integer.parseInt( sampleAttributes.getProperty( "time" ) );
        long timestamp = Long.parseLong( sampleAttributes.getProperty( "timeStamp" ) );
        boolean success = "true".equals( sampleAttributes.getProperty( "success" ) );
        String threadId = sampleAttributes.getProperty( "threadName" ).intern();
        collector.collect(sampleName, responsetime, timestamp, success, threadId);
    }

    private void collectJtl21(final Properties sampleAttributes) {
        String sampleName = sampleAttributes.getProperty("lb");
        int responsetime = Integer.parseInt( sampleAttributes.getProperty( "t" ) );
        long timestamp = Long.parseLong( sampleAttributes.getProperty( "ts" ) );
        boolean success = "true".equals( sampleAttributes.getProperty( "s" ) );
        String threadId = sampleAttributes.getProperty( "tn" ).intern();
        collector.collect(sampleName, responsetime, timestamp, success, threadId);
    }


    private void reset() {
        testMethodNameSB.setLength( 0 );
        insideSample = false;
        sampleAttributes = null;
    }

    private static class Collector {
        private final SortedMap<String, Element> sampleGroupsByName = new TreeMap<String, Element>();
        private final Map<String, List<Element>> samplesByName = new HashMap<String, List<Element>>();
        private static int lastIndex = 0;

        /**
         * <code>int</code> representing the number of succeeded samples.
         */
        protected int succeeded;


        private void collect(String sampleName, int responsetime, long timestamp, boolean success, String threadId) {
            if ( success )
            {
                succeeded++;
            }
            Element groupElement = sampleGroupsByName.get(sampleName);
            if ( groupElement == null )
            {
                int currentIndex = ++lastIndex;
                groupElement = new Element( "responsetimesamplegroup" );
                groupElement.setAttribute( "name", sampleName);
                groupElement.setAttribute( "index", Integer.toString( currentIndex ) );
                sampleGroupsByName.put(sampleName, groupElement);
            }

            Element sampleElement = new Element( "sample" );
            sampleElement.setAttribute( "responsetime", Integer.toString( responsetime ) );
            sampleElement.setAttribute( "timestamp", Long.toString( timestamp ) );
            sampleElement.setAttribute( "success", Boolean.toString( success ) );
            sampleElement.setAttribute( "threadId", threadId );

            List<Element> sampleList = samplesByName.get(sampleName);
            if ( sampleList == null )
            {
                sampleList = new ArrayList<Element>();
                samplesByName.put(sampleName, sampleList );
            }
            sampleList.add(sampleElement);
        }

        /**
         * @return the generated samples obtained by parsing the logfile
         */
        Element getChronosXml() {
            Element xml = new Element("groupedresponsetimesamples");
            xml.setAttribute("succeeded", Integer.toString( succeeded ) );
            for ( String sampleName : sampleGroupsByName.keySet() )
            {
                final Element sampleGroupXml = sampleGroupsByName.get(sampleName);
                for ( Element sample : samplesByName.get(sampleName))
                {
                    sampleGroupXml.addContent( sample );
                }
                xml.addContent(sampleGroupXml);
            }
            return xml;
        }
    }


    /**
     * @return the generated samples obtained by parsing the logfile
     */
    Element getChronosXml() {
        return collector.getChronosXml();
    }
}
