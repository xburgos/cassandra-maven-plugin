package org.apache.maven.diagrams.connectors.classes.edge_source;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.connectors.classes.ClassDataSourceException;
import org.apache.maven.diagrams.connectors.classes.config.ImplementEdgeType;
import org.apache.maven.diagrams.connectors.classes.graph.ClassEdge;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.connectors.classes.graph.ImplementationEdge;

public class ImplementEdgeSource extends AbstractEdgeSource
{

    public ImplementEdgeSource( ImplementEdgeType edgeType )
    {
    }

    @Override
    protected List<ClassEdge> createOutgoingEdges( ClassNode sourceNode )
    {
        List<ClassEdge> result = new LinkedList<ClassEdge>();
        for ( String interf : sourceNode.getInterfaceNames() )
        {
            try
            {
                result.add( new ImplementationEdge( sourceNode, getClassNodesRepository().getClassNode( interf ) ) );
            }
            catch ( ClassDataSourceException e )
            {
                if ( getLogger() != null )
                    getLogger().warn(
                                      "Cannot get informations about interface: " + interf + " (interface of "
                                                      + sourceNode.getFull_name() + ") - skipping", e );
            }
        }
        return result;
    }

    @Override
    protected AddNodeStatus canAddNode( ClassNode node )
    {
        return AddNodeStatus.DONT_ADD_NODE;
    }
}
