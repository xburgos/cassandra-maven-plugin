package org.apache.maven.diagrams.gui.bindings.connectors;

public class ResolveBindingException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -3251125949847167842L;

    public ResolveBindingException()
    {
        super();
    }

    public ResolveBindingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ResolveBindingException( String message )
    {
        super( message );
    }

    public ResolveBindingException( Throwable cause )
    {
        super( cause );
    }

}
