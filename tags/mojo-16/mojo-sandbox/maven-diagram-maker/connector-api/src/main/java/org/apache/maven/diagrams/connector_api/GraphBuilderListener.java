package org.apache.maven.diagrams.connector_api;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.Graph;
import org.apache.maven.diagrams.graph_api.GraphMetadata;
import org.apache.maven.diagrams.graph_api.Node;
import org.apache.maven.diagrams.graph_api.impl.GraphImpl;

/**
 * Example/simple implementation of GraphListener thats just builds the graph from the events.
 * 
 * @author Piotr Tabor
 * 
 */
public class GraphBuilderListener implements GraphListener
{
    private Graph resultGraph = null;

    public GraphBuilderListener()
    {
        resultGraph = new GraphImpl();
    }

    public void addEdge( Edge edge )
    {
        resultGraph.addEdge( edge );

    }

    public void addNode( Node node )
    {
        resultGraph.addNode( node );
    }

    public void finish()
    {
    }

    public void init( GraphMetadata metadata )
    {
    }

    /** Returns the current (builded) graph. */
    public Graph getGraph()
    {
        return resultGraph;
    }

    public void delEdge( Node node )
    {
        // TODO Auto-generated method stub
        
    }

    public void delNode( Node node )
    {
        // TODO Auto-generated method stub
        
    }

}
