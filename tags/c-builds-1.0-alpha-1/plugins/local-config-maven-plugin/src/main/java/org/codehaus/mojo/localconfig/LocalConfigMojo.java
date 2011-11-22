package org.codehaus.mojo.localconfig;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpander;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpansionException;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpansionRequest;
import org.codehaus.plexus.util.FileUtils;


/**
 * Modify the user's local environment, given a set of archives containing the new copies of the
 * files to overwrite.
 * 
 * @goal configure
 * 
 * @requiresProject true
 * 
 * @author jdcasey
 *
 */
public class LocalConfigMojo
    extends AbstractMojo
{

    /**
     * The list of archive expansions to perform in this local configuration. List items are of
     * type: org.apache.maven.plugins.localconfig.ArchiveExpansion.
     * 
     * @parameter
     * @required
     */
    private List configurationArchives;

    /**
     * This is a temporary location to which the mojo will expand all archives, to facilitate
     * scanning and filtering of files contained within. Scanning, so it can process includes and
     * excludes; filtering, so it can prompt the user for any input required to customize the files
     * to the local environment.
     * 
     * @parameter default-value="${project.build.directory}/local-config-workdir/config-archives"
     * @required
     */
    private File tempExpansionDir;
    
    /**
     * @component
     */
    private ArchiveExpander archiveExpander;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( Iterator it = configurationArchives.iterator(); it.hasNext(); )
        {
            ConfigurationArchive archive = (ConfigurationArchive) it.next();

            try
            {
                expandConfigurationArchive( archive );
            }
            catch ( ArchiveExpansionException e )
            {
                throw new MojoExecutionException( "Failed to expand configuration archive: " + archive, e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to process configuration archive: " + archive, e );
            }

            try
            {
                copyIntoLocalEnvironment( archive );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to process configuration archive: " + archive, e );
            }
        }
    }

    private void copyIntoLocalEnvironment( ConfigurationArchive archive )
        throws IOException
    {
        FileSetManager mgr = new FileSetManager( getLog() );

        String[] includedFiles = mgr.getIncludedFiles( archive );

        File archiveDir = new File( archive.getDirectory() ).getCanonicalFile();
        File outputDir = new File( archive.getOutputDirectory() ).getCanonicalFile();

        for ( int i = 0; i < includedFiles.length; i++ )
        {
            String path = includedFiles[i];

            File from = new File( archiveDir, path );
            File to = new File( outputDir, path );

            FileUtils.copyFile( from, to );
        }
    }

    private void expandConfigurationArchive( ConfigurationArchive archive )
        throws ArchiveExpansionException, IOException
    {
        tempExpansionDir.mkdirs();

        File archiveSource = archive.getSource();

        String tempDirName = archiveSource.getPath().replace( '/', '_' ).replace( '\\', '_' ).replaceAll( ":", "" );

        File archiveWorkDir = new File( tempExpansionDir, tempDirName );

        ArchiveExpansionRequest request = new ArchiveExpansionRequest();
        request.setSourceArchive( archiveSource );
        request.setExpandTarget( archiveWorkDir );
        request.setOverwrite( true );
        
        archiveExpander.expand( request );

        archive.setExpandedDirectory( archiveWorkDir.getCanonicalPath() );
    }

}
