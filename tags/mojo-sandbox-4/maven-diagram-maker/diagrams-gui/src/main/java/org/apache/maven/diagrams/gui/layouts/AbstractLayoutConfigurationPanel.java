package org.apache.maven.diagrams.gui.layouts;

import javax.swing.JPanel;

import org.apache.maven.diagrams.gui.controller.LayoutController;

public abstract class AbstractLayoutConfigurationPanel<AbstractLayoutConfigurationSubclass extends AbstractLayoutConfiguration>
    extends JPanel
{
    private LayoutController controller;

    public abstract AbstractLayoutConfigurationSubclass getCurrentConfiguration();

    public abstract void setCurrentConfiguration( AbstractLayoutConfigurationSubclass configuration );

    public void setController( LayoutController controller )
    {
        this.controller = controller;
    }

    public LayoutController getController()
    {
        return controller;
    }

    public void apply()
    {
        AbstractLayoutConfigurationSubclass abstractLayoutConfigurationSubclass = getCurrentConfiguration();
        controller.updateLayoutConfiguration( abstractLayoutConfigurationSubclass );
    }

    public boolean isAutoApply()
    {
        return controller.isAutoApplyMode();
    }
}
