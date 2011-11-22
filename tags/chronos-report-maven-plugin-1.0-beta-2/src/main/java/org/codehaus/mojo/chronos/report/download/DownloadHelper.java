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
  * $HeadURL$
  * $Id$
  */
package org.codehaus.mojo.chronos.report.download;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class used for downloading JMeter from a remote location.<br />
 * JMeter does not generate a POM file, and can therefore not be directly included in the POM of Chronos.<br />
 * As a helping feature - Chronos can now download JMeter, and automatically include it in its classpath.
 *
 * @author ads/ksr
 */
public class DownloadHelper
{
    private static final int BUFFER = 2048;

    private String remoteLocation;

    private String destination;

    private final Log log;

    /**
     * Constructor for the <code>DownloadHelper</code> class.
     *
     * @param remoteLocation The remote location to retrieve JMeter from
     * @param destination    The local destination
     * @param log            Current Maven log instance.
     */
    public DownloadHelper( String remoteLocation, String destination, Log log )
    {
        this.remoteLocation = remoteLocation;
        this.destination = destination;
        this.log = log;
    }

    /**
     * Initiate the download process.
     *
     * @throws IOException Thrown if an IO exception occures during download or unzip.
     */
    public final void downloadZipFile()
        throws IOException
    {
        log.info( "Downloading " + remoteLocation + " storing it inside " + destination );
        URL u = new URL( remoteLocation );
        URLConnection uc = u.openConnection();

        byte[] inData = getRemoteZipfile( uc );

        ZipInputStream zis = new ZipInputStream( new BufferedInputStream( new ByteArrayInputStream( inData ) ) );
        ZipEntry entry;
        while ( ( entry = zis.getNextEntry() ) != null )
        {
            writeOutputFile( zis, entry );
        }
        zis.close();
    }

    private byte[] getRemoteZipfile( URLConnection uc )
        throws IOException
    {
        int contentLength = uc.getContentLength();

        InputStream is = new BufferedInputStream( uc.getInputStream() );
        byte[] inData = new byte[contentLength];
        int offset = 0;
        while ( offset < contentLength )
        {
            final int bytesRead = is.read( inData, offset, inData.length - offset );
            if ( bytesRead == -1 )
            {
                break;
            }
            offset += bytesRead;
        }
        is.close();

        if ( offset != contentLength )
        {
            throw new IOException( "Only read " + offset + " bytes; Expected " + contentLength + " bytes" );
        }
        return inData;
    }

    private void writeOutputFile( ZipInputStream zis, ZipEntry entry )
        throws IOException
    {
        String destinationFileName = extractDestination( entry.getName() );

        File outputFile = new File( destination, destinationFileName );
        if ( entry.isDirectory() )
        {
            outputFile.mkdir();
        }
        else
        {
            log.debug( "Saving file " + outputFile );
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            final OutputStream fos = new BufferedOutputStream( new FileOutputStream( outputFile ), BUFFER );
            try
            {
                int length;
                byte[] outData = new byte[BUFFER];
                while ( ( length = zis.read( outData, 0, BUFFER ) ) != -1 )
                {
                    fos.write( outData, 0, length );
                }
                fos.flush();
            }
            finally
            {
                fos.close();
            }
        }
    }

    private String extractDestination( String fileName )
    {
        int firstSeparatorSlash = fileName.indexOf( '/' );
        int firstSeparatorBackslash = fileName.indexOf( '\\' );

        int index = Math.max( firstSeparatorSlash, firstSeparatorBackslash );
        String tmp = fileName.substring( 0, index );

        if ( tmp.contains( new File( destination ).getName() ) )
        {
            fileName = fileName.substring( index );
        }

        return fileName;
    }
}