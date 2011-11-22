package org.codehaus.mojo.minijar.resource;

import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarEntry;

/** @author Jason van Zyl */
public interface MinijarResourceMatcher
{
    boolean keepResourceWithName( String name, InputStream is )
        throws IOException;
}
