package org.apache.maven.diagrams.connectors.classes.graph;

/**
 * The edge between class and aggregated class
 * @author Piotr Tabor
 *
 */
public class AggregationEdge extends ClassEdge
{

    public AggregationEdge( ClassNode child, ClassNode parent )
    {
        super( child, parent );
    }

}
