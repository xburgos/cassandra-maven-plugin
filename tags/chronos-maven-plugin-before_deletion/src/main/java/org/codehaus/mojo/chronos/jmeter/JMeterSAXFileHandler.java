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

import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSample;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * SAXHandler for JMeter xml logs.
 *
 * @author ksr@lakeside.dk
 */
public final class JMeterSAXFileHandler
    extends DefaultHandler
{
    private final GroupedResponsetimeSamples samples = new GroupedResponsetimeSamples();

    private Properties sampleAttributes;

    private boolean inProperty = false;

    private boolean insideSample = false;

    private StringBuffer testMethodNameSB = new StringBuffer();

    private Properties parentSampleAttributes;
    private static final String JUNIT_SAMPLER_20 = "org.apache.jmeter.protocol.java.sampler.JUnitSampler";

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
            String result;
            // it seems like when generated from Jmeter 2.1, the label will always
            // contain the String
            // 'org.apache.jmeter.protocol.java.sampler.JUnitSampler'
            String label = sampleAttributes.getProperty( "label" );
            if ( JUNIT_SAMPLER_20.equals( label ) && !"".equals(embeddedPropertyValue) )
            {
                result = embeddedPropertyValue;
            }
            else
            {
                result = label;
            }
            String sampleName = result;
            ResponsetimeSample sample = parseJtl20( sampleAttributes );
            samples.put( sampleName, sample );
            testMethodNameSB.setLength( 0 );
            insideSample = false;
            sampleAttributes = null;
        }
        else if ( "httpSample".equals( qName ) || "sample".equals( qName ) )
        {
            // jtl21
            if ( !insideSample )
            {
                sampleAttributes = parentSampleAttributes;
            }

            String sampleName = sampleAttributes.getProperty("lb");
            ResponsetimeSample sample = parseJtl21( sampleAttributes );
            samples.put( sampleName, sample );
            testMethodNameSB.setLength( 0 );
            sampleAttributes = null;
            insideSample = false;
        }
    }

    /**
     * @return the generated samples obtained by parsing the logfile
     */
    public GroupedResponsetimeSamples getJMeterSamples()
    {
        return samples;
    }

    /**
     * @param attributes
     */
    private static ResponsetimeSample parseJtl20( Properties attributes )
    {
        int responsetime = Integer.parseInt( attributes.getProperty( "time" ) );
        long timestamp = Long.parseLong( attributes.getProperty( "timeStamp" ) );
        boolean success = "true".equals( attributes.getProperty( "success" ) );
        String threadId = attributes.getProperty( "threadName" ).intern();
        return new ResponsetimeSample(responsetime, timestamp, success, threadId);
    }

    /**
     * @param attributes the attributes of t he sample element
     */
    private static ResponsetimeSample parseJtl21( Properties attributes )
    {
        int responsetime = Integer.parseInt( attributes.getProperty( "t" ) );
        long timestamp = Long.parseLong( attributes.getProperty( "ts" ) );
        boolean success = "true".equals( attributes.getProperty( "s" ) );
        String threadId = attributes.getProperty( "tn" ).intern();
        return new ResponsetimeSample(responsetime, timestamp, success, threadId);
    }
}
