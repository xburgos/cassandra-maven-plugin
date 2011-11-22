package org.apache.maven.diagrams.connector_api;

import javax.security.auth.login.Configuration;

public interface DynamicDiagramConnector extends DiagramConnector
{	
	public void setGraphListener(GraphListener listener);	
	
	public void prepare(Configuration config);
	
	/**
	 * Starts (in a new thread) sending events to the listener
	 */
	public void start();
	
	/**
	 * It stops the thread that is sending event's 
	 * 
	 *@throws IllegalStateException - if 
	 */
	public void interrupt();
	
}
