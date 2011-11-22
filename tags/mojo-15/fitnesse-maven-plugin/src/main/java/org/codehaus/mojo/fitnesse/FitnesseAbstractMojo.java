package org.codehaus.mojo.fitnesse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;

public abstract class FitnesseAbstractMojo extends AbstractMojo
{

    public static final String FITNESSE_RESULT_PREFIX = "fitnesseResult";

    /**
     * This is the list of FitNesse server pages.<br/> A FitNesse tag is compose of the nested tags:<BR/> <code>
     * &lt;fitnesses&gt;<BR/>
     *     &lt;fitnesse&gt;<BR/>
     *         &lt;pageName&gt;This is the only required parameter, the name of 
     * the FitNesse page&lt;/pageName&gt;<BR/>
     *         &lt;hostName&gt;default is <i>locahost</i>&lt;/hostName&gt;<BR/>
     *         &lt;port&gt;: default is <i>80</i>;&lt;/port&gt;<BR/>
     *         &lt;serverId&gt;ServerId defined in your settings.xml, this allows to use credentials 
     * (basic athentification) for calling your FitNesse pages&lt;/serverId&gt;<BR/>
     *         &lt;type&gt;Override the default type of the page (Suite or Test).;&lt;/type&gt;<BR/>
     *         &lt;/fitnesse&gt;<BR/>
     *     ... <BR/>
     * &lt;/fitnesses&gt;:<BR/>
     * </code>
     * 
     * @parameter
     * @required
     */
    private List fitnesses;

    /**
     * Fail the build if fitnesse pages have error.
     * 
     * @parameter default-value=false
     */
    private boolean failOnError;

    /**
     * Date format for FitNesse page timestamp.
     * 
     * @parameter default-value="dd/MM/yyyy HH:mm"
     */
    private String dateFormat;

    /**
     * List of the servers.
     * 
     * @parameter expression="${settings.servers}"
     * @required
     * @readonly
     */
    public List servers = new ArrayList();

    /**
     * @parameter expression="${project.build.directory}/fitnesse"
     * @required
     */
    protected String workingDir;

    /**
     * @parameter expression="${fitnesse.page}"
     */
    String cmdFitnessePage;

    /**
     * @parameter expression="${fitnesse.hostName}"
     */
    String cmdFitnesseHostName;

    /**
     * @parameter expression="${fitnesse.port}" defaul="-1"
     */
    int cmdFitnessePort = -1;

    void checkConfiguration() throws MojoExecutionException
    {
        changeConfigWithCmdLineParameters();

        if ( fitnesses == null || fitnesses.size() == 0 )
        {
            String errorMessage =
                "Your should configure at least one Fitnesse server. "
                                + "Check your maven-fitnesse-plugin configuration.";
            getLog().error( errorMessage );
            throw new MojoExecutionException( errorMessage );
        }
        else
        {
            for ( Iterator tIt = fitnesses.iterator(); tIt.hasNext(); )
            {
                ( (Fitnesse) tIt.next() ).checkConfiguration();
            }
        }
    }

    private void changeConfigWithCmdLineParameters()
    {
        if ( cmdFitnesseHostName != null || cmdFitnessePort != -1 || cmdFitnessePage != null )
        {
            getLog().info( "Command line parameters detected, merging with pom configuration." );
            Fitnesse tFit =
                ( fitnesses != null && fitnesses.size() > 0 ? (Fitnesse) fitnesses.get( 0 )
                                : new Fitnesse( "localhost", 80, cmdFitnessePage ) );
            fitnesses = new ArrayList();
            fitnesses.add( tFit );
            if ( cmdFitnessePage != null )
            {
                tFit.setPageName( cmdFitnessePage );
            }
            if ( cmdFitnesseHostName != null )
            {
                tFit.setHostName( cmdFitnesseHostName );
            }
            if ( cmdFitnessePort != -1 )
            {
                tFit.setPort( cmdFitnessePort );
            }
            getLog().info(
                           "using url=[http://" + tFit.getHostName() + ":" + tFit.getPort() + "/" + tFit.getPageName()
                                           + "]" );
        }
    }

    public void setFitnesses( List pFitnesses )
    {
        fitnesses = pFitnesses;
    }

    protected Fitnesse getFitnesse( int pPosition )
    {
        return (Fitnesse) fitnesses.get( pPosition );
    }

    protected int getFitnesseSize()
    {
        return fitnesses.size();
    }

    UsernamePasswordCredentials getCredential( String pServerId ) throws MojoExecutionException
    {
        UsernamePasswordCredentials tResult = null;
        Server tServer;
        for ( Iterator tEnum = servers.iterator(); tEnum.hasNext(); )
        {
            tServer = (Server) tEnum.next();
            if ( pServerId.equals( tServer.getId() ) )
            {
                tResult = new UsernamePasswordCredentials( tServer.getUsername(), tServer.getPassword() );
            }
        }
        if ( tResult == null )
        {
            throw new MojoExecutionException( "Unable to find credential for ServerId=[" + pServerId
                            + "], you must define a <Server> tag in your settings.xml for this Id." );
        }
        return tResult;
    }

    public void addServer( Server pServer )
    {
        this.servers.add( pServer );
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError( boolean failOnError )
    {
        this.failOnError = failOnError;
    }

    void transformHtml( InputStream pIn, Writer pOut, String pOutputFileName, String pStatus )
        throws IOException, MojoExecutionException
    {
        String tHtml = FileUtil.getString( pIn );
        int curPosStart = tHtml.indexOf( "<title>" ) + 7;
        int curPosEnd = tHtml.indexOf( "</title>" );
        pOut.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.DTD\">\r\n" );
        pOut.append( "<html>\r\n" );
        pOut.append( "\t<head>\r\n" );
        pOut.append( "\t\t<title>" ).append( tHtml.substring( curPosStart, curPosEnd ) );
        pOut.append( " [" ).append( getCurrentTimeAsString() ).append( "]</title>\r\n" );
        pOut.append( "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"fitnesse_base.css\" media=\"screen\"/>\r\n" );
        pOut.append( "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"fitnesse_print.css\" media=\"print\"/>\r\n" );
        pOut.append( "\t\t<script src=\"fitnesse.js\" type=\"text/javascript\"></script>\r\n" );
        pOut.append( "\t</head>\r\n" );
        pOut.append( "\t<body>\r\n" );
        pOut.append( "\t\t<div id=\"execution-status\">\r\n" );
        pOut.append( "\t\t\t<a href=\"" );
        pOut.append( pOutputFileName );
        pOut.append( "\"><img src=\"images/executionStatus/" );
        pOut.append( getImage( pStatus ) );
        pOut.append( "\"/></a>\r\n" );
        pOut.append( "\t\t\t<br/>\r\n" );
        pOut.append( "\t\t\t<a href=\"" );
        pOut.append( pOutputFileName );
        pOut.append( "\">Tests Executed " );
        pOut.append( pStatus );
        pOut.append( "</a>\r\n" );
        pOut.append( "\t\t</div>\r\n" );
        pOut.append( "\t\t<h3>Test executed on " + getCurrentTimeAsString() + "</h3>\r\n" );
        curPosStart = tHtml.indexOf( "<div class=\"main\">" );
        tHtml = tHtml.substring( curPosStart, tHtml.length() );
        tHtml = tHtml.replaceAll( "/files/", "" );
        curPosStart = tHtml.indexOf( "<div id=\"execution-status\">" );
        curPosEnd = tHtml.indexOf( "</div>", curPosStart );
        if ( curPosStart >= 0 && curPosEnd >= 0 )
        {
            pOut.append( tHtml.substring( 0, curPosStart ) );
            pOut.append( tHtml.substring( curPosEnd + "</div>".length()+2, tHtml.length() ) );
        }
        else
        {
            pOut.append( tHtml );
        }
        pOut.flush();
    }

    private CharSequence getImage( String pStatus ) throws MojoExecutionException
    {
        if ( FitnessePage.STATUS_OK.equals( pStatus ) )
        {
            return "ok.gif";
        }
        else if ( FitnessePage.STATUS_ERROR.equals( pStatus ) )
        {
            return "error.gif";
        }
        else if ( FitnessePage.STATUS_FAIL.equals( pStatus ) )
        {
            return "output.gif";
        }
        else
        {
            throw new MojoExecutionException( "Invalid status [" + pStatus + "]" );
        }
    }

    protected String getCurrentTimeAsString()
    {
        SimpleDateFormat tFormat = new SimpleDateFormat( dateFormat );
        return tFormat.format( new Date() );
    }

    public void setDateFormat( String pDateFormat )
    {
        dateFormat = pDateFormat;
    }

    public String getDateFormat()
    {
        return dateFormat;
    }

    String getTmpFileName( Fitnesse pServer )
    {
        return getResultFileName( pServer, "_tmp", "html" );
    }

    String getFinalFileName( Fitnesse pServer )
    {
        return getResultFileName( pServer, "", "html" );
    }

    abstract String getOutputFileName( Fitnesse pServer );

    abstract String getOutputUrl( Fitnesse pServer );

    protected String getResultFileName( Fitnesse pServer, String pPostfix, String pExtension )
    {
        return this.workingDir + "/" + FITNESSE_RESULT_PREFIX + "_" + pServer.getHostName() + "_"
                        + pServer.getPageName() + pPostfix + "." + pExtension;
    }

}
