package org.apache.maven.diagrams.gui.renderers.artifact;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.connectors.dependencies.graph.ArtifactNode;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.umlClass.renderer.ListRenderer;
import org.apache.maven.diagrams.gui.renderers.umlClass.renderer.RendererListItem;
import org.apache.maven.diagrams.gui.renderers.umlClass.renderer.TextItem;

import prefuse.visual.VisualItem;

public class ArtifactNodeRenderer extends ListRenderer {
	public ArtifactNodeRenderer() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected List<RendererListItem> getList(VisualItem vi) {
		List<RendererListItem> list;
		// = (List<RendererListItem>) vi.get( "vcache" );
		// if ( list == null )

		list = new LinkedList<RendererListItem>();

		ArtifactNode node = (ArtifactNode) vi.get("node");

		list.add(new TextItem(node.artifact.getDependencyConflictId(), true,
				false, true));

		return list;
	}

	@Override
	public void setConfiguration(RendererConfiguration newRendererConfiguration) {
	}
}