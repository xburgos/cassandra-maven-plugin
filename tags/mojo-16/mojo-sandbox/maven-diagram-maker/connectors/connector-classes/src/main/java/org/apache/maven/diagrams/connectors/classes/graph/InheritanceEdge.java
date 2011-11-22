package org.apache.maven.diagrams.connectors.classes.graph;

/**
 * The edge between class (child) and its superclass (parent)
 * 
 * @author Piotr Tabor
 * 
 */
public class InheritanceEdge extends ClassEdge
{

    public InheritanceEdge( ClassNode child, ClassNode parent )
    {
        super( child, parent );
    }

}
