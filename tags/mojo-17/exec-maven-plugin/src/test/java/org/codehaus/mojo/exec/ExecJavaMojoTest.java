package org.codehaus.mojo.exec;

/*
 * Copyright 2005-2006 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.util.StringOutputStream;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Jerome Lacoste <jerome@coffeebreaks.org>
 * @version $Id$
 */
public class ExecJavaMojoTest
    extends AbstractMojoTestCase
{

    /*
    This one won't work yet
    public void xxtestSimpleRunPropertiesAndArguments()
        throws MojoExecutionException, Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project2/pom.xml" );

        String output = execute( pom, "java" );

        System.out.println(output);

        assertEquals( -1, output.trim().indexOf( "ERROR" ) );
    }
    */

    /**
     * Check that a simple execution with no arguments and no system properties produces the expected result
     * <p/>
     * we load the config from a pom file and fill up the MavenProject property ourselves
     */
    public void testSimpleRun()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project4/pom.xml" );

        String output = execute( pom, "java" );

        assertEquals( "Hello" + System.getProperty( "line.separator" ), output );
    }

    /**
     * MEXEC-10 Check that an execution with no arguments and an system property with no value
     * produces the expected result
     * <p/>
     * we load the config from a pom file and fill up the MavenProject property ourselves
     */
    public void testEmptySystemProperty()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project5/pom.xml" );

        assertNull( "System property not yet created",
                     System.getProperty( "project5.property.with.no.value" ) );

        execute( pom, "java" );

        assertEquals( "System property now empty",
                       "",
                       System.getProperty( "project5.property.with.no.value" ) );
    }

    /**
     * MEXEC-29 exec:java throws NPE if the mainClass main method has not a correct signature
     * <p/>
     */
    public void testIncorrectMainMethodSignature()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project12/pom.xml" );

        try {
            String output = execute( pom, "java" );
        } catch (MojoExecutionException e) {
            assertTrue( stringContains( e.getMessage(), "The specified mainClass doesn't contain a main method with appropriate signature." ) );
        }

    }
    
    // java 1.4 compatibility
    private boolean stringContains( String str, String sequence )
    {
        return str.indexOf( sequence ) != -1;
    }

    /**
     * For cases where the Java code spawns Threads and main returns soon.
     * See <a href="http://jira.codehaus.org/browse/MEXEC-6">MEXEC-6</a>.
     */
    public void testWaitNoDaemonThreads()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project7/pom.xml" );

        String output = execute( pom, "java" );

        assertEquals( MainWithThreads.ALL_EXITED, output.trim() );
    }

    /**
     * For cases where the Java code spawns Threads and main returns soon, but code contains non interruptible threads.
     * User is required to timeout the execution, otherwise it will hang.
     * See <a href="http://jira.codehaus.org/browse/MEXEC-15">MEXEC-15</a>.
     */
    public void testWaitNonInterruptibleDaemonThreads()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project9/pom.xml" );

        String output = execute( pom, "java" );

        assertEquals( MainWithThreads.TIMER_IGNORED, output.trim() );
    }

    /**
     * See <a href="http://jira.codehaus.org/browse/MEXEC-15">MEXEC-15</a>.
     * FIXME: this sometimes fail with unit.framework.ComparisonFailure: expected:<...> but was:<...3(f)>
     */
    public void testUncooperativeThread()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project10/pom.xml" );
        String output = execute( pom, "java" );
        // note: execute() will wait a little bit before returning the output,
        // thereby allowing the stop()'ed thread to output the final "(f)".
        assertEquals( MainUncooperative.SUCCESS, output.trim() );
    }

    /**
     * See <a href="http://jira.codehaus.org/browse/MEXEC-17">MEXEC-17</a>.
     */
    /**
    This test doesn't work because the sun.tools.javac.Main class referenced in the
    project pom is found even if the system scope dependencies are not added by the plugin.
    The class was probably loaded by another plugin ?!

    To fix the test we have to:
    - maybe use a different system scope dependency/class to load.
    - find a different way to test.

    When ran manually, the test works though (i.e. removing the code that manually adds
    the system scope dependencies make the test fail).

    public void testSystemDependencyFound()
        throws Exception
    {
        File pom = new File( getBasedir(), "src/test/projects/project11/pom.xml" );

        String output = execute( pom, "java" );

        assertEquals( FindClassInClasspath.FOUND_ALL, output.trim() );
    }
    */

    /**
     * Test the commandline parsing facilities of the {@link AbstractExecMojo} class
     */
    public void testRunWithArgs() throws Exception
    {

        String resultString = execute( new File( getBasedir(), "src/test/projects/project8/pom.xml" ), "java" );

        String LS = System.getProperty("line.separator");
        String expectedResult = "Hello" + LS + "Arg1" + LS +"Arg2a Arg2b" + LS;
        assertEquals( expectedResult, resultString );
    }

    /**
     * @return output from System.out during mojo execution
     */
    private String execute( File pom, String goal ) throws Exception {

        ExecJavaMojo mojo;
        mojo = (ExecJavaMojo) lookupMojo( goal, pom );

        setUpProject( pom, mojo );

        MavenProject project = (MavenProject) getVariableValueFromObject( mojo, "project" );

        // why isn't this set up by the harness based on the default-value?  TODO get to bottom of this!
        setVariableValueToObject( mojo, "includeProjectDependencies", Boolean.TRUE );
        setVariableValueToObject( mojo, "killAfter", new Long( -1 ) );
        setVariableValueToObject( mojo, "cleanupDaemonThreads", Boolean.TRUE );

        assertNotNull( mojo );
        assertNotNull( project );

        // trap System.out
        PrintStream out = System.out;
        StringOutputStream stringOutputStream = new StringOutputStream();
        System.setOut( new PrintStream( stringOutputStream ) );
        // ensure we don't log unnecessary stuff which would interfere with assessing success of tests
        mojo.setLog( new DefaultLog( new ConsoleLogger( Logger.LEVEL_ERROR, "exec:java" ) ) );

        try
        {
            mojo.execute();
        }
        finally
        {
            // see testUncooperativeThread() for explaination
            Thread.sleep( 150 ); // time seems about right
            System.setOut( out );
        }

        return stringOutputStream.toString();
    }

    private void setUpProject( File pomFile, AbstractMojo mojo )
        throws Exception
    {
        MavenProjectBuilder builder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepositoryLayout localRepositoryLayout =
            (ArtifactRepositoryLayout) lookup( ArtifactRepositoryLayout.ROLE, "default" );

        String path = "src/test/repository";

        ArtifactRepository localRepository = new DefaultArtifactRepository( "local", "file://" +
            new File( path ).getAbsolutePath(), localRepositoryLayout );

        MavenProject project = builder.buildWithDependencies( pomFile, localRepository, null );
        // this gets the classes for these tests of this mojo (exec plugin) onto the project classpath for the test
        project.getBuild().setOutputDirectory( new File( "target/test-classes" ).getAbsolutePath() );
        setVariableValueToObject( mojo, "project", project );
    }
}
