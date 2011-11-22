package org.apache.maven.diagrams.connectors.classes.edge_source;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.diagrams.connector_api.logger.Logger;
import org.apache.maven.diagrams.connectors.classes.ClassNodesRepository;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.filter.FilterRepository;
import org.apache.maven.diagrams.connectors.classes.graph.ClassEdge;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.connectors.classes.model.ClassModel;

public abstract class AbstractEdgeSource
{
    private Logger logger;

    /**
     * The object's instance that answers if the given class should be added to the resulting graph or be skipped (with keeping
     * edges or not)
     */
    private FilterRepository filterRepository;

    /**
     * The object's instance that provides and caches important informations ( {@link ClassModel}) for
     * the given class name 
     */
    private ClassNodesRepository classNodesRepository;

    /**
     * The whole ClassesConnecector's configuration
     */
    private ClassesConnectorConfiguration configuration;

    /**
     * Sets the main (dependent components) in one call.
     * 
     * @param a_filterRepository
     * @param a_classNodesRepository
     * @param a_configuration
     */
    public void configure( FilterRepository a_filterRepository, ClassNodesRepository a_classNodesRepository,
                           ClassesConnectorConfiguration a_configuration )
    {
        filterRepository = a_filterRepository;
        classNodesRepository = a_classNodesRepository;
        configuration = a_configuration;
    }

    /**
     * The method returns created edges. It can also add new nodes to "resultNodes"
     */
    public Set<ClassEdge> calculateEdges( Set<ClassNode> resultNodes )
    {
        LinkedList<ClassNode> queue = new LinkedList<ClassNode>( resultNodes );
        Set<ClassEdge> result = new HashSet<ClassEdge>();
        Set<ClassNode> done = new HashSet<ClassNode>();

        while ( !queue.isEmpty() )
        {
            ClassNode head = queue.poll();
            if ( !done.contains( head ) )
            {
                done.add( head );
                List<ClassEdge> newEdges = createOutgoingEdges( head );
                for ( ClassEdge newEdge : newEdges )
                {
                    if ( resultNodes.contains( newEdge.getEndNode() ) )
                    {
                        result.add( newEdge );
                    }
                    else
                    {
                        switch ( canAddNode( (ClassNode) newEdge.getEndNode() ) )
                        {
                            case ADD_NODE_AND_RESOLVE_TRANSITIVES:
                                resultNodes.add( (ClassNode) newEdge.getEndNode() );
                                /* no break */
                            case ADD_NODE_DONT_RESOLVE_TRANSITIVES:
                                queue.add( (ClassNode) newEdge.getEndNode() );
                                result.add( newEdge );
                                break;
                            case DONT_ADD_NODE:
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Implementation of this method should prepare list of outgoing edges
     * from the given node. The method shouldn't check if the target node
     * already exists in the graph. The calculate edges method
     * will check that depending on results of canAddNode method. 
     * 
     * The implementation can call 
     * @param sourceNode - node from that we need edges.
     * @return
     */
    protected abstract List<ClassEdge> createOutgoingEdges( ClassNode sourceNode );

    /**
     * The method obtains information what can be done with the given node 
     * (if it should be added to the graph or skipped (with all without its dependencies))
     *  
     * @param node
     * @return
     */
    protected abstract AddNodeStatus canAddNode( ClassNode node );

    protected static enum AddNodeStatus
    {
        /**Add the node to graph and process its dependences*/
        ADD_NODE_AND_RESOLVE_TRANSITIVES, 
        
        /**Add the node to graph and don't process its dependences*/
        ADD_NODE_DONT_RESOLVE_TRANSITIVES,
        
        /** Don't add the node and don't work on its transitive dependencies*/
        DONT_ADD_NODE
    }

    /* --------------------- Getters and setters----------------------------- */

    public FilterRepository getFilterRepository()
    {
        return filterRepository;
    }

    public ClassNodesRepository getClassNodesRepository()
    {
        return classNodesRepository;
    }

    public ClassesConnectorConfiguration getConfiguration()
    {
        return configuration;
    }
    
    public Logger getLogger()
    {
        return logger;
    }
    
    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }
};