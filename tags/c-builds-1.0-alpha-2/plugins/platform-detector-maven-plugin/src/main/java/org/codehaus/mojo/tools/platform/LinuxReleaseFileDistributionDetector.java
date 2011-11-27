package org.codehaus.mojo.tools.platform;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

/**
 * @plexus.component role="org.codehaus.mojo.tools.platform.SystemDistributionDetector" role-hint="linux-release-file"
 * @author jdcasey
 *
 */
public class LinuxReleaseFileDistributionDetector implements SystemDistributionDetector, LogEnabled
{
    
    public static final String ROLE_HINT = "linux-release-file";
    
    public static final File FEDORA_CORE_RELEASE_FILE = new File( "/etc/fedora-release" );

    public static final File REDHAT_RELEASE_FILE = new File( "/etc/redhat-release" );

    public static final File GENTOO_RELEASE_FILE = new File( "/etc/gentoo-release" );

    public static final File UBUNTU_RELEASE_FILE = new File( "/etc/lsb-release" );

    public static final File DEBIAN_RELEASE_FILE = new File( "/etc/debian_version" );

    public static final File CENTOS_RELEASE_FILE = new File( "/etc/redhat-release" );

    private static final List LINUX_RELEASE_FILES = new ArrayList()
    {
        {
            add( FEDORA_CORE_RELEASE_FILE );
            add( REDHAT_RELEASE_FILE );
            add( GENTOO_RELEASE_FILE );
            add( UBUNTU_RELEASE_FILE );
            add( DEBIAN_RELEASE_FILE );
            add( CENTOS_RELEASE_FILE );
        }
    };
    
    // cached.
    private File releaseFile;

    private Logger logger;

    public String getDistributionInfo() throws PlatformDetectionException
    {
        try
        {
            String content = FileUtils.fileRead( releaseFile );
            
            getLogger().debug( "Contents of Linux release-file: " + releaseFile.getAbsolutePath() + ":\n\'" + content + "\'" );
            
            return content;
        }
        catch ( IOException e )
        {
            throw new PlatformDetectionException( "Failed to read contents of platform-release file." );
        }
    }

    public String getDistributionInfoSource() throws PlatformDetectionException
    {
        File releaseFile = getReleaseFile();
        
        if ( releaseFile == null )
        {
            throw new PlatformDetectionException( "Cannot detect any Linux release files." );
        }
        
        String source = releaseFile.getName();
        
        getLogger().debug( "Got distribution information source: \'" + source + "\'." );
        
        return source;
    }

    public boolean isEnabled()
    {
        if ( "Linux".equals( System.getProperty( "os.name" ) ) )
        {
            return getReleaseFile() != null;
        }
        
        return false;
    }

    private File getReleaseFile()
    {
        if ( releaseFile == null )
        {
            getLogger().debug( "Checking for Linux release files:" );
            
            for ( Iterator it = LINUX_RELEASE_FILES.iterator(); it.hasNext(); )
            {
                File releaseFile = (File) it.next();
                
                getLogger().debug( "Checking " + releaseFile.getAbsolutePath() + "..." );
                
                if ( releaseFile.exists() )
                {
                    getLogger().debug( "found it." );
                    this.releaseFile = releaseFile;
                    break;
                }
                else
                {
                    getLogger().debug( "not found." );
                }
            }
        }
        
        return releaseFile;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
    
    protected Logger getLogger()
    {
        return logger;
    }

}