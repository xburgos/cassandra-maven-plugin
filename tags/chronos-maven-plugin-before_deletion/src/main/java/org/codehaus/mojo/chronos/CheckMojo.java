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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.gc.GCSamples;
import org.codehaus.mojo.chronos.responsetime.ResponsetimeSamples;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * Checks the latest performancetests to verify that performance targets have been met.
 *
 * @author ksr@lakeside.dk
 * @goal check
 * @phase verify
 */
public class CheckMojo
    extends AbstractMojo
{
    private static final int DEFAULT_DURATION = 20000;

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
    protected int averageduration = DEFAULT_DURATION; // 20 seconds

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
    private MavenProject project;

    /**
     * Determines if the build should fail if at least one of the samples failed.
     *
     * @parameter default-value=false
     */
    protected boolean stopOnFailed;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            ResponsetimeSamples rtSamples = Utils.readResponsetimeSamples( project.getBasedir(), dataid );
            if ( rtSamples == null )
            {
                throw new MojoExecutionException( "Response time samples not found for " + dataid );
            }
            long totalTime = rtSamples.getTotalTime();
            validateMaxThroughput( rtSamples );
            validateFailedSamples( rtSamples );
            validateAverageResponsetime( rtSamples );
            validatePercentile95Responsetime( rtSamples );
            validatePercentile99Responsetime( rtSamples );
            validateMaxResponsetime( rtSamples );
            GCSamples gcSamples = Utils.readGCSamples( project.getBasedir(), dataid );
            if ( gcSamples != null )
            {
                validateGCTime( gcSamples, totalTime );
                validateCollectedPrSecond( gcSamples, totalTime );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failure", e );
        }
        catch ( JDOMException e )
        {
            throw new MojoExecutionException( "Failure", e );
        }
    }

    private void validateFailedSamples( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( stopOnFailed )
        {
            int failed = samples.getFailed();
            if ( failed > 0 )
            {
                throw new MojoExecutionException( failed + " JMeter sample(s) failed" );
            }
        }
    }

    private void validateGCTime( GCSamples gcSamples, long totalTime )
        throws MojoExecutionException
    {
        if ( gctimeratio <= 0 )
        {
            return;
        }
        double actualRatio = gcSamples.getGarbageCollectionRatio( totalTime );
        if ( actualRatio > gctimeratio )
        {
            throw new MojoExecutionException(
                "To much time spent garbagecollection. Ratio of time spent was " + actualRatio
                    + " but acceptable level was " + gctimeratio );
        }
    }

    private void validateCollectedPrSecond( GCSamples samples, long totalTime )
        throws MojoExecutionException
    {
        if ( collectedprsecond <= 0 )
        {
            return;
        }
        double actual = samples.getCollectedKBPerSecond( totalTime );
        if ( actual > collectedprsecond )
        {
            throw new MojoExecutionException( "To much stuff garbagecollected. Garbagecollected pr second was " + actual
                + "kb but acceptable level was " + collectedprsecond );
        }
    }

    private void validateMaxThroughput( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( maxthroughput <= 0 )
        {
            return;
        }
        double actual = samples.getMaxAverageThroughput( averageduration );
        if ( actual < maxthroughput )
        {
            throw new MojoExecutionException(
                "Throughput too low. Throughput was " + actual + " but required throughput was " + maxthroughput );
        }
    }

    private void validateAverageResponsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( responsetimeaverage <= 0 )
        {
            return;
        }
        validateComparison( "Average responsetime too high.", responsetimeaverage, samples.getAverage() );
    }

    private void validatePercentile95Responsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( responsetime95 <= 0 )
        {
            return;
        }
        validateComparison( "95 percentile responsetime too high.", responsetime95, samples.getPercentile95() );
    }

    private void validatePercentile99Responsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( responsetime99 <= 0 )
        {
            return;
        }
        validateComparison( "99 percentile responsetime too high.", responsetime99, samples.getPercentile99() );
    }

    private void validateMaxResponsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( responsetimemax <= 0 )
        {
            return;
        }
        validateComparison( "Max responsetime too high.", responsetimemax, samples.getMax() );
    }

    private void validateComparison( String message, double largest, double smallest )
        throws MojoExecutionException
    {
        if ( smallest > largest )
        {
            String error = message + " Measured was " + smallest + " but acceptable was " + largest;
            throw new MojoExecutionException( error );
        }
    }
}
