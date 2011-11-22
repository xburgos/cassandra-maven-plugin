package org.codehaus.mojo.unix.util.line;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
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
 */

import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

public abstract class AbstractLineStreamWriter
    implements LineStreamWriter
{
    public static final String EOL = System.getProperty( "line.separator" );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected abstract void onLine( String line );

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public LineStreamWriter add()
    {
        onLine( null );
        return this;
    }

    public LineStreamWriter add( String line )
    {
        onLine( line );
        return this;
    }

    public LineStreamWriter addIfNotEmpty( String value )
    {
        if ( StringUtils.isNotEmpty( value ) )
        {
            onLine( value );
        }
        return this;
    }

    public LineStreamWriter add( LineProducer lineProducer )
    {
        lineProducer.streamTo( this );
        return this;
    }

    public LineStreamWriter addIfNotEmpty( String field, String value )
    {
        if ( StringUtils.isNotEmpty( value ) )
        {
            onLine( field + " " + value );
        }
        return this;
    }

    public LineStreamWriter addIfNotNull( File file )
    {
        if ( file != null )
        {
            onLine( file.getName() );
        }
        return this;
    }

    public LineStreamWriter addIf( boolean flag, String value )
    {
        if ( flag )
        {
            onLine( value );
        }
        return this;
    }

    public LineStreamWriter addAllLines( Iterator<String> lines )
    {
        if ( lines != null )
        {
            while ( lines.hasNext() )
            {
                add( (String) ((Iterator) lines).next() );
            }
        }
        return this;
    }

    public LineStreamWriter addAllLines( Iterable<String> lines )
    {
        if ( lines != null )
        {
            return addAllLines( lines.iterator() );
        }
        return this;
    }
}
