package org.codehaus.mojo.minijar.resource;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.IOUtil;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileWriter;

/**
 * Components XML file filter.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class NoopResourceTransformer
    implements ResourceTransformer
{
    public boolean canTransformResource( String resource )
    {
        return false;
    }

    public void processResource( File componentsXml )
        throws IOException, XmlPullParserException
    {
        throw new UnsupportedOperationException();
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
