package org.apache.maven.diagrams.gui.renderers;

import java.util.Map;

public interface RendererConfiguration
{
    public Map<String, RendererConfigurationItem> getRenderConfigurationItems();

    public boolean isVisible( String name );
}
