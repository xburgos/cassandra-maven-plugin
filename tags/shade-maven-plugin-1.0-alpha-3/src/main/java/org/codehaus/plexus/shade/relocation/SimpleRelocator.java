package org.codehaus.plexus.shade.relocation;

/** @author Jason van Zyl */
public class SimpleRelocator
    implements Relocator
{
    private String pattern;

    private String exclude;

    public SimpleRelocator( String pattern, String exclude )
    {
        this.pattern = pattern;

        this.exclude = exclude;
    }

    public boolean canRelocate( String clazz )
    {
        return !(exclude != null && clazz.equals( exclude )) && clazz.startsWith( pattern );
    }

    public String relocate( String clazz )
    {
        return "hidden/" + clazz;
    }
}
