package org.codehaus.mojo.unix.ar;

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

import junit.framework.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArWriterTest
    extends TestCase
{
    private static File yo = new File( System.getProperty( "basedir" ), "target/yo.deb" );
    private static File yo2 = new File( System.getProperty( "basedir" ), "target/yo2.deb" );
    private static File hello = new File( System.getProperty( "basedir" ), "target/hello.txt" );

    public static void writeFiles()
        throws Exception
    {
        if ( yo.exists() )
        {
            assertTrue( yo.delete() );
        }
        if ( yo2.exists() )
        {
            assertTrue( yo2.delete() );
        }
        if ( hello.exists() )
        {
            assertTrue( hello.delete() );
        }

        Writer writer = new OutputStreamWriter( new FileOutputStream( hello ), "UTF-16" );
        writer.write( "Hello World!" );
        writer.close();
    }

    public ArWriterTest()
        throws Exception
    {
        writeFiles();
    }

    public void test1()
        throws Exception
    {
        ArWriter writer = new ArWriter( yo );
        writer.close();

        assertTrue( yo.canRead() );
        assertEquals( 8, yo.length() );
    }

    public void test2()
        throws Exception
    {
        ArWriter writer = new ArWriter( yo );
        writer.close();

        ArFile yoFile = new ArFile();
        yoFile.file = yo;
        yoFile.name = yo.getName();

        ArFile helloFile = new ArFile();
        helloFile.file = hello;
        helloFile.name = hello.getName();

        writer = new ArWriter( yo2 );
        writer.add( yoFile );
        writer.add( helloFile );
        writer.close();

        assertTrue( yo2.canRead() );
        int expectedSize =
            8 +      // ar magic
                60 + 8 + // entry header + data
                60 + 26;
        assertEquals( expectedSize, yo2.length() );
    }
}
