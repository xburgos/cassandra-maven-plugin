package org.apache.maven.diagrams.graph_api;

import java.util.List;

/**
 * Interface for object providing basic (general) informations about the graph
 * 
 * @author Piotr Tabor
 * 
 */
public interface GraphMetadata
{
    /**
     * Return's if the edges in the graph are directed
     * 
     * @return information if edges in the graph are directed
     */
    public boolean isDirected();

    /**
     * List of node's properties (nodes are JavaBeans) so it
     * should be list of possible properties names in the graph's 
     * node implementation
     * 
     * @return
     */
    public List<String> getNodePropertiesNames();

    /**
     * List of edge's properties (edges are JavaBeans) so it
     * should be list of possible properties names in the graph's 
     * edge implementation
     * 
     * @return
     */
    public List<String> getEdgePropertiesNames();
}
