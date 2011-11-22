package org.apache.maven.diagrams.gui.renderers;

import prefuse.render.Renderer;

public interface ConfigurableRenderer extends Renderer
{
    public RendererConfiguration getConfiguration();

    public void setConfiguration( RendererConfiguration newRendererConfiguration );
}
