package org.codehaus.mojo.fitnesse;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.net.ServerSocketFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class FitnesseRemoteRunnerMojoTest extends MockObjectTestCase
{
    protected String error;
    private String mRequest;
	private FitnesseRemoteRunnerMojo mMojo = null;
	private Mock mMockLog = null;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		mMojo = getMojo();
	}

    private FitnesseRemoteRunnerMojo getMojo()
    {
        FitnesseRemoteRunnerMojo tMojo = new FitnesseRemoteRunnerMojo();
        tMojo.setFailOnError( false );
        tMojo.setFailOnError( false );
        tMojo.setWorkingDir( "target/fitnesse" );
        
        List<Fitnesse> tFitnesses = new ArrayList<Fitnesse>();
        tFitnesses.add( new Fitnesse() );
        tMojo.setFitnesses( tFitnesses );
        
        mMockLog = mock( Log.class );
        mMockLog.stubs().method("info").withAnyArguments();
        tMojo.setLog((Log)mMockLog.proxy());
        
        return tMojo;
    }
    
    public void testTransformHtmlForSimpleTest() throws IOException
    {
        String tSrcFile = getClass().getClassLoader().getResource( "remote/TestBrut.html" ).getPath();
        String tExpectedFile = getClass().getClassLoader().getResource( "remote/TestResult.html" ).getPath();
        compareTransformFile( tSrcFile, tExpectedFile );
    }

    public void testTransformHtmlForSuite() throws IOException
    {
        String tSrcFile = getClass().getClassLoader().getResource( "remote/SuiteBrut.html" ).getPath();
        String tExpectedFile = getClass().getClassLoader().getResource( "remote/SuiteResult.html" ).getPath();
        compareTransformFile( tSrcFile, tExpectedFile );
    }

    private void compareTransformFile( String tSrcFile, String tExpectedFile )
        throws FileNotFoundException, IOException
    {
        FileInputStream tInput = new FileInputStream( tExpectedFile );
        String tExpected = getFileAsString( tInput );
        ByteArrayOutputStream tTransform = new ByteArrayOutputStream();
        mMojo.transformHtml( new FileInputStream( tSrcFile ), new OutputStreamWriter( tTransform ) );
        StringTokenizer tTokExp = new StringTokenizer( tExpected, "\n" );
        StringTokenizer tTokRes = new StringTokenizer( tTransform.toString(), "\n" );
        while ( tTokExp.hasMoreElements() )
        {
            assertEquals( tTokExp.nextToken(), tTokRes.nextToken() );
        }
        assertFalse( tTokRes.hasMoreElements() );
    }

    private String getFileAsString( FileInputStream tInput ) throws IOException
    {
        ByteArrayOutputStream tExpectedString = new ByteArrayOutputStream();
        byte[] tBytes = new byte[512];
        int tRead = tInput.read( tBytes );
        while ( tRead >= 0 )
        {
            tExpectedString.write( tBytes, 0, tRead );
            tRead = tInput.read( tBytes );
        }
        return tExpectedString.toString();
    }

    public void testGetRemoteResourceWithoutCredential() throws IOException, MojoExecutionException
    {
        List<Fitnesse> tFitnesses = new ArrayList<Fitnesse>();
        Fitnesse tServer = new Fitnesse();
        tServer.setHostName( "localhost" );
        tServer.setPort( 83 );
        tFitnesses.add( tServer );
        mMojo.setFitnesses( tFitnesses );

        startServer();
        ByteArrayOutputStream tOut = new ByteArrayOutputStream();
        try
        {
            mMojo.getRemoteResource( "http://localhost:83/url", tOut, tServer );
            fail();
        }
        catch ( MojoExecutionException e )
        {
            assertNotNull( e.getCause() );
            assertEquals( "Connection reset", e.getCause().getMessage() );
        }

        assertEquals( "GET /url HTTP/1.1\r\n" + "User-Agent: Jakarta Commons-HttpClient/3.0\r\n"
                        + "Host: localhost:83\r\n\r", mRequest );

        tServer.setServerId( "TestId" );
        Server tMavenServer = new Server();
        tMavenServer.setId( "TestId" );
        tMavenServer.setUsername( "myLogin" );
        tMavenServer.setPassword( "myPassword" );
        mMojo.addServer( tMavenServer );
        startServer();
        tOut = new ByteArrayOutputStream();
        try
        {
            mMojo.getRemoteResource( "http://localhost:83/url", tOut, tServer );
            fail();
        }
        catch ( MojoExecutionException e )
        {
            assertNotNull( e.getCause() );
        }

        assertEquals( "GET /url HTTP/1.1\r\n" + "Authorization: Basic bXlMb2dpbjpteVBhc3N3b3Jk\r\n"
                        + "User-Agent: Jakarta Commons-HttpClient/3.0\r\n" + "Host: localhost:83\r\n\r", mRequest );
        assertNull( error );
    }

    private void startServer() throws IOException
    {
        Runnable tRun = new Runnable()
        {
            public void run()
            {
                try
                {
                    ServerSocket tServerSock;
                    tServerSock = ServerSocketFactory.getDefault().createServerSocket( 83 );
                    Socket tSocket = tServerSock.accept();
                    InputStream tIn = tSocket.getInputStream();
                    int tRead = tIn.read();
                    StringBuffer tBuf = new StringBuffer();
                    boolean tIsActive = true;
                    int[] tLastRead = new int[4];
                    while ( tRead != -1 && tIsActive )
                    {
                        tBuf.append( (char) tRead );
                        //System.out.print( (char) tRead );
                        //System.out.flush();
                        tRead = tIn.read();
                        tIsActive = checkActive( tRead, tLastRead );
                    }
                    mRequest = tBuf.toString();
                    OutputStreamWriter tWriter = new OutputStreamWriter( tSocket.getOutputStream() );
                    tWriter.append( "HTTP/1.1 404 Not Found\r\n" );
                    tWriter.append( "Content-Type: text/html; charset=utf-8\r\n" );
                    tWriter.append( "Content-Length: 0\r\n" );
                    tWriter.append( "Connection: close\r\n" );
                    tWriter.append( "Server: FitNesse-20050731\r\n\r" );
                    tSocket.close();
                    Thread.sleep( 100 );
                    tServerSock.close();
                }
                catch ( IOException e )
                {
                    error = e.getMessage();
                }
                catch ( InterruptedException e )
                {
                    error = e.getMessage();
                }
            }

            private boolean checkActive( int read, int[] lastRead )
            {
                lastRead[0] = lastRead[1];
                lastRead[1] = lastRead[2];
                lastRead[2] = lastRead[3];
                lastRead[3] = read;
                return !( lastRead[0] == 13 && lastRead[1] == 10 && lastRead[2] == 13 && lastRead[3] == 10 );
            }
        };
        Thread tThread = new Thread( tRun );
        tThread.setDaemon( true );
        tThread.start();
    }

    public void testCheckFailureWithFailureOn()
        throws FileNotFoundException, IOException, MojoFailureException, MojoExecutionException
    {
        checkReport( true, "remoteFailure/TestOk.html" );
        checkReport( true, "remoteFailure/SuiteOk.html" );
        checkReport( true, "remoteFailure/SuiteInfraOk.html" );
        try
        {
            checkReport( true, "remoteFailure/TestInvalid.html" );
            fail( "should not find result" );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( e.getMessage().startsWith( "Unable to find failure result into FitNesse page, resultFile=[" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/TestInvalid.html]." ) );
        }
        try
        {
            checkReport( true, "remoteFailure/TestInvalid2.html" );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( e.getMessage().startsWith( "Find both success and fail result into FitNesse page , resultFile=" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/TestInvalid2.html]." ) );
        }
        try
        {
            checkReport( true, "remoteFailure/TestFail.html" );
            fail( "report TestFail.html should throw a Failure" );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( e.getMessage().startsWith( "FitNesse page fail, resultFile=" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/TestFail.html]." ) );
        }
        try
        {
            checkReport( true, "remoteFailure/SuiteFail.html" );
            fail( "report SuiteFail.html should throw a Failure" );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( e.getMessage().startsWith( "FitNesse page fail, resultFile=" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/SuiteFail.html]." ) );
        }
        try
        {
            checkReport( true, "remoteFailure/SuiteInfraFail.html" );
            fail( "report SuiteInfraFail.html should throw a Failure" );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( e.getMessage().startsWith( "FitNesse page fail, resultFile=[" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/SuiteInfraFail.html]." ) );
        }

        try
        {
            checkReport( true, "remoteFailure/SuiteException.html" );
            fail( "report SuiteException.html should throw a Failure" );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( e.getMessage().startsWith( "FitNesse page fail, resultFile=[" ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin/target/test-classes/remoteFailure/SuiteException.html]." ) );
        }

    }

    public void testCheckFailureWithFailureOff()
        throws FileNotFoundException, IOException, MojoFailureException, MojoExecutionException
    {
        checkReport( false, "remoteFailure/TestOk.html" );
        checkReport( false, "remoteFailure/SuiteOk.html" );
        checkReport( false, "remoteFailure/SuiteInfraOk.html" );
        checkReport( false, "remoteFailure/TestInvalid.html" );
        checkReport( false, "remoteFailure/TestInvalid2.html" );
        checkReport( false, "remoteFailure/TestFail.html" );
        checkReport( false, "remoteFailure/SuiteFail.html" );
        checkReport( false, "remoteFailure/SuiteInfraFail.html" );
        checkReport( false, "remoteFailure/SuiteException.html" );
    }

    private void checkReport( boolean pFailOnError, String pFileName )
        throws FileNotFoundException, IOException, MojoFailureException, MojoExecutionException
    {
        String tSrcFile = getClass().getClassLoader().getResource( pFileName ).getPath();
        mMojo.setFailOnError( pFailOnError );

        mMojo.checkFailure( getFileAsString( new FileInputStream( tSrcFile ) ), tSrcFile );
    }

}
