package org.codehaus.mojo.tools.platform.detective;

import java.io.File;

import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.plexus.PlexusTestCase;

public class DefaultPlatformDetectiveTest extends PlexusTestCase
{
    
    private static final File RH_RELEASE_FILE = new File( "/etc/redhat-release" );
    
    private PlatformDetective platformDetective;
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        platformDetective = (PlatformDetective) lookup( PlatformDetective.ROLE, DefaultPlatformDetective.ROLE_HINT );
    }
    
    public void testShouldDetectRHEL3UsingDefaultPatternsWhenOnRHEL3() throws PlatformDetectionException
    {
        if ( RH_RELEASE_FILE.exists() )
        {
            String result = platformDetective.getOperatingSystemToken();
            
            assertEquals( "rhel3", result );
        }
        else
        {
            System.out.println( "Skipping tests for Red Hat - /etc/redhat-release file is not present." );
        }
    }

}
