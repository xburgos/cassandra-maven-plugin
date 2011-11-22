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

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LineStreamUtil
{
    public static Iterator<String> prefix( final Iterator<String> lines, final String prefix )
    {
        return new Iterator<String>()
        {
            public boolean hasNext()
            {
                return lines.hasNext();
            }

            public String next()
            {
                return prefix + lines.next();
            }

            public void remove()
            {
                lines.remove();
            }
        };
    }

    public static Iterable<String> prefix( final Iterable<String> lines, final String prefix )
    {
        return new Iterable<String>()
        {
            public Iterator<String> iterator()
            {
                return prefix( lines.iterator(), prefix );
            }
        };
    }

    // -----------------------------------------------------------------------
    // LineProducer
    // -----------------------------------------------------------------------

    public static Iterator<String> toIterator( LineProducer lineProducer )
    {
        LineFile lines = new LineFile();
        lineProducer.streamTo( lines );
        return lines.iterator();
    }

    public static Iterator<String> toIterator( Iterable<LineProducer> lineProducers )
    {
        return toIterator( lineProducers.iterator() );
    }

    public static Iterator<String> toIterator( final Iterator<LineProducer> lineProducers )
    {
        if ( lineProducers == null || !lineProducers.hasNext() )
        {
            return Collections.<String>emptyList().iterator();
        }

        return new Iterator<String>()
        {
            Iterator<String> lines;

            public boolean hasNext()
            {
                return lines != null || lineProducers.hasNext();
            }

            public String next()
            {
                if ( lines == null )
                {
                    LineFile file = new LineFile();
                    lineProducers.next().streamTo( file );
                    lines = file.iterator();
                }

                return lines.next();
            }

            public void remove()
            {
                throw new RuntimeException( "Not implemented" );
            }
        };
    }

    public static void toFile( LineProducer lineProducer, File file )
        throws IOException
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter( file );
            LineWriterWriter writer = new LineWriterWriter( fileWriter );
            lineProducer.streamTo( writer );
            writer.close();
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    public static String toString( LineProducer lineProducer )
    {
        LineFile lines = new LineFile();
        lineProducer.streamTo( lines );
        return lines.toString();
    }
}
