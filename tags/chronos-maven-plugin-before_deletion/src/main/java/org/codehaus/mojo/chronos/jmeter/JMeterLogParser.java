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
package org.codehaus.mojo.chronos.jmeter;

import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Responsible for parsing the jmeter log.
 *
 * @author ksr@lakeside.dk
 */
public final class JMeterLogParser
{
    private final SAXParser saxParser;

    public JMeterLogParser()
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try
        {
            saxParser = saxParserFactory.newSAXParser();
        }
        catch ( ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }
        catch ( SAXException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Parse the jmeter (jtl) log.
     *
     * @param file The file to parseJtl20
     * @return The {@link GroupedResponsetimeSamples} obtained by parsing the log
     * @throws SAXException If there is some XML related error in the logfile
     * @throws IOException  If the JMeter logfile cannot be read
     */
    public GroupedResponsetimeSamples parseJMeterLog( File file )
        throws SAXException, IOException
    {
        JMeterSAXFileHandler saxHandler = new JMeterSAXFileHandler();
        saxParser.parse( file, saxHandler );
        return saxHandler.getJMeterSamples();
    }

}
