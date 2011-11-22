package org.codehaus.mojo.tools.fs.archive.manager;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import org.codehaus.mojo.tools.fs.archive.tar.TarUnArchiver;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

public class LocalOverrideArchiverManagerTest
    extends PlexusTestCase
{
    
    private ArchiverManager archiverManager;
    
    @Before public void setUp() throws Exception
    {
        super.setUp();

        archiverManager = (ArchiverManager) lookup( ArchiverManager.ROLE );
    }

    @Test public void testShouldRetrieveLocalOverrideOfTarUnArchiverForTarGzFile() throws NoSuchArchiverException
    {
        LocalOverrideArchiverManager localMgr = new LocalOverrideArchiverManager( archiverManager, getContainer() );
        
        File f = new File( "test.tar.gz" );
        UnArchiver unArchiver = localMgr.getUnArchiver( f );
        
        assertTrue( unArchiver instanceof TarUnArchiver );
    }

}
