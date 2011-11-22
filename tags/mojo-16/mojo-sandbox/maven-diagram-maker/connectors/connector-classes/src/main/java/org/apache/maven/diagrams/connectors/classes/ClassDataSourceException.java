package org.apache.maven.diagrams.connectors.classes;

/**
 * Common exception for all problems with ClassDataSource's
 * 
 * @author Piotr Tabor 
 */
public class ClassDataSourceException extends Exception
{
    private static final long serialVersionUID = 5994291237121022242L;

    public ClassDataSourceException()
    {
        super();
    }

    public ClassDataSourceException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ClassDataSourceException( String message )
    {
        super( message );
    }

    public ClassDataSourceException( Throwable cause )
    {
        super( cause );
    }

}
