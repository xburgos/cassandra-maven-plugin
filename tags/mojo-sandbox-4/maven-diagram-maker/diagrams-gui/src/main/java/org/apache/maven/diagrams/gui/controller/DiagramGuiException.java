package org.apache.maven.diagrams.gui.controller;

public class DiagramGuiException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DiagramGuiException()
    {
        super();
    }

    public DiagramGuiException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public DiagramGuiException( String message )
    {
        super( message );
    }

    public DiagramGuiException( Throwable cause )
    {
        super( cause );
    }

}
