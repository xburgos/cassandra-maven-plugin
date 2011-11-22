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

import org.codehaus.mojo.chronos.common.model.HistoricSample;
import org.codehaus.mojo.chronos.common.model.HistoricSamples;
import org.jdom.DocType;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class HistoricDataDirectory
{
    /**
     * Prefix for history sample files.
     */
    /* pp */ private static final String HISTORYSAMPLE_FILE_PREFIX = "history-";

    private File dataDirectory;

    public HistoricDataDirectory( File chronosDir, String dataId )
    {
        this.dataDirectory = new File( chronosDir, dataId );
    }

    /**
     * Read the saved <code>HistoricSamples</code><br />
     * The method is backwards compatible, the method will look for previous .ser files - if found then the old files
     * will be loaded.<br />
     *
     * @return The corresponding <code>HistoricSamples</code> instance.
     * @throws java.io.IOException    Thrown if loading the contents fails.
     * @throws org.jdom.JDOMException Thrown if parsing the contents fails.
     */
    public HistoricSamples readHistoricSamples()
        throws JDOMException, IOException
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
                        HistoricSample sample = HistoricSample.fromXML( historyFiles[i] );
                        samples.addHistoricSample( sample );
                    }
                }
            }
        }

        return samples;
    }

    public void writeHistorySample( HistoricSample sample )
        throws IOException
    {
        String fileName =
            HISTORYSAMPLE_FILE_PREFIX + sample.getTimestamp() + '.' + TestDataDirectory.XML_FILE_EXTENSION;
        File historyFile = new File( dataDirectory, fileName );
        if ( historyFile.exists() )
        {
            historyFile.delete();
        }

        IOUtil.writeXmlToFile( historyFile, sample.toXML(),
                               new DocType( "historysamples", "SYSTEM", "chronos-history.dtd" ) );
    }

}
