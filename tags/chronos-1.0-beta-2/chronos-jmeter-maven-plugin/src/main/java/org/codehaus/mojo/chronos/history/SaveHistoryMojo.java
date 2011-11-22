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
package org.codehaus.mojo.chronos.history;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.chronos.common.HistoricDataDirectory;
import org.codehaus.mojo.chronos.common.ProjectBaseDir;
import org.codehaus.mojo.chronos.common.TestDataDirectory;
import org.codehaus.mojo.chronos.common.model.GCSamples;
import org.codehaus.mojo.chronos.common.model.GroupedResponsetimeSamples;
import org.codehaus.mojo.chronos.common.model.HistoricSample;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;

/**
 * Save a snapshot of the currently executed test to enable later historic reports.
 *
 * @goal savehistory
 * @phase post-integration-test
 */
public class SaveHistoryMojo
    extends AbstractMojo
{
    /**
     * The current maven project.
     *
     * @parameter expression="${project}"
     */
    /* pp */ MavenProject project;

    /**
     * The directory where historic data are stored.
     *
     * @parameter expression="${basedir}/target/chronos-history"
     */
    /* pp */ File historydir;

    /**
     * The id of the currently executed performancetest.
     *
     * @parameter default-value="performancetest"
     */
    /* pp */ String dataid;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        HistoricDataDirectory historicDataDirectory = new HistoricDataDirectory( historydir, dataid );
        final ProjectBaseDir projectBaseDir = new ProjectBaseDir( project, getLog());
        final TestDataDirectory testDataDirectory = projectBaseDir.getDataDirectory(dataid);

        try
        {
            GroupedResponsetimeSamples responseSamples = testDataDirectory.readResponsetimeSamples();
            if ( responseSamples.size() == 0 )
            {
                throw new MojoExecutionException( "Response time samples not found for " + dataid );
            }

            GCSamples gcSamples = testDataDirectory.readGCSamples();
            HistoricSample history = new HistoricSample( responseSamples, gcSamples );
            historicDataDirectory.writeHistorySample( history );
        }
        catch ( IOException ex )
        {
            throw new MojoExecutionException( "unable to find gcsamples with dataid=" + dataid, ex );
        }
        catch ( JDOMException ex )
        {
            throw new MojoExecutionException( "unable to find gcsamples with dataid=" + dataid, ex );
        }

    }
}