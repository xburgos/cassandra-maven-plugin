package org.codehaus.plexus.shade.mojo;

import java.util.Set;

/** @author Jason van Zyl */
public class Resource
{
    private Set includes;

    private Set excludes;

    public Set getIncludes()
    {
        return includes;
    }

    public Set getExcludes()
    {
        return excludes;
    }
}
