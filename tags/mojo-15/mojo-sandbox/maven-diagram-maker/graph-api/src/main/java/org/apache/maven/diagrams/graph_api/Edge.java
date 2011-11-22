package org.apache.maven.diagrams.graph_api;

public interface Edge {
	
	/**
	 * Return's if the edge is directed
	 *
	 * @return information if edge is directed
	 */
	public boolean isDirected();
	
	/**
	 * Get businness identificator of the edge 		
	 * 
	 * Business identificator should be unique within the graph																																	
	 * @return businness identificator of the edge  
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
	 * Returns the start node (in directed graph) or secound of the two 
	 * nodes (in indirected) of the edge. 
	 * 
	 * @return the node
	 */
	public Node getEndNode();
	
}
