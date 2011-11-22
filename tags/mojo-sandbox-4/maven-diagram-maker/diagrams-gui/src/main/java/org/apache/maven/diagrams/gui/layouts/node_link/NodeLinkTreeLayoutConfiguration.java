package org.apache.maven.diagrams.gui.layouts.node_link;

import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfiguration;

import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;

public class NodeLinkTreeLayoutConfiguration extends AbstractLayoutConfiguration<NodeLinkTreeLayout>
{
    public static final int ORIENT_LEFT_RIGHT = 0;

    /** A right-to-left layout orientation */
    public static final int ORIENT_RIGHT_LEFT = 1;

    /** A top-to-bottom layout orientation */
    public static final int ORIENT_TOP_BOTTOM = 2;

    /** A bottom-to-top layout orientation */
    public static final int ORIENT_BOTTOM_TOP = 3;

    private int m_orientation; // the orientation of the tree

    private double m_bspace; // the spacing between sibling nodes

    private double m_tspace; // the spacing between subtrees

    private double m_dspace; // the spacing between depth levels

    @Override
    public void readFromLayout( NodeLinkTreeLayout l )
    {
        m_orientation = l.getOrientation();
        m_bspace = l.getBreadthSpacing();
        m_tspace = l.getSubtreeSpacing();
        m_dspace = l.getDepthSpacing();

    }

    @Override
    public void updateLayout( NodeLinkTreeLayout l )
    {
        l.setOrientation( m_orientation );
        l.setBreadthSpacing( m_bspace );
        l.setSubtreeSpacing( m_tspace );
        l.setDepthSpacing( m_dspace );
    }
    
    @Override
    public void setDefaultConfiguration()
    {
        m_orientation=ORIENT_LEFT_RIGHT;
        m_bspace = 5;
        m_tspace = 5;
        m_dspace = 30;      
        
    }
    
    /*==================== Getters and setters =====================*/
    
    

    public int getOrientation()
    {
        return m_orientation;
    }

    public void setOrientation( int m_orientation )
    {
        this.m_orientation = m_orientation;
    }

    public double getBspace()
    {
        return m_bspace;
    }

    public void setBspace( double m_bspace )
    {
        this.m_bspace = m_bspace;
    }

    public double getTspace()
    {
        return m_tspace;
    }

    public void setTspace( double m_tspace )
    {
        this.m_tspace = m_tspace;
    }

    public double getDspace()
    {
        return m_dspace;
    }

    public void setDspace( double m_dspace )
    {
        this.m_dspace = m_dspace;
    }
    
    @Override
    public boolean canUpdateLayout( Layout layout )
    {
        return NodeLinkTreeLayout.class.isInstance(layout);
    }
    
    
}
