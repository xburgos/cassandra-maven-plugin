package org.codehaus.mojo.failsafe;

/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.surefire.booter.SurefireBooter;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.booter.SurefireExecutionException;
import org.apache.maven.surefire.booter.ForkConfiguration;
import org.codehaus.mojo.failsafe.model.io.xpp3.FailsafeSummaryXpp3Writer;
import org.codehaus.mojo.failsafe.model.io.xpp3.FailsafeSummaryXpp3Reader;
import org.codehaus.mojo.failsafe.model.FailsafeSummary;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * Verify integration tests ran using Surefire.
 *
 * @author Stephen Connolly
 * @author Jason van Zyl
 * @requiresProject true
 * @goal verify
 * @phase verify
 */
public class VerifyMojo
    extends AbstractMojo
{

    /**
     * Set this to 'true' to skip running tests, but still compile them. Its use is NOT RECOMMENDED, but quite
     * convenient on occasion.
     *
     * @parameter expression="${skipTests}"
     * @since 2.4
     */
    private boolean skipTests;

    /**
     * DEPRECATED This old parameter is just like skipTests, but bound to the old property maven.test.skip.exec.
     * Use -DskipTests instead; it's shorter.
     *
     * @parameter expression="${maven.test.skip.exec}"
     * @since 2.3
     * @deprecated
     */
    private boolean skipExec;

    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT RECOMMENDED, especially if you
     * enable it using the "maven.test.skip" property, because maven.test.skip disables both running the
     * tests and compiling the tests.  Consider using the skipTests parameter instead.
     *
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * Set this to true to ignore a failure during testing. Its use is NOT RECOMMENDED, but quite convenient on
     * occasion.
     *
     * @parameter expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    /**
     * The directory containing generated test classes of the project being tested.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    private File testClassesDirectory;

    /**
     * Base directory where all reports are written to.
     *
     * @parameter expression="${project.build.directory}/failsafe-reports"
     */
    private File reportsDirectory;

    /**
     * The summary file to write integration test results to.
     *
     * @parameter expression="${project.build.directory}/failsafe-reports/failsafe-summary.xml"
     * @required
     */
    private File summaryFile;

    /**
     * Set this to "true" to cause a failure if there are no tests to run. Defaults to false.
     *
     * @parameter expression="${failIfNoTests}"
     * @since 2.4
     */
    private Boolean failIfNoTests;

    /**
     * The character encoding scheme to be applied.
     *
     * @parameter expression="${encoding}" default-value="${project.reporting.outputEncoding}"
     */
    protected String encoding;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( verifyParameters() )
        {
            getLog().info( "Failsafe report directory: " + reportsDirectory );

            int result;
            try
            {
                String encoding;
                if ( StringUtils.isEmpty( this.encoding ) )
                {
                    getLog().warn(
                        "File encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
                            + ", i.e. build is platform dependent!" );
                    encoding = ReaderFactory.FILE_ENCODING;
                }
                else
                {
                    encoding = this.encoding;
                }

                FileInputStream fos = new FileInputStream( summaryFile );
                BufferedInputStream bos = new BufferedInputStream( fos );
                Reader w = new InputStreamReader( bos, encoding );
                FailsafeSummaryXpp3Reader reader = new FailsafeSummaryXpp3Reader();
                final FailsafeSummary summary = reader.read( w );
                result = summary.getResult();
                w.close();
                bos.close();
                fos.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            catch ( XmlPullParserException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            if ( result == 0 )
            {
                return;
            }

            String msg;

            if ( result == SurefireBooter.NO_TESTS_EXIT_CODE )
            {
                if ( ( failIfNoTests == null ) || !failIfNoTests.booleanValue() )
                {
                    return;
                }
                // TODO: i18n
                throw new MojoFailureException(
                    "No tests were executed!  (Set -DfailIfNoTests=false to ignore this error.)" );
            }
            else
            {
                // TODO: i18n
                msg = "There are test failures.\n\nPlease refer to " + reportsDirectory
                    + " for the individual test results.";

            }

            if ( testFailureIgnore )
            {
                getLog().error( msg );
            }
            else
            {
                throw new MojoFailureException( msg );
            }
        }
    }

    private boolean verifyParameters()
        throws MojoFailureException
    {
        if ( skip || skipTests || skipExec )
        {
            getLog().info( "Tests are skipped." );
            return false;
        }

        if ( !testClassesDirectory.exists() )
        {
            if ( failIfNoTests != null && failIfNoTests.booleanValue() )
            {
                throw new MojoFailureException( "No tests to run!" );
            }
            return false;
        }

        return true;
    }


}
