package org.apache.maven.diagrams.gui.renderers.umlClass.renderer;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;

public class SeparatorItem implements RendererListItem
{
    double SIZE=2.0;
    
    /**
     * Can return null ( don't modify current box width)
     * 
     * @param fontMetrics
     * @return
     */
    public Double getWidth( FontRenderContext context )
    {
        return null;
    }
   

    /**
     * Can not return null.
     * 
     * @param fontMetrics
     * @return
     */

    public Double getHeight( FontRenderContext context )
    {
        return SIZE;
    }

    public void draw( double x, double y, double max_width, Graphics2D convas )
    {            
        convas.draw( new Line2D.Double(x,y+SIZE/2,x+max_width,y+SIZE/2));
    }
    
    
}
