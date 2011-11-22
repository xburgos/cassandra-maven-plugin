package org.apache.maven.diagrams.gui.renderers;

import java.util.HashMap;
import java.util.Map;

public class AbstractRendererConfiguration implements RendererConfiguration
{
    private Map<String, RendererConfigurationItem> renderConfigurationItems;

    public Map<String, RendererConfigurationItem> getRenderConfigurationItems()
    {
        if ( renderConfigurationItems == null )
        {
            renderConfigurationItems = new HashMap<String, RendererConfigurationItem>();
        }
        return renderConfigurationItems;
    }

    public boolean isVisible( String name )
    {
        RendererConfigurationItem item = renderConfigurationItems.get( name );
        if ( item != null )
        {
            return item.isVisible();
        }
        else
            return false;
    }
}
