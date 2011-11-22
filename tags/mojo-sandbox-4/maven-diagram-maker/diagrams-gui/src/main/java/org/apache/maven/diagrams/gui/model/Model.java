package org.apache.maven.diagrams.gui.model;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.gui.MainWindow;

import prefuse.data.Graph;

public class Model
{
   // private ConnectorContext connectorContext;

    private DiagramConnector connector;

    private ConnectorConfiguration connectorConfiguration;

    private MainWindow mainWindow;

    // ===============================================================

    private Graph graph;

//    public ConnectorContext getConnectorContext()
//    {
//        return connectorContext;
//    }
//
//    public void setConnectorContext( ConnectorContext context )
//    {
//        this.connectorContext = context;
//    }

    public ConnectorConfiguration getConnectorConfiguration()
    {
        return connectorConfiguration;
    }

    public void setConnectorConfiguration( ConnectorConfiguration connectorConfiguration )
    {
        this.connectorConfiguration = connectorConfiguration;
    }

    
    public Graph getGraph()
    {
        return graph;
    }

    public void setGraph( Graph graph )
    {
        this.graph = graph;
    }

    public DiagramConnector getConnector()
    {
        return connector;
    }

    public void setConnector( DiagramConnector connector )
    {
        this.connector = connector;
    }
    
    

 }
