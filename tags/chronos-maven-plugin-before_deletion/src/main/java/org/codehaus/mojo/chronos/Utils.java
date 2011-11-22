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
package org.codehaus.mojo.chronos;

import org.codehaus.mojo.chronos.gc.GCSamples;
import org.codehaus.mojo.chronos.history.HistoricSample;
import org.codehaus.mojo.chronos.history.HistoricSamples;
import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jfree.data.time.Millisecond;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class primarily for handling files.
 */
public class Utils
{

    /**
     * Prefix for GC files.
     */
    /* pp */static final String GC_FILE_PREFIX = "gc-";

    /**
     * Extension for XML files.
     */
    /* pp */static final String XML_FILE_EXTENSION = "xml";

    /**
     * Extension for legacy serialized data files.
     */
    /* pp */static final String SERIALIZED_FILE_EXTENSION = ".ser";

    /**
     * Prefix for history sample files.
     */
    /* pp */static final String HISTORYSAMPLE_FILE_PREFIX = "history-";

    /**
     * Prefix for performance sample files.
     */
    /* pp */static final String PERFORMANCESAMPLE_FILE_PREFIX = "perf-";

    private static final int IGNORED_YEAR = 1970;

    /**
     * Converst the specified number of milli seconds into a <code>Millisecond</code> object.
     *
     * @param millisecond The time represented in milli seconds.
     * @return The corresponding <code>Millisecond</code> instance.
     */
    public static Millisecond createMS( long millisecond )
    {
        return new Millisecond( (int) millisecond, 0, 0, 0, 1, 1, IGNORED_YEAR );
    }

    /**
     * Retrieve the <code>ResourceBundle</code> for the specified <code>Locale</code>.
     *
     * @param locale The <code>Locale</code>
     * @return The associated <code>ResourceBundle</code>.
     */
    public static final ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "chronos", locale, Utils.class.getClassLoader() );
    }

    /**
     * Retrieve the XML file containing the GC samples for the specified id.
     *
     * @param baseDir The base directory of the build.
     * @param id      The id of the JMeter test.
     * @return <code>File</code> pointing on the XML file.
     */
    public static File getGcSamplesXml( File baseDir, String id )
    {
        File chronosDir = getChronosDir( baseDir );
        return new File( new File( chronosDir, "gc" ), GC_FILE_PREFIX + id + XML_FILE_EXTENSION );
    }

    /**
     * Read the saved <code>GSSamples</code> with the specified id.<br />
     * The method is backwards compatible, the method will look for previous .ser files - if found then the old files
     * will be loaded.<br />
     *
     * @param baseDir The base directory of the build.
     * @param dataId  The id of the JMeter test.
     * @return The corresponding <code>GCSamples</code> instance.
     * @throws IOException   Thrown if loading the contents fails.
     * @throws JDOMException Thrown if parsing the contents fails.
     */
    public static GCSamples readGCSamples( File baseDir, String dataId )
        throws IOException, JDOMException
    {
        File gcSer = UtilsLegacy.getGcSamplesSer( baseDir, dataId );
        if ( gcSer.exists() )
        {
            GCSamples gcSamples = (GCSamples) Utils.readObject( gcSer );
            return gcSamples;
        }

        GCSamples samples = new GCSamples();
        File gcDir = getGCSamplesDir( baseDir );
        File[] gcFiles = gcDir.listFiles();
        if ( gcFiles != null )
        {
            Arrays.sort( gcFiles );
            for ( int i = 0; i < gcFiles.length; i++ )
            {
                if ( gcFiles[i].isFile() )
                {
                    if ( gcFiles[i].getName().startsWith( GC_FILE_PREFIX ) )
                    {
                        if ( gcFiles[i].getName().endsWith( XML_FILE_EXTENSION ) )
                        {
                            GCSamples tmp = GCSamples.fromXML( gcFiles[i] );
                            samples.addAll( tmp );
                        }
                    }
                }
            }
        }
        return samples;
    }

    /**
     * Read the saved <code>HistoricSamples</code><br />
     * The method is backwards compatible, the method will look for previous .ser files - if found then the old files
     * will be loaded.<br />
     *
     * @param dataDirectory The base directory of the build.
     * @return The corresponding <code>HistoricSamples</code> instance.
     * @throws IOException   Thrown if loading the contents fails.
     * @throws JDOMException Thrown if parsing the contents fails.
     */
    public static HistoricSamples readHistorySamples( File dataDirectory )
        throws IOException, JDOMException
    {
        HistoricSamples samples = new HistoricSamples();

        File[] historyFiles = dataDirectory.listFiles();
        if ( historyFiles != null )
        {
            Arrays.sort( historyFiles );
            for ( int i = 0; i < historyFiles.length; i++ )
            {
                if ( historyFiles[i].isFile() )
                {
                    if ( historyFiles[i].getName().startsWith( HISTORYSAMPLE_FILE_PREFIX ) )
                    {
                        HistoricSample sample;
                        if ( historyFiles[i].getName().endsWith( SERIALIZED_FILE_EXTENSION ) )
                        {
                            sample = (HistoricSample) Utils.readObject( historyFiles[i] );
                            samples.addHistoricSample( sample );
                        }
                        else if ( historyFiles[i].getName().endsWith( XML_FILE_EXTENSION ) )
                        {
                            sample = HistoricSample.fromXML( historyFiles[i] );
                            samples.addHistoricSample( sample );
                        }
                    }
                }
            }
        }

        return samples;
    }

    /**
     * Writes the <code>GCSamples</code> to an XML file.<br />
     * Once the file has been persisted - any old .ser files are removed.
     *
     * @param samples The <code>GCSamples</code> to store.
     * @param id      The id of the JMeter test.
     * @return <code>File</code> pointing on the created XML file.
     * @throws IOException Thrown if writing data to the file fails.
     */
    public static File writeGCSamples( GCSamples samples, String id )
        throws IOException
    {
        File file = Utils.getGcSamplesXml( new File( "." ), id );
        writeXmlToFile( file, samples.toXML(), new DocType( "gcsamples", "SYSTEM", "chronos-gc.dtd" ) );

        // Delete old serialized samples file if present.
        File gcSer = UtilsLegacy.getGcSamplesSer( new File( "." ), id );
        if ( gcSer.exists() )
        {
            gcSer.delete();
        }

        return file;
    }

    /**
     * Writes the <code>HistoricSample</code> to an XML file.<br />
     *
     * @param history       The <code>GCSamples</code> to store.
     * @param dataDirectory The directory to store the history in.
     * @throws IOException Thrown if writing data to the file fails.
     */
    public static void writeHistorySample( HistoricSample history, File dataDirectory )
        throws IOException
    {
        String fileName = HISTORYSAMPLE_FILE_PREFIX + history.getTimestamp() + XML_FILE_EXTENSION;
        File historyFile = new File( dataDirectory, fileName );
        if ( historyFile.exists() )
        {
            historyFile.delete();
        }

        writeXmlToFile( historyFile, history.toXML(),
                        new DocType( "historysamples", "SYSTEM", "chronos-history.dtd" ) );
    }

    /**
     * Writes the <code>GroupedResponsetimeSamples</code> to an XML file.<br />
     * Once the file has been persisted - any old .ser files are removed.
     *
     * @param samples The <code>GroupedResponsetimeSamples</code> to store.
     * @param dataId  The dataId of the JMeter test.
     * @param jtlName The name of the jtl file.
     * @return <code>File</code> pointing on the created XML file.
     * @throws IOException Thrown if writing data to the file fails.
     */
    public static File writeResponsetimeSamples( GroupedResponsetimeSamples samples, String dataId, String jtlName )
        throws IOException
    {
        File dataDir = getOrCreateDataDir( new File( "." ), dataId );
        File performanceSamplesXml =
            new File( dataDir, getAdjustedFileName( jtlName, PERFORMANCESAMPLE_FILE_PREFIX, XML_FILE_EXTENSION ) );
        final DocType docType = new DocType( "responsetimesamples", "SYSTEM", "chronos-responsetimesamples.dtd" );
        writeXmlToFile( performanceSamplesXml, samples.toXML(), docType );

        File psSer = UtilsLegacy.getPerformanceSamplesSer( new File( "." ), dataId );
        if ( psSer.exists() )
        {
            psSer.delete();
        }
        return performanceSamplesXml;
    }

    /**
     * Read the saved <code>GroupedResponsetimeSamples</code> with the specified id.<br />
     * The method is backwards compatible, the method will look for previous .ser files - if found then the old files
     * will be loaded.<br />
     *
     * @param dataDirectory The base directory of the build.
     * @param dataId        The id of the JMeter test.
     * @return The corresponding <code>GroupedResponsetimeSamples</code> instance.
     * @throws IOException   Thrown if loading the contents fails.
     * @throws JDOMException Thrown if parsing the contents fails.
     */
    public static GroupedResponsetimeSamples readResponsetimeSamples( File dataDirectory, String dataId )
        throws IOException, JDOMException
    {
        File psSer = UtilsLegacy.getPerformanceSamplesSer( dataDirectory, dataId );
        if ( psSer.exists() )
        {
            GroupedResponsetimeSamples samples = (GroupedResponsetimeSamples) Utils.readObject( psSer );
            return samples;
        }

        File chronosDir = getChronosDir( new File( "." ) );
        File dir = new File( chronosDir, dataId );
        GroupedResponsetimeSamples result = new GroupedResponsetimeSamples();
        File[] dirContent = listFilesWithExtension( dir, "xml" );
        for ( int i = 0; i < dirContent.length; i++ )
        {
            result.addAll( GroupedResponsetimeSamples.fromXmlFile( dirContent[i] ) );
        }
        return result;
    }

    private static File getGCSamplesDir( File baseDir )
    {
        File chronosDir = getChronosDir( baseDir );
        return new File( chronosDir, "gc" );
    }

    private static Serializable readObject( File ser )
        throws IOException
    {
        ObjectInputStream input = new ObjectInputStream( new FileInputStream( ser ) );
        try
        {
            return (Serializable) input.readObject();
        }
        catch ( ClassNotFoundException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            input.close();
        }
    }

    private static void writeXmlToFile( File file, Element xml, DocType doctype )
        throws IOException
    {
        Document doc = new Document( xml );
        doc.setDocType( doctype );
        ensureDir( file.getParentFile() );

        if ( doctype != null )
        {
            // Ensure DTD
            InputStream dtdIs = Utils.class.getResourceAsStream( doctype.getSystemID() );
            File dtd = new File( file.getParentFile(), doctype.getSystemID() );
            writeTextFile( dtd, dtdIs );
        }

        Format format = Format.getCompactFormat();

        OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
        new XMLOutputter( format ).output( doc, writer );
        writer.flush();
        writer.close();
    }

    private static void writeTextFile( File file, InputStream is )
        throws IOException
    {
        OutputStream out = new FileOutputStream( file );
        byte[] buf = new byte[1024];
        int len;
        while ( ( len = is.read( buf ) ) > 0 )
        {
            out.write( buf, 0, len );
        }
        out.flush();
        out.close();
        is.close();
    }

    /**
     * Retrieve the data directory for the specified <code>dataId</code>.<br />
     * If the directory does not exist - it will be created.
     *
     * @param baseDir The base directory of the project.
     * @param dataId  The id of the data sequence.
     * @return The data directory.
     */
    public static File getOrCreateDataDir( File baseDir, String dataId )
    {
        File chronosDir = getChronosDir( baseDir );
        File dataDir = new File( chronosDir, dataId );
        return ensureDir( dataDir );
    }

    /**
     * Based on the specied base directory - return the chronos directory of the running build.<br />
     * If the chronos directory does not exits - it will be created.
     *
     * @param baseDir The base directory of the build.
     * @return <code>File</code> pointing on the chronos directory.
     */
    public static File getChronosDir( File baseDir )
    {
        File target = new File( baseDir, "target" );
        File chronos = new File( target, "chronos" );
        return ensureDir( chronos );
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

    /**
     * Creates a new file name based on the supplied input.<br />
     * The file name is like using the following pattern:<br />
     * <i>prefix</i><b>&lt;original filename&gt;</b>.<i>extension</i>
     *
     * @param name      The currrent name of the file.
     * @param prefix    The text to be prefixed the current name - note: this may not be <code>null</code>.
     * @param extension The new extension of the file - note: this may not be <code>null</code>.
     * @return The generated file name.
     */
    public static String getAdjustedFileName( String name, String prefix, String extension )
    {
        return new StringBuffer().append( prefix ).append( removeExtension( name ) ).append( '.' ).append(
            extension ).toString();
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
     * Retrieve all files with the specified extension in the specified directory.
     *
     * @param dir       The directory to scane.
     * @param extension The extension to retrieve.
     * @return <code>File[]</code> containing the matching files - or <code>null</code> if the directory is invalid.
     */
    public static File[] listFilesWithExtension( File dir, final String extension )
    {
        if ( !dir.exists() )
        {
            throw new IllegalArgumentException( "Unknown directory " + dir + " Maybe the performancetests have not yet been run");
        }
        if ( !dir.isDirectory() )
        {
            throw new IllegalArgumentException( dir + " is not a directory. This may be caused by a configuration error." );
        }
        File[] dirContent = dir.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.endsWith( "." + extension );
            }
        } );
        return dirContent;
    }
}