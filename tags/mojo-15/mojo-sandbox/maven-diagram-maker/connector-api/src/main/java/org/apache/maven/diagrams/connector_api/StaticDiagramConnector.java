package org.apache.maven.diagrams.connector_api;

public interface StaticDiagramConnector extends DiagramConnector
{
	
	public void calculateGraph(ConnectorConfiguration configuration);	
	
	
}
