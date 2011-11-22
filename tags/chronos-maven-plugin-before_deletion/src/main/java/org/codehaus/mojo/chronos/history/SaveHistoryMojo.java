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
import org.codehaus.mojo.chronos.Utils;
import org.codehaus.mojo.chronos.gc.GCSamples;
import org.codehaus.mojo.chronos.responsetime.GroupedResponsetimeSamples;
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
     * @parameter expression="${basedir}/target/chronos/history"
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
        File dataDirectory = Utils.ensureDir( new File( historydir, dataid ) );

        try
        {
            GroupedResponsetimeSamples responseSamples = Utils.readResponsetimeSamples( project.getBasedir(), dataid );

            GCSamples gcSamples = Utils.readGCSamples( project.getBasedir(), dataid );
            HistoricSample history = new HistoricSample( responseSamples, gcSamples );
            Utils.writeHistorySample( history, dataDirectory );
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