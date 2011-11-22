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

import java.util.Map;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarLongFileMode;

public final class ArchiverManagerUtils
{

    public static final String TAR_LONG_FILE_MODE_OPTION = "tarLongFileMode";

    private ArchiverManagerUtils()
    {
    }

    public static Archiver getArchiver( ArchiverManager archiverManager, String extension, Map archiverOptions )
        throws NoSuchArchiverException, ArchiverException
    {
        Archiver archiver;
        
        if ( extension.startsWith( "tar" ) )
        {
            archiver = archiverManager.getArchiver( "tar" );

            if ( extension.length() > 3 )
            {
                // it's not a straight tar archive, but a compressed one. Figure out the compression
                // method.
                String compressionType = extension.substring( "tar.".length() );

                TarArchiver.TarCompressionMethod compressionMethod = new TarArchiver.TarCompressionMethod();

                if ( "gz".equals( compressionType ) )
                {
                    compressionMethod.setValue( "gzip" );
                }
                else if ( "bz2".equals( compressionType ) )
                {
                    compressionMethod.setValue( "bzip2" );
                }

                if ( compressionMethod.getValue() == null )
                {
                    throw new ArchiverException( "Invalid tar compression method specified: " + compressionType );
                }
                else
                {
                    ( (TarArchiver) archiver ).setCompression( compressionMethod );
                }
            }

            if ( archiverOptions != null )
            {
                String tarLongFileMode = (String) archiverOptions.get( TAR_LONG_FILE_MODE_OPTION );

                if ( tarLongFileMode != null )
                {
                    TarLongFileMode tarFileMode = new TarLongFileMode();

                    tarFileMode.setValue( tarLongFileMode );

                    ( (TarArchiver) archiver ).setLongfile( tarFileMode );
                }
            }
        }
        else
        {
            archiver = archiverManager.getArchiver( extension );
        }

        return archiver;
    }

}
