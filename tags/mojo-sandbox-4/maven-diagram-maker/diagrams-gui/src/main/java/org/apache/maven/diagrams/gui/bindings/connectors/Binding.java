package org.apache.maven.diagrams.gui.bindings.connectors;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.renderers.ConfigurableRenderer;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationPanel;

public class Binding
{
    private String name;

    private String connectorClassName;

    private String graphType;

    private String connectorConfigurationPanelClassName;

    private String rendererClassName;

    private String rendererConfigurationPanelClassName;

    private String rendererConfigurationClassName;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getConnectorClassName()
    {
        return connectorClassName;
    }

    public void setConnectorClassName( String connectorClassName )
    {
        this.connectorClassName = connectorClassName;
    }

    public String getGraphType()
    {
        return graphType;
    }

    public void setGraphType( String graphType )
    {
        this.graphType = graphType;
    }

    public String getConnectorConfigurationPanelClassName()
    {
        return connectorConfigurationPanelClassName;
    }

    public void setConnectorConfigurationPanelClassName( String connectorConfigurationPanelClassName )
    {
        this.connectorConfigurationPanelClassName = connectorConfigurationPanelClassName;
    }

    // --------------------------------------------------------------------------

    public String getRendererConfigurationPanelClassName()
    {
        return rendererConfigurationPanelClassName;
    }

    public void setRendererConfigurationPanelClassName( String rendererConfigurationPanelClassName )
    {
        this.rendererConfigurationPanelClassName = rendererConfigurationPanelClassName;
    }

    public String getRendererConfigurationClassName()
    {
        return rendererConfigurationClassName;
    }

    public void setRendererConfigurationClassName( String rendererConfigurationClassName )
    {
        this.rendererConfigurationClassName = rendererConfigurationClassName;
    }

    public String getRendererClassName()
    {
        return rendererClassName;
    }

    public void setRendererClassName( String rendererClassName )
    {
        this.rendererClassName = rendererClassName;
    }

    /* ========================== Class resolving ================================ */

    public ResolvedBinding<DiagramConnector> resolveBinding() throws ResolveBindingException
    {
        return resolveBinding( this.getClass().getClassLoader() );
    }

    @SuppressWarnings( "unchecked" )
    public ResolvedBinding<DiagramConnector> resolveBinding( ClassLoader classLoader ) throws ResolveBindingException
    {
        try
        {
            ResolvedBinding<DiagramConnector> resolvedBinding = new ResolvedBinding<DiagramConnector>();
            resolvedBinding.setName( name );
            resolvedBinding.setGraphType( graphType );

            Class<DiagramConnector> resolvedDiagramConnectorClass =
                (Class<DiagramConnector>) classLoader.loadClass( connectorClassName );
            resolvedBinding.setConnectorClass( resolvedDiagramConnectorClass );

            Class<AbstractConnectorConfigurationPanel<ConnectorConfiguration>> resolvedConnectorConfigurationPanelClass;
            resolvedConnectorConfigurationPanelClass =
                (Class<AbstractConnectorConfigurationPanel<ConnectorConfiguration>>) classLoader.loadClass( connectorConfigurationPanelClassName );
            resolvedBinding.setConnectorConfigurationPanelClass( resolvedConnectorConfigurationPanelClass );
            if ( rendererClassName != null )
                resolvedBinding.setRenderer( (Class<ConfigurableRenderer>) classLoader.loadClass( rendererClassName ) );
            if ( rendererConfigurationPanelClassName != null )
                resolvedBinding.setRendererConfigurationPanel( (Class<RendererConfigurationPanel>) classLoader.loadClass( rendererConfigurationPanelClassName ) );
            if ( rendererConfigurationClassName != null )
                resolvedBinding.setRendererConfiguration( (Class<RendererConfiguration>) classLoader.loadClass( rendererConfigurationClassName ) );

            return resolvedBinding;
        }
        catch ( ClassNotFoundException e )
        {
            throw new ResolveBindingException( "Cannot create binding named:" + name, e );
        }
        catch ( ClassCastException e )
        {
            throw new ResolveBindingException( "Cannot create binding named:" + name, e );
        }
    }

}
