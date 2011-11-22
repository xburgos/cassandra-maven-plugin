package org.codehaus.mojo.shade.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;

public class ApacheLegalResourceTransformer
    implements ResourceTransformer
{
    public boolean canTransformResource( String resource )
    {
        String s = resource.toLowerCase();

        if ( s.startsWith( "meta-inf/license.txt" ) || s.equals( "meta-inf/license" ) ||
            s.equals( "meta-inf/notice.txt" ) || s.equals( "meta-inf/notice" ) )
        {
            return true;
        }

        return false;
    }

    public void processResource( InputStream is )
        throws IOException
    {
    }

    public boolean hasTransformedResource()
    {
        return false;
    }

    public void modifyOutputStream( JarOutputStream os )
        throws IOException
    {
    }
}
