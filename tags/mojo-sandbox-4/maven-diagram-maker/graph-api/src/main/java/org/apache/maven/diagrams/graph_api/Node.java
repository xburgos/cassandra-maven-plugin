package org.apache.maven.diagrams.graph_api;

/**
 * Interfarce for single graph's node (directed or undirected)
 * @author Piotr Tabor
 *
 */
public interface Node {
	
	/**
	 * Get business id of the node 		
	 * 
	 * Business id should be unique within the graph																																	
	 * @return business id of the node  
	 */
	public String getId();

}
