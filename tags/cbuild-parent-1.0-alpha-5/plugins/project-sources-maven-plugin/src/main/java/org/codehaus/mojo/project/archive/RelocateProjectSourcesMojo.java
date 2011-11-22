package org.codehaus.mojo.project.archive;

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
 * Move a set of files (usually unpacked project sources) from one place to another, optionally
 * preserving the original location (copy-only).
 * 
 * @goal relocate
 */
public class RelocateProjectSourcesMojo
    extends AbstractProjectSourcesMojo
{
    
    /**
     * The originating location of the sources to be moved.
     * 
     * @parameter
     * @required
     */
    private File from;
    
    /**
     * Flag determining whether this mojo will attempt to execute when the current project's packaging
     * is 'pom'. Default behavior is to skip 'pom' projects.
     * 
     * @parameter default-value="true"
     */
    private boolean skipPomProjects;
    
    /**
     * The target location where project sources should be moved to.
     * 
     * @parameter
     * @required
     */
    private File to;
    
    /**
     * If true, copy the source directory structure, don't move it. This means the original location
     * will not be deleted when the operation completes. Default value is false.
     * 
     * @parameter default-value="false"
     */
    private boolean copyOnly;
    
    /**
     * Construct the File instance to the relative path within the unpack directory, and move everything below
     * this relative path into ${basedir}.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipPomProjects && "pom".equals( getProject().getPackaging() ) )
        {
            getLog().info( "Relocate mojo: skipping 'pom' project." );
            return;
        }
        
        if ( !from.exists() )
        {
            throw new MojoExecutionException( "Cannot find original source directory ('from' directory): " + from + "." );
        }
        
        to.mkdirs();
        
        try
        {
            FileUtils.copyDirectoryStructure( from, to );
            
            if ( !copyOnly )
            {
                FileUtils.deleteDirectory( from );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to relocate project sources from: " + from + " to: " + to, e );
        }
    }

}
