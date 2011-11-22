package org.codehaus.mojo.patch;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.artifact.manager.CredentialsDataSourceException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.mojo.tools.context.BuildAdvisor;

/**
 * Retrieve patch files from remote URLs
 * 
 * @goal get
 * @phase initialize
 */
public class GetPatchesMojo
    extends AbstractMojo
{
    
    /**
     * Whether to skip this mojo's execution.
     * 
     * @parameter default-value="false" alias="patch.get.skip"
     */
    private boolean skipRetrieval;

    /**
     * URLs from which to retrieve the patch files.
     * 
     * @parameter
     */
    private List urls;

    /**
     * The directory into which to store downloaded patch files.
     * 
     * @parameter default-value="${project.build.directory}/patches"
     * @required
     */
    private File patchSourceDir;
    
    /**
     * Component used to manage the different Wagon providers. These providers
     * handle retrieving files from remote URLs, mapped by protocol.
     * 
     * @component role="org.apache.maven.artifact.manager.WagonManager"
     * @required
     * @readonly
     */
    private WagonManager wagonManager;
    
    /** 
     * The current project being built.  Used to get definitions in the POM like packaging.
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /** 
     * Session will be used in Maven 3.0 for a memory based implementation of BuildAdvisor.
     *
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;
    
    /**
     * BuildAdvisor is used to get/set state between plugins.
     *
     * @component role-hint="default"
     */
    private BuildAdvisor buildAdvisor;

    /**
     * Perform the patch file retrieval.
     * 
     * This involves:
     * 
     * o Setting up the transfer monitor (displays progress for the download)
     * o Download the patch file to the desired directory
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipRetrieval )
        {
            getLog().info( "Skipping remote patchfile retrieval (per configuration)." );
            return;
        }
        
        if ( buildAdvisor.isProjectBuildSkipped( session ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        if ( urls == null || urls.isEmpty() )
        {
            getLog().info( "Nothing to do" );
            return;
        }
        else
        {
            if ( !patchSourceDir.exists() )
            {
                patchSourceDir.mkdirs();
            }
            
            for ( Iterator it = urls.iterator(); it.hasNext(); )
            {
                String url = (String) it.next();
                
                retrievePatch( url );
            }
        }
    }

    private void retrievePatch( String url )
        throws MojoExecutionException
    {
        URL sourceUrl;
        try
        {
            sourceUrl = new URL( url );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Invalid source URL: \'" + url + "\'", e );
        }

        Wagon wagon = null;

        File downloaded;

        try
        {
            // Retrieve the correct Wagon instance used to download the remote archive
            wagon = wagonManager.getWagon( sourceUrl.getProtocol() );

            // split the download URL into base URL and remote path for connecting, then retrieving.
            String remotePath = sourceUrl.getPath();
            String baseUrl = url.substring( 0, url.length() - remotePath.length() );
            
            int lastSlash = remotePath.replace('\\', '/').lastIndexOf( '/' );
            
            String filename = remotePath;
            
            if ( lastSlash > -1 && lastSlash < remotePath.length() - 1 )
            {
                filename = remotePath.substring( lastSlash + 1 );
            }

            // create the landing file in /tmp for the downloaded source archive
            downloaded = new File( patchSourceDir, filename );

            // connect to the remote site, and retrieve the archive. Note the separate methods in which
            // base URL and remote path are used.
            Repository repo = new Repository( "source-get", baseUrl );
            
            wagon.connect( repo, wagonManager.getAuthenticationInfo( repo.getId() ), wagonManager.getProxy( sourceUrl
                .getProtocol() ) );
            
            wagon.get( remotePath, downloaded );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new MojoExecutionException( "Invalid source protocol: \'" + sourceUrl.getProtocol() + "\'", e );
        }
        catch ( TransferFailedException e )
        {
            throw new MojoExecutionException( "Failed to download source from: " + url, e );
        }
        catch ( ResourceDoesNotExistException e )
        {
            throw new MojoExecutionException( "Failed to download source from: " + url, e );
        }
        catch ( AuthorizationException e )
        {
            throw new MojoExecutionException( "Failed to download source from: " + url, e );
        }
        catch ( ConnectionException e )
        {
            throw new MojoExecutionException( "Failed to download source from: " + url, e );
        }
        catch ( AuthenticationException e )
        {
            throw new MojoExecutionException( "Failed to download source from: " + url, e );
        }
// uncomment when upgrading to maven 3.0
//      catch ( CredentialsDataSourceException e )
//      {
//          throw new MojoExecutionException( "Failed to download source from: " + url, e );
//      }
        finally
        {
            // ensure the Wagon instance is closed out properly.
            if ( wagon != null )
            {
                try
                {
                    wagon.disconnect();
                }
                catch ( ConnectionException e )
                {
                    getLog().debug( "Failed to disconnect wagon for: " + url, e );
                }
            }
        }
    }

}
