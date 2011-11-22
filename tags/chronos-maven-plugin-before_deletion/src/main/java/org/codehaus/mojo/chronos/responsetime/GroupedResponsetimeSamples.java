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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A grouping collection of samples (grouped by the name of the samples).
 *
 * @author ksr@lakeside.dk
 */
public final class GroupedResponsetimeSamples
    extends ResponsetimeSamples
{
    private static final String GROUPEDRESPONSETIMESAMPLES_TAG = "groupedresponsetimesamples";

    private static final String SUCCEEDED_ATTRIBUTE = "succeeded";

    private static final String GROUP_TAG = "responsetimesamplegroup";

    private static final long serialVersionUID = 5054656881107118329L;

    private final SortedMap<String, ResponsetimeSampleGroup> sampleGroupsByName = new TreeMap<String, ResponsetimeSampleGroup>();

    public void addAll( GroupedResponsetimeSamples other )
    {
        for (ResponsetimeSampleGroup group : other.getSampleGroups())
        {
            addGroup( group );
        }
    }

    private void addGroup( ResponsetimeSampleGroup group )
    {
        String groupName = group.getName();
        getSampleGroup( groupName ).addAll( group );
        this.addAll( group );
    }

    /**
     * Add a sample (and group it).
     *
     * @param sampleName The name of the sample (individual testcase)
     * @param sample     A result from invocation of that sample.
     * @see ResponsetimeSamples#add(ResponsetimeSample)
     */
    public void put( String sampleName, ResponsetimeSample sample )
    {
        super.add( sample );
        getSampleGroup( sampleName ).add( sample );
    }

    private ResponsetimeSamples getSampleGroup( String sampleName )
    {
        ResponsetimeSampleGroup sampleGroup = sampleGroupsByName.get( sampleName );
        if ( sampleGroup == null )
        {
            sampleGroup = new ResponsetimeSampleGroup( sampleName );
            sampleGroupsByName.put( sampleName, sampleGroup );
        }
        return sampleGroup;
    }

    /**
     * @return a list of {@link ResponsetimeSampleGroup} sorted by the name of the group
     */
    public List<ResponsetimeSampleGroup> getSampleGroups()
    {
        return new ArrayList<ResponsetimeSampleGroup>( sampleGroupsByName.values() );
    }

    public Element toXML()
    {
        Element xml = new Element( GROUPEDRESPONSETIMESAMPLES_TAG );
        xml.setAttribute( SUCCEEDED_ATTRIBUTE, Integer.toString( succeeded ) );

        for ( ResponsetimeSampleGroup sampleGroup : sampleGroupsByName.values() )
        {
            xml.addContent( sampleGroup.toXML() );
        }

        return xml;
    }

    public static GroupedResponsetimeSamples fromXmlFile( File file )
        throws JDOMException, IOException
    {
        GroupedResponsetimeSamples grs = new GroupedResponsetimeSamples();
        if ( !file.exists() )
        {
            return grs;
        }

        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build( file );

        if ( !doc.getRootElement().getName().equals( GROUPEDRESPONSETIMESAMPLES_TAG ) )
        {
            String message = "Unexpected rootelement " + doc.getRootElement().getName();
            throw new IllegalArgumentException( message );
        }

        Element xml = doc.getRootElement();
        grs.succeeded = Integer.parseInt( xml.getAttributeValue( SUCCEEDED_ATTRIBUTE ) );

        List<Element> groups = xml.getChildren( GROUP_TAG );
        for ( Element gXml : groups )
        {
            ResponsetimeSampleGroup rsg = ResponsetimeSampleGroup.fromXml( gXml );
            grs.addGroup( rsg );
        }
        return grs;
    }
}