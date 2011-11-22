package org.apache.maven.diagrams.graph_api;

/**
 * Interface for single graph edge (director or undirected)
 * 
 * @author Piotr Tabor
 *
 */
public interface Edge {
	/**
	 * Get business id of the edge 		
	 * 
	 * Business id should be unique within the graph																																	
	 * @return business id of the edge  
	 */
	public String getId();

	/**
	 * Returns the start node (in directed graph) or one of the two 
	 * nodes (in indirected) of the edge. 
	 * 
	 * @return the node
	 */
	public Node getStartNode();

	/**
	 * Returns the start node (in directed graph) or second of the two 
	 * nodes (in indirected) of the edge. 
	 * 
	 * @return the node
	 */
	public Node getEndNode();
	
}
