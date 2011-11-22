package org.codehaus.mojo.fitnesse;

public class Fitnesse
{
    enum PageType
    {
        suite, test
    };

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
    private PageType type;

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

    @Override
    public String toString()
    {
        return "Fitnesse address=http://" + hostName + ":" + port + "/" + pageName;
    }

    void setPageName( String pPageName )
    {
        pageName = pPageName;
    }

    public PageType getType()
    {
        PageType tResult;
        if ( type == null )
        {
            if ( pageName.startsWith( "Suite" ) )
            {
                tResult = PageType.suite;
            }
            else
            {
                tResult = PageType.test;
            }
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

}
