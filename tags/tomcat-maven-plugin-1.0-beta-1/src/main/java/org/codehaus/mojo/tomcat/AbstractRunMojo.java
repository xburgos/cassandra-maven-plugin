package org.codehaus.mojo.tomcat;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Embedded;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Abstract goal that provides common configuration for embedded Tomcat goals.
 * 
 * @author Jurgen Lust
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public abstract class AbstractRunMojo
    extends AbstractI18NMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The packaging of the Maven project that this goal operates upon.
     * 
     * @parameter expression = "${project.packaging}"
     * @required
     * @readonly
     */
    private String packaging;

    /**
     * The directory to create the Tomcat server configuration under.
     * 
     * @parameter expression="${project.build.directory}/tomcat"
     */
    private File configurationDir;

    /**
     * The port to run the Tomcat server on.
     * 
     * @parameter expression="${maven.tomcat.port}" default-value="8080"
     */
    private int port;

    /**
     * List of System properties to pass to the Tomcat Server.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    private Map<String, String> systemProperties;

    /**
     * The directory contains additional configuration Files that copied in the Tomcat conf Directory.
     * 
     * @parameter expression = "${maven.tomcat.additionalConfigFilesDir}" default-value="${basedir}/src/main/tomcatconf"
     * @since 1.0-alpha-2
     */
    private File additionalConfigFilesDir;

    /**
     * server.xml to use <b>Note if you use this you must configure in this file your webapp paths</b>.
     * 
     * @parameter expression="${maven.tomcat.serverXml}"
     * @since 1.0-alpha-2
     */
    private File serverXml;

    /**
     * overriding the providing web.xml to run tomcat
     * 
     * @parameter expression="${maven.tomcat.webXml}"
     * @since 1.0-alpha-2
     */
    private File tomcatWebXml;

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * The embedded Tomcat server.
     */
    private Embedded container;

    // ----------------------------------------------------------------------
    // Mojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // ensure project is a web application
        if ( !isWar() )
        {
            getLog().info( getMessage( "AbstractRunMojo.nonWar" ) );
            return;
        }

        try
        {
            getLog().info( getMessage( "AbstractRunMojo.runningWar", getWebappUrl() ) );

            initConfiguration();
            startContainer();
            waitIndefinitely();
        }
        catch ( LifecycleException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractRunMojo.cannotStart" ), exception );
        }
        catch ( IOException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractRunMojo.cannotCreateConfiguration" ), exception );
        }
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /**
     * Gets the webapp context path to use for the web application being run.
     * 
     * @return the webapp context path
     */
    protected String getPath()
    {
        return path;
    }

    /**
     * Gets the context to run this web application under for the specified embedded Tomcat.
     * 
     * @param container the embedded Tomcat container being used
     * @return the context to run this web application under
     * @throws IOException if the context could not be created
     */
    protected Context createContext( Embedded container )
        throws IOException
    {
        Context context = container.createContext( getPath(), getDocBase().getAbsolutePath() );

        context.setLoader( createWebappLoader() );
        context.setConfigFile( getContextFile().getAbsolutePath() );

        return context;
    }

    /**
     * Gets the webapp loader to run this web application under.
     * 
     * @return the webapp loader to use
     * @throws IOException if the webapp loader could not be created
     */
    protected WebappLoader createWebappLoader()
        throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        return new WebappLoader( classLoader );
    }

    /**
     * Gets the webapp directory to run.
     * 
     * @return the webapp directory
     */
    protected abstract File getDocBase();

    /**
     * Gets the Tomcat context XML file to use.
     * 
     * @return the context XML file
     */
    protected abstract File getContextFile();

    // ----------------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------------

    /**
     * Gets whether this project uses WAR packaging.
     * 
     * @return whether this project uses WAR packaging
     */
    protected boolean isWar()
    {
        return "war".equals( packaging );
    }

    /**
     * Gets the URL of the running webapp.
     * 
     * @return the URL of the running webapp
     * @throws MalformedURLException if the running webapp URL is invalid
     */
    private URL getWebappUrl()
        throws MalformedURLException
    {
        return new URL( "http", "localhost", port, getPath() );
    }

    /**
     * Creates the Tomcat configuration directory with the necessary resources.
     * 
     * @throws IOException if the Tomcat configuration could not be created
     * @throws MojoExecutionException if the Tomcat configuration could not be created
     */
    private void initConfiguration()
        throws IOException, MojoExecutionException
    {
        if ( configurationDir.exists() )
        {
            getLog().info( getMessage( "AbstractRunMojo.usingConfiguration", configurationDir ) );
        }
        else
        {
            getLog().info( getMessage( "AbstractRunMojo.creatingConfiguration", configurationDir ) );

            configurationDir.mkdirs();

            File confDir = new File( configurationDir, "conf" );
            confDir.mkdir();

            copyFile( "/conf/tomcat-users.xml", new File( confDir, "tomcat-users.xml" ) );
            if ( tomcatWebXml != null )
            {
                if ( !tomcatWebXml.exists() )
                {
                    throw new MojoExecutionException( " tomcatWebXml " + tomcatWebXml.getPath() + " not exists" );
                }

                copyFile( tomcatWebXml.getPath(), new File( confDir, "web.xml" ) );
            }
            else
            {
                copyFile( "/conf/web.xml", new File( confDir, "web.xml" ) );
            }

            File logDir = new File( configurationDir, "logs" );
            logDir.mkdir();

            File webappsDir = new File( configurationDir, "webapps" );
            webappsDir.mkdir();

            if ( additionalConfigFilesDir != null && additionalConfigFilesDir.exists() )
            {
                DirectoryScanner scanner = new DirectoryScanner();
                scanner.addDefaultExcludes();
                scanner.setBasedir( additionalConfigFilesDir.getPath() );
                scanner.scan();

                String[] files = scanner.getIncludedFiles();

                if ( files != null && files.length > 0 )
                {
                    getLog().info( "Coping additional tomcat config files" );

                    for ( int i = 0; i < files.length; i++ )
                    {
                        File file = new File( additionalConfigFilesDir, files[i] );

                        getLog().info( " copy " + file.getName() );

                        FileUtils.copyFileToDirectory( file, confDir );
                    }
                }
            }
        }
    }

    /**
     * Copies the specified class resource to the specified file.
     * 
     * @param fromPath the path of the class resource to copy
     * @param toFile the file to copy to
     * @throws IOException if the file could not be copied
     */
    private void copyFile( String fromPath, File toFile )
        throws IOException
    {
        URL fromURL = getClass().getResource( fromPath );

        if ( fromURL == null )
        {
            throw new FileNotFoundException( fromPath );
        }

        FileUtils.copyURLToFile( fromURL, toFile );
    }

    /**
     * Starts the embedded Tomcat server.
     * 
     * @throws IOException if the server could not be configured
     * @throws LifecycleException if the server could not be started
     * @throws MojoExecutionException if the server could not be configured
     */
    private void startContainer()
        throws IOException, LifecycleException, MojoExecutionException
    {
        if ( serverXml != null )
        {
            if ( !serverXml.exists() )
            {
                throw new MojoExecutionException( serverXml.getPath() + " not exists" );
            }

            container = new Catalina();
            container.setCatalinaHome( configurationDir.getAbsolutePath() );
            ( (Catalina) container ).setConfigFile( serverXml.getPath() );
            container.start();
        }
        else
        {
            // create server
            container = new Embedded();
            container.setCatalinaHome( configurationDir.getAbsolutePath() );
            container.setRealm( new MemoryRealm() );

            // create context
            Context context = createContext( container );

            // create host
            String appBase = new File( configurationDir, "webapps" ).getAbsolutePath();
            Host host = container.createHost( "localHost", appBase );
            host.addChild( context );

            // create engine
            Engine engine = container.createEngine();
            engine.setName( "localEngine" );
            engine.addChild( host );
            engine.setDefaultHost( host.getName() );
            container.addEngine( engine );

            // create http connector
            Connector httpConnector = container.createConnector( (InetAddress) null, port, false );
            container.addConnector( httpConnector );
        }

        // Set the system properties
        setupSystemPropteries();

        // start server
        container.start();

        // add shutdown hook to stop server
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                stopContainer();
            }
        } );
    }

    /**
     * Causes the current thread to wait indefinitely. This method does not return.
     */
    private void waitIndefinitely()
    {
        Object lock = new Object();

        synchronized ( lock )
        {
            try
            {
                lock.wait();
            }
            catch ( InterruptedException exception )
            {
                getLog().warn( getMessage( "AbstractRunMojo.interrupted" ), exception );
            }
        }
    }

    /**
     * Stops the embedded Tomcat server.
     */
    private void stopContainer()
    {
        try
        {
            if ( container != null )
            {
                container.stop();
            }
        }
        catch ( LifecycleException exception )
        {
            getLog().warn( getMessage( "AbstractRunMojo.cannotStop" ), exception );
        }
    }

    /**
     * Set the SystemProperties from the configuration.
     */
    private void setupSystemPropteries()
    {
        if ( systemProperties != null && !systemProperties.isEmpty() )
        {
            getLog().info( "setting SystemProperties:" );

            for ( String key : systemProperties.keySet() )
            {
                String value = systemProperties.get( key );

                if ( value != null )
                {
                    getLog().info( " " + key + "=" + value );
                    System.setProperty( key, value );
                }
                else
                {
                    getLog().info( "skip sysProps " + key + " with empty value" );
                }
            }
        }
    }
}
