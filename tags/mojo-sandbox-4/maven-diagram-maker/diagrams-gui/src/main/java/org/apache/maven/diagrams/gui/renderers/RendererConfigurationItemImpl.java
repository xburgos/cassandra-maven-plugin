package org.apache.maven.diagrams.gui.renderers;

public class RendererConfigurationItemImpl implements RendererConfigurationItem
{
    private String name;

    private boolean visible;

    public RendererConfigurationItemImpl( String a_name, boolean a_visible )
    {
        name = a_name;
        visible = a_visible;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible( boolean a_visible )
    {
        visible = a_visible;
    }
}
