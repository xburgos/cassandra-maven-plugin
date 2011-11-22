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

import junit.framework.TestCase;
import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSampleGroup;

import java.io.File;
import java.util.Iterator;

/**
 * Test case for the <code>JMeterLogParser</code> class.
 * 
 * @author ksr@lakeside.dk
 */
public class JMeterLogParserTest extends TestCase {

    /**
     * Checks if the all samples from log is being parsed
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testParseJMeterLog() throws Exception {
        File file = new File("src/test/resources/test1-junitsamples.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(6, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(3, sampleGroup.size());
        }
    }

    /**
     * Checks correct parsing
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testParseJmeter23WebLog() throws Exception {
        File file = new File("src/test/resources/webtest-jmeter22-resulttable.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(2, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(150, sampleGroup.size());
        }
    }

    /**
     * Checks correct parsing
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testJtl20Combined() throws Exception {
        File file = new File("src/test/resources/combinedtest-jtl20-summaryreport.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(4, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(150, sampleGroup.size());
        }
    }

    /**
     * Checks correct parsing
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testJtl21Combined() throws Exception {
        File file = new File("src/test/resources/combinedtest-jtl21-summaryreport.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(4, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(150, sampleGroup.size());
        }
    }

    /**
     * Checks correct parsing
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testJtl22Combined2() throws Exception {
        File file = new File("src/test/resources/combinedtest-jtl22-summaryreport.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(4, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(150, sampleGroup.size());
        }
    }

    /**
     * Nested httpSample elements breaks jmeter performance reporting.<br />
     * See <a href="http://jira.codehaus.org/browse/MOJO-1343 JiraTask">JIRA</a> for more information.
     * 
     * @throws Exception
     *             Throw if something unexpected fails
     */
    public void testJtlNestedHttpSample() throws Exception {
        File file = new File("src/test/resources/jmeter-nested-httpsample.jtl");
        GroupedResponsetimeSamples samples = new JMeterLogParser().parseJMeterLog(file);
        assertEquals(4, samples.getSampleGroups().size());
        for (Iterator it = samples.getSampleGroups().iterator(); it.hasNext();) {
            ResponsetimeSampleGroup sampleGroup = (ResponsetimeSampleGroup)it.next();
            assertEquals(1, sampleGroup.size());
        }
    }

}
