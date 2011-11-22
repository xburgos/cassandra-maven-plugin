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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Embedded;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
     * The AJP port to run the Tomcat server on.
     * By default it's 0 this means won't be started. 
     * The ajp connector will be started only for value > 0. 
     * @since 1.2
     * @parameter expression="${maven.tomcat.ajp.port}" default-value="0"
     */
    private int ajpPort;
    
    /**
     * The AJP protocol to run the Tomcat server on.
     * By default it's ajp. 
     * NOTE The ajp connector will be started only if {@link #ajpPort} > 0. 
     * @since 1.2
     * @parameter expression="${maven.tomcat.ajp.protocol}" default-value="ajp"
     */    
    private String ajpProtocol;
    
    /**
     * The https port to run the Tomcat server on.
     * By default it's 0 this means won't be started. 
     * The https connector will be started only for value > 0. 
     * @since 1.0
     * @parameter expression="${maven.tomcat.httpsPort}" default-value="0"
     */
    private int httpsPort;

    /**
     * The character encoding to use for decoding URIs.
     * @since 1.0
     * @parameter expression="${maven.tomcat.uriEncoding}" default-value="ISO-8859-1"
     */
    private String uriEncoding;

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
    
    /**
     * Set this to true to allow Maven to continue to execute after invoking 
     * the run goal.
     * 
     * @parameter expression="${maven.tomcat.fork}" default-value="false"
     * @since 1.0
     */
    private boolean fork;
    
    /**
     * Will create a tomcat context for each dependencies of war type with 'scope' set to 'tomcat'.
     * In other words, dependencies with:
     * <pre>
     *    &lt;type&gt;war&lt;/type&gt;
     *    &lt;scope&gt;tomcat&lt;/scope&gt;
     * </pre>
     * To preserve backward compatibility it's false by default.
     * @parameter expression="${maven.tomcat.addContextWarDependencies}" default-value="false"
     * @since 1.0
     */
    private boolean addContextWarDependencies;
        
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @since 1.0
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The archive manager.
     * @since 1.0
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * if <code>true</code> a new classLoader separated from maven core will be created to start tomcat.
     * @parameter expression="${tomcat.useSeparateTomcatClassLoader}" default-value="false"
     * @since 1.0
     */
    protected boolean useSeparateTomcatClassLoader;
        
    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @since 1.0
     */
    @SuppressWarnings("rawtypes")
    private List pluginArtifacts;
   
    /**
     * If set to true ignore if packaging of project is not 'war'.
     * @since 1.0
     * @parameter expression="${tomcat.ignorePackaging}" default-value="false"
     */
    private boolean ignorePackaging;

    /**
     * Override the default keystoreFile for the HTTPS connector (if enabled)
     * @since 1.1
     * @parameter
     */
    private String keystoreFile;

    /**
     * Override the default keystorePass for the HTTPS connector (if enabled)
     * @since 1.1
     * @parameter
     */
    private String keystorePass;

    /**
     * <p>
     * Enables or disables naming support for the embedded Tomcat server. By default the embedded Tomcat
     * in Tomcat 6 comes with naming enabled. In contrast to this the embedded Tomcat 7 comes with
     * naming disabled by default.
     * </p>
     * <p>
     * With release 1.2 of this plugin useNaming defaults to false as it does for Tomcat 7. To enable it,
     * set this parameter to true or set the property <code>maven.tomcat.useNaming</code>.
     * </p>
     * <p>
     * <strong>Note:</strong> This setting is ignored if you provide a <code>server.xml</code> for your
     * Tomcat. Instead please configure naming in the <code>server.xml</code>.
     * </p>
     * @see <a href="http://tomcat.apache.org/tomcat-6.0-doc/api/org/apache/catalina/startup/Embedded.html">org.apache.catalina.startup.Embedded</a>
     * @see <a href="http://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/catalina/startup/Tomcat.html">org.apache.catalina.startup.Tomcat</a>
     * @parameter expression="${maven.tomcat.useNaming}" default-value="false"
     * @since 1.2
     * @todo adopt documentation once Tomcat 7 is supported (MTOMCAT-62)
     */
    private boolean useNaming;
    
    /**
     * Force context scanning if you don't use a context file with reloadable = "true".
     * The other way to use contextReloadable is to add attribute reloadable = "true"
     * in your context file.
     * @parameter expression="${maven.tomcat.contextReloadable}" default-value="false"
     * @since 1.2
     */
    protected boolean contextReloadable;
    

    /**
     * The path of the Tomcat context XML file.
     * 
     * @parameter expression = "src/main/webapp/META-INF/context.xml"
     */
    protected File contextFile;    

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * @since 1.0
     */
    private ClassRealm tomcatRealm;
    
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
        ClassLoader originalClassLoaser = Thread.currentThread().getContextClassLoader();
        try
        {
            if ( useSeparateTomcatClassLoader )
            {
                Thread.currentThread().setContextClassLoader( getTomcatClassLoader() );
            }
            getLog().info( getMessage( "AbstractRunMojo.runningWar", getWebappUrl() ) );

            initConfiguration();
            startContainer();
            if ( !fork )
            {
                waitIndefinitely();
            }
        }
        catch ( LifecycleException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractRunMojo.cannotStart" ), exception );
        }
        catch ( IOException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractRunMojo.cannotCreateConfiguration" ), exception );
        } finally
        {
            if (useSeparateTomcatClassLoader)
            {
                Thread.currentThread().setContextClassLoader( originalClassLoaser );
            }
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
     * @throws MojoExecutionException in case of an error creating the context
     */
    protected Context createContext( Embedded container )
        throws IOException, MojoExecutionException
    {
        String contextPath = getPath();
        Context context = container.createContext( "/".equals( contextPath ) ? "" : contextPath, getDocBase()
            .getAbsolutePath() );

        if ( useSeparateTomcatClassLoader )
        {
            context.setParentClassLoader( getTomcatClassLoader() );
        }

        context.setLoader( createWebappLoader() );
        File contextFile = getContextFile();
        if (contextFile != null)
        {
            context.setConfigFile( getContextFile().getAbsolutePath() );
        }
        return context;
    }

    /**
     * Gets the webapp loader to run this web application under.
     * 
     * @return the webapp loader to use
     * @throws IOException if the webapp loader could not be created
     * @throws MojoExecutionException in case of an error creating the webapp loader
     */
    protected WebappLoader createWebappLoader()
        throws IOException, MojoExecutionException
    {
        if ( useSeparateTomcatClassLoader )
        {
            return ( isContextReloadable() )
                    ? new ExternalRepositoriesReloadableWebappLoader( getTomcatClassLoader(), getLog() )
                    : new WebappLoader( getTomcatClassLoader() );
        }

        return ( isContextReloadable() )
                ? new ExternalRepositoriesReloadableWebappLoader( Thread.currentThread().getContextClassLoader(), getLog()  )
                : new WebappLoader( Thread.currentThread().getContextClassLoader() );
    }

    /**
     * Determine whether the passed context.xml file declares the context as reloadable or not.
     * @return false by default, true if  reloadable="true" in context.xml.
     */
    protected boolean isContextReloadable() throws MojoExecutionException
    {
        if ( contextReloadable )
        {
            return true;
        }
        // determine whether to use a reloadable Loader or not (default is false).
        boolean reloadable = false;
        try
        {
            if ( contextFile !=null && contextFile.exists() )
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document contextDoc = builder.parse( contextFile );
                contextDoc.getDocumentElement().normalize();

                NamedNodeMap nodeMap = contextDoc.getDocumentElement().getAttributes();
                Node reloadableAttribute = nodeMap.getNamedItem( "reloadable" );

                reloadable = ( reloadableAttribute != null ) ? Boolean.valueOf( reloadableAttribute.getNodeValue() )
                                                            : false;
            }
            getLog().debug( "context reloadable: " + reloadable );
        }
        catch ( IOException ioe )
        {
            getLog().error( "Could not parse file: [" + contextFile.getAbsolutePath() + "]", ioe );
        }
        catch ( ParserConfigurationException pce )
        {
            getLog().error( "Could not configure XML parser", pce );
        }
        catch ( SAXException se )
        {
            getLog().error( "Could not parse file: [" + contextFile.getAbsolutePath() + "]", se );
        }

        return reloadable;
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
    protected abstract File getContextFile() throws MojoExecutionException;

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
        return "war".equals( packaging ) || ignorePackaging;
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

            File confDir = new File( configurationDir, "conf");
            confDir.mkdir();

            copyFile("/conf/tomcat-users.xml", new File( confDir, "tomcat-users.xml" ) );
            if ( tomcatWebXml != null )
            {
                if ( !tomcatWebXml.exists() )
                {
                    throw new MojoExecutionException( " tomcatWebXml " + tomcatWebXml.getPath() + " not exists" );
                }
                //MTOMCAT-42  here it's a real file resources not a one coming with the mojo 
                FileUtils.copyFile( tomcatWebXml, new File( confDir, "web.xml" ) );
                //copyFile( tomcatWebXml.getPath(), new File( confDir, "web.xml" ) );
            }
            else
            {
                copyFile("/conf/web.xml", new File( confDir, "web.xml" ) );
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
        
        // Set the system properties
        setupSystemProperties();
        final Embedded container;
        if ( serverXml != null )
        {
            if ( !serverXml.exists() )
            {
                throw new MojoExecutionException( serverXml.getPath() + " not exists" );
            }

            container = new Catalina();
            container.setCatalinaHome( configurationDir.getAbsolutePath() );
            ( (Catalina) container).setConfigFile( serverXml.getPath() );
            container.start();
        }
        else
        {
            // create server
            container = new Embedded();
            container.setCatalinaHome( configurationDir.getAbsolutePath() );
            container.setRealm( new MemoryRealm() );
            container.setUseNaming( useNaming );

            //container.createLoader( getTomcatClassLoader() ).

            // create context
            Context context = createContext(container);


            // create host
            String appBase = new File( configurationDir, "webapps" ).getAbsolutePath();
            Host host = container.createHost( "localHost", appBase );
            
            host.addChild( context );
            
            if ( addContextWarDependencies )
            {
                Collection<Context> dependecyContexts = createDependencyContexts(container);
                for ( Context extraContext : dependecyContexts )
                {
                    host.addChild( extraContext );
                }
            }

            // create engine
            Engine engine = container.createEngine();
            engine.setName( "localEngine" );
            engine.addChild( host );
            engine.setDefaultHost( host.getName() );
            container.addEngine( engine );
            if ( useSeparateTomcatClassLoader )
            {
                engine.setParentClassLoader( getTomcatClassLoader() );
            }
            // create http connector
            Connector httpConnector = container.createConnector( (InetAddress) null, port, false );
            if ( httpsPort > 0 )
            {
                httpConnector.setRedirectPort( httpsPort );
            }
            httpConnector.setURIEncoding( uriEncoding );
            container.addConnector( httpConnector );

            // create https connector
            if ( httpsPort > 0 )
            {
                Connector httpsConnector = container.createConnector( (InetAddress) null, httpsPort, true );
                if ( keystoreFile!=null)
                {
                    httpsConnector.setAttribute("keystoreFile", keystoreFile);
                }
                if ( keystorePass!=null)
                {
                    httpsConnector.setAttribute("keystorePass", keystorePass);
                }
                container.addConnector( httpsConnector );

            }

            // create ajp connector
            if ( ajpPort > 0 )
            {
                Connector ajpConnector = container.createConnector( (InetAddress) null, ajpPort, ajpProtocol );
                ajpConnector.setURIEncoding( uriEncoding );
                container.addConnector( ajpConnector );
            }
        }
        container.start();
        
        EmbeddedRegistry.getInstance().register(container);
    }

    protected ClassRealm getTomcatClassLoader()
        throws MojoExecutionException
    {
        if ( this.tomcatRealm != null )
        {
            return tomcatRealm;
        }
        try
        {
            ClassWorld world = new ClassWorld();
            ClassRealm root = world.newRealm( "tomcat", Thread.currentThread().getContextClassLoader() );

            for ( @SuppressWarnings("rawtypes")
            Iterator i = pluginArtifacts.iterator(); i.hasNext(); )
            {
                Artifact pluginArtifact = (Artifact) i.next();
                if ( "org.apache.tomcat".equals( pluginArtifact.getGroupId() ) )
                {
                    if ( pluginArtifact.getFile() != null )
                    {
                        root.addURL( pluginArtifact.getFile().toURI().toURL() );
                    }
                }
            }
            tomcatRealm = root;
            return root;
        }
        catch ( DuplicateRealmException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
        
    @SuppressWarnings( "unchecked" )
    public Set<Artifact> getProjectArtifacts()
    {
        return project.getArtifacts();
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
     * Set the SystemProperties from the configuration.
     */
    private void setupSystemProperties()
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
    
        
    /**
     * Allows the startup of additional webapps in the tomcat container by declaration with scope
     * "tomcat".
     * 
     * @param container tomcat
     * @return dependency tomcat contexts of warfiles in scope "tomcat"
     */
    private Collection<Context> createDependencyContexts( Embedded container ) throws MojoExecutionException
    {
        getLog().info( "Deploying dependency wars" );
        // Let's add other modules
        List<Context> contexts = new ArrayList<Context>();

        ScopeArtifactFilter filter = new ScopeArtifactFilter( "tomcat" );
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = project.getArtifacts();
        for ( Artifact artifact : artifacts )
        {

            // Artifact is not yet registered and it has neither test, nor a
            // provided scope, not is it optional
            if ( "war".equals( artifact.getType() ) && !artifact.isOptional() && filter.include( artifact ) )
            {
                getLog().info( "Deploy warfile: " + String.valueOf( artifact.getFile() ) );
                File webapps = new File( configurationDir, "webapps" );
                File artifactWarDir = new File( webapps, artifact.getArtifactId() );
                if ( !artifactWarDir.exists() )
                {
                    //dont extract if exists
                    artifactWarDir.mkdir();
                    try
                    {
                        UnArchiver unArchiver = archiverManager.getUnArchiver( "zip" );
                        unArchiver.setSourceFile( artifact.getFile() );
                        unArchiver.setDestDirectory( artifactWarDir );

                        // Extract the module
                        unArchiver.extract();
                    }
                    catch ( IOException e )
                    {
                        getLog().error( e );
                        continue;
                    }
                    catch ( NoSuchArchiverException e )
                    {
                        getLog().error( e );
                        continue;
                    }
                    catch ( ArchiverException e )
                    {
                        getLog().error( e );
                        continue;
                    }
                }
                WebappLoader webappLoader = new WebappLoader( Thread.currentThread().getContextClassLoader() );
                Context context = container.createContext( "/" + artifact.getArtifactId(), artifactWarDir.getAbsolutePath() );
                context.setLoader( webappLoader );
                File contextFile = getContextFile();
                if (contextFile != null)
                {
                    context.setConfigFile( getContextFile().getAbsolutePath() );
                }
                contexts.add( context );
                
            }
        }
        return contexts;
    }
}
