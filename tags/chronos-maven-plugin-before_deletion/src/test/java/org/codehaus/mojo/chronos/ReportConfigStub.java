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

public class ReportConfigStub implements ReportConfig {

    public int getAverageduration() {
        return 20000;
    }

    public String getDescription() {
        return "description";
    }

    public String getId() {
        return "id";
    }

    public long getThreadcountduration() {
        return 20000;
    }

    public String getTitle() {
        return "Title";
    }

    public boolean isShowaverage() {
        return true;
    }

    public boolean isShowdetails() {
        return true;
    }

    public boolean isShowgc() {
        return true;
    }

    public boolean isShowhistogram() {
        return true;
    }

    public boolean isShowinfotable() {
        return true;
    }

    public boolean isShowpercentile95() {
        return true;
    }

    public boolean isShowpercentile99() {
        return true;
    }

    public boolean isShowresponse() {
        return true;
    }

    public boolean isShowsummary() {
        return true;
    }

    public boolean isShowthroughput() {
        return true;
    }

    public boolean isShowtimeinfo() {
        return true;
    }

    public boolean isShowsummarycharts() {
        return true;
    }

    /* Merged from Atlassion */
    public double getHistoryChartUpperBound() {
        return 30000;
    }

    public String getMetadata() {
        return "src/test/resources/metadata.properties";
    }
}