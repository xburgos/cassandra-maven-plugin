package org.apache.maven.diagrams.connector_api;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.GraphMetadata;
import org.apache.maven.diagrams.graph_api.Node;

/**
 * Interface for listening event's about graph's changes.
 * 
 * @author Piotr Tabor
 *
 */
public interface GraphListener {	
	public void init(GraphMetadata metadata);
	
	public void addNode(Node node);

	public void delNode(Node node);
	
	public void addEdge(Edge edge);
	
	public void delEdge(Node node);
	
	/**
	 * Marks that the graph is finished (no more changes are allowed)
	 */
	public void finish();
}
