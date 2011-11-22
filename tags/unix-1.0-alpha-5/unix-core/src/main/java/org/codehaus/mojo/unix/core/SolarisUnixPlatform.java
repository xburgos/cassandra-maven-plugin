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

import static fj.data.Option.*;
import org.codehaus.mojo.unix.*;
import static org.codehaus.mojo.unix.UnixFileMode.*;

public class SolarisUnixPlatform
    implements UnixPlatform
{
    private static final FileAttributes defaultFileAttributes =
        new FileAttributes( some( "nobody" ), some( "nogroup" ), some( _0644 ) );

    private static final FileAttributes defaultDirectoryAttributes = defaultFileAttributes.mode( _0755 );

    public FileAttributes getDefaultFileAttributes()
    {
        return defaultFileAttributes;
    }

    public FileAttributes getDefaultDirectoryAttributes()
    {
        return defaultDirectoryAttributes;
    }
}
