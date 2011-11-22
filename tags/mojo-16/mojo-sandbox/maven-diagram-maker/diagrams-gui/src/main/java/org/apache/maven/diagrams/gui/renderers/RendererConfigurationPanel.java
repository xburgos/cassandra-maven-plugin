package org.apache.maven.diagrams.gui.renderers;

import javax.swing.JPanel;

public abstract class RendererConfigurationPanel extends JPanel
{
    public abstract RendererConfiguration getCurrentRendererConfiguration();

    public abstract void setCurrentRendererConfiguration( RendererConfiguration configuration );
}
