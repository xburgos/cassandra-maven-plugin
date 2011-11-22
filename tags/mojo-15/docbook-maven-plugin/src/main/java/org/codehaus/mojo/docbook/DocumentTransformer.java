/* 
 * maven-docbook-plugin - Copyright (C) 2005 OPEN input - http://www.openinput.com/
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
 * $Id$
 */
package org.codehaus.mojo.docbook;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author jgonzalez
 */
public class DocumentTransformer
{
    protected Log log;

    protected File sourceDirectory;

    protected File resourceDirectory;

    protected File databaseDirectory;

    protected File outputDirectory;

    protected URI stylesheetLocation;

    /**
     * @param log
     * @param sourceDirectory
     * @param outputDirectory
     */
    public DocumentTransformer( Log log, File sourceDirectory, File resourceDirectory, File databaseDirectory,
                               File outputDirectory, URI stylesheetLocation )
    {
        this.log = log;
        this.sourceDirectory = sourceDirectory;
        this.resourceDirectory = resourceDirectory;
        this.databaseDirectory = databaseDirectory;
        this.outputDirectory = outputDirectory;
        this.stylesheetLocation = stylesheetLocation;
    }

    public void transform()
    {
        StaleSourceScanner scanner = new StaleSourceScanner( 0, Collections.singleton( "**/*.xml" ),
                                                             Collections.EMPTY_SET );
        scanner.addSourceMapping( new SuffixMapping( ".xml", ".html" ) );
        Set staleDocbookFiles;
        try
        {
            staleDocbookFiles = scanner.getIncludedSources( this.sourceDirectory, this.outputDirectory );
        }
        catch ( InclusionScanException e )
        {
            throw new RuntimeException( "Error scanning sources in " + sourceDirectory, e );
        }

        if ( staleDocbookFiles.size() > 0 )
        {
            DirectoryScanner docbookScanner = new DirectoryScanner();
            docbookScanner.setBasedir( this.sourceDirectory );
            docbookScanner.setFollowSymlinks( true );
            docbookScanner.setIncludes( new String[] { "**/*.xml" } );
            docbookScanner.scan();
            String[] docbookFiles = docbookScanner.getIncludedFiles();

            this.prepareFileSystem( docbookFiles );
            this.transformDocuments( staleDocbookFiles );
        }
        else
        {
            this.log.info( "Generated docbook files up to date" );
        }

        if ( this.resourceDirectory.exists() )
        {
            try
            {
                FileUtils.copyDirectoryStructure( this.resourceDirectory, this.outputDirectory );
            }
            catch ( IOException e )
            {
                throw new RuntimeException( "Unable to copy directory from " + resourceDirectory + " to "
                    + outputDirectory, e );
            }
        }
        else
        {
            this.outputDirectory.mkdirs();
            this.log.warn( "Specified resource directory does not exist: " + this.resourceDirectory.toString() );
        }
    }

    /**
     * @param docbookFiles
     */
    protected void prepareFileSystem( String[] docbookFiles )
    {
        this.log.debug( "Creating output directories for the following files - "
            + Arrays.asList( docbookFiles ).toString() );
        // TODO: This should be a bit smarter also, shouldn't it?
        for ( int fileIndex = 0; fileIndex < docbookFiles.length; fileIndex++ )
        {
            String docbookFile = docbookFiles[fileIndex];
            int lastFileSeparator = docbookFile.lastIndexOf( File.separator );
            if ( lastFileSeparator > 0 )
            {
                File directory = new File( this.outputDirectory, docbookFile.substring( 0, lastFileSeparator ) );
                directory.mkdirs();
            }
        }
    }

    protected void transformDocuments( Set docbookFiles )
    {
        this.log.info( "Transforming " + docbookFiles.size() + " Docbook stale file(s)" );
        Source docbookStyleSheetSource = new StreamSource( this.stylesheetLocation.resolve( "xhtml/docbook.xsl" )
            .toString() );

        Transformer documentTransformer;
        try {
            documentTransformer = TransformerFactory.newInstance().newTransformer( docbookStyleSheetSource );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( "Unable to get a transformer instance from source " + docbookStyleSheetSource.getSystemId(), e );
        }

        URI olinkDBURI = new File( this.databaseDirectory + File.separator + "olinkdb.xml" ).toURI();
        documentTransformer.setParameter( "target.database.document", olinkDBURI.toString() );
        documentTransformer.setParameter( "generate.toc", "" );
        this.log.debug( "Style sheet loaded." );

        Iterator filesIterator = docbookFiles.iterator();
        while ( filesIterator.hasNext() )
        {
            File docbookFile = (File) filesIterator.next();
            this.log.debug( "Processing " + docbookFile );
            Source source = new StreamSource( docbookFile );
            String relativePath = docbookFile.getAbsolutePath().substring(
                                                                           (int) this.sourceDirectory.getAbsolutePath()
                                                                               .length() );
            File resultFile = new File( this.outputDirectory, relativePath
                .substring( 0, relativePath.lastIndexOf( '.' ) )
                + ".html" );
            Result result = new StreamResult( resultFile.getAbsolutePath() );

            documentTransformer.setParameter( "current.docid", OLinkDBUpdater.computeFileID( relativePath ) );
            // TODO: Parametrize this !!!!
            documentTransformer
                .setParameter( "html.stylesheet", this.pathToResources( relativePath ) + "css/xhtml.css" );

            try {
                documentTransformer.transform( source, result );
            }
            catch ( TransformerException e )
            {
                throw new RuntimeException( "Unable to transform from source " + source.getSystemId() + " into " + result.getSystemId(), e );
            }

            this.log.debug( "Generated " + this.databaseDirectory + File.separator + docbookFile );
        }
    }

    protected String pathToResources( String relativePath )
    {
        StringBuffer pathToResources = new StringBuffer();
        int separatorIndex = relativePath.indexOf( File.separator, 1 );
        while ( separatorIndex != -1 )
        {
            pathToResources.append( "../" );
            separatorIndex = relativePath.indexOf( File.separator, separatorIndex + 1 );
        }
        return pathToResources.toString();
    }
}
