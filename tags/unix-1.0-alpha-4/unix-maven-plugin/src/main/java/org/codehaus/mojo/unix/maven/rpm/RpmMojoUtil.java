package org.codehaus.mojo.unix.maven.rpm;

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
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.maven.*;
import org.codehaus.plexus.util.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id: PackageRpmMojo.java 9221 2009-03-15 22:52:14Z trygvis $
 */
public class RpmMojoUtil
{
    public static final F2<RpmSpecificSettings, UnixPackage, UnixPackage> validateMojoSettingsAndApplyFormatSpecificSettingsToPackage = new F2<RpmSpecificSettings, UnixPackage, UnixPackage>()
    {
        public UnixPackage f( RpmSpecificSettings rpmSpecificSettings, UnixPackage unixPackage )
        {
            return validateMojoSettingsAndApplyFormatSpecificSettingsToPackage( rpmSpecificSettings, unixPackage );
        }
    };

    public static UnixPackage validateMojoSettingsAndApplyFormatSpecificSettingsToPackage( RpmSpecificSettings rpm,
                                                                                           UnixPackage unixPackage )
    {
        if ( rpm.getGroup().isNone() )
        {
            throw new MissingSettingException( "group" );
        }

        return RpmUnixPackage.cast( unixPackage ).
            group( rpm.getGroup().some() );
    }
}
