package org.codehaus.mojo.tools.fs.archive.manager;

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

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.components.io.resources.PlexusIoResourceCollection;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * ArchiverManager implementation that supports local overrides to the standard Archiver/Unarchiver
 * implementations given by the DefaultArchiverManager (which is the one given when no role-hint
 * is specified in the component requirement).
 * 
 * @plexus.component role="org.codehaus.mojo.tools.fs.archive.manager.ArchiverManager" role-hint="local-overrides"
 * 
 * @author jdcasey
 * 
 * @todo Change this to be an alternative to the DefaultArchiverManager, once that component has a role-hint.
 *
 */
public class LocalOverrideArchiverManager
    implements ArchiverManager, Contextualizable, LogEnabled
{
    
    public static final String LOCAL_OVERRIDE_ROLE_HINT_SUFFIX = "-local";
    
    /**
     * @plexus.requirement
     */
    private org.codehaus.plexus.archiver.manager.ArchiverManager archiverManager;

    private Logger logger;
    
    public LocalOverrideArchiverManager()
    {
        // used for plexus init
    }
    
    private PlexusContainer container;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    
    public Archiver getArchiver( String archiverName )
        throws NoSuchArchiverException
    {
        Archiver archiver = (Archiver) lookupLocal( Archiver.ROLE, archiverName );
        
        if ( archiver == null )
        {
            archiver = archiverManager.getArchiver( archiverName );
        }
        
        return archiver;
    }

    public UnArchiver getUnArchiver( String unArchiverName )
        throws NoSuchArchiverException
    {
        UnArchiver archiver = (UnArchiver) lookupLocal( UnArchiver.ROLE, unArchiverName );
        
        if ( archiver == null )
        {
            archiver = archiverManager.getUnArchiver( unArchiverName );
        }
        
        return archiver;
    }

    public PlexusIoResourceCollection getResourceCollection( String resourceCollectionName )
        throws NoSuchArchiverException
    {
        PlexusIoResourceCollection archiver = (PlexusIoResourceCollection) 
            lookupLocal( PlexusIoResourceCollection.ROLE, resourceCollectionName );
        
        if ( archiver == null )
        {
            archiver = archiverManager.getResourceCollection( resourceCollectionName );
        }
        
        return archiver;
    }    

    private String getFileExtention ( File file )
    {
        String path = file.getAbsolutePath();
        
        String archiveExt = FileUtils.getExtension( path ).toLowerCase();
        
        if ( "gz".equals( archiveExt ) || "bz2".equals( archiveExt ) )
        {
            String [] tokens = StringUtils.split( path, "." );
            
            if ( tokens.length > 2  && "tar".equals( tokens[tokens.length - 2].toLowerCase() ) )
            {
                archiveExt = "tar." + archiveExt;
            }
        }
        
        return archiveExt;
        
    }
    public Archiver getArchiver( File file )
        throws NoSuchArchiverException
    {
        return getArchiver( getFileExtention( file ) );
    }
    
    public UnArchiver getUnArchiver( File file )
        throws NoSuchArchiverException
    {        
        return getUnArchiver( getFileExtention( file ) );
    }

    public PlexusIoResourceCollection getResourceCollection( File file )
        throws NoSuchArchiverException
    {
        return getResourceCollection( getFileExtention( file ) );
    }

    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "LocalOverrideArchiverManager::internal" );
        }
        
        return logger;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    public LocalOverrideArchiverManager( org.codehaus.plexus.archiver.manager.ArchiverManager archiverManager,
                                         PlexusContainer container )
    {
        this.archiverManager = archiverManager;
        this.container = container;
    }

    protected Object lookupLocal( String role, String hint )
    {
        Object result = null;
        
        try
        {
            result = container.lookup( role, hint + LOCAL_OVERRIDE_ROLE_HINT_SUFFIX );
        }
        catch ( ComponentLookupException e )
        {
            getLogger().debug( "Failed to lookup local version for: " + role + hint + ". Reason: " + e.getMessage() );
        }
        
        return result;
    }

}
