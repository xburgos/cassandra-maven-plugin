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
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.LinkedList;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
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
                                       List prototype,
                                       Defaults directoryDefaults,
                                       Defaults fileDefaults )
        throws MojoFailureException, MojoExecutionException
    {
        prototype = getEntries( prototype );

        // -----------------------------------------------------------------------
        // Validate
        // -----------------------------------------------------------------------

        for ( Iterator it = prototype.iterator(); it.hasNext(); )
        {
            Object entry = it.next();

            Defaults defaults;

            if ( entry instanceof DirectoryEntry || entry instanceof DirectoryCollection )
            {
                defaults = directoryDefaults;
            }
            else
            {
                defaults = fileDefaults;
            }

            AbstractPrototypeEntry prototypeEntry = (AbstractPrototypeEntry) entry;

            prototypeEntry.validate( defaults );
        }

        AbstractEntryCollection defaultDirectoryCollection =
            new DirectoryCollection( directoryDefaults.getPkgClass(),
                                     directoryDefaults.getMode(),
                                     directoryDefaults.getUser(),
                                     directoryDefaults.getGroup(),
                                     directoryDefaults.getIncludes(),
                                     directoryDefaults.getExcludes() );

        AbstractEntryCollection defaultFileCollection =
            new FileCollection( fileDefaults.getPkgClass(),
                                fileDefaults.getMode(),
                                fileDefaults.getUser(),
                                fileDefaults.getGroup(),
                                fileDefaults.getIncludes(),
                                fileDefaults.getExcludes() );

        prototype.add( 0, defaultDirectoryCollection );
        prototype.add( 0, defaultFileCollection );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        PrototypeEntryList collectedPrototypeEntries = new PrototypeEntryList();

        // -----------------------------------------------------------------------
        // Iterate through the prototype
        // -----------------------------------------------------------------------

        for ( Iterator it = prototype.iterator(); it.hasNext(); )
        {
            Object entry = it.next();

            if ( entry instanceof SinglePrototypeEntry )
            {
                collectedPrototypeEntries.put( (SinglePrototypeEntry) entry );

                continue;
            }

            AbstractEntryCollection collection = (AbstractEntryCollection) entry;

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( packageRoot );
            scanner.setIncludes( collection.getIncludes() );
            scanner.setExcludes( collection.getExcludes() );
            scanner.scan();

            if ( collection instanceof DirectoryCollection )
            {
                String[] includedDirectories = scanner.getIncludedDirectories();
                for ( int i = 0; i < includedDirectories.length; i++ )
                {
                    String includedDirectory = includedDirectories[i];

                    if ( includedDirectory.equals( "/" ) )
                    {
                        continue;
                    }

                    collectedPrototypeEntries.put( new DirectoryEntry( collection.getPkgClass(),
                        collection.getMode(),
                        collection.getUser(),
                        collection.getGroup(),
                        "/" + includedDirectory,
                        null ) );
                }
            }

            if ( collection instanceof FileCollection )
            {
                String[] includedFiles = scanner.getIncludedFiles();
                for ( int i = 0; i < includedFiles.length; i++ )
                {
                    collectedPrototypeEntries.put( new FileEntry( collection.getPkgClass(),
                        collection.getMode(),
                        collection.getUser(),
                        collection.getGroup(),
                        "/" + includedFiles[i],
                        null ) );
                }
            }

            if ( collection instanceof EditableCollection )
            {
                String[] includedFiles = scanner.getIncludedFiles();
                for ( int i = 0; i < includedFiles.length; i++ )
                {
                    collectedPrototypeEntries.put( new EditableEntry( collection.getPkgClass(),
                        collection.getMode(),
                        collection.getUser(),
                        collection.getGroup(),
                        "/" + includedFiles[i],
                        null ) );
                }
            }
        }

        return collectedPrototypeEntries.iterator();
    }

    private List getEntries( List original )
    {
        return original == null ? new LinkedList() : original;
    }

    private class PrototypeEntryList
    {
        private TreeSet collectedPrototypeEntries;

        private PrototypeEntryList()
        {
            collectedPrototypeEntries = new TreeSet( new Comparator()
            {
                public int compare( Object o, Object o1 )
                {
                    SinglePrototypeEntry a = (SinglePrototypeEntry) o;
                    SinglePrototypeEntry b = (SinglePrototypeEntry) o1;
                    return a.getPath().compareTo( b.getPath() );
                }
            } );
        }

        public void put( SinglePrototypeEntry entry )
        {
            collectedPrototypeEntries.remove( entry );
            collectedPrototypeEntries.add( entry );
        }

        public Iterator iterator()
        {
            return collectedPrototypeEntries.iterator();
        }
    }
}
