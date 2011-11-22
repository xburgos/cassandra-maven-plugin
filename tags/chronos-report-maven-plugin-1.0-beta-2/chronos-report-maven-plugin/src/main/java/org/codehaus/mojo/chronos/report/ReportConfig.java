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
package org.codehaus.mojo.chronos.report;

/**
 * Represents a readonly view of the configuration.<br/>
 * Makes it more obvious that the reporting will not change the configuration
 *
 * @author ksr@lakeside.dk
 */
public interface ReportConfig
{
    /**
     * @return the id of this report (default is
     */
    String getId();

    /**
     * @return the title of this report
     */
    String getTitle();

    /**
     * @return the description of the report
     */
    String getDescription();

    /**
     * @return whether a summary of the results should be shown.
     */
    boolean isShowsummary();

    /**
     * @return whether a summary of the results should be shown.
     */
    boolean isShowsummarycharts();

    /**
     * @return Should a report of each individual test be shown?
     */
    boolean isShowdetails();

    /**
     * @return Will the report contain an information table for the tests (both the summary and the individual tests)?
     */
    boolean isShowinfotable();

    /**
     * @return Will the information table contain timeinfo?
     */
    boolean isShowtimeinfo();

    /**
     * @return Will the report contain graphs of response times?
     */
    boolean isShowresponse();

    /**
     * @return Will the report contain a histogram
     */
    boolean isShowhistogram();

    /**
     * @return Will the report contain throughput information (graphically
     */
    boolean isShowthroughput();

    /**
     * @return Will the report contain garbage collection statistics
     */
    boolean isShowgc();

    /**
     * @return the duration of the threadcount
     */
    long getThreadcountduration();

    /**
     * @return the average duration
     */
    int getAverageduration();

    /**
     * @return whether the charts of response times and histograms will contain 95% percentiles
     */
    boolean isShowpercentile95();

    /**
     * @return whether the charts of response times and histograms will contain 99% percentiles
     */
    boolean isShowpercentile99();

    /**
     * @return whether the charts of response times and histograms will contain average times
     */
    boolean isShowaverage();

    /**
     * @return Set the maximum upper bound for a chart in the historyreport goal
     */
    double getHistoryChartUpperBound(); /* Merged from Atlassion */

    /**
     * Points to a simple text file containing meta data about the build.<br />
     * The information will be added to the reports under <i>Additional build info</i>.<br />
     * The file is read line for line and added the report.<br />
     * The readed expects the <code>tab</code> character to seperate keys and values:
     * <p/>
     * <pre>
     * Build no.    567
     * Svn tag  Test
     * </pre>
     *
     * @return The location of the metadata file.
     */
    String getMetadata();
}