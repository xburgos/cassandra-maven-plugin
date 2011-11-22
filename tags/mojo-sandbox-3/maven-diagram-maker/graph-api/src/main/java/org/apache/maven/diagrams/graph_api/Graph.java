package org.apache.maven.diagrams.graph_api;


public interface Graph
{
    public abstract void addEdge( Edge edge );

    public abstract void addNode( Node node );
    
    public Edge getEdge( String id );
    
    public Node getNode( String id );

}