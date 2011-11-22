package org.apache.maven.diagrams.gui.graph_adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.gui.renderers.RendererNodeCache;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;

import prefuse.data.Graph;
import prefuse.data.Node;

public class GraphToPrefuseGraph {
	static public Graph graph2PrefuseGraph(
			org.apache.maven.diagrams.graph_api.Graph sourceGraph) {
		Graph g = new Graph(true);

		g.addColumn(RendererNodeCache.CACHE_ITEM_COLUMN_NAME, Object.class);

		g.addColumn("node", org.apache.maven.diagrams.graph_api.Node.class);
		g.addColumn("id", String.class);

		if (sourceGraph.getGraphMetadata() != null) {
			for (String prop : sourceGraph.getGraphMetadata()
					.getNodePropertiesNames()) 
			{
				g.addColumn(prop, Object.class);
			}
		}

		Map<String, Node> nodes = new HashMap<String, Node>(sourceGraph
				.getNodes().size());

		for (org.apache.maven.diagrams.graph_api.Node node : sourceGraph
				.getNodes().values()) {
			Node n = g.addNode();
			n.set("node", node);
			n.setString("id", node.getId());

			if (sourceGraph.getGraphMetadata() != null) {
				for (String prop : sourceGraph.getGraphMetadata()
						.getNodePropertiesNames()) {
					try {
						n.set(prop, ReflectionValueExtractor.evaluate(prop,
								node, false));
					} catch (Exception e) {
						// TODO: Add logging (transform this to plexus
						// component)
						// getLogger().
						e.printStackTrace(System.err);
					}
				}
			}

			// TO_MA_SIE NIE KOMPILOWACn.setBoolean( "interface", (Boolean) (
			// (ClassNode) node ).isInterface() );
			// n.setDouble( "width", -1.0);
			// n.setDouble( "height", -1.0);
			nodes.put(node.getId(), n);
		}

		int i = 0;
		for (Edge edge : sourceGraph.getEdges().values()) {
			if (i == 1)
				break;
			Node n1 = nodes.get(edge.getStartNode().getId());
			Node n2 = nodes.get(edge.getEndNode().getId());
			g.addEdge(n1, n2);
		}

		return g;
	}
}
