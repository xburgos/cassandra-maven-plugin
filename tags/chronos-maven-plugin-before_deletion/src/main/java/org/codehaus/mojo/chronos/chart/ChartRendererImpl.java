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
package org.codehaus.mojo.chronos.chart;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for performing the actual rendering of charts.
 *
 * @author ksr@lakeside.dk
 */
public final class ChartRendererImpl
    implements ChartRenderer
{

    private static final int HEIGHT = 400;

    private static final int WIDTH = 800;

    private String outputDirectory;

    /**
     * Constructor for the <code>ChartRendererImpl</code> class.
     *
     * @param outputDir The directory where generated charts is to be saved.
     */
    public ChartRendererImpl( String outputDir )
    {
        this.outputDirectory = outputDir;
    }

    /**
     * Save a {@link JFreeChart} to the filesystem.
     *
     * @param filename The filename of the chart to save
     * @param chart    the {@link JFreeChart} to save as a file
     * @throws IOException If the file cannot be saved
     */
    public void renderChart( String filename, JFreeChart chart )
        throws IOException
    {
        File parentDir = new File( outputDirectory );

        File imageDir = new File( parentDir, "images" );
        if ( !imageDir.exists() )
        {
            imageDir.mkdirs();
        }

        File pngFile = new File( imageDir, filename + ".png" );
        ChartUtilities.saveChartAsPNG( pngFile, chart, WIDTH, HEIGHT );
    }
}