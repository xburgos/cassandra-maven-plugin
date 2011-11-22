package org.codehaus.mojo.fitnesse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class FitnesseRunnerMojoTest extends MockObjectTestCase
{
    private FitnesseRunnerMojo mMojo = null;

    private Mock mMockLog = null;

    public void testCheckConfigurationOk() throws MojoFailureException, MojoExecutionException
    {
        mMojo.checkConfiguration();
    }

    public void testCheckConfigurationClassPathProviderOk() throws MojoFailureException, MojoExecutionException
    {
        mMojo.setClassPathProvider( "fitnesse" );
        mMojo.checkConfiguration();
        mMojo.setClassPathProvider( "maven" );
        mMojo.checkConfiguration();
    }

    public void testCheckConfigurationClassPathProviderKo() throws MojoFailureException, MojoExecutionException
    {

        try
        {
            mMojo.setClassPathProvider( null );
            mMojo.checkConfiguration();
            fail( "Should fail" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "classPathProvider accepts only \"fitnesse\" ou \"maven\" values. [null] is not valid.",
                          e.getMessage() );
        }
        try
        {
            mMojo.setClassPathProvider( "invalid" );
            mMojo.checkConfiguration();
            fail( "Should fail" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "classPathProvider accepts only \"fitnesse\" ou \"maven\" values. [invalid] is not valid.",
                          e.getMessage() );
        }
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        mMojo = getMojo();
    }

    private FitnesseRunnerMojo getMojo()
    {
        mMojo = new FitnesseRunnerMojo();
        mMojo.setFailOnError( false );
        mMojo.setDebug( false );
        mMojo.setFailOnError( false );
        mMojo.setClassPathProvider( "fitnesse" );
        mMojo.setVerbose( false );
        mMojo.setPluginArtifacts( new ArrayList<Artifact>() );
        mMojo.setJdk( "java" );
        mMojo.setWorkingDir( "target/fitnesse" );
        mMojo.setFitnesseRunnerClass( "fitnesse.runner.TestRunner" );
        mMojo.setPluginArtifact( getArtifact() );

        List<Fitnesse> tFitnesses = new ArrayList<Fitnesse>();
        tFitnesses.add( new Fitnesse() );
        mMojo.setFitnesses( tFitnesses );

        mMojo.setClassPathSubstitions( new ArrayList<ClassPathSubstitution>() );

        mMockLog = mock( Log.class );
        mMockLog.stubs().method( "info" ).withAnyArguments();
        mMojo.setLog( (Log) mMockLog.proxy() );

        return mMojo;
    }

    public void testCheckConfigurationWithoutFitnesseProject() throws MojoFailureException
    {
        String errorMessage =
            "Your should configure at least one Fitnesse server. " + "Check your maven-fitnesse-plugin configuration.";
        mMockLog.stubs().method( "error" ).with( eq( errorMessage ) );

        mMojo.setFitnesses( null );
        try
        {
            mMojo.checkConfiguration();
            fail( "Fitnesses addresses are required..." );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( errorMessage, e.getMessage() );
        }
        mMojo.setFitnesses( new ArrayList<Fitnesse>() );
        try
        {
            mMojo.checkConfiguration();
            fail( "Fitnesses addresses are required..." );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( errorMessage, e.getMessage() );
        }
    }

    public void testCheckConfigurationWithOneFitnesseProject() throws MojoFailureException
    {
        try
        {
            mMojo.setFitnesseRunnerClass( "fitnesseRunner" );
            mMojo.checkConfiguration();
            fail( "Should not pass" );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( !e.getMessage().startsWith( "Fitnesses addresses are required" ) );
        }
    }

    public void testCheckConfigurationWithBadRunnerClassName() throws MojoFailureException
    {
        mMojo.setFitnesseRunnerClass( "badName" );
        try
        {
            mMojo.checkConfiguration();
            fail( "Should not pass" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals(
                          "The class [badName] could not be found, check your maven-fitnesse-plugin configuration and the plugin documentation.",
                          e.getMessage() );
        }

        mMojo.setFitnesseRunnerClass( this.getClass().getName() );
        try
        {
            mMojo.checkConfiguration();
            fail( "Should not pass" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals(
                          "The class [org.codehaus.mojo.fitnesse.FitnesseRunnerMojoTest] doesn't have a \"main\" accessible method.",
                          e.getMessage() );
        }
    }

    static class TestFitnesseRunner
    {
        public void main( String[] params )
        {
        }
    }

    public void testCheckConfigurationWithGoodRunnerClassName() throws MojoFailureException, MojoExecutionException
    {
        mMojo.setFitnesseRunnerClass( TestFitnesseRunner.class.getName() );
        mMojo.checkConfiguration();
    }

    static class MyFile extends File
    {
        public MyFile()
        {
            super( "bidon.jar" );
        }

        public String getAbsolutePath()
        {
            return "bidon.jar";
        }
    }

    private Artifact getArtifact()
    {
        Artifact tArtif =
            new DefaultArtifact( "junit", "junit", VersionRange.createFromVersion( "3.8.1" ), "test", "jar", null,
                                 new DefaultArtifactHandler() );
        tArtif.setFile( new MyFile() );
        return tArtif;
    }

    public void testPrepareCommandLine() throws MojoExecutionException
    {
        assertEquals(
                      "java -cp bidon.jar"
                                      + File.pathSeparatorChar
                                      + " fitnesse.runner.TestRunner -html "
                                      + "target/fitnesse/fitnesseResult_localhost_MustBeDefinedByProject.html -nopath localhost 80 MustBeDefinedByProject",
                      mMojo.prepareCommandLine( 0, "bidon.jar;" ).toString() );

        mMojo.setVerbose( true );
        assertEquals(
                      "java -cp bidon.jar"
                                      + File.pathSeparatorChar
                                      + " fitnesse.runner.TestRunner -v -html "
                                      + "target/fitnesse/fitnesseResult_localhost_MustBeDefinedByProject.html -nopath localhost 80 MustBeDefinedByProject",
                      mMojo.prepareCommandLine( 0, "bidon.jar;" ).toString() );

        mMojo.setDebug( true );
        assertEquals(
                      "java -cp bidon.jar"
                                      + File.pathSeparatorChar
                                      + " fitnesse.runner.TestRunner -v -debug "
                                      + "-html target/fitnesse/fitnesseResult_localhost_MustBeDefinedByProject.html -nopath localhost 80 MustBeDefinedByProject",
                      mMojo.prepareCommandLine( 0, "bidon.jar;" ).toString() );

    }

    public void testExecuteCommandWithFailure() throws MojoExecutionException, MojoFailureException
    {
        mMojo.setFailOnError( true );
        Mock tMockProcess = mock( Process.class );
        byte[] tInputByte =
            ( "TestSimpleClass1 has failures\n"
                            + "Test Pages: 0 right, 1 wrong, 0 ignored, 0 exceptions\n"
                            + "Assertions: 4 right, 1 wrong, 0 ignored, 0 exceptions\n"
                            + "Formatting as html to D:\\SCM\\ProjectSVN\\maven-fitnesse-plugin\\src\\it\\multiproject\\target/fitnesse/fitnesseResultSuiteCoverage2.html" ).getBytes();
        tMockProcess.stubs().method( "getInputStream" ).will( returnValue( new ByteArrayInputStream( tInputByte ) ) );
        tMockProcess.stubs().method( "getErrorStream" ).will( returnValue( new ByteArrayInputStream( new byte[0] ) ) );
        tMockProcess.stubs().method( "waitFor" ).will( returnValue( 2 ) );
        try
        {
            mMojo.executeCommand( new MockCommandLine( (Process) tMockProcess.proxy() ) );
            fail( "Should fail" );
        }
        catch ( MojoFailureException e )
        {
            assertEquals( "Fitnesse command ended with errors, exit code:2", e.getMessage() );
        }
        verify();

        tMockProcess = mock( Process.class );
        tMockProcess.stubs().method( "getInputStream" ).will( returnValue( new ByteArrayInputStream( tInputByte ) ) );
        tMockProcess.stubs().method( "getErrorStream" ).will( returnValue( new ByteArrayInputStream( new byte[0] ) ) );
        tMockProcess.stubs().method( "waitFor" ).will( returnValue( 2 ) );
        mMojo.setFailOnError( false );
        mMojo.executeCommand( new MockCommandLine( (Process) tMockProcess.proxy() ) );
        verify();
    }

    public void testExecuteCommandWithoutError()
        throws MojoExecutionException, MojoFailureException, CommandLineException
    {
        mMojo.setFailOnError( true );
        Commandline tCmd = new Commandline();
        tCmd.setExecutable( "java" );
        tCmd.createArgument().setValue( "-version" );

        mMojo.executeCommand( tCmd );
        verify();

        mMojo.setFailOnError( false );
        mMojo.executeCommand( tCmd );
        verify();

    }

    public void testExecuteCommandWithError() throws MojoExecutionException, MojoFailureException, CommandLineException
    {
        mMojo.setFailOnError( true );
        Commandline tCmd = new Commandline();
        tCmd = new Commandline();
        tCmd.setExecutable( "java" );
        tCmd.createArgument().setValue( "totalInvalide" );
        try
        {
            mMojo.executeCommand( tCmd );
            fail( "Should throw a MojoExecutionException" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "Unable to run Fitnesse, exit code [1]", e.getMessage() );
        }
        verify();

        mMojo.setFailOnError( false );
        try
        {
            mMojo.executeCommand( tCmd );
            fail( "Should throw a MojoExecutionException" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "Unable to run Fitnesse, exit code [1]", e.getMessage() );
        }
        verify();
    }

}
