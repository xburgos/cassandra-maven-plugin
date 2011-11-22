package org.codehaus.mojo.tools.fs.archive;

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

public class ArchiveExpansionRequest
{

    /**
     * The source archive to unpack.
     */
    private File sourceArchive;
    
    /**
     * The target directory into which the project source archive should be expanded.
     */
    private File expandTarget;
    
    /**
     * Whether to force this to unpack, regardless of whether there is already a directory in place.
     */
    private boolean overwrite;
    
    /**
     * The resulting unpacked directory to check when !overwrite.
     */
    private String overwriteCheckSubpath;

    public File getExpandTarget()
    {
        return expandTarget;
    }

    public void setExpandTarget( File expandTarget )
    {
        this.expandTarget = expandTarget;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
    }

    public String getOverwriteCheckSubpath()
    {
        return overwriteCheckSubpath;
    }

    public void setOverwriteCheckSubpath( String overwriteCheckSubpath )
    {
        this.overwriteCheckSubpath = overwriteCheckSubpath;
    }

    public File getSourceArchive()
    {
        return sourceArchive;
    }

    public void setSourceArchive( File sourceArchive )
    {
        this.sourceArchive = sourceArchive;
    }
    
}
