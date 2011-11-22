package org.codehaus.mojo.unix;

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

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnixFileModeTest
    extends TestCase
{
    public void testConstants()
    {
        testConstant( UnixFileMode._0644, "rw-r--r--", "644" );
        testConstant( UnixFileMode._0755, "rwxr-xr-x", "755" );
    }

    public void testParsing()
    {
        assertEquals( "0001", UnixFileMode.fromString( "--------x" ).toOctalString() );
        assertEquals( "0544", UnixFileMode.fromString( "r-xr--r--" ).toOctalString() );
        assertEquals( "0755", UnixFileMode.fromString( "rwxr-xr-x" ).toOctalString() );
    }

    private void testConstant( UnixFileMode unixFileMode, String string, String octalString )
    {
        assertEquals( "unixFileMode.toInt()", octalString, Integer.toString( unixFileMode.toInt(), 8 ) );
        assertEquals( "unixFileMode.toString().length()", string.length(), unixFileMode.toString().length() );
        assertEquals( "unixFileMode.toString()", string, unixFileMode.toString() );

        assertEquals( "UnixFileMode.fromString(string).toOctalString()", "0" + octalString, UnixFileMode.fromString( string ).toOctalString() );
    }
}
