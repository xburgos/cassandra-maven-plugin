package org.apache.maven.diagrams.connector_api;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.GraphMetadata;

import com.sun.corba.se.impl.orbutil.graph.Node;

public interface GraphListener {	
	public void init(GraphMetadata metadata);
	
	public void addNode(Node node);
	
	public void addEdge(Edge edge);
	
	public void finish();
	
}
