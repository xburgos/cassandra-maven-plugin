package org.codehaus.mojo.fitnesse;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

import sun.security.action.GetLongAction;

public class FitnesseAbstractMojoTest extends MockObjectTestCase
{
    private Mock mMockLog;

    public static class MojoTest extends FitnesseAbstractMojo
    {
        public MojoTest()
        {
        }

        public MojoTest( Log pLog )
        {
            setLog( (Log) pLog );
        }

        public void execute() throws MojoExecutionException, MojoFailureException
        {
        }

    }

    public void testGetCredentialOk() throws MojoExecutionException
    {
        MojoTest tMojo = getMojo();
        UsernamePasswordCredentials tCred = tMojo.getCredential( "Server0" );
        assertEquals( "Login0", tCred.getUserName() );
        assertEquals( "Password0", tCred.getPassword() );
        tCred = tMojo.getCredential( "Server1" );
        assertEquals( "Login1", tCred.getUserName() );
        assertEquals( "Password1", tCred.getPassword() );
        tCred = tMojo.getCredential( "Server2" );
        assertEquals( "Login2", tCred.getUserName() );
        assertEquals( "Password2", tCred.getPassword() );
    }

    public void testGetCredentialNotFound()
    {
        MojoTest tMojo = getMojo();
        try
        {
            tMojo.getCredential( "Server3" );
            fail( "Should not find credential" );
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "Unable to find credential for ServerId=[Server3], "
                            + "you must define a <Server> tag in your settings.xml for this Id.", e.getMessage() );
        }
    }

    private MojoTest getMojo()
    {
        MojoTest tMojo = new MojoTest();
        Server tServer = new Server();
        tServer.setId( "Server0" );
        tServer.setUsername( "Login0" );
        tServer.setPassword( "Password0" );
        tMojo.addServer( tServer );
        tServer = new Server();
        tServer.setId( "Server1" );
        tServer.setUsername( "Login1" );
        tServer.setPassword( "Password1" );
        tMojo.addServer( tServer );
        tServer = new Server();
        tServer.setId( "Server2" );
        tServer.setUsername( "Login2" );
        tServer.setPassword( "Password2" );
        tMojo.addServer( tServer );
        return tMojo;
    }

    public void testCheckConfigurationWithoutCommandLineNorConfig() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        mMockLog.stubs().method( "error" ).with(
                                                 eq( "Your should configure at least one Fitnesse "
                                                                 + "server. Check your maven-fitnesse-plugin configuration." ) );
        try
        {
            tMojo.setFitnesses( null );
            tMojo.checkConfiguration();
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "Your should configure at least one Fitnesse server. "
                            + "Check your maven-fitnesse-plugin configuration.", e.getMessage() );
        }
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tMojo.setFitnesses( tList );
        try
        {
            tMojo.checkConfiguration();
        }
        catch ( MojoExecutionException e )
        {
            assertEquals( "Your should configure at least one Fitnesse server. "
                            + "Check your maven-fitnesse-plugin configuration.", e.getMessage() );
        }
    }

    public void testCheckConfigurationWithoutCommandLineAndOneFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MaPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    public void testCheckConfigurationWithoutCommandLineAndSeveralFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tList.add( new Fitnesse( "localhost2", 8080, "MaPage2" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 2, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MaPage", tMojo.getFitnesse( 0 ).getPageName() );
        assertEquals( "localhost2", tMojo.getFitnesse( 1 ).getHostName() );
        assertEquals( 8080, tMojo.getFitnesse( 1 ).getPort() );
        assertEquals( "MaPage2", tMojo.getFitnesse( 1 ).getPageName() );
    }

    public void testCheckConfigurationWithCommandLineAndWithoutConfig() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.setFitnesses( null );
        tMojo.cmdFitnessePage = "MaPage";
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MaPage", tMojo.getFitnesse( 0 ).getPageName() );

        tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.cmdFitnessePage = "MaPage";
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MaPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    public void testCheckConfigurationWithSimpleCommandLineAndOneFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.cmdFitnessePage = "MyPage";
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MyPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    public void testCheckConfigurationWithFullCommandLineAndOneFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.cmdFitnessePage = "MyPage";
        tMojo.cmdFitnesseHostName="myHost";
        tMojo.cmdFitnessePort=8080;
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "myHost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 8080, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MyPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    public void testCheckConfigurationWithCommandLineAndSeveralFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.cmdFitnessePage = "MyPage";
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tList.add( new Fitnesse( "localhost2", 8080, "MaPage2" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "localhost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 80, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MyPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    public void testCheckConfigurationWithFullCommandLineAndSeveralFitnesse() throws MojoExecutionException
    {
        MojoTest tMojo = new MojoTest( (Log) mMockLog.proxy() );
        tMojo.cmdFitnessePage = "MyPage";
        tMojo.cmdFitnesseHostName="myHost";
        tMojo.cmdFitnessePort=8081;
        List<Fitnesse> tList = new ArrayList<Fitnesse>();
        tList.add( new Fitnesse( "localhost", 80, "MaPage" ) );
        tList.add( new Fitnesse( "localhost2", 8080, "MaPage2" ) );
        tMojo.setFitnesses( tList );
        tMojo.checkConfiguration();
        assertEquals( 1, tMojo.getFitnesseSize() );
        assertEquals( "myHost", tMojo.getFitnesse( 0 ).getHostName() );
        assertEquals( 8081, tMojo.getFitnesse( 0 ).getPort() );
        assertEquals( "MyPage", tMojo.getFitnesse( 0 ).getPageName() );
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mMockLog = mock( Log.class );
        mMockLog.stubs().method( "info" ).withAnyArguments();

    }
}
