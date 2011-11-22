package org.apache.maven.diagrams.graph_api;

import java.util.Map;

/**
 * The class implementing this interface is a single graph.  
 * 
 * @author Piotr Tabor
 */
public interface Graph
{
    public abstract void addEdge( Edge edge );

    public abstract void addNode( Node node );
    
    public Edge getEdge( String id );
    
    public Node getNode( String id );
    
    public Map<String,Node> getNodes();
    
    public Map<String,Edge> getEdges();

}