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

import org.codehaus.mojo.chronos.common.IOUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

    private final SortedMap<String, ResponsetimeSampleGroup> sampleGroupsByName =
        new TreeMap<String, ResponsetimeSampleGroup>();

    public GroupedResponsetimeSamples()
    {

    }

    public GroupedResponsetimeSamples( Document doc )
    {
        String rootElementName = doc.getRootElement().getName();
        if ( !rootElementName.equals( GROUPEDRESPONSETIMESAMPLES_TAG ) )
        {
            String message = "Unexpected rootelement: " + rootElementName;
            throw new IllegalArgumentException( message );
        }

        Element xml = doc.getRootElement();
        succeeded = Integer.parseInt( xml.getAttributeValue( SUCCEEDED_ATTRIBUTE ) );

        List groups = xml.getChildren( GROUP_TAG );
        for ( Iterator iterator = groups.iterator(); iterator.hasNext(); )
        {
            Element gXml = (Element) iterator.next();
            ResponsetimeSampleGroup rsg = ResponsetimeSampleGroup.fromXml( gXml );
            addGroup( rsg );
        }

    }

    public void addAll( GroupedResponsetimeSamples other )
    {
        Iterator groupIt = other.getSampleGroups().iterator();
        while ( groupIt.hasNext() )
        {
            ResponsetimeSampleGroup group = (ResponsetimeSampleGroup) groupIt.next();
            addGroup( group );
        }
    }

    private void addGroup( ResponsetimeSampleGroup group )
    {
        String groupName = group.getName();
        getSampleGroup( groupName ).addAll( group );
        this.addAll( group );
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
     * @return a list of {@link org.codehaus.mojo.chronos.common.model.ResponsetimeSampleGroup} sorted by the name of the group
     */
    public List<ResponsetimeSampleGroup> getSampleGroups()
    {
        return new ArrayList<ResponsetimeSampleGroup>( sampleGroupsByName.values() );
    }

    public static GroupedResponsetimeSamples fromXmlFile( File file )
        throws JDOMException, IOException
    {
        IOUtil.copyDTDToDir( "chronos-responsetimesamples.dtd", file.getParentFile() );
        Document doc = new SAXBuilder().build( file );

        return new GroupedResponsetimeSamples( doc );
    }

}