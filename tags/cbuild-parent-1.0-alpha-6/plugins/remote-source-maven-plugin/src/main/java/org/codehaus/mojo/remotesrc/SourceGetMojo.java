package org.codehaus.mojo.remotesrc;

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
import java.util.List;
import java.util.Vector;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.io.download.DownloadFailedException;
import org.apache.maven.shared.io.download.DownloadManager;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;
import org.codehaus.plexus.util.StringUtils;

/**
 * Retrieve project sources from some external URL, and unpack them (into target, by default).
 * Dependency resolution is required here to keep from downloading a potentially huge source tarball
 * if the dependencies cannot be satisfied.
 * 
 * @goal get
 * @phase initialize
 * @requiresDependencyResolution compile
 */
public class SourceGetMojo
    extends GetSourceArtifactMojo
{

    /**
     * URL from which to retrieve the project sources.
     * 
     * @parameter
     */
    private String url;

    /**
     * Component used to manage the retrieval of files from remote URLs, mapped by protocol.
     * 
     * @component role="org.apache.maven.shared.io.download.DownloadManager"
     * @required
     * @readonly
     */
    private DownloadManager downloadManager;
    
    /**
     * Perform the source archive retrieval, and unpack it.
     * 
     * This involves:
     * 
     * o 
     * o Setting up the transfer monitor (displays progress for the download)
     * o Download the archive to a temp file
     * o 
     */
    public void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Resolving source archive for project: " + getProjectId() + " from: {local, URL, repository}." );
        
        if ( StringUtils.isNotEmpty( url ) )
        {
            File local = new File( url );
            
            if ( local.exists() )
            {
                getLog().info( "Using local archive at: " + local );
                
                setSourceArchiveFile( getProjectId(), local );
            }
            else
            {
                getLog().info( "Supplied archive URL is not a local file." );
            }
        }
        else
        {
            getLog().info( "Source archive URL was not specified." );
        }
        
        if ( getSourceArchiveFile( getProjectId() ) == null )
        {
            getLog().info( "Resolving source archive for project: " + getProjectId() + " from: {URL, repository}." );
            
            List transferListeners = new Vector();
            
            try
            {
                File downloaded = downloadManager.download( url, transferListeners, new DefaultMessageHolder() );
                
                String ext = ArchiveFileExtensions.getArchiveFileExtension( url );
                String basename = downloaded.getName();
                
                basename = basename.substring( 0, basename.length() - ext.length() );
                
                File newName = new File( downloaded.getParentFile(), basename + "." + ext );
                
                downloaded.renameTo( newName );
                
                downloaded = newName;
                
                setSourceArchiveFile( getProject().getId(), downloaded );
            }
            catch ( DownloadFailedException e )
            {
                getLog().debug( "Failed to download source from: " + url, e );
            }
        }
        
        if ( getSourceArchiveFile( getProjectId() ) == null )
        {
            getLog().info( "Attempting to retrieve source artifact from repository." );
            super.doExecute();
        }
    }
    
    protected CharSequence getSkipMessage()
    {
        return "Skipping remote source retrieval (per configuration).";
    }
}
