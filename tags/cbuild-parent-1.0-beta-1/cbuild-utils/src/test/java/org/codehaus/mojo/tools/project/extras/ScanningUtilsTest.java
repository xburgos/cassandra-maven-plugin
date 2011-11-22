package org.codehaus.mojo.tools.project.extras;

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

import org.codehaus.plexus.util.FileUtils;

import junit.framework.TestCase;

/**
 * ScanningUtilsTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class ScanningUtilsTest
    extends TestCase
{
    private final static String SCAN_DIRECTORY = "scanDir";
    
    private File scanDir;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        scanDir = new File( SCAN_DIRECTORY );
        
        if ( scanDir.exists() )
        {
            FileUtils.cleanDirectory( scanDir );
        }
        else
        {
            FileUtils.mkdir( SCAN_DIRECTORY );
        }
    }
    
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        FileUtils.forceDelete( SCAN_DIRECTORY );
    }

    public void testGetLatestLastMod_ShouldCreateTwoFilesAndReturnLastModifiedOfLatestCreatedFile()
        throws Exception
    {
        File file1 = new File( scanDir, "file1.tmp" );
        file1.createNewFile();
        
        // delay for a given amount of time to produce a difference in the 'last modified' attributes of the created files
        Thread.sleep( 1000 );
        
        File file2 = new File( scanDir, "file2.tmp" );
        file2.createNewFile();
        
        long lastModified = ScanningUtils.getLatestLastMod( scanDir, null, null );
        
        assertEquals( file2.lastModified(), lastModified );
    }
    
    public void testGetLatestLastMod_ShouldReturnZeroOnEmptyDirectory()
        throws Exception
    {
        long lastModified = ScanningUtils.getLatestLastMod( scanDir , null, null );
        
        assertTrue( lastModified == 0 );
    }

}
