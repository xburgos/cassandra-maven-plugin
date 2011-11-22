package org.apache.maven.diagrams.gui.connector;

import javax.swing.JPanel;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;

public abstract class AbstractConnectorConfigurationPanel<ConnectorConfigurationSubclass extends ConnectorConfiguration>
    extends JPanel
{

    public abstract ConnectorConfigurationSubclass getCurrentConfiguration();

    public abstract void setCurrentConfiguration( ConnectorConfigurationSubclass configuration );
}
