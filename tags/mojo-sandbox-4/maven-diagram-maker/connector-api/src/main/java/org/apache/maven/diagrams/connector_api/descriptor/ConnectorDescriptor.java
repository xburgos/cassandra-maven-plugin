package org.apache.maven.diagrams.connector_api.descriptor;

import java.util.EnumSet;
import java.util.List;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.DiagramConnector;

public interface ConnectorDescriptor
{

    public abstract DiagramConnector createConnectorInstance() throws ConnectorException;

    public abstract String getGroupId();

    public abstract void setGroupId( String groupId );

    public abstract String getArtifactId();

    public abstract void setArtifactId( String artifactId );

    public abstract String getVersion();

    public abstract void setVersion( String version );

    public abstract String getDescription();

    public abstract void setDescription( String description );

    public abstract EnumSet<ConnectorInterfaceEnum> getProvidedInterfaces();

    public abstract void setProvidedInterfaces( EnumSet<ConnectorInterfaceEnum> providedInterfaces );

    /**
     * Return which type of interface to the described connector (static or dynamic) should be preferred by libraries
     * using the connector.
     * 
     * It have to be one of interfaces returned by getProvidedInterfaces
     * 
     * @return preferred interface type.
     */
    public abstract ConnectorInterfaceEnum getPreferredInterface();

    /**
     * Sets the preferred interface.
     * 
     * @param preferredInterface
     */
    public abstract void setPreferredInterface( ConnectorInterfaceEnum preferredInterface );

    public abstract String getName();

    public abstract void setName( String name );

    /**
     * The method returns the ConnectorConfiguration class that the connector use.
     * 
     * @return the class implementing {@link ConnectorConfiguration}
     */
    public abstract Class<? extends ConnectorConfiguration> getConfigurationClass();

    /**
     * The method sets the ConnectorConfiguration class that the connector use.
     * 
     * @param configurationClass
     *            to set.
     */
    public abstract void setConfigurationClass( Class<? extends ConnectorConfiguration> configurationClass );

    /**
     * It return set of mappings "tag name to class" used by xstream library to serialize and deserialize
     * {@link ConnectorConfiguration}
     * 
     * @return
     */
    public abstract List<Mapping> getMappings();

    /**
     * It sets mappings "tag name to class" used by xstream library to serialize and deserialize
     * {@link ConnectorConfiguration}
     * 
     * @param mappings
     *            to be set
     */
    public abstract void setMappings( List<Mapping> mappings );

}