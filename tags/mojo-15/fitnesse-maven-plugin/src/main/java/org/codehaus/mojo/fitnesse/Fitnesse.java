package org.codehaus.mojo.fitnesse;

import org.apache.maven.plugin.MojoExecutionException;

public class Fitnesse
{
    static final String PAGE_TYPE_SUITE = "suite";

    static final String PAGE_TYPE_TEST = "test";

    public Fitnesse( String hostName, int port, String pageName )
    {
        super();
        this.hostName = hostName;
        this.port = port;
        this.pageName = pageName;
    }

    public Fitnesse()
    {
        super();
    }

    /**
     * FitNesse server name.
     * 
     * @parameter default-value="localhost"
     */
    private String hostName = "localhost";

    /**
     * Type of page, Suite or Test. Default value depend of the page name.
     * 
     * @parameter
     */
    private String type;

    /**
     * Server port of fitnesse.
     * 
     * @parameter default-value="80"
     */
    private int port = 80;

    /**
     * Name of the fitnesse page @ required
     * @parameter default-value="MustBeDefinedByProject"
     */
    private String pageName = "MustBeDefinedByProject";

    /**
     * Id of the settings.server, this allow to provide credential fot FitNesse basic authentification.
     * 
     * @parameter
     */
    private String serverId;

    public String getHostName()
    {
        return hostName;
    }

    public String getPageName()
    {
        return pageName;
    }

    public int getPort()
    {
        return port;
    }

    /**
     * @Override
     */
    public String toString()
    {
        return "Fitnesse address=http://" + hostName + ":" + port + "/" + pageName;
    }

    void setPageName( String pPageName )
    {
        pageName = pPageName;
    }

    public String getType() throws MojoExecutionException
    {
        String tResult;
        String tShortPageName =
            ( pageName.indexOf( "." ) == -1 ? pageName : pageName.substring( pageName.indexOf( "." ) + 1 ) );
        type = ( type == null ? null : type.toLowerCase() );

        if ( type == null || type.length() == 0 )
        {
            if ( tShortPageName.startsWith( "Suite" ) )
            {
                tResult = PAGE_TYPE_SUITE;
            }
            else if ( tShortPageName.startsWith( "Test" ) )
            {
                tResult = PAGE_TYPE_TEST;
            }
            else
            {
                throw new MojoExecutionException( "Parameter 'type' is mandatory is the page name doesn't "
                                + "begin with 'Test' or 'Suite' according to FitNesse convention. FitNesse server is: "
                                + this.toString() );
            }
        }
        else if ( !PAGE_TYPE_SUITE.equals( type ) && !PAGE_TYPE_TEST.equals( type ) )
        {
            throw new MojoExecutionException( "Invalid type [" + type + "] for the server [" + this.toString()
                            + "], should be either [suite] or [test]." );
        }
        else
        {
            tResult = type;
        }
        return tResult;
    }

    public void setHostName( String hostName )
    {
        this.hostName = hostName;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public String getServerId()
    {
        return serverId;
    }

    public void setServerId( String serverId )
    {
        this.serverId = serverId;
    }

    public void checkConfiguration() throws MojoExecutionException
    {
        if ( hostName == null || hostName.length() == 0 )
        {
            throw new MojoExecutionException( "Fitnesse host is mandatory." );
        }
        if ( port <= 0 || port > 65535 )
        {
            throw new MojoExecutionException( "The port should be a valid IP port [" + port + "]." );
        }
        if ( pageName == null || pageName.length() == 0 )
        {
            throw new MojoExecutionException( "Fitnesse page name is mandatory." );
        }
    }

    public void setType( String type )
    {
        this.type = type;
    }

}
