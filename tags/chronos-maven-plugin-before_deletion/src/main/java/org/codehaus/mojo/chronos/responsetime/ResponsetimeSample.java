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

import org.jdom.Element;

import java.io.Serializable;

/**
 * Contains info from a jmeter logentry.
 *
 * @author ksr@lakeside.dk
 */
public class ResponsetimeSample implements Serializable
{
    private final int responsetime;

    private final long timestamp;

    private final boolean success;

    private final String threadId;


    public ResponsetimeSample( int responsetime, long timestamp, boolean success, String threadId )
    {
        this.responsetime = responsetime;
        this.timestamp = timestamp;
        this.success = success;
        this.threadId = threadId;
    }

    /**
     * @return Returns the responsetime.
     * @see ResponsetimeSample#getResponsetime()
     */
    public final int getResponsetime()
    {
        return responsetime;
    }

    /**
     * @return Returns the timestamp.
     * @see ResponsetimeSample#getTimestamp()
     */
    public final long getTimestamp()
    {
        return timestamp;
    }

    /**
     * @return Returns the success.
     * @see ResponsetimeSample#isSuccess()
     */
    public final boolean isSuccess()
    {
        return success;
    }

    /**
     * @return Returns the threadgroupId.
     * @see ResponsetimeSample#getThreadId()
     */
    public final String getThreadId()
    {
        return threadId;
    }


    /**
     * Returns a XML representation of the <code>ResponsetimeSample</code>.
     *
     * @return XML representation.
     */
    public final Element toXML()
    {
        Element xml = new Element( "sample" );
        xml.setAttribute( "responsetime", Integer.toString( responsetime ) );
        xml.setAttribute( "timestamp", Long.toString( timestamp ) );
        xml.setAttribute( "success", Boolean.toString( success ) );
        xml.setAttribute( "threadId", threadId );
        return xml;
    }


    /**
     * Transforms the xml into a <code>ResponsetimeSample</code> entity.
     *
     * @param xml The xml to parseJtl20.
     * @return The corresponding <code>ResponsetimeSample</code> instance.
     */
    /**
     * Transforms the xml into a <code>Jtl21Sample</code> entity.
     *
     * @param xml The xml to parseJtl20.
     * @return The corresponding <code>Jtl21Sample</code> instance.
     */
    public static ResponsetimeSample fromXml(Element xml) {
        int responsetime = Integer.parseInt( xml.getAttributeValue( "responsetime" ) );
        long timestamp = Long.parseLong( xml.getAttributeValue( "timestamp" ) );
        boolean success = Boolean.parseBoolean( xml.getAttributeValue( "success" ) );
        String threadId = xml.getAttributeValue( "threadId" );

        return new ResponsetimeSample( responsetime, timestamp, success, threadId );
    }
}