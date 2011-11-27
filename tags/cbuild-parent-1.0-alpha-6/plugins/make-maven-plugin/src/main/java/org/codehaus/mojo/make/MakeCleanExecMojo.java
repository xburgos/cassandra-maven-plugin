package org.codehaus.mojo.make;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

/**
 * Remove from source directory all files generated during the make process.
 *
 * @goal make-clean
 * @phase clean
 */
public class MakeCleanExecMojo
        extends AbstractMakeExecMojo
{

    public static final String MAKE_CLEAN_BIN = "clean";
    public static final String MAKE_CLEAN_CONFIG = "distclean";

    /**
     * Whether we should remove the program binaries and object files from the source code
     * directory.
     *
     * @parameter default-value="true"
     */
    private boolean cleanBinaries = true;

    /**
     * Whether we should remove the files that the configuration process generated.
     *
     * @parameter default-value="false"
     */
    private boolean cleanConfig;

    /**
     * The temporary working directory where the project is actually built.
     *
     * @parameter
     */
    private File makeWorkDir;

    /**
     * The list of filesets to delete, in addition to the default directories.
     *
     * @parameter
     */
    private List filesets;

    /**
     * Be verbose in the debug log-level?
     *
     * @parameter default-value="false"
     */
    private boolean verbose;

    private FileSetManager fileSetManager;

    /**
     * Remove files generated by the Make process.
     */
    public void execute()
        throws MojoExecutionException
    {
        getLog().debug( "In MakeCleanExecMojo.execute:: workDir= " + getWorkDir() );

        setTarget( null );

        if ( makeWorkDir != null )
        {
            setWorkDir( makeWorkDir );
        }

        List options = new ArrayList();

        if( cleanBinaries )
        {
            options.add( MAKE_CLEAN_BIN );
        }

        if( cleanConfig )
        {
            options.add( MAKE_CLEAN_CONFIG );
        }

        setOptions( options );

        try
        {
            // remove Make-generated files
            super.execute();
        }
        catch ( MojoExecutionException e )
        {
            getLog().info( "Error cleaning up build generated files. " + e.getMessage() );
        }

        try
        {
            // remove left-over files
            removeAdditionalFilesets();
        }
        catch ( MojoExecutionException e )
        {
            getLog().info( e.getMessage() );
        }

    }

    private void removeAdditionalFilesets()
        throws MojoExecutionException
    {
        if ( filesets != null && !filesets.isEmpty() )
        {
            fileSetManager = new FileSetManager( getLog(), verbose );

            for ( Iterator it = filesets.iterator(); it.hasNext(); )
            {
                Fileset fileset = (Fileset) it.next();

                try
                {
                    getLog().info( "Deleting " + fileset );

                    fileSetManager.delete( fileset );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException(
                        "Failed to delete directory: " + fileset.getDirectory() + ". Reason: " + e.getMessage(), e );
                }
            }
        }
    }

    /**
     * Add a fileset to the list of filesets to clean.
     *
     * @param fileset the fileset
     */
    public void addFileset( Fileset fileset )
    {
        if ( filesets == null )
        {
            filesets = new LinkedList();
        }
        filesets.add( fileset );
    }
}