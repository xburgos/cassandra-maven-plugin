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

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Representation of the datadirectory for a single performancetest.
 */
public class TestDataDirectory
{
    private File dataDirectory;

    /**
     * Prefix for GC files.
     */
    /* pp */ public static final String GC_FILE_PREFIX = "gc";

    /**
     * Extension for XML files.
     */
    /* pp */ public static final String XML_FILE_EXTENSION = "xml";

    private static final String PERFORMANCESAMPLE_FILE_PREFIX = "perf";

    private String name;
    private Log log;

    TestDataDirectory(File chronosDir, String dataId, Log log)
    {
        this.dataDirectory = new File( chronosDir, dataId );
        this.name = dataId;
        this.log = log;
    }

    public String getDataId()
    {
        return name;
    }

    /**
     * Read the saved <code>GSSamples</code> with the specified id.<br />
     *
     * @return The corresponding <code>GCSamples</code> instance.
     * @throws java.io.IOException    Thrown if loading the contents fails.
     * @throws org.jdom.JDOMException Thrown if parsing the contents fails.
     */
    public GCSamples readGCSamples()
        throws JDOMException, IOException
    {
        File[] gcFiles = listFilesWith( GC_FILE_PREFIX, XML_FILE_EXTENSION );

        GCSamples samples = new GCSamples();
        if ( gcFiles != null )
        {
            for ( File gcFile : gcFiles )
            {
                GCSamples tmp = GCSamples.fromXML( gcFile );
                samples.addAll( tmp );
            }
        }
        return samples;
    }

    /**
     * Read the saved <code>GroupedResponsetimeSamples</code> with the specified id.<br />
     *
     * @return The corresponding <code>GroupedResponsetimeSamples</code> instance.
     * @throws IOException   Thrown if loading the contents fails.
     * @throws JDOMException Thrown if parsing the contents fails.
     */
    public GroupedResponsetimeSamples readResponsetimeSamples()
        throws JDOMException, IOException
    {
        File[] dirContent = listFilesWith( PERFORMANCESAMPLE_FILE_PREFIX, XML_FILE_EXTENSION );
        GroupedResponsetimeSamples result = new GroupedResponsetimeSamples();
        for ( File file : dirContent )
        {
            result.addAll( GroupedResponsetimeSamples.fromXmlFile( file ) );
        }
        return result;
    }

    /**
     * Retrieve all files with the specified prefix and extension in the specified directory.
     * This means we are looking of files in the form prefix-foo.extension
     *
     * @param prefix    The extension to retrieve.
     * @param extension The extension to retrieve.
     * @return <code>File[]</code> containing the matching files - or <code>null</code> if the directory is invalid.
     */
    public File[] listFilesWith( final String prefix, final String extension )
    {
        final FilenameFilter filenameFilter = new FilenameFilter()
        {
            public boolean accept( File parentDir, String name )
            {
                return name.startsWith( prefix + "-" ) && name.endsWith( "." + extension );
            }
        };
        return IOUtil.listFiles( dataDirectory, filenameFilter );
    }

    public void writeResponsetimeSamples( String jtlName, Element samplesElement )
        throws IOException
    {
        ensure();
        File performanceSamplesXml = new File( dataDirectory,
                                               IOUtil.getAdjustedFileName( jtlName, PERFORMANCESAMPLE_FILE_PREFIX,
                                                                           XML_FILE_EXTENSION ) );
        final DocType docType = new DocType( "responsetimesamples", "SYSTEM", "chronos-responsetimesamples.dtd" );
        IOUtil.writeXmlToFile( performanceSamplesXml, samplesElement, docType );
    }

    public void writeGCLog( Element gcSamplesXml )
        throws IOException
    {
        ensure();

        File file = new File( dataDirectory, GC_FILE_PREFIX + "-" + name + '.' + XML_FILE_EXTENSION );
        log.debug("Writing garbage collection log to " + file);
        IOUtil.writeXmlToFile( file, gcSamplesXml, new DocType( "gcsamples", "SYSTEM", "chronos-gc.dtd" ) );
    }

    public TestDataDirectory ensure()
    {
        IOUtil.ensureDir( dataDirectory );
        return this;
    }

    public File getDirectory()
    {
        return dataDirectory;
    }

}
