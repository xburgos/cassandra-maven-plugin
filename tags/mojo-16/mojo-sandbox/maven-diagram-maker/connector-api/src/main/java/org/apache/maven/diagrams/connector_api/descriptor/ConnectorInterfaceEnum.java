package org.apache.maven.diagrams.connector_api.descriptor;

public enum ConnectorInterfaceEnum
{
    /** Means that the connector provide whole graph at once */
    STATIC,
    /** Means that the connector is able to send events about adding/deleting nodes/edges */
    LISTENER
}
