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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.plexus.util.StringUtils;

/**
 * Abstract goal that provides common configuration for Catalina-based goals.
 * 
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public abstract class AbstractCatalinaMojo
    extends AbstractI18NMojo
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    /**
     * The name of this Maven plugin. Used to produce the user agent when communicating with Tomcat manager.
     */
    private String name = "Tomcat Maven Plugin";

    /**
     * The default username to use when authenticating with Tomcat manager.
     */
    private static final String DEFAULT_USERNAME = "admin";

    /**
     * The default password to use when authenticating with Tomcat manager.
     */
    private static final String DEFAULT_PASSWORD = "";

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The Maven Wagon manager to use when obtaining server authentication details.
     * 
     * @component role="org.apache.maven.artifact.manager.WagonManager"
     * @required
     * @readonly
     */
    private WagonManager wagonManager;

    /**
     * The full URL of the Tomcat manager instance to use.
     * 
     * @parameter expression="${maven.tomcat.url}" default-value="http://localhost:8080/manager"
     * @required
     */
    private URL url;

    /**
     * The server id in settings.xml to use when authenticating with Tomcat manager, or <code>null</code> to use
     * defaults of username <code>admin</code> and no password.
     * 
     * @parameter expression="${maven.tomcat.server}"
     */
    private String server;

    /**
     * The URL encoding charset to use when communicating with Tomcat manager.
     * 
     * @parameter expression="${maven.tomcat.charset}" default-value="ISO-8859-1"
     * @required
     */
    private String charset;

    /**
     * The tomcat username to use for deployment
     * 
     * @parameter expression="${tomcat.username}"
     * @since 1.0-alpha-2
     */
    private String username;

    /**
     * The password to use for deployment
     * 
     * @parameter expression="${tomcat.password}"
     * @since 1.0-alpha-2
     */
    private String password;

    /**
     * @parameter expression="${plugin.version}"
     * @required
     * @readonly
     */
    private String version;

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * The Tomcat manager wrapper object.
     */
    private TomcatManager manager;

    // ----------------------------------------------------------------------
    // Mojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException
    {
        try
        {
            invokeManager();
        }
        catch ( TomcatManagerException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractCatalinaMojo.managerError", exception.getMessage() ) );
        }
        catch ( IOException exception )
        {
            throw new MojoExecutionException( getMessage( "AbstractCatalinaMojo.managerIOError" ), exception );
        }
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /**
     * Invokes Tomcat manager when this Mojo is executed.
     * 
     * @throws MojoExecutionException if there was a problem executing this goal
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    protected abstract void invokeManager()
        throws MojoExecutionException, TomcatManagerException, IOException;

    /**
     * Gets the Tomcat manager wrapper object configured for this goal.
     * 
     * @return the Tomcat manager wrapper object
     * @throws MojoExecutionException if there was a problem obtaining the authentication details
     */
    protected TomcatManager getManager()
        throws MojoExecutionException
    {
        // lazily instantiate when config values have been injected
        if ( manager == null )
        {
            String userName;
            String password;

            if ( server == null )
            {
                // no server set, use defaults
                getLog().debug( getMessage( "AbstractCatalinaMojo.defaultAuth" ) );
                userName = DEFAULT_USERNAME;
                password = DEFAULT_PASSWORD;
            }
            else
            {
                // obtain authenication details for specified server from wagon
                AuthenticationInfo info = wagonManager.getAuthenticationInfo( server );
                if ( info == null )
                {
                    throw new MojoExecutionException( getMessage( "AbstractCatalinaMojo.unknownServer", server ) );
                }

                // derive username
                userName = info.getUserName();
                if ( userName == null )
                {
                    getLog().debug( getMessage( "AbstractCatalinaMojo.defaultUserName" ) );
                    userName = DEFAULT_USERNAME;
                }

                // derive password
                password = info.getPassword();
                if ( password == null )
                {
                    getLog().debug( getMessage( "AbstractCatalinaMojo.defaultPassword" ) );
                    password = DEFAULT_PASSWORD;
                }
            }

            // if userName/password are defined in the mojo or the cli they override
            if ( !StringUtils.isEmpty( this.username ) )
            {
                userName = this.username;
                password = this.password == null ? "" : this.password;
            }

            manager = new TomcatManager( url, userName, password, charset );
            manager.setUserAgent( name + "/" + version );
        }

        return manager;
    }

    /**
     * Gets the full URL of the Tomcat manager instance.
     * 
     * @return the full URL of the Tomcat manager instance to use
     */
    protected URL getURL()
    {
        return url;
    }

    /**
     * Gets the webapp context path to use when communicating with Tomcat manager.
     * 
     * @return the webapp context path to use
     */
    protected String getPath()
    {
        return path;
    }

    /**
     * Gets the URL of the deployed webapp.
     * 
     * @return the URL of the deployed webapp
     * @throws MalformedURLException if the deployed webapp URL is invalid
     */
    protected URL getDeployedURL()
        throws MalformedURLException
    {
        return new URL( getURL(), getPath() );
    }

    /**
     * Splits the given string into lines and writes each one separately to the log at info level.
     * 
     * @param string the string to write
     */
    protected void log( String string )
    {
        StringTokenizer tokenizer = new StringTokenizer( string, "\n\r" );

        while ( tokenizer.hasMoreTokens() )
        {
            getLog().info( tokenizer.nextToken() );
        }
    }
}
