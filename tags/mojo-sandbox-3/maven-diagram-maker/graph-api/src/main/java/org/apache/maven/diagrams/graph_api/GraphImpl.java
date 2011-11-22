package org.apache.maven.diagrams.graph_api;

import java.util.LinkedHashMap;

public class GraphImpl implements Graph
{

    /**
     * Node's id to node map
     */
    private LinkedHashMap<String, Node> nodes;

    /**
     * Edge's id to edge map
     */
    private LinkedHashMap<String, Edge> edges;

    public GraphImpl()
    {
        nodes = new LinkedHashMap<String, Node>();
        edges = new LinkedHashMap<String, Edge>();
    }

    public LinkedHashMap<String, Edge> getEdges()
    {
        return edges;
    }

    public LinkedHashMap<String, Node> getNodes()
    {
        return nodes;
    }

    public void addEdge( Edge edge )
    {
        if ( !nodes.containsKey( edge.getStartNode() ) )
            throw new IllegalStateException( "Start node (" + edge.getStartNode().getId() + ") of the edge: "
                            + edge.getId() + " does not belong to the graph" );
        if ( !nodes.containsKey( edge.getEndNode() ) )
            throw new IllegalStateException( "End node (" + edge.getStartNode().getId() + ") of the edge: "
                            + edge.getId() + " does not belong to the graph" );
        edges.put( edge.getId(), edge );
    }

    public void addNode( Node node )
    {
        nodes.put( node.getId(), node );
    }

    public Edge getEdge( String id )
    {
        return edges.get( id );
    }

    public Node getNode( String id )
    {
        return nodes.get( id );
    }

}
