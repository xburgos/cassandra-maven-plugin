package org.apache.maven.diagrams.connector_api.descriptor;

import java.util.EnumSet;
import java.util.List;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.DiagramConnector;

/**
 * ConnectorDescriptor is the same for Connector as PluginDescriptor for MavenPlugin.
 * 
 * It contains basic information about connector and the xstream mappings for the connector's configuration file.
 * 
 * In most cases the class is serialized and deserialized form XML by {@link ConnectorDescriptorBuilder}
 * 
 * @author Piotr Tabor
 */
public class ConnectorDescriptor
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;
    
    private String mainClassName;

    // private String source;

    private Class<? extends ConnectorConfiguration> configurationClass;

    private ConnectorInterfaceEnum preferredInterface;

    private EnumSet<ConnectorInterfaceEnum> providedInterfaces;

    // private List<Parameter> parameters;

    private List<Mapping> mappings;
    
    public DiagramConnector createConnectorInstance() throws ConnectorException
    {
        try
        {
            return (DiagramConnector)this.getClass().getClassLoader().loadClass( mainClassName ).newInstance();
        }
        catch ( InstantiationException e )
        {
            throw new ConnectorException("Cannot create instance of the connector: "+artifactId+":",e);
        }
        catch ( IllegalAccessException e )
        {
            throw new ConnectorException("Cannot create instance of the connector: "+artifactId+":",e);
        }
        catch ( ClassNotFoundException e )
        {
            throw new ConnectorException("Cannot create instance of the connector: "+artifactId+":",e);
        }
    }

    /*---------------- Getters and Setters ---------------------*/

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public EnumSet<ConnectorInterfaceEnum> getProvidedInterfaces()
    {
        return providedInterfaces;
    }

    public void setProvidedInterfaces( EnumSet<ConnectorInterfaceEnum> providedInterfaces )
    {
        this.providedInterfaces = providedInterfaces;
    }

    /**
     * Return which type of interface to the described connector (static or dynamic) should be preferred by libraries
     * using the connector.
     * 
     * It have to be one of interfaces returned by getProvidedInterfaces
     * 
     * @return preferred interface type.
     */
    public ConnectorInterfaceEnum getPreferredInterface()
    {
        return preferredInterface;
    }

    /**
     * Sets the preferred interface.
     * 
     * @param preferredInterface
     */
    public void setPreferredInterface( ConnectorInterfaceEnum preferredInterface )
    {
        this.preferredInterface = preferredInterface;
    }

    

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * The method returns the ConnectorConfiguration class that the connector use.
     * 
     * @return the class implementing {@link ConnectorConfiguration}
     */
    public Class<? extends ConnectorConfiguration> getConfigurationClass()
    {
        return configurationClass;
    }

    /**
     * The method sets the ConnectorConfiguration class that the connector use.
     * 
     * @param configurationClass
     *            to set.
     */
    public void setConfigurationClass( Class<? extends ConnectorConfiguration> configurationClass )
    {
        this.configurationClass = configurationClass;
    }

    /**
     * It return set of mappings "tag name to class" used by xstream library to serialize and deserialize
     * {@link ConnectorConfiguration}
     * 
     * @return
     */
    public List<Mapping> getMappings()
    {
        return mappings;
    }

    /**
     * It sets mappings "tag name to class" used by xstream library to serialize and deserialize
     * {@link ConnectorConfiguration}
     * 
     * @param mappings
     *            to be set
     */
    public void setMappings( List<Mapping> mappings )
    {
        this.mappings = mappings;
    }
}
