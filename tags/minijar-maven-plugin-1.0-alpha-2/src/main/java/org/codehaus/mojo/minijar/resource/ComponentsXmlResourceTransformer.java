package org.codehaus.mojo.minijar.resource;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// 1. We want to process all resources that are common in all the JARs that we are processing.
// 2. At the end of processing we want to hand back the transformation of the resources.

// In my particular case I want to grab all the plexus components.xml files and aggregate them
// and then stick them in one place in the aggregated JAR.

/**
 * Components XML file filter.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class ComponentsXmlResourceTransformer
    implements ResourceTransformer
{
    private Map components = new LinkedHashMap();

    public static final String COMPONENTS_XML_PATH = "META-INF/plexus/components.xml";

    public boolean canTransformResource( String resource )
    {
        return COMPONENTS_XML_PATH.equals( resource );
    }

    public void processResource( InputStream is )
        throws IOException
    {
        // We can't just read the stream because the plexus dom builder closes the stream

        File f = File.createTempFile( "minijar-maven-plugin", "tmp" );

        f.deleteOnExit();
        
        String n = f.getAbsolutePath();

        OutputStream os = new FileOutputStream( f );

        IOUtil.copy( is, os );

        os.close();

        //

        Reader reader;

        Xpp3Dom newDom;

        try
        {
            reader = new FileReader( n );

            newDom = Xpp3DomBuilder.build( reader );
        }
        catch ( Exception e )
        {
            throw new IOException( "Error parsing components.xml in " + is );
        }

        Xpp3Dom[] children = newDom.getChild( "components" ).getChildren( "component" );

        for ( int i = 0; i < children.length; i++ )
        {
            Xpp3Dom component = children[i];

            String role = component.getChild( "role" ).getValue();

            Xpp3Dom child = component.getChild( "role-hint" );

            String roleHint = child != null ? child.getValue() : "";

            components.put( role + roleHint, component );
        }
    }

    public boolean hasTransformedResource()
    {
        return components.size() > 0;
    }

    public File getTransformedResource()
        throws IOException
    {
        File f = File.createTempFile( "minijar-maven-plugin-plx", "tmp" );

        f.deleteOnExit();

        FileWriter fileWriter = new FileWriter( f );
        try
        {
            Xpp3Dom dom = new Xpp3Dom( "component-set" );

            Xpp3Dom componentDom = new Xpp3Dom( "components" );

            dom.addChild( componentDom );

            for ( Iterator i = components.values().iterator(); i.hasNext(); )
            {
                Xpp3Dom component = (Xpp3Dom) i.next();
                componentDom.addChild( component );
            }

            Xpp3DomWriter.write( fileWriter, dom );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }

        return f;
    }
}
