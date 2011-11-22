package org.codehaus.mojo.tools.fs;

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

import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;

import junit.framework.TestCase;

public class ArchiveFileExtensionsTest
    extends TestCase
{
    
    public void testZipExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "zip" ) );
    }

    public void testTarGzExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "tar.gz" ) );
    }

    public void testTgzExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "tgz" ) );
    }

    public void testTarZExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "tar.Z" ) );
    }

    public void testTarBz2ExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "tar.bz2" ) );
    }

    public void testTarExtensionShouldBeRecognized()
    {
        assertTrue( ArchiveFileExtensions.isArchiveExtensionRecognized( "tar" ) );
    }

    public void testShouldFindZipExtensionInFilename()
    {
        String filename = "my-archive.something.zip";
        
        assertEquals( "zip", ArchiveFileExtensions.getArchiveFileExtension( filename ) );
    }

    public void testShouldFindTarGzExtensionInFilename()
    {
        String filename = "my-archive.something.tar.gz";
        
        assertEquals( "tar.gz", ArchiveFileExtensions.getArchiveFileExtension( filename ) );
    }

}
