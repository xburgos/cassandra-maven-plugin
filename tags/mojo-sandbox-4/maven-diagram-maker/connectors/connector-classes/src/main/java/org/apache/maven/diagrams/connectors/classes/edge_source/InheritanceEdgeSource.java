package org.apache.maven.diagrams.connectors.classes.edge_source;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.connectors.classes.ClassDataSourceException;
import org.apache.maven.diagrams.connectors.classes.filter.ClassFilterStatus;
import org.apache.maven.diagrams.connectors.classes.graph.ClassEdge;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.connectors.classes.graph.InheritanceEdge;

public class InheritanceEdgeSource extends AbstractEdgeSource
{
    public InheritanceEdgeSource()
    {
    }

    @Override
    public List<ClassEdge> createOutgoingEdges( ClassNode sourceNode )
    {
        List<ClassEdge> result = new LinkedList<ClassEdge>();
        String s = sourceNode.getSuperclassName();
        if ( s != null )
        {
            try
            {
                result.add( new InheritanceEdge( sourceNode, getClassNodesRepository().getClassNode( s ) ) );
            }
            catch ( ClassDataSourceException e )
            {
                if ( getLogger() != null )
                    getLogger().warn(
                                      "Cannot get informations about class: " + s + " (superclass of "
                                                      + sourceNode.getFull_name() + ") - skipping", e );
            }
        }
        return result;
    }

    @Override
    protected AddNodeStatus canAddNode( ClassNode node )
    {
        if ( getConfiguration().getFullInheritancePaths() )
            return AddNodeStatus.ADD_NODE_AND_RESOLVE_TRANSITIVES;
        else
        {
            ClassFilterStatus classFilterStatus = getFilterRepository().getStatus( node.getFull_name() );
            switch ( classFilterStatus )
            {
                case NOT_INCLUDED:
                case EXCLUDED_WITHOUT_KEEP_EDGES:
                    return AddNodeStatus.DONT_ADD_NODE;
                case EXCLUDED_WITH_KEEP_EDGES:
                case INCLUDED:
                    return AddNodeStatus.ADD_NODE_AND_RESOLVE_TRANSITIVES;
                default:
                    getLogger().error( "Unsupported filter class state: " + classFilterStatus );
                    assert ( false );
                    return AddNodeStatus.DONT_ADD_NODE;
            }
        }
    }
}
