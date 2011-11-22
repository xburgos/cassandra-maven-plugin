package org.codehaus.mojo.tools.fs.archive;

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
import java.io.IOException;

import org.codehaus.mojo.tools.fs.archive.manager.ArchiverManager;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Unpack an archive file. Be flexible on the types of archives handled.
 * 
 * @plexus.component role="org.codehaus.mojo.tools.fs.archive.ArchiveExpander"
 *            role-hint="default"
 *            
 * @author jdcasey
 */
public class DefaultArchiveExpander
    implements ArchiveExpander
{

    public static final String ROLE_HINT = "default";
    
    /**
     * @plexus.requirement role-hint="local-overrides"
     */
    private ArchiverManager archiverManager;
    
    /**
     * execute the expansion
     * @throws ArchiveExpansionException 
     * @throws IOException 
     */
    public void expand( ArchiveExpansionRequest request )
        throws ArchiveExpansionException, IOException
    {
        File expandTarget = request.getExpandTarget();
        String overwriteCheckSubpath = request.getOverwriteCheckSubpath();

        if ( expandTarget == null )
        {
            throw new ArchiveExpansionException( "No expandTarget specified. Nowhere to unpack into!" );
        }

        // If we're set to NOT overwrite, we need to determine whether the unpack 
        // code should run.
        if ( !request.isOverwrite() )
        {
            File workDir = new File( expandTarget, overwriteCheckSubpath );

            if ( workDir.exists() )
            {
                return;
            }
        }

        UnArchiver unArchiver;
        try
        {
            unArchiver = archiverManager.getUnArchiver( request.getSourceArchive() );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new ArchiveExpansionException( "Error retrieving un-archiver for: " + request.getSourceArchive(), e );
        }
        
        unArchiver.setSourceFile( request.getSourceArchive() );
        unArchiver.setDestDirectory( request.getExpandTarget() );
        unArchiver.setOverwrite( request.isOverwrite() );
        
        try
        {
            unArchiver.extract();
        }
        catch ( ArchiverException e )
        {
            throw new ArchiveExpansionException( "Error unpacking archive: " + request.getSourceArchive(), e );
        }
    }
}
