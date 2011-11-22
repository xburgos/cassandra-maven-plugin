package org.apache.maven.diagrams.gui.bindings.connectors;

import java.util.List;

public class DiagramGUIMapping
{
    private List<Binding> bindings;
    
    public List<Binding> getBindings()
    {
        return bindings;
    }
    
    public void setBindings( List<Binding> bindings )
    {
        this.bindings = bindings;
    }
}
