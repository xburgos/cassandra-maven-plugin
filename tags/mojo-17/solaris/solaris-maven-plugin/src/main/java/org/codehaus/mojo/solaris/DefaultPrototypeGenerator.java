package org.codehaus.mojo.solaris;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultPrototypeGenerator
    extends AbstractLogEnabled
    implements PrototypeGenerator
{
    // -----------------------------------------------------------------------
    // PrototypeGenerator Implementation
    // -----------------------------------------------------------------------

    public Iterator generatePrototype( File packageRoot,
                                       List prototypeEntries,
                                       PrototypeEntryCollection defaultDirectoryEntry,
                                       PrototypeEntryCollection defaultFileEntry )
        throws MojoFailureException
    {
        prototypeEntries = getEntries( prototypeEntries );

        // -----------------------------------------------------------------------
        // Validate
        // -----------------------------------------------------------------------

        for ( Iterator it = prototypeEntries.iterator(); it.hasNext(); )
        {
            Object entry = it.next();

            if(entry instanceof PrototypeEntryCollection)
            {
                PrototypeEntryCollection collection = (PrototypeEntryCollection) entry;

                collection.validate( defaultDirectoryEntry, defaultFileEntry );
            }
        }

        PrototypeEntryCollection defaultDirectoryCollection =
            new PrototypeEntryCollection( defaultDirectoryEntry.getIncludes(),
                                          defaultDirectoryEntry.getExcludes(),
                                          PrototypeEntryCollection.TYPE_DIR,
                                          defaultDirectoryEntry.getPkgClass(),
                                          defaultDirectoryEntry.getMode(),
                                          defaultDirectoryEntry.getUser(),
                                          defaultDirectoryEntry.getGroup() );

        PrototypeEntryCollection defaultFileCollection = 
            new PrototypeEntryCollection( defaultFileEntry.getIncludes(),
                                          defaultFileEntry.getExcludes(),
                                          PrototypeEntryCollection.TYPE_FILE,
                                          defaultFileEntry.getPkgClass(),
                                          defaultFileEntry.getMode(),
                                          defaultFileEntry.getUser(),
                                          defaultFileEntry.getGroup() );

        prototypeEntries.add( 0, defaultDirectoryCollection );
        prototypeEntries.add( 0, defaultFileCollection );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        SortedSet collectedPrototypeEntries = new TreeSet( new Comparator()
        {
            public int compare( Object o, Object o1 )
            {
                return ( (AbstractPrototypeEntry) o ).getPath().compareTo( ( (AbstractPrototypeEntry) o1 ).getPath() );
            }
        } );

        // -----------------------------------------------------------------------
        // Iterate through the entry collection
        // -----------------------------------------------------------------------

        for ( Iterator it = prototypeEntries.iterator(); it.hasNext(); )
        {
            Object entry = it.next();

            if( entry instanceof AbstractPrototypeEntry ) {
                collectedPrototypeEntries.remove( entry );
                collectedPrototypeEntries.add( entry );

                continue;
            }

            PrototypeEntryCollection collection = (PrototypeEntryCollection) entry;

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( packageRoot );
            scanner.setIncludes( collection.getIncludes() );
            scanner.setExcludes( collection.getExcludes() );
            scanner.scan();

            String type = collection.getType();

            if ( type == null || PrototypeEntryCollection.TYPE_DIR.equals( type ) )
            {
                String[] includedDirectories = scanner.getIncludedDirectories();
                for ( int i = 0; i < includedDirectories.length; i++ )
                {
                    String includedDirectory = "/" + includedDirectories[i];

                    if ( includedDirectory.equals( "/" ) )
                    {
                        continue;
                    }

                    AbstractPrototypeEntry prototypeEntry = new DirectoryPrototypeEntry( includedDirectory, collection );
                    collectedPrototypeEntries.remove( prototypeEntry );
                    collectedPrototypeEntries.add( prototypeEntry );
                }
            }

            if ( type == null || PrototypeEntryCollection.TYPE_FILE.equals( type ) )
            {
                String[] includedFiles = scanner.getIncludedFiles();
                for ( int i = 0; i < includedFiles.length; i++ )
                {
                    String includedFile = "/" + includedFiles[i];
                    AbstractPrototypeEntry prototypeEntry = new FilePrototypeEntry( includedFile, collection );
                    collectedPrototypeEntries.remove( prototypeEntry );
                    collectedPrototypeEntries.add( prototypeEntry );
                }
            }

            if ( type == null || PrototypeEntryCollection.TYPE_EDITABLE.equals( type ) )
            {
                String[] includedFiles = scanner.getIncludedFiles();
                for ( int i = 0; i < includedFiles.length; i++ )
                {
                    String includedFile = "/" + includedFiles[i];
                    AbstractPrototypeEntry prototypeEntry = new EditablePrototypeEntry( includedFile, collection );
                    collectedPrototypeEntries.remove( prototypeEntry );
                    collectedPrototypeEntries.add( prototypeEntry );
                }
            }
        }

        return collectedPrototypeEntries.iterator();
    }

    private List getEntries( List original )
    {
        return original == null ? new LinkedList() : original;
    }
}
