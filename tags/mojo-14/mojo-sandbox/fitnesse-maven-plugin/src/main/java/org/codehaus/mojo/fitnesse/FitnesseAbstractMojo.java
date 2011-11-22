package org.codehaus.mojo.fitnesse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;

public abstract class FitnesseAbstractMojo extends AbstractMojo
{

    /**
     * This is the list of FitNesse server pages.<br/> A FitNesse tag is compose of the nested tags:<BR/> <code>
     * &lt;fitnesses&gt;<BR/>
     * &#160;&#160;&lt;fitnesse&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;pageName&gt;This is the only required parameter, the name of 
     * the FitNesse page&lt;/pageName&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;hostName&gt;default is <i>locahost</i>&lt;/hostName&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;port&gt;: default is <i>80</i>;&lt;/port&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;serverId&gt;ServerId defined in your settings.xml, this allows to use credentials 
     * (basic athentification) for calling your FitNesse pages&lt;/serverId&gt;<BR/>
     * &#160;&#160;&lt;/fitnesse&gt;<BR/>
     * &#160;&#160;... <BR/>
     * &lt;/fitnesses&gt;:<BR/>
     * </code>
     * 
     * @parameter
     * @required
     */
    private List<Fitnesse> fitnesses;

    /**
     * Fail the build if fitnesse pages have error.
     * 
     * @parameter default-value=false
     */
    private boolean failOnError;

    /**
     * List of the servers.
     * 
     * @parameter expression="${settings.servers}"
     * @required
     * @readonly
     */
    public List<Server> servers = new ArrayList<Server>();

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
        if ( cmdFitnessePage != null )
        {
            getLog().info( "Command line parameters detected, merging with pom configuration." );
            Fitnesse tFit =
                ( fitnesses != null && fitnesses.size() > 0 ? fitnesses.get( 0 ) : new Fitnesse( "localhost", 80,
                                                                                                 cmdFitnessePage ) );
            tFit.setPageName( cmdFitnessePage );
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
            fitnesses = new ArrayList<Fitnesse>();
            fitnesses.add( tFit );
        }

        if ( fitnesses == null || fitnesses.size() == 0 )
        {
            String errorMessage =
                "Your should configure at least one Fitnesse server. "
                                + "Check your maven-fitnesse-plugin configuration.";
            getLog().error( errorMessage );
            throw new MojoExecutionException( errorMessage );
        }
    }

    public void setFitnesses( List<Fitnesse> pFitnesses )
    {
        fitnesses = pFitnesses;
    }

    protected Fitnesse getFitnesse( int pPosition )
    {
        return fitnesses.get( pPosition );
    }

    protected int getFitnesseSize()
    {
        return fitnesses.size();
    }

    UsernamePasswordCredentials getCredential( String pServerId ) throws MojoExecutionException
    {
        UsernamePasswordCredentials tResult = null;
        for ( Server tServer : servers )
        {
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

}
