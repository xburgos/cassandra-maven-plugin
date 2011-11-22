package org.apache.maven.diagrams.gui.renderers;

public interface RendererNodeCache
{
    public final static String CACHE_ITEM_COLUMN_NAME = "_renderer_cache";

    public Double getNodeWidth();
    public Double getNodeHeight();

    public void setNodeHeight( Double a_height );
    public void setNodeWidth( Double a_width );
}
