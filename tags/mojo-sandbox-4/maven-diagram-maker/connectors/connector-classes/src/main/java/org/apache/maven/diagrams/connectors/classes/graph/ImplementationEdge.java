package org.apache.maven.diagrams.connectors.classes.graph;

/**
 * The edge between class (child) and its interface (parent)
 * 
 * @author Piotr Tabor
 * 
 */

public class ImplementationEdge extends ClassEdge
{

    public ImplementationEdge( ClassNode child, ClassNode parent )
    {
        super( child, parent );
    }

}
