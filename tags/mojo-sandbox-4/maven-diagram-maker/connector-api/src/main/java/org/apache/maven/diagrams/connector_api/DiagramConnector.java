package org.apache.maven.diagrams.connector_api;

import org.apache.maven.diagrams.connector_api.descriptor.ConnectorDescriptor;
import org.apache.maven.diagrams.graph_api.Graph;
import org.apache.maven.project.MavenProject;

/**
 * Interface for all connector's
 * 
 * @author ptab
 * 
 */
public interface DiagramConnector
{
    /**
     * If the connector supports the "Static interface" (ConnectorInterfaceEnum.STATIC) the method should return
     * calculated graph. It returns null otherwise
     * 
     * @param configuration
     * @return
     * @throws ConnectorException
     */
    public Graph calculateGraph( ConnectorConfiguration configuration ) throws ConnectorException;

    /**
     * If the connector supports the "Dynamic interface / Listener model" (ConnectorInterfaceEnum.Dynamic) the method
     * should return DynamicDiagramConnector (for the Listening management - not thread safe, for single thread use
     * only)
     * 
     * @param configuration
     * @return
     * @throws ConnectorException
     */
    public DynamicDiagramConnector getDynamicDiagramConnector() throws ConnectorException;

    /**
     * Returns connector's descriptor.
     * 
     * @throws ConnectorException
     */
    public ConnectorDescriptor getConnectorDescriptor() throws ConnectorException;

    //
    // /**
    // * Sets the connector's context (environment)
    // *
    // * @param new_context
    // */
    // public void setConnectorContext( ConnectorContext new_context );
    //
    // /**
    // * Returns the connector's context (environment)
    // *
    // * @return
    // */
    // public ConnectorContext getConnectorContext();

    public void setMavenProject(MavenProject mavenProject);
}
