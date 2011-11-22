package org.apache.maven.diagrams.connector_api;

/**
 * Base exception to handle all connector's faults 
 * @author Piotr Tabor
 *
 */
public class ConnectorException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ConnectorException()
    {
        super();
    }

    public ConnectorException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConnectorException( String message )
    {
        super( message );
    }

    public ConnectorException( Throwable cause )
    {
        super( cause );
    }

}
