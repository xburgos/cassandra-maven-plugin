package org.apache.maven.diagrams.gui.renderers;


public class RendererNodeCacheImpl implements RendererNodeCache
{
    private Double height;

    private Double width;

    public RendererNodeCacheImpl()
    {
        height = null;
        width = null;
    }

    public Double getNodeHeight()
    {
        return height;
    }

    public Double getNodeWidth()
    {

        return width;
    }

    public void setNodeHeight( Double a_height )
    {
        height = a_height;
    }

    public void setNodeWidth( Double a_width )
    {
        width = a_width;
    }

}
