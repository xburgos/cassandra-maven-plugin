package org.codehaus.mojo.minijar.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;

/** @author Jason van Zyl */
public interface ResourceTransformer
{
    boolean canTransformResource( String resource );

    boolean hasTransformedResource();

    File getTransformedResource()
        throws IOException;

    void processResource( InputStream is )
        throws IOException;    
}
