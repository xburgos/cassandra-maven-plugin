package org.apache.maven.diagrams.connector_api.manager;

import java.io.InputStream;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.descriptor.ConnectorDescriptor;
import org.apache.maven.diagrams.connector_api.descriptor.Mapping;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * The class is able to create ConnectorConfiguration class from XML file (and the Connector's description) (from XML
 * method).
 * 
 * It also can serialize the ConnectorConfiguration class to the file (toXML method)
 * 
 * @author Piotr Tabor
 */
public class ConnectorManager
{
    public String toXML( ConnectorConfiguration config, ConnectorDescriptor desc ) throws ConnectorException
    {
        XStream xstream = getConfiguredXStream( desc );
        return xstream.toXML( config );
    }

    public ConnectorConfiguration fromXML( InputStream is, ConnectorDescriptor desc ) throws ConnectorException
    {
        XStream xstream = getConfiguredXStream( desc );
        ConnectorConfiguration result;
        try
        {
            result = desc.getConfigurationClass().newInstance();
        }
        catch ( InstantiationException e )
        {
            throw new ConnectorException( "Cannot create instance of class: " + desc.getConfigurationClass().getName(),
                                          e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ConnectorException( "Cannot create instance of class: " + desc.getConfigurationClass().getName(),
                                          e );
        }
        xstream.fromXML( is, result );
        return result;
    };

    private XStream getConfiguredXStream( ConnectorDescriptor desc ) throws ConnectorException
    {
        XStream xstream = new XStream();
        xstream.aliasType( "configuration", ConnectorConfiguration.class );
        if ( desc!=null && desc.getMappings() != null )
        {
            for ( Mapping m : desc.getMappings() )
            {
                xstream.aliasType( m.getTagName(), m.getClazz() );
                if ( m.getConverter() != null )
                {

                    try
                    {
                        if ( SingleValueConverter.class.isAssignableFrom( m.getConverter() ) )
                            xstream.registerConverter( (SingleValueConverter) m.getConverter().newInstance() );

                        if ( Converter.class.isAssignableFrom( m.getConverter() ) )
                            xstream.registerConverter( (Converter) m.getConverter().newInstance() );
                    }
                    catch ( InstantiationException e )
                    {
                        throw new ConnectorException( "Cannot create instance of class: " + m.getConverter().getName(),
                                                      e );
                    }
                    catch ( IllegalAccessException e )
                    {
                        throw new ConnectorException( "Cannot create instance of class: " + m.getConverter().getName(),
                                                      e );
                    }

                }
            }
        }
        return xstream;
    }

}
