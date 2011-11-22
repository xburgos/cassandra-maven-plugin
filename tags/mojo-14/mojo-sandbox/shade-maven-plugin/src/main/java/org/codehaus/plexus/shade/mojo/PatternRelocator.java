package org.codehaus.plexus.shade.mojo;

import java.util.List;

/** @author Jason van Zyl */
public class PatternRelocator
{
    private String pattern;

    private List excludes;

    public String getPattern()
    {
        return pattern;
    }

    public List getExcludes()
    {
        return excludes;
    }
}
