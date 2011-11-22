package org.apache.maven.diagrams.gui.renderers.umlClass.renderer;

import java.util.List;

import org.apache.maven.diagrams.gui.renderers.RendererNodeCacheImpl;

public class ListRendererNodeCache extends RendererNodeCacheImpl
{
    private List<RendererListItem> rendererListItemList;

    public List<RendererListItem> getRendererListItemList()
    {
        return rendererListItemList;
    }

    public void setRendererListItemList( List<RendererListItem> rendererListItemList )
    {
        this.rendererListItemList = rendererListItemList;
    }

}
