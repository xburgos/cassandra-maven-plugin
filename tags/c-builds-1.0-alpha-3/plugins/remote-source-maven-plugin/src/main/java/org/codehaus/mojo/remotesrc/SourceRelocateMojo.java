package org.codehaus.mojo.remotesrc;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Move a set of unpacked source files from the temporary unpack directory (usually in /target somewhere)
 * to the project ${basedir}
 * 
 * @goal relocate
 */
public class SourceRelocateMojo
    extends AbstractRemoteSourceMojo
{
    
    /**
     * @parameter default-value="false"
     */
    private boolean skipRelocate;

    /**
     * The directory structure beneath the expansion target (the directory into which the source archive was
     * unpacked) where the true sources exist...this directory structure will NOT be preserved during relocation.
     * 
     * @parameter
     */
    private String relativeSourceBase;
    
    /**
     * The location where the source archive was unpacked.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File expandTarget;
    
    /**
     * The project's base directory, into which the project sources will be moved.
     * 
     * @parameter default-value="${basedir}"
     * @required
     */
    private File relocateTarget;

    /**
     * Construct the File instance to the relative path within the unpack directory, and move everything below
     * this relative path into ${basedir}.
     */
    public void doExecute()
        throws MojoExecutionException
    {
        File expandedSourceDir = expandTarget;
        
        if ( relativeSourceBase != null )
        {
            expandedSourceDir = new File( expandTarget, relativeSourceBase );
        }
        
        if ( !expandedSourceDir.exists() )
        {
            throw new MojoExecutionException( "Cannot find relative source directory: " + relativeSourceBase + " within unpacked source." );
        }
        
        try
        {
            FileUtils.copyDirectoryStructure( expandedSourceDir, relocateTarget );
            FileUtils.deleteDirectory( expandedSourceDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy relative source directory: " + relativeSourceBase + " to basedir", e );
        }
    }

    protected CharSequence getSkipMessage()
    {
        return "Skipping relocate step (per configuration).";
    }

    protected boolean isSkip()
    {
        return skipRelocate;
    }

}
