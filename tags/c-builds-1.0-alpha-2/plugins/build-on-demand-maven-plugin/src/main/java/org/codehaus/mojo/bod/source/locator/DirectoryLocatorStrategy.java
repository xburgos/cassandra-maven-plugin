package org.codehaus.mojo.bod.source.locator;

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

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.location.FileLocation;
import org.apache.maven.shared.io.location.Location;
import org.apache.maven.shared.io.location.LocatorStrategy;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.plexus.util.FileUtils;

public class DirectoryLocatorStrategy
    implements LocatorStrategy
{
    private static final String FILE_PROTOCOL = "file:";
    private File targetDir;
    private MavenProject project;
    private String includes;
    private String excludes;
    
    public DirectoryLocatorStrategy( MavenProject project, File targetDir, String includes, String excludes )
    {
        this.targetDir = targetDir;
        this.project = project;
        this.includes = includes;
        this.excludes = excludes;
    }

    public Location resolve( String locationSpecification, MessageHolder messageHolder )
    {
        
        File projectDirectory = getProjectDirectory( locationSpecification, messageHolder );
        
        if( projectDirectory != null )
        {
            try
            {
                List files = FileUtils.getFiles( projectDirectory, includes, excludes, false );
                copyFiles( files, projectDirectory, targetDir );
                
                return new FileLocation( targetDir, targetDir.getPath() );
            }
            catch ( IOException e )
            {
                messageHolder.addMessage( "Error retreiving project sources.", e );
            }
        }
        
        return null;
    }
    
    private File getProjectDirectory( String url, MessageHolder messageHolder )
    {
        if( url.startsWith( FILE_PROTOCOL ) )
        {
            String projectRelativePath = project.getGroupId().replace('.', '/') + '/' + project.getArtifactId();
            
            File projectDirectory = new File( url.substring( FILE_PROTOCOL.length() ), projectRelativePath );
            
            if ( projectDirectory.exists() )
            {
                return projectDirectory;
            }
            else
            {
                messageHolder.addMessage( projectDirectory + " does not exist." );
            }
        }
        else
        {
            messageHolder.addMessage( url + " is not a file URL. A file URL should be prefixed with 'file:'." );
        }
        
        return null;
    }
    
    private void copyFiles( List files, File source, File destination ) throws IOException
    {
        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            String relativePath = i.next().toString();
            
            FileUtils.copyFile( new File( source, relativePath ), new File( destination, relativePath ) );
        }
    }
}
