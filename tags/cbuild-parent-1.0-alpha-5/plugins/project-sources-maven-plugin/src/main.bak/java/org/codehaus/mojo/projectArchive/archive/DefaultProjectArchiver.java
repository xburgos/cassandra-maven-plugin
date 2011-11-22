package org.codehaus.mojo.projectArchive.archive;

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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.mojo.projectArchive.files.Fileset;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;


/**
 * @plexus.component role="org.codehaus.mojo.projectArchive.archive.ProjectArchiver"
 *                   role-hint="default"
 *                   
 * @author jdcasey
 *
 */
public class DefaultProjectArchiver
    implements ProjectArchiver
{

    /**
     * @plexus.requirement
     */
    private ArchiverManager archiverManager;

    public DefaultProjectArchiver()
    {
        // used for component instantiation only.
    }

    public DefaultProjectArchiver( ArchiverManager archiverManager )
    {
        this.archiverManager = archiverManager;
    }

    public void create( MavenProject project, File outputFile, String rootPath, List filesets, Log log )
        throws NoSuchArchiverException, ArchiverException, IOException
    {
        Archiver archiver = archiverManager.getArchiver( outputFile );

        archiver.setDestFile( outputFile );
        archiver.setIncludeEmptyDirs( true );

        FileSetManager fsManager = new FileSetManager( log );
        
        String root = rootPath;
        
        if ( root == null )
        {
            root = "";
        }
        else
        {
            root = root.replace( '\\', '/' );
        }
        
        if ( !root.endsWith( "/" ) )
        {
            root += "/";
        }

        for ( Iterator it = filesets.iterator(); it.hasNext(); )
        {
            Fileset fs = (Fileset) it.next();

            fs = fs.getInterpolatedCopy( project );

            File basedir = new File( fs.getDirectory() );

            String[] includedFiles = fsManager.getIncludedFiles( fs );

            String outputDirectory = fs.getOutputDirectory();

            if ( outputDirectory != null )
            {
                outputDirectory = outputDirectory.replace( '\\', '/' );
                
                if ( outputDirectory.startsWith( "/" ) )
                {
                    outputDirectory = outputDirectory.substring( 1 );
                }

                if ( !outputDirectory.endsWith( "/" ) )
                {
                    outputDirectory += "/";
                }
            }

            for ( int i = 0; i < includedFiles.length; i++ )
            {
                String relativePath = includedFiles[i];

                File file = new File( basedir, relativePath );

                if ( outputDirectory != null )
                {
                    archiver.addFile( file, root + outputDirectory + relativePath );
                }
                else
                {
                    archiver.addFile( file, root + relativePath );
                }
            }
        }

        archiver.createArchive();
    }
}
