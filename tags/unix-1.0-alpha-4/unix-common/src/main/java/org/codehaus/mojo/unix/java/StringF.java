package org.codehaus.mojo.unix.java;

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

import fj.*;
import fj.data.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StringF
{
    public static final F2<String, String, Boolean> equals = new F2<String, String, Boolean>()
    {
        public Boolean f( final String a, final String b )
        {
            return a.equals( b );
        }
    };

    public static final F<String, Boolean> isEmpty = new F<String, Boolean>()
    {
        public Boolean f( final String string )
        {
            return string.length() == 0;
        }
    };

    public static final F3<String, String, String, String> replaceAll = new F3<String, String, String, String>()
    {
        public String f( final String string, final String regex, final String replacement )
        {
            return string.replaceAll( regex, replacement );
        }
    };

    public static final F2<String, String, List<String>> split = new F2<String, String, List<String>>()
    {
        public List<String> f( final String string, final String regex )
        {
            return List.list( string.split( regex ) );
        }
    };

    public static final F2<String, String, Boolean> startsWith = new F2<String, String, Boolean>()
    {
        public Boolean f( final String string, final String startsWith )
        {
            return string.startsWith( startsWith );
        }
    };

    public static final F<String, String> trim = new F<String, String>()
    {
        public String f( final String a )
        {
            return a.trim();
        }
    };

    // -----------------------------------------------------------------------
    // Extra
    // -----------------------------------------------------------------------

    public static final F<String, F2<String, String, String>> joiner = new F<String, F2<String, String, String>>()
    {
        public F2<String, String, String> f( final String separator )
        {
            return new F2<String, String, String>()
            {
                public String f( String s, String o )
                {
                    return s + separator + o;
                }
            };
        }
    };
}
