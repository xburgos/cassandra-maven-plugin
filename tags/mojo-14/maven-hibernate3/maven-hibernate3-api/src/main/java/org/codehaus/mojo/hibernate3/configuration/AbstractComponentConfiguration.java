package org.codehaus.mojo.hibernate3.configuration;

import org.codehaus.mojo.hibernate3.ExporterMojo;
import org.codehaus.mojo.hibernate3.HibernateUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.xml.sax.EntityResolver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.util.Properties;

public abstract class AbstractComponentConfiguration
    implements ComponentConfiguration
{
    private ExporterMojo exporterMojo;

// --------------------- Interface ComponentConfiguration ---------------------

    /**
     * @see ComponentConfiguration#getConfiguration(ExporterMojo)
     */
    public Configuration getConfiguration( ExporterMojo exporterMojo )
        throws MojoExecutionException
    {
        setExporterMojo( exporterMojo );

        validateParameters();

        Configuration configuration = createConfiguration();
        if ( configuration == null )
        {
            throw new MojoExecutionException( "Couldn't create Configuration object" );
        }
        doConfiguration( configuration );
        configuration.buildMappings();
        return configuration;
    }

    protected abstract Configuration createConfiguration();

    protected void doConfiguration( Configuration configuration )
    {
        String entityResolver = getExporterMojo().getComponentProperty( "entityresolver" );
        if ( entityResolver != null )
        {
            Object object = HibernateUtils.getClass( entityResolver, this.getClass() );
            if ( object != null )
            {
                configuration.setEntityResolver( (EntityResolver) object );
            }
        }

        String namingStrategy = getExporterMojo().getComponentProperty( "namingstrategy" );
        if ( namingStrategy != null )
        {
            Object object = HibernateUtils.getClass( namingStrategy, this.getClass() );
            if ( object != null )
            {
                getExporterMojo().getLog().info( "Using as namingstrategy " + namingStrategy );
                configuration.setNamingStrategy( (NamingStrategy) object );
            }
            else
            {
                getExporterMojo().getLog().error( "Couldn't resolve " + namingStrategy );
            }
        }

        File configurationFile = getConfigurationFile();
        if ( configurationFile != null )
        {
            configuration.configure( configurationFile );
        }

        Properties propertyFile = getPropertyFile();
        if ( propertyFile != null )
        {
            configuration.setProperties( propertyFile );
        }
    }

    protected File getConfigurationFile()
    {
        String configurationFile =
            getExporterMojo().getComponentProperty( "configurationfile", "src/main/resources/hibernate.cfg.xml" );
        getExporterMojo().getLog().debug( "basedir: " + getExporterMojo().getProject().getBasedir() );
        File configfile = HibernateUtils.getFile( getExporterMojo().getProject().getBasedir(), configurationFile );
        if ( configfile == null )
        {
            getExporterMojo().getLog().info(
                configurationFile + " not found within the project. Trying absolute path." );
            configfile = HibernateUtils.getFile( null, configurationFile );
        }

        if ( configfile != null )
        {
            getExporterMojo().getLog().info( "Configuration XML file loaded: " + configfile );
            return configfile;
        }

        getExporterMojo().getLog().info( "No hibernate configuration file loaded." );
        return null;
    }

    protected Properties getPropertyFile()
    {
        String propertyFile =
            getExporterMojo().getComponentProperty( "propertyfile", "src/main/resources/database.properties" );
        File propFile = HibernateUtils.getFile( getExporterMojo().getProject().getBasedir(), propertyFile );
        if ( propFile == null )
        {
            getExporterMojo().getLog().info( propertyFile + " not found within the project. Trying absolute path." );
            propFile = HibernateUtils.getFile( null, propertyFile );
        }

        if ( propFile != null )
        {
            try
            {
                getExporterMojo().getLog().info( "Configuration Properties file loaded: " + propFile );
                Properties properties = new Properties();
                properties.load( new FileInputStream( propFile ) );
                return properties;
            }
            catch ( IOException ioe )
            {
                getExporterMojo().getLog().info( "No hibernate properties file loaded: " + ioe.getMessage() );
            }
        }

        getExporterMojo().getLog().info( "No hibernate properties file loaded." );
        return null;
    }

    public ExporterMojo getExporterMojo()
    {
        return exporterMojo;
    }

    public void setExporterMojo( ExporterMojo exporterMojo )
    {
        this.exporterMojo = exporterMojo;
    }

    protected void validateParameters()
        throws MojoExecutionException
    {
        // noop
    }
}
