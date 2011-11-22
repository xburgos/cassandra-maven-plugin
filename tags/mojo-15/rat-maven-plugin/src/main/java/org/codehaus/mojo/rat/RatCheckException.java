package org.codehaus.mojo.rat;

import org.apache.maven.plugin.MojoFailureException;

public class RatCheckException extends MojoFailureException
{
    public RatCheckException( String message )
    {
        super( message );
    }
}
