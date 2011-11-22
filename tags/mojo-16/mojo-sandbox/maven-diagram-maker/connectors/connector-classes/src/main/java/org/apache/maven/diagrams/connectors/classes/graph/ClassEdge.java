package org.apache.maven.diagrams.connectors.classes.graph;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.Node;

/**
 * The common Edge conneting two classes
 * 
 * @author Piotr Tabor
 * 
 */
public abstract class ClassEdge implements Edge
{
    /** source */
    private ClassNode child;

    /** destination */
    private ClassNode parent;

    public ClassEdge( ClassNode child, ClassNode parent )
    {
        this.child = child;
        this.parent = parent;
    }

    public Node getEndNode()
    {
        return parent;
    }

    public String getId()
    {
        return child.getId() + "-" + parent.getId();
    }

    public Node getStartNode()
    {
        return child;
    }

    /**
     * Is the edge directed
     */
    public boolean isDirected()
    {
        return true;
    }

}
