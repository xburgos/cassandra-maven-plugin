package org.apache.maven.diagrams.gui.layouts;

import prefuse.action.layout.Layout;

public abstract class AbstractLayoutConfiguration<TLayout extends Layout>
{
    abstract public void readFromLayout( TLayout l );

    abstract public void updateLayout( TLayout l );
    
    abstract public void setDefaultConfiguration();
    
    abstract public boolean canUpdateLayout(Layout layout);
}
