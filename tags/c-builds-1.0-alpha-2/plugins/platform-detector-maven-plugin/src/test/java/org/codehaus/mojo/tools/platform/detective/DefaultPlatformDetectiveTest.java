package org.codehaus.mojo.tools.platform.detective;

import java.io.File;

import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.plexus.PlexusTestCase;

public class DefaultPlatformDetectiveTest
    extends PlexusTestCase
{

    private static final File FEDORA_RELEASE_FILE = new File( "/etc/fedora-release" );

    private PlatformDetective platformDetective;

    public void setUp()
        throws Exception
    {
        super.setUp();

        platformDetective = (PlatformDetective) lookup( PlatformDetective.ROLE, DefaultPlatformDetective.ROLE_HINT );
    }

    public void testShouldDetectFCPrefixUsingDefaultPatternsWhenOnFedora()
        throws PlatformDetectionException
    {
        if ( FEDORA_RELEASE_FILE.exists() )
        {
            String result = platformDetective.getOperatingSystemToken();

            System.out.println( "result= " + result );
            assertTrue( "Incorrect OS token detected; should begin with 'fc'.", result.startsWith( "fc" ) );
        }
        else
        {
            System.out.println( "Skipping test for Fedora - /etc/fedora-release file is not present." );
        }
    }

}
