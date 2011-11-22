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
package org.codehaus.mojo.chronos.check;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.abstractmojo.AbstractCheckMojo;

/**
 * Checks the latest performancetests to verify that performance targets have been met.
 * Extends abstract baseclass inside the reporting plugin to avoid duplication of code.
 *
 * @author ksr@lakeside.dk
 * @goal check
 * @phase verify
 */
public class CheckMojo
    extends AbstractCheckMojo
{
    /**
     * The maximum allowed ratio of time spent garbage collecting.
     *
     * @parameter
     */
    protected double gctimeratio;

    /**
     * The maximum memory (in kb) garbagecollected pr second.
     *
     * @parameter
     */
    protected double collectedprsecond;

    /**
     * The minimum required maximum throughput (in requests/sec).
     *
     * @parameter
     */
    protected double maxthroughput;

    /**
     * The maximum acceptable average responsetime (in millis).
     *
     * @parameter
     */
    protected double responsetimeaverage;

    /**
     * The maximum acceptable 95 percentage responsetime (in millis).
     *
     * @parameter
     */
    protected double responsetime95;

    /**
     * The maximum acceptable 99 percentage responsetime (in millis).
     *
     * @parameter
     */
    protected double responsetime99;

    /**
     * The maximum acceptable responsetime (in millis).
     *
     * @parameter
     */
    protected double responsetimemax;

    /**
     * The timeinterval to base moving average calculations on (in millis).
     *
     * @parameter default-value = 20000
     */
    protected int averageduration = 20000; // 20 seconds

    /**
     * The id of the JMeter test
     *
     * @parameter default-value="performancetest"
     */
    protected String dataid;

    /**
     * The current maven project.
     *
     * @parameter expression="${project}"
     */
    protected MavenProject project;

    /**
     * Determines if the build should fail if at least one of the samples failed.
     *
     * @parameter default-value=false
     */
    protected boolean stopOnFailed;

    @Override
    protected boolean getStopOnFailedSamples()
    {
        return stopOnFailed;
    }

    @Override
    protected String getDataId()
    {
        return dataid;
    }

    @Override
    protected double getCollectedPrSecond()
    {
        return collectedprsecond;
    }

    @Override
    protected double getGcTimeRatio()
    {
        return gctimeratio;
    }

    @Override
    protected int getAverageDuration()
    {
        return averageduration;
    }

    @Override
    protected double getMinThroughput()
    {
        return maxthroughput;
    }

    @Override
    protected double getResponsetimeAverage()
    {
        return responsetimeaverage;
    }

    @Override
    protected double getResponsetime95()
    {
        return responsetime95;
    }

    @Override
    protected double getResponsetime99()
    {
        return responsetime99;
    }

    @Override
    protected double getResponsetimeMax()
    {
        return responsetimemax;
    }

    @Override
    protected MavenProject getMavenProject()
    {
        return project;
    }
}
