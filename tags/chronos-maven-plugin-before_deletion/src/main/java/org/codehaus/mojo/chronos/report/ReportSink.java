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
package org.codehaus.mojo.chronos.report;

import org.codehaus.doxia.sink.Sink;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

/**
 * Utility calss to assist in report generation.
 *
 * @author ksr@lakeside.dk
 */
class ReportSink
{
    private ResourceBundle bundle;

    private Sink sink;

    public ReportSink( ResourceBundle bundle, Sink sink )
    {
        this.bundle = bundle;
        this.sink = sink;
    }

    void constructHeaderSection( String title, String description, String anchor )
    {
        if ( title != null )
        {
            sink.sectionTitle1();
            sinkAnchor( anchor );
            sink.text( title );
            sink.sectionTitle1_();
        }
        if ( description != null )
        {
            sink.rawText( description );
            sinkLineBreak();
        }
    }

    void metadataTable( String title, String anchor, Map metadata )
    {
        if ( !metadata.isEmpty() )
        {
            title2( title, anchor );

            sink.table();

            for ( Iterator entryIterator = metadata.entrySet().iterator(); entryIterator.hasNext(); )
            {
                Entry entry = (Entry) entryIterator.next();

                sink.tableRow();
                sink.tableCell();
                sink.rawText( (String) entry.getKey() );
                sink.tableCell_();
                sink.tableCell();
                sink.rawText( (String) entry.getValue() );
                sink.tableCell_();
                sink.tableRow_();
            }
            sink.table_();
        }
    }

    void title2( String text, String anchor )
    {
        sink.sectionTitle2();
        sinkAnchor( anchor );
        sink.text( text );
        sink.sectionTitle2_();
    }

    void title3( String text, String anchor )
    {
        sink.sectionTitle3();
        sinkAnchor( anchor );
        sink.text( text );
        sink.sectionTitle3_();

    }

    void graphics( String name )
    {
        try
        {
            sink.figure();
            String encodedName = URLEncoder.encode( name, "UTF-8" );
            sink.figureGraphics( "images" + File.separatorChar + encodedName );
            sink.figure_();
            sinkLineBreak();
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }
    }

    void sinkLineBreak()
    {
        sink.lineBreak();
        sink.lineBreak();
    }

    void table( List headerLabels, List dataLines )
    {
        sink.table();
        sink.tableRow();
        Iterator it = headerLabels.iterator();
        while ( it.hasNext() )
        {
            String headerLabel = (String) it.next();
            th( headerLabel );
        }
        sink.tableRow_();
        Iterator data = dataLines.iterator();
        while ( data.hasNext() )
        {
            Object next = data.next();
            List dataLine = (List) next;
            sink.tableRow();
            Iterator items = dataLine.iterator();
            while ( items.hasNext() )
            {
                String item = (String) items.next();
                sinkCell( item );
            }
            sink.tableRow_();
        }
        sink.table_();
        sinkLineBreak();
    }

    void th( String key )
    {
        sink.tableHeaderCell();
        sink.text( bundle.getString( key ) );
        sink.tableHeaderCell_();
    }

    void sinkCell( String text )
    {
        sink.tableCell();
        sink.text( text );
        sink.tableCell_();
    }

    void sinkLink( String text, String link )
    {
        sink.rawText( "[" );
        sink.link( "#" + link );
        sink.text( text );
        sink.link_();
        sink.rawText( "]" );
    }

    void sinkCellLink( String text, String link )
    {
        sink.tableCell();
        sink.link( link );
        sink.text( text );
        sink.link_();
        sink.tableCell_();
    }

    void sinkAnchor( String anchor )
    {
        sink.anchor( anchor );
        sink.anchor_();
    }
}
