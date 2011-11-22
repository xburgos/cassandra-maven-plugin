package org.apache.maven.diagrams.connectors.classes.edge_source;

import org.apache.maven.diagrams.connectors.classes.config.AggregateEdgeType;
import org.apache.maven.diagrams.connectors.classes.config.EdgeType;
import org.apache.maven.diagrams.connectors.classes.config.ImplementEdgeType;
import org.apache.maven.diagrams.connectors.classes.config.InheritanceEdgeType;

/**
 * The object creates EdgeSource instance for given EdgeType configuration.
 * 
 * @author Piotr Tabor
 * 
 */
public class EdgeSourceFactory
{
    /**
     * The method creates EdgeSource instance for given EdgeType configuration.
     * 
     * @param edgeType
     * @return
     */
    public static AbstractEdgeSource createEdgeSource( EdgeType edgeType )
    {
        if ( ImplementEdgeType.class.isInstance( edgeType ) )
        {
            return new ImplementEdgeSource( (ImplementEdgeType) edgeType );
        }
        else if ( InheritanceEdgeType.class.isInstance( edgeType ) )
        {
            return new InheritanceEdgeSource( (InheritanceEdgeType) edgeType );
        }
        else if ( AggregateEdgeType.class.isInstance( edgeType ) )
        {
            return new AggregateEdgeSource( (AggregateEdgeType) edgeType );
        }

        return null;
    }
}
