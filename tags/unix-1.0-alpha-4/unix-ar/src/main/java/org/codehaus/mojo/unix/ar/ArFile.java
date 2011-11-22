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
public class ArFile
{
    protected File file;
    protected String name;
    protected long lastModified;
    protected int ownerId;
    protected int groupId;
    protected int mode;
    protected long size;

    public String getName()
    {
        return name;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public int getOwnerId()
    {
        return ownerId;
    }

    public int getGroupId()
    {
        return groupId;
    }

    public int getMode()
    {
        return mode;
    }

    public long getSize()
    {
        return size;
    }

    public static ArFile fromFile( File file )
    {
        if ( file == null )
        {
            throw new NullPointerException( "file" );
        }
        ArFile arFile = new ArFile();
        arFile.file = file;
        if ( arFile.name == null )
        {
            arFile.name = file.getName();
        }
        arFile.mode = 420; // 664
        arFile.lastModified = file.lastModified() / 1000;
        arFile.size = file.length();

        if ( arFile.name.length() > 16 )
        {
            throw new FileNameTooLongException();
        }

        return arFile;
    }
}
