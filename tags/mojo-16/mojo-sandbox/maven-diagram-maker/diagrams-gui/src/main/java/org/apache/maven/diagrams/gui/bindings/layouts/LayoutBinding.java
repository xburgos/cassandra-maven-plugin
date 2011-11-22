package org.apache.maven.diagrams.gui.bindings.layouts;

import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfiguration;
import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfigurationPanel;

import prefuse.action.layout.Layout;

public class LayoutBinding<LCC extends AbstractLayoutConfiguration>
{
    private Class<? extends Layout> layoutClass;

    private String name;

    private String description;

    private Class<LCC> configurationClass;

    private Class<? extends AbstractLayoutConfigurationPanel<LCC>> editingPanelClass;

    public Class<? extends Layout> getLayoutClass()
    {
        return layoutClass;
    }

    public void setLayoutClass( Class<? extends Layout> layoutClass )
    {
        this.layoutClass = layoutClass;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public Class<LCC> getConfigurationClass()
    {
        return configurationClass;
    }

    public void setConfigurationClass( Class<LCC> configurationClass )
    {
        this.configurationClass = configurationClass;
    }

    public Class<? extends AbstractLayoutConfigurationPanel<LCC>> getEditingPanelClass()
    {
        return editingPanelClass;
    }

    public void setEditingPanelClass( Class<? extends AbstractLayoutConfigurationPanel<LCC>> editingPanelClass )
    {
        this.editingPanelClass = editingPanelClass;
    }

}
