package org.apache.maven.diagrams.graph_api.impl;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.Graph;
import org.apache.maven.diagrams.graph_api.GraphMetadata;
import org.apache.maven.diagrams.graph_api.Node;

/**
 * Simple {@link Graph} implementation (using list of nodes and list of edges)
 * 
 * @author ptab
 * 
 */
public class GraphImpl implements Graph {

	/**
	 * Node's id to node map
	 */
	private LinkedHashMap<String, Node> nodes;

	/**
	 * Edge's id to edge map
	 */
	private LinkedHashMap<String, Edge> edges;

	private GraphMetadata metadata;

//	public GraphImpl() {
//		nodes = new LinkedHashMap<String, Node>();
//		edges = new LinkedHashMap<String, Edge>();
//		metadata = null;
//	}

	public GraphImpl(GraphMetadata a_metadata) {
		nodes = new LinkedHashMap<String, Node>();
		edges = new LinkedHashMap<String, Edge>();
		metadata = a_metadata;
	}

	public LinkedHashMap<String, Edge> getEdges() {
		return edges;
	}

	public LinkedHashMap<String, Node> getNodes() {
		return nodes;
	}

	public void addEdge(Edge edge) {
		if (!nodes.containsKey(edge.getStartNode().getId()))
			throw new IllegalStateException("Start node ("
					+ edge.getStartNode().getId() + ") of the edge: "
					+ edge.getId() + " does not belong to the graph");
		if (!nodes.containsKey(edge.getEndNode().getId()))
			throw new IllegalStateException("End node ("
					+ edge.getStartNode().getId() + ") of the edge: "
					+ edge.getId() + " does not belong to the graph");
		edges.put(edge.getId(), edge);
	}

	public void addNode(Node node) {
		nodes.put(node.getId(), node);
	}

	public void addNodes(Collection<? extends Node> nodes) {
		for (Node node : nodes)
			addNode(node);
	}

	public void addEdges(Collection<? extends Edge> edges) {
		for (Edge edge : edges)
			addEdge(edge);
	}

	public Edge getEdge(String id) {
		return edges.get(id);
	}

	public Node getNode(String id) {
		return nodes.get(id);
	}

	public GraphMetadata getGraphMetadata() {
		return metadata;
	}

	public void setGraphMetadata(GraphMetadata metadata) {
		this.metadata = metadata;
	}

}
