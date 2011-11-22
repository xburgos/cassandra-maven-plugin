package org.codehaus.plexus.shade.relocation;

/** @author Jason van Zyl */
public class SimpleRelocator
    implements Relocator
{
    private String pattern;

    public SimpleRelocator( String pattern )
    {
        this.pattern = pattern;
    }

    public boolean canRelocate( String clazz )
    {
        return clazz.startsWith( pattern );
    }

    public String relocate( String clazz )
    {
        return "hidden/" + clazz;
    }
}
