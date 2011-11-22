/*
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
  *
  * Copyright (c) to all contributors
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
  *
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/responsetime/ResponsetimeSamples.java $
  * $Id: ResponsetimeSamples.java 14459 2011-08-12 13:41:52Z soelvpil $
  */
package org.codehaus.mojo.chronos.common;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class IOUtil
{

    public static void copyDTDToDir( String dtdFileName, File targetDir )
        throws IOException
    {
        InputStream dtdInput = IOUtil.class.getResourceAsStream( dtdFileName );
        File dtdTmp = new File( targetDir, dtdFileName );
        try
        {
            copyContent( dtdInput, dtdTmp );
        }
        finally
        {
            dtdInput.close();
        }
    }

    public static void copyContent( InputStream in, File output )
        throws IOException
    {
        OutputStream out = new FileOutputStream( output );
        try
        {
            copy( in, out );
        }
        finally
        {
            out.close();
        }
    }

    public static void copy( InputStream in, OutputStream out )
        throws IOException
    {
        final byte[] buf = new byte[1024];
        int len;
        while ( ( len = in.read( buf ) ) > 0 )
        {
            out.write( buf, 0, len );
        }
        out.flush();
    }

    /**
     * Call this method to ensure the existence of the supplied directory.
     *
     * @param directory The directory.
     * @return The supplied <code>File</code> object.
     */
    public static File ensureDir( File directory )
    {
        if ( !directory.exists() )
        {
            ensureDir( directory.getParentFile() );
            directory.mkdir();
        }
        return directory;
    }

    public static void writeXmlToFile( File targetFile, Element xml, DocType doctype )
        throws IOException
    {
        final File targetDir = targetFile.getParentFile();
        ensureDir( targetDir );
        Document doc = new Document( xml );
        doc.setDocType( doctype );
        copyDTDToDir( doctype.getSystemID(), targetDir );

        Format format = Format.getCompactFormat();

        OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream( targetFile ), "UTF-8" );
        new XMLOutputter( format ).output( doc, writer );
        writer.flush();
        writer.close();
    }

    /**
     * Retrieve the name file name part of the file excluding any file extension (ex: .doc, .jmx, etc.).
     *
     * @param fileName The file name.
     * @return The truncated file name.
     */
    public static String removeExtension( String fileName )
    {
        final int separatorIndex = fileName.lastIndexOf( '.' );
        if ( separatorIndex == -1 )
        {
            return fileName;
        }
        return fileName.substring( 0, separatorIndex );
    }

    /**
     * Creates a new file name based on the supplied input.<br />
     * The file name is like using the following pattern:<br />
     * <i>prefix</i><b>&lt;original filename&gt;</b>.<i>extension</i>
     *
     * @param shortName The currrent name of the file.
     * @param prefix    The text to be prefixed the current name - note: this may not be <code>null</code>.
     * @param extension The new extension of the file - note: this may not be <code>null</code>.
     * @return The generated file name.
     */
    public static String getAdjustedFileName( String shortName, String prefix, String extension )
    {
        return new StringBuffer().append( prefix ).append( '-' ).append( shortName ).append( '.' ).append(
            extension ).toString();
    }

    /**
     * Retrieve all files with the specified extension in the specified directory.
     *
     * @param dir       The directory to scan.
     * @param extension The extension to retrieve.
     * @return <code>File[]</code> containing the matching files - or <code>null</code> if the directory is invalid.
     */
    public static File[] listFilesWithExtension( File dir, final String extension )
    {
        final FilenameFilter filenameFilter = new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.endsWith( "." + extension );
            }
        };
        return listFiles( dir, filenameFilter );
    }

    public static File[] listFiles( File dir, FilenameFilter filenameFilter )
    {
        if ( !dir.exists() )
        {
            throw new IllegalArgumentException(
                "Unknown directory " + dir + " Maybe the performancetests have not yet been run" );
        }
        if ( !dir.isDirectory() )
        {
            throw new IllegalArgumentException(
                dir + " is not a directory. This may be caused by a configuration error." );
        }
        return dir.listFiles( filenameFilter );
    }

    public static void copyFile( File input, File output )
        throws IOException
    {
        if ( output.exists() )
        {
            output.delete();
        }
        InputStream in = new FileInputStream( input );
        try
        {
            copyContent( in, output );
        }
        finally
        {
            in.close();
        }
    }
}
