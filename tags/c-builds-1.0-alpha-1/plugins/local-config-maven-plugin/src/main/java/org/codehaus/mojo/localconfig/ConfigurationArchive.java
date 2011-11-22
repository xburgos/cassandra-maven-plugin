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

import org.apache.maven.shared.model.fileset.FileSet;

public class ConfigurationArchive
    extends FileSet
{
    
    private static final long serialVersionUID = 1L;
    
    private File archiveSource;

    public void setSource( File archiveSource )
    {
        this.archiveSource = archiveSource;
    }
    
    public File getSource()
    {
        return archiveSource;
    }

    public void setExpandedDirectory( String archiveWorkDir )
    {
        String subdir = getDirectory();
        
        if ( subdir != null )
        {
            setDirectory( new File( archiveWorkDir, subdir ).getPath() );
        }
        else
        {
            setDirectory( archiveWorkDir );
        }
    }

    public String toString()
    {
        return "ConfigurationArchive@" + archiveSource.getPath();
    }
}
