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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Generates a System V package <code>prototype</code> file.
 *
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal generate-prototype
 */
public class GeneratePrototypeMojo
    extends AbstractSolarisMojo
{
    public static final String[] DEFAULT_INCLUDES = new String[]{"*/**"};

    public static final String[] DEFAULT_EXCLUDES;

    public static final Defaults DEFAULT_DIRECTORY_ENTRY;

    public static final Defaults DEFAULT_FILE_ENTRY;

    static
    {
        DEFAULT_EXCLUDES = new String[2 + DirectoryScanner.DEFAULTEXCLUDES.length];
        DEFAULT_EXCLUDES[0] = "*prototype*";
        DEFAULT_EXCLUDES[1] = "*pkginfo*";
        System.arraycopy( DirectoryScanner.DEFAULTEXCLUDES, 0, DEFAULT_EXCLUDES, 2,
                          DirectoryScanner.DEFAULTEXCLUDES.length );

        // These has to be here as the static initialization block is run *after* the static field assignments.
        DEFAULT_DIRECTORY_ENTRY = new Defaults( "none",
                                                "0755",
                                                "root",
                                                "sys",
                                                DEFAULT_INCLUDES,
                                                DEFAULT_EXCLUDES );

        DEFAULT_FILE_ENTRY = new Defaults( "none",
                                           "0644",
                                           "root",
                                           "sys",
                                           DEFAULT_INCLUDES,
                                           DEFAULT_EXCLUDES );
    }

    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * There directory where <code>pkgmk</code> and <code>pkgtrans</code> will be executed. All files that are to be
     * a part of the package has to be in this directory before the prototype file is generated.
     *
     * @parameter expression="${project.build.directory}/solaris/assembled-pkg"
     */
    private File packageRoot;

    /**
     * A collection of prototype entries and entry collections. If two entries match the same path, the latter will
     * override the first.
     *
     * @parameter
     * @see AbstractPrototypeEntry
     * @see DirectoryEntry
     * @see EditableEntry
     * @see FileEntry
     * @see IEntry
     * @see AbstractEntryCollection
     */
    private List generatedPrototype;

    /**
     * The default class for the objects.
     *
     * @parameter default-value="none"
     * @required
     */
    private String defaultClass;

    /**
     * The default modes of directories.
     *
     * @parameter default-value="0755"
     * @required
     */
    private String defaultDirectoryMode;

    /**
     * The default mode for files.
     *
     * @parameter default-value="0664"
     * @required
     */
    private String defaultFileMode;

    /**
     * The default owning user for all objects.
     *
     * @parameter default-value="root"
     * @required
     */
    private String defaultUser;

    /**
     * The default owning group for all objects.

     * @parameter default-value="sys"
     * @required
     */
    private String defaultGroup;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component
     */
    private PrototypeGenerator prototypeGenerator;

    // -----------------------------------------------------------------------
    // Mojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File generatedPrototypeFile = new File( packageRoot, "generated-prototype" );

        if ( !packageRoot.isDirectory() )
        {
            getLog().debug( "package root is not a directory: " + packageRoot.getAbsolutePath() );

            try
            {
                FileUtils.fileWrite( generatedPrototypeFile.getAbsolutePath(), "" );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error while writing empty file: " +
                    "'" + generatedPrototypeFile.getAbsolutePath() + "'." );
            }

            return;
        }

        mkParentDirs( generatedPrototypeFile );

        Defaults directoryDefaults = new Defaults( defaultClass,
                                                   defaultDirectoryMode,
                                                   defaultUser,
                                                   defaultGroup,
                                                   DEFAULT_INCLUDES,
                                                   DEFAULT_EXCLUDES );

        Defaults fileDefaults = new Defaults( defaultClass,
                                              defaultFileMode,
                                              defaultUser,
                                              defaultGroup,
                                              DEFAULT_INCLUDES,
                                              DEFAULT_EXCLUDES );

        Iterator prototypeFile = prototypeGenerator.generatePrototype( packageRoot, generatedPrototype,
            directoryDefaults, fileDefaults );

        writePrototype( generatedPrototypeFile, prototypeFile );
    }

    public static void writePrototype( File generatedPrototype, Iterator prototypeFile )
        throws MojoFailureException, MojoExecutionException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( generatedPrototype );
            PrintWriter printer = new PrintWriter( writer );

            while ( prototypeFile.hasNext() )
            {
                printer.println( ( (SinglePrototypeEntry) prototypeFile.next() ).getPrototypeLine() );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while writing generatedPrototype file: " +
                generatedPrototype.getAbsolutePath() + ".", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }
}
