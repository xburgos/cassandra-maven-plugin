package org.apache.maven.diagrams.gui.renderers.umlClass.renderer;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;

public interface RendererListItem
{
    /**
     * Can return null ( don't modify current box width)
     * 
     * @param fontMetrics
     * @return
     */
    public Double getWidth( FontRenderContext context );

    /**
     * Can not return null.
     * 
     * @param fontMetrics
     * @return
     */

    public Double getHeight( FontRenderContext context );

    public void draw( double x, double y, double max_width, Graphics2D convas );
}
