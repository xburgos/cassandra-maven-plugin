package org.codehaus.mojo.tools.project.extras;

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
import java.util.Set;

import org.codehaus.plexus.util.DirectoryScanner;

public final class ScanningUtils
{
    
    private ScanningUtils()
    {
    }
    
    public static long getLatestLastMod( File directory, Set includes, Set excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        
        if ( includes != null && !includes.isEmpty() )
        {
            String[] incl = (String[]) includes.toArray( new String[includes.size()] );
            scanner.setIncludes( incl );
        }
        
        if ( excludes != null && !excludes.isEmpty() )
        {
            String[] excl = (String[]) excludes.toArray( new String[excludes.size()] );
            scanner.setExcludes( excl );
        }
        
        scanner.setBasedir( directory );
        
        scanner.scan();
        
        String[] results = scanner.getIncludedFiles();
        
        long lastMod = 0;
        
        for ( int i = 0; i < results.length; i++ )
        {
            String filename = results[i];
            
            File file = new File( directory, filename );
            
            long lm = file.lastModified();
            
            if ( lm > lastMod )
            {
                lastMod = lm;
            }
        }
        
        return lastMod;
    }

}
