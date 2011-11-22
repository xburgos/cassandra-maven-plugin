package org.codehaus.mojo.unix.maven;

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

import fj.data.*;
import static fj.data.Option.*;
import org.apache.commons.vfs.*;
import org.apache.maven.plugin.*;
import org.codehaus.mojo.unix.*;
import org.codehaus.mojo.unix.core.*;
import org.codehaus.mojo.unix.util.*;
import static org.codehaus.mojo.unix.util.RelativePath.*;

import static java.util.Arrays.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SetAttributes
    extends AssemblyOp
{
    private RelativePath basedir = RelativePath.BASE;

    private List<String> includes = Collections.emptyList();

    private List<String> excludes = Collections.emptyList();

    private Option<MojoFileAttributes> fileAttributes = none();

    private Option<MojoFileAttributes> directoryAttributes = none();

    public SetAttributes()
    {
        super( "set-attributes" );
    }

    public void setBasedir( String basedir )
    {
        this.basedir = relativePath( basedir );
    }

    public void setIncludes( String[] includes )
    {
        this.includes = asList( includes );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = asList( excludes );
    }

    public void setFileAttributes( MojoFileAttributes fileAttributes )
    {
        this.fileAttributes = fromNull( fileAttributes );
    }

    public void setDirectoryAttributes( MojoFileAttributes directoryAttributes )
    {
        this.directoryAttributes = fromNull( directoryAttributes );
    }

    public AssemblyOperation createOperation( FileObject basedir, FileAttributes defaultFileAttributes,
                                              FileAttributes defaultDirectoryAttributes )
        throws MojoFailureException, FileSystemException
    {
        return new SetAttributesOperation( this.basedir, includes, excludes,
            fileAttributes.map( MojoFileAttributes.create_ ),
            directoryAttributes.map( MojoFileAttributes.create_ ) );
    }
}
