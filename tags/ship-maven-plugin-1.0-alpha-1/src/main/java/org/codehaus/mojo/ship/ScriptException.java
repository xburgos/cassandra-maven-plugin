package org.codehaus.mojo.ship;

public class ScriptException
    extends Exception
{
    /**
     * Creates a new exception with the specified cause.
     *
     * @param cause The cause, may be <code>null</code>.
     */
    public ScriptException( Throwable cause )
    {
        super( cause );
    }

    public ScriptException( String message )
    {
        super( message );
    }

    public ScriptException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
