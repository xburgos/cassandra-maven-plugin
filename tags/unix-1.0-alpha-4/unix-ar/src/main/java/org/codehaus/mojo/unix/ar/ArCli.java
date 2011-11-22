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

import java.io.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ArCli
{
    public static void main( String[] args )
        throws IOException
    {

        File file = new File( "/Users/trygvis/dev/org.codehaus.mojo/trunk/sandbox/deb-maven-plugin/bash_3.1dfsg-8_i386.deb" );

        ArReader reader = null;
        try
        {
            reader = Ar.read( file ) ;
            for (ArFile arFile : reader) {
                System.out.println("arFile.getName() = " + arFile.getName());
                System.out.println("arFile.getLastModified() = " + arFile.getLastModified());
                System.out.println("arFile.getOwnerId() = " + arFile.getOwnerId());
                System.out.println("arFile.getGroupId() = " + arFile.getGroupId());
                System.out.println("arFile.getMode() = " + arFile.getMode());
                System.out.println("arFile.getSize() = " + arFile.getSize());
            }
        }
        finally
        {
            ArUtil.close( reader );
        }
    }
}
