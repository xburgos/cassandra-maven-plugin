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

import junit.framework.TestCase;

public class ReportMojoTest extends TestCase {

    public void testSimple() throws Exception {
        TestHelper.performReport("src/test/resources/test1-junitsamples.jtl", "src/test/resources/test1-gc.txt",
                "test1");
    }

    public void testJtl22Combined2() throws Exception {
        TestHelper.performReport("src/test/resources/combinedtest-jtl22-summaryreport.jtl", null, "test5");
    }

    public void testOutputName() {
        ReportMojo mojo = new ReportMojo();
        mojo.reportid = "out";
        assertEquals("out", mojo.getOutputName());
    }

    public void testGetGc() {
        ReportMojo mojo = new ReportMojo();
        mojo.project = TestHelper.newMavenProject();
        assertFalse(mojo.getConfig().isShowgc());
    }

    public void testGetId() {
        ReportMojo mojo = new ReportMojo();
        mojo.reportid = "xx";
        assertEquals("xx", mojo.getConfig().getId());
        mojo.reportid = "yy";
        assertEquals("yy", mojo.getConfig().getId());
        mojo.reportid = null;
        mojo.dataid = "zz";
        assertEquals("zz", mojo.getConfig().getId());
    }
}