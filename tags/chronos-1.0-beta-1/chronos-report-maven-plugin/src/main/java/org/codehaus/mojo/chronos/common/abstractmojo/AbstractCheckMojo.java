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
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos/chronos-report-maven-plugin/src/main/java/org/codehaus/mojo/chronos/report/CheckMojo.java $
  * $Id: CheckMojo.java 14785 2011-10-06 08:57:28Z soelvpil $
  */
package org.codehaus.mojo.chronos.common.abstractmojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.ResponsetimeSamples;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * Checks the latest performancetests to verify that performance targets have been met.
 * The class is abstract, since it should reside inside the build-plugin (chronos-jmeter-maven-plugin)
 *
 * @author ksr@lakeside.dk
 */
public abstract class AbstractCheckMojo
    extends AbstractMojo
{

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            final ProjectBaseDir projectBaseDir = new ProjectBaseDir( getMavenProject() );
            final TestDataDirectory testDataDirectory = projectBaseDir.getDataDirectory( getDataId() );
            ResponsetimeSamples rtSamples = testDataDirectory.readResponsetimeSamples();
            if ( rtSamples.size() == 0 )
            {
                throw new MojoExecutionException(
                    "Response time samples not found in " + testDataDirectory.getDirectory() );
            }
            long totalTime = rtSamples.getTotalTime();
            validateMaxThroughput( rtSamples );
            validateFailedSamples( rtSamples );
            validateAverageResponsetime( rtSamples );
            validatePercentile95Responsetime( rtSamples );
            validatePercentile99Responsetime( rtSamples );
            validateMaxResponsetime( rtSamples );

            GCSamples gcSamples = testDataDirectory.readGCSamples();
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
        if ( getStopOnFailedSamples() )
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
        if ( getGcTimeRatio() <= 0 )
        {
            return;
        }
        double actualRatio = gcSamples.getGarbageCollectionRatio( totalTime );
        if ( actualRatio > getGcTimeRatio() )
        {
            throw new MojoExecutionException(
                "Too much time spent garbagecollection. Ratio of time spent was " + actualRatio
                    + " but acceptable level was " + getGcTimeRatio() );
        }
    }

    private void validateCollectedPrSecond( GCSamples samples, long totalTime )
        throws MojoExecutionException
    {
        if ( getCollectedPrSecond() <= 0 )
        {
            return;
        }
        double actual = samples.getCollectedKBPerSecond( totalTime );
        if ( actual > getCollectedPrSecond() )
        {
            throw new MojoExecutionException( "To much stuff garbagecollected. Garbagecollected pr second was " + actual
                                                  + "kb but acceptable level was " + getCollectedPrSecond() );
        }
    }

    private void validateMaxThroughput( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( getMinThroughput() <= 0 )
        {
            return;
        }
        double actual = samples.getMaxAverageThroughput( getAverageDuration() );
        if ( actual < getMinThroughput() )
        {
            throw new MojoExecutionException(
                "Throughput too low. Throughput was " + actual + " but required throughput was " + getMinThroughput() );
        }
    }

    private void validateAverageResponsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( getResponsetimeAverage() <= 0 )
        {
            return;
        }
        validateLessThan( "Average responsetime too high.", getResponsetimeAverage(), samples.getAverage() );
    }

    private void validatePercentile95Responsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( getResponsetime95() <= 0 )
        {
            return;
        }
        validateLessThan( "95 percentile responsetime too high.", getResponsetime95(), samples.getPercentile95() );
    }

    private void validatePercentile99Responsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( getResponsetime99() <= 0 )
        {
            return;
        }
        validateLessThan( "99 percentile responsetime too high.", getResponsetime99(), samples.getPercentile99() );
    }

    private void validateMaxResponsetime( ResponsetimeSamples samples )
        throws MojoExecutionException
    {
        if ( getResponsetimeMax() <= 0 )
        {
            return;
        }
        validateLessThan( "Max responsetime too high.", getResponsetimeMax(), samples.getMax() );
    }

    private void validateLessThan( String message, double threshold, double measured )
        throws MojoExecutionException
    {
        if ( measured > threshold )
        {
            String error = message + " Measured was " + measured + " but acceptable was " + threshold;
            throw new MojoExecutionException( error );
        }
    }

    protected abstract MavenProject getMavenProject();

    protected abstract boolean getStopOnFailedSamples();

    protected abstract String getDataId();

    protected abstract double getCollectedPrSecond();

    protected abstract double getGcTimeRatio();

    protected abstract int getAverageDuration();

    protected abstract double getMinThroughput();

    protected abstract double getResponsetimeAverage();

    protected abstract double getResponsetime95();

    protected abstract double getResponsetime99();

    protected abstract double getResponsetimeMax();
}
