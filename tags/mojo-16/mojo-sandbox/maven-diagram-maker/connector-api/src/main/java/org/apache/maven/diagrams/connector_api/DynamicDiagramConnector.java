package org.apache.maven.diagrams.connector_api;

/**
 * It is listening manager for one (single thread) session. 
 * It allows to register listener and to start raising events from
 * the connector to the listener. 
 *  
 * @author Piotr Tabor
 *
 */
public interface DynamicDiagramConnector
{	
    /**
     * Sets single listener
     * 
     * @param listener
     */
	public void setGraphListener(GraphListener listener);	
	
	/**
	 * The method should be call once before "start".
	 * 
	 * It prepares everything to raising the events. 
	 *  
	 * @param config
	 */
	public void prepare(ConnectorConfiguration config);
	
	/**
	 * Starts (in a new thread) sending events to the listener
	 */
	public void start();
	
	/**
	 * It stops the thread that is sending events  to the listener/ 
	 * 
	 *@throws IllegalStateException
	 */
	public void interrupt() throws InterruptedException;
	
	/**
	 * Wait's until the "events sender thread" is finished
	 */
	public void waitUntilFinished() throws InterruptedException;
	
}
