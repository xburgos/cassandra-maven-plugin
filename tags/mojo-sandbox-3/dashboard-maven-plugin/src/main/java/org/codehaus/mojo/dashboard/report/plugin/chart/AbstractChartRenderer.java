package org.codehaus.mojo.dashboard.report.plugin.chart;

/*
 * Copyright 2006 David Vicente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public abstract class AbstractChartRenderer
{

    /**
     * Chart name.
     */
    private String name = null;

    /**
     * Wrapped chart.
     */
    protected JFreeChart report = null;
    
    /**
     * Width of the resulting chart file.
     */
    private int width = ChartUtils.STANDARD_WIDTH;
    
    /**
     * Height of the resulting chart file.
     */
    private int height = ChartUtils.STANDARD_HEIGHT;
    /**
     * 
     */
    protected IDashBoardReportBean dashboardReport = null;
    /**
     * 
     */
    protected IDatasetStrategy datasetStrategy;

    /**
     * 
     * @param bundle
     * @param dashboardReport
     * @param strategy
     */
    public AbstractChartRenderer( IDashBoardReportBean dashboardReport,
                                  IDatasetStrategy strategy )
    {

        this.dashboardReport = dashboardReport;
        this.name = this.dashboardReport.getProjectName();
        //this.bundle = bundle;
        this.datasetStrategy = strategy;
        createChart( this.dashboardReport );
    }
    /**
     * Wrap the specified document in a new wrapper instance.
     * 
     * @param 
     *      Name of the report.
     * @param 
     *      Chart to wrap as a report.
     * @param width
     *            Width of the resulting report file.
     * @param height
     *            Height of the resulting report file.
     */
    public AbstractChartRenderer( IDashBoardReportBean dashboardReport,
                                  IDatasetStrategy strategy, int width, int height )
    {
        this( dashboardReport, strategy );
        this.width = width;
        this.height = height;
    }
    /**
     * 
     * @param dashboardReport
     */
    protected abstract void createChart( IDashBoardReportBean dashboardReport );
    
    public boolean isEmpty()
    {
        return this.datasetStrategy.isDatasetEmpty();
    }
    /**
     * Return the file extension of the report : <tt>txt</tt>.
     * 
     * @return Report file name extension.
     * @see LAReport#getFileExtension()
     */
    public String getFileExtension()
    {
        return "png";
    }

    /**
     * Return the name of the report.
     * 
     * @return Report name.
     * @see net.logAnalyzer.reports.LAReport#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Set the name of the report.
     * 
     * @param name
     *            Report name.
     * @see net.logAnalyzer.reports.LAReport#setName(String)
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Return the mime type of the report.
     * 
     * @return Mime type of the report.
     * @see net.logAnalyzer.reports.LAReport#getMimeType()
     */
    public String getMimeType()
    {
        return "image/png";
    }

    /**
     * Save the report with the specified filename. The filename can contain a relative or absolute path.
     * <p>
     * If the file exists, it is overwritten.
     * </p>
     * 
     * @param filename
     *            Name of the output file.
     * @throws IOException
     *             If an I/O exception occurs.
     * @see net.logAnalyzer.reports.LAReport#saveToFile(java.lang.String)
     */
    public void saveToFile( String filename ) throws IOException
    {
        File imageFile = new File( filename );
        
        imageFile.getParentFile().mkdirs();
        
        ChartUtilities.saveChartAsPNG( new File( filename ), report, width, height );
    }

    /**
     * Create an image from the report as a {@link BufferedImage}.
     * 
     * @param imageWidth
     *            Image width.
     * @param imageHeight
     *            Image height.
     * @return Image from the report; <tt>null</tt> if unsupported feature.
     * @see JFreeChart#createBufferedImage(int, int)
     */
    public BufferedImage createBufferedImage( int imageWidth, int imageHeight )
    {
        return report.createBufferedImage( imageWidth, imageHeight );
    }
    /**
     * set the height of the image saved as file
     * @param _height
     */
    public void setHeight( int _height )
    {
        this.height = _height;
    }
    /**
     * set the width of the image saved as file
     * @param _width
     */
    public void setWidth( int _width )
    {
        this.width = _width;
    }
}