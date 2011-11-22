package org.codehaus.mojo.unix.core;

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

import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFsObject.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;
import static org.codehaus.mojo.unix.util.line.LineStreamUtil.*;
import org.codehaus.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 */
public class CreateDirectoriesOperation
    extends AssemblyOperation
{
    private String[] paths;

    private FileAttributes attributes;

    public CreateDirectoriesOperation( String[] paths, FileAttributes attributes )
    {
        this.paths = paths;
        this.attributes = attributes;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        for ( String path : paths )
        {
            fileCollector.addDirectory( directory( relativePath( path ), new LocalDateTime(), attributes ) );
        }
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Create directories:" ).
            add( " Paths: " ).
            addAllLines( prefix( Arrays.asList( paths ), "  " ) ).
            add( " Attributes: " + attributes );
    }
}
