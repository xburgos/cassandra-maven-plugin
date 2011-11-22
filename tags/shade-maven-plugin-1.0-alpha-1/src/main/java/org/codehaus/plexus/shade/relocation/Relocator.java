package org.codehaus.plexus.shade.relocation;

/** @author Jason van Zyl */
public interface Relocator
{
    String ROLE = Relocator.class.getName();

    boolean canRelocate( String clazz );

    String relocate( String clazz );
}
