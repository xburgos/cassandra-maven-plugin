package org.codehaus.mojo.tomcat;

/*
 * Copyright 2006 Mark Hobson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Abstract goal that provides common configuration for embedded Tomcat goals.
 * 
 * @author Jurgen Lust
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public abstract class AbstractRunMojo extends AbstractI18NMojo
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
     * The webapp context path to use for the web application being run.
     * This must always start with a forward-slash ('/').
     *
     * @parameter expression = "/${project.build.finalName}"
     * @required
     */
    private String path;

    /**
     * The directory to create the Tomcat server configuration under.
     * 
     * @parameter expression = "${project.build.directory}/tomcat"
     */
    private String configurationDir;

    /**
     * The port to run the Tomcat server on.
     * 
     * @parameter default-value = "8080"
     */
    private int port;

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

    /*
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // ensure project is a web application
        if ( !isWar() )
        {
            getLog().info( getMessage( "RunMojo.nonWar" ) );
            return;
        }

        // set the maven log for our JCL adapter to use
        LogFactory.getFactory().setAttribute( "maven.log", getLog() );

        try
        {
            initConfiguration();
            startContainer();
            waitIndefinitely();
        }
        catch ( LifecycleException exception )
        {
            throw new MojoExecutionException( getMessage( "RunMojo.cannotStart" ), exception );
        }
        catch ( IOException exception )
        {
            throw new MojoExecutionException( getMessage( "RunMojo.cannotCreateConfiguration" ), exception );
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
     * @param container
     *            the embedded Tomcat container being used
     * @return the context to run this web application under
     * @throws IOException
     *             if the context could not be created
     */
    protected abstract Context createContext( Embedded container ) throws IOException;

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
     * Creates the Tomcat configuration directory with the necessary resources.
     * 
     * @throws IOException
     *             if the Tomcat configuration could not be created
     */
    private void initConfiguration() throws IOException
    {
        File configurationDirFile = new File( configurationDir );

        if ( configurationDirFile.exists() )
        {
            getLog().info( getMessage( "RunMojo.usingConfiguration", configurationDir ) );
        }
        else
        {
            getLog().info( getMessage( "RunMojo.creatingConfiguration", configurationDir ) );

            configurationDirFile.mkdirs();

            File confDir = new File( configurationDirFile, "conf" );
            confDir.mkdir();

            copyFile( "/conf/tomcat-users.xml", new File( confDir, "tomcat-users.xml" ) );
            copyFile( "/conf/web.xml", new File( confDir, "web.xml" ) );

            File webappsDir = new File( configurationDirFile, "webapps" );
            webappsDir.mkdir();
        }
    }

    /**
     * Copies the specified class resource to the specified file.
     * 
     * @param fromPath
     *            the path of the class resource to copy
     * @param toFile
     *            the file to copy to
     * @throws IOException
     *             if the file could not be copied
     */
    private void copyFile( String fromPath, File toFile ) throws IOException
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
     * @throws IOException
     *             if the server could not be configured
     * @throws LifecycleException
     *             if the server could not be started
     */
    private void startContainer() throws IOException, LifecycleException
    {
        // create server
        container = new Embedded();
        container.setCatalinaHome( configurationDir );
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

        // start server
        container.start();

        // add shutdown hook to stop server
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
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
                getLog().warn( getMessage( "RunMojo.interrupted" ), exception );
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
            getLog().warn( getMessage( "RunMojo.cannotStop" ), exception );
        }
    }
}
