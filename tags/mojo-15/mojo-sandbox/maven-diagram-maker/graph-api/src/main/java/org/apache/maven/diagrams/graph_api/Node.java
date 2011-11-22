package org.apache.maven.diagrams.graph_api;

public interface Node {
	
	/**
	 * Get businness identificator of the node 		
	 * 
	 * Business identificator should be unique within the graph																																	
	 * @return businness identificator of the node  
	 */
	public String getId();

}
