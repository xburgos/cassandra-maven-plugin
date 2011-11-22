package org.apache.maven.diagrams.gui.bindings.connectors;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.renderers.ConfigurableRenderer;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationPanel;

public class ResolvedBinding<DiagramConnectorSubclass extends DiagramConnector>
{
    private String name;

    private Class<DiagramConnectorSubclass> connectorClass;

    private String graphType;

    private Class<? extends AbstractConnectorConfigurationPanel<? extends ConnectorConfiguration>> connectorConfigurationPanelClass;

    private Class<? extends RendererConfigurationPanel> rendererConfigurationPanel;

    private Class<? extends RendererConfiguration> rendererConfiguration;

    private Class<? extends ConfigurableRenderer> renderer;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getGraphType()
    {
        return graphType;
    }

    public void setGraphType( String graphType )
    {
        this.graphType = graphType;
    }

    public Class<DiagramConnectorSubclass> getConnectorClass()
    {
        return connectorClass;
    }

    public void setConnectorClass( Class<DiagramConnectorSubclass> connectorClass )
    {
        this.connectorClass = connectorClass;
    }

    public Class<? extends AbstractConnectorConfigurationPanel<? extends ConnectorConfiguration>> getConnectorConfigurationPanelClass()
    {
        return connectorConfigurationPanelClass;
    }

    public void setConnectorConfigurationPanelClass(
                                                     Class<? extends AbstractConnectorConfigurationPanel<? extends ConnectorConfiguration>> connectorConfigurationPanelClass )
    {
        this.connectorConfigurationPanelClass = connectorConfigurationPanelClass;
    }

    public Class<? extends RendererConfiguration> getRendererConfiguration()
    {
        return rendererConfiguration;
    }

    public Class<? extends RendererConfigurationPanel> getRendererConfigurationPanel()
    {
        return rendererConfigurationPanel;
    }

    public void setRendererConfigurationPanel( Class<RendererConfigurationPanel> rendererConfigurationPanel )
    {
        this.rendererConfigurationPanel = rendererConfigurationPanel;
    }

    public void setRendererConfiguration( Class<RendererConfiguration> rendererConfiguration )
    {
        this.rendererConfiguration = rendererConfiguration;
    }

    public Class<? extends ConfigurableRenderer> getRenderer()
    {
        return renderer;
    }

    public void setRenderer( Class<? extends ConfigurableRenderer> renderer )
    {
        this.renderer = renderer;
    }
}
