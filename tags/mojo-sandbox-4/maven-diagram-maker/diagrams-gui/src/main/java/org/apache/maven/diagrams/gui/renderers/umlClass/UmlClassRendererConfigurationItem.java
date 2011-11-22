package org.apache.maven.diagrams.gui.renderers.umlClass;

import org.apache.maven.diagrams.gui.renderers.RendererConfigurationItemImpl;

public class UmlClassRendererConfigurationItem extends RendererConfigurationItemImpl
{
    private boolean full_class_names;

    public UmlClassRendererConfigurationItem( String a_name, boolean a_visible, boolean a_full_class_names )
    {
        super( a_name, a_visible );
        full_class_names=a_full_class_names;
    }

    public void setFull_class_names( boolean full_class_names )
    {
        this.full_class_names = full_class_names;
    }

    public boolean getFull_class_names()
    {
        return full_class_names;
    }
}
