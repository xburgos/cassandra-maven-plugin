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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ArchiveFileExtensions
{

    public static final String ZIP_EXTENSION = "zip";

    public static final String TGZ_EXTENSION = "tar.gz";

    public static final String TGZ_EXTENSION2 = "tgz";

    public static final String TBZ_EXTENSION = "tar.Z";

    public static final String TBZ_EXTENSION2 = "tar.bz2";

    public static final String[] ARCHIVE_EXTENSIONS = {
        ZIP_EXTENSION,
        TGZ_EXTENSION,
        TGZ_EXTENSION2,
        TBZ_EXTENSION,
        TBZ_EXTENSION2,
        "tar" };

    public static final List BZIP_EXTENSIONS = new ArrayList()
    {
        {
            add( TBZ_EXTENSION );
            add( TBZ_EXTENSION2 );
        }
    };

    public static final List GZIP_EXTENSIONS = new ArrayList()
    {
        {
            add( TGZ_EXTENSION );
            add( TGZ_EXTENSION2 );
        }
    };

    private ArchiveFileExtensions()
    {
    }

    public static String getArchiveFileExtension( File archiveFile )
    {
        return getArchiveFileExtension( archiveFile.getName() );
    }
    
    public static String getArchiveFileExtension( String archiveFileName )
    {
        for ( int i = 0; i < ARCHIVE_EXTENSIONS.length; i++ )
        {
            String ext = ARCHIVE_EXTENSIONS[i];
            
            if ( archiveFileName.endsWith( ext ) )
            {
                return ext;
            }
        }
        
        int lastDot = archiveFileName.lastIndexOf( '.' );
        if ( lastDot > -1 )
        {
            return archiveFileName.substring( lastDot + 1 );
        }
        
        return null;
    }
    
    public static boolean isArchiveExtensionRecognized( String ext )
    {
        return Arrays.asList( ARCHIVE_EXTENSIONS ).contains( ext );
    }

}
