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

import org.jdom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * @author ksr@lakeside.dk
 */
public class ResponsetimeSampleGroup
    extends ResponsetimeSamples
{
    private static int lastIndex = 0;

    private final String name;

    private final int index;

    public ResponsetimeSampleGroup( String name )
    {
        this( name, ++lastIndex );
    }

    private ResponsetimeSampleGroup( String name, int index )
    {
        this.name = name;
        this.index = index;

        if ( lastIndex < this.index )
        {
            lastIndex = this.index;
        }
    }

    public final int getIndex()
    {
        return index;
    }

    /**
     * @return the name of this samplegroup
     */
    public final String getName()
    {
        return name;
    }

    public static ResponsetimeSampleGroup fromXml( Element xml )
    {
        if ( !xml.getName().equals( "responsetimesamplegroup" ) )
        {
            throw new IllegalArgumentException( "Unknown tag: " + xml.getName() );
        }

        String name = xml.getAttributeValue( "name" );
        int index = Integer.parseInt( xml.getAttributeValue( "index" ) );

        List samples = xml.getChildren();
        final ResponsetimeSampleGroup result = new ResponsetimeSampleGroup( name, index );
        for ( Iterator iterator = samples.iterator(); iterator.hasNext(); )
        {
            Element sXml = (Element) iterator.next();
            result.add( ResponsetimeSample.fromXml( sXml ) );
        }
        return result;
    }
}
