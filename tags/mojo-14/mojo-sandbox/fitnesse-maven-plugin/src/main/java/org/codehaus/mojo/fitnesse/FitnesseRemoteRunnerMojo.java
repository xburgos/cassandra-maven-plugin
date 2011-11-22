package org.codehaus.mojo.fitnesse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This goal uses the <code>fitnesse.runner.TestRunner</code> class for calling a remote FitNesse web page and
 * executes the <i>tests</i> or <i>suites</i> locally into a forked JVM. It's possible to define several pages and/or
 * servers.
 * 
 * @goal remotecall
 * @aggregator
 */
public class FitnesseRemoteRunnerMojo extends FitnesseAbstractMojo
{

    public static final String FITNESSE_RESULT_PREFIX = "/fitnesseResult";

    public static final String START_REPORT_TAG_KO = "document.getElementById(\"test-summary\").className = \"fail\"";

    public static final String START_REPORT_TAG_KO2 = "document.getElementById(\"test-summary\").className = \"error\"";

    public static final String START_REPORT_TAG_OK = "document.getElementById(\"test-summary\").className = \"pass\"";

    /**
     * @parameter expression="${project.build.directory}/fitnesse"
     * @required
     */
    private String workingDir;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        new File( this.workingDir ).mkdirs();
        checkConfiguration();
        getLog().info( "Found " + getFitnesseSize() + " Fitnesse configuration." );
        for ( int i = 0; i < getFitnesseSize(); i++ )
        {
            callFitnesse( i );
        }

    }

    /**
     * Call a Fitnesse server page.
     * 
     * @param pServerConfPosition
     *            The number of the Fitnesse configuration.
     * @throws MojoFailureException
     * @throws MojoExecutionException
     */
    void callFitnesse( int pServerConfPosition ) throws MojoFailureException, MojoExecutionException
    {
        Fitnesse tServer = getFitnesse( pServerConfPosition );

        File tOutputFile =
            new File( this.workingDir + FITNESSE_RESULT_PREFIX + "_" + tServer.getHostName() + "_"
                            + tServer.getPageName() + ".html" );

        if ( tOutputFile.exists() )
        {
            tOutputFile.delete();
        }
        try
        {
            tOutputFile.createNewFile();
            ByteArrayOutputStream tOut = new ByteArrayOutputStream();
            getRemoteResource( "http://" + tServer.getHostName() + ":" + tServer.getPort() + "/"
                            + tServer.getPageName() + "?" + tServer.getType(), tOut, tServer );
            transformHtml( new ByteArrayInputStream( tOut.toByteArray() ), new FileWriter( tOutputFile ) );
            checkFailure( tOut.toString(), tOutputFile.getAbsolutePath() );

        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to create File [" + tOutputFile.getAbsolutePath() + "].", e );
        }

    }

    void checkFailure( String pFileContent, String pFileName ) throws MojoFailureException, MojoExecutionException
    {
        if ( isFailOnError() )
        {
            int tIndexOk = pFileContent.indexOf( START_REPORT_TAG_OK );
            int tIndexKo = pFileContent.indexOf( START_REPORT_TAG_KO );
            int tIndexKo2 = pFileContent.indexOf( START_REPORT_TAG_KO2 );
            if ( tIndexOk == -1 )
            {
                if ( ( tIndexKo == -1 ) && ( tIndexKo2 == -1 ) )
                {
                    throw new MojoExecutionException( "Unable to find failure result into FitNesse page, resultFile=["
                                    + pFileName + "]." );
                }
                else
                {
                    throw new MojoFailureException( "FitNesse page fail, resultFile=[" + pFileName + "]." );
                }
            }
            else
            {
                if ( ( tIndexKo == -1 ) && ( tIndexKo2 == -1 ) )
                {
                    // SUCCESS
                }
                else
                {
                    throw new MojoExecutionException(
                                                      "Find both success and fail result into FitNesse page , resultFile=["
                                                                      + pFileName + "]." );
                }
            }
        }

    }

    void transformHtml( InputStream pIn, Writer pOut ) throws IOException
    {
        String tHtml = getString( pIn );
        int curPosStart = tHtml.indexOf( "<title>" ) + 7;
        int curPosEnd = tHtml.indexOf( "</title>" );
        pOut.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.DTD\">\r\n" );
        pOut.append( "<html>\r\n" );
        pOut.append( "\t<head>\r\n" );
        pOut.append( "\t\t<title>" ).append( tHtml.substring( curPosStart, curPosEnd ) ).append( "</title>\r\n" );
        pOut.append( "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"fitnesse_base.css\" media=\"screen\"/>\r\n" );
        pOut.append( "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"fitnesse_print.css\" media=\"print\"/>\r\n" );
        pOut.append( "\t\t<script src=\"fitnesse.js\" type=\"text/javascript\"></script>\r\n" );
        pOut.append( "\t</head>\r\n" );
        pOut.append( "\t<body>\r\n" );
        curPosStart = tHtml.indexOf( "<div class=\"main\">" );
        tHtml = tHtml.substring( curPosStart, tHtml.length() );
        pOut.append( tHtml.replaceAll( "/files/", "" ) );
        pOut.flush();
    }

    private String getString( InputStream pIn ) throws IOException
    {
        StringBuffer tBuf = new StringBuffer();
        byte[] tbytes = new byte[512];
        int tReadBytes = pIn.read( tbytes );
        while ( tReadBytes >= 0 )
        {
            tBuf.append( new String( tbytes, 0, tReadBytes, "UTF-8" ) );
            tReadBytes = pIn.read( tbytes );
        }
        return tBuf.toString();
    }

    void getRemoteResource( String pUrl, OutputStream pOutStream, Fitnesse pServer ) throws MojoExecutionException
    {
        try
        {
            HttpClient tClient = new HttpClient();
            getLog().info( "Request resources from [" + pUrl + "]" );
            if ( pServer.getServerId() != null )
            {
                tClient.getParams().setAuthenticationPreemptive( true );
                Credentials defaultcreds = getCredential( pServer.getServerId() );
                AuthScope tAuthScope = new AuthScope( pServer.getHostName(), pServer.getPort(), AuthScope.ANY_REALM );
                tClient.getState().setCredentials( tAuthScope, defaultcreds );
                getLog().info( "Use credential for remote connection" );
            }
            HttpMethod tMethod = new GetMethod( pUrl );
            int statusCode = tClient.executeMethod( tMethod );
            if ( statusCode != 200 )
            {
                throw new MojoExecutionException( "Bad response code from resource [" + pUrl + "]" );
            }

            InputStream tResponseStream = tMethod.getResponseBodyAsStream();
            byte[] tbytes = new byte[512];
            int tReadBytes = tResponseStream.read( tbytes );
            while ( tReadBytes >= 0 )
            {
                pOutStream.write( tbytes, 0, tReadBytes );
                tReadBytes = tResponseStream.read( tbytes );
            }
            pOutStream.flush();
            tMethod.releaseConnection();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to read FitNesse server response.", e );
        }
        finally
        {
            try
            {
                pOutStream.close();
            }
            catch ( IOException e )
            {
                getLog().error( "Unable to close Stream." );
            }
        }
    }

    public void setWorkingDir( String pWorkingDir )
    {
        workingDir = pWorkingDir;
    }

}
