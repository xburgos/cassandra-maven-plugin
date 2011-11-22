package org.codehaus.mojo.minijar.resource;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;

/**
 * Components XML file filter.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
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

    public File getTransformedResource()
        throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
