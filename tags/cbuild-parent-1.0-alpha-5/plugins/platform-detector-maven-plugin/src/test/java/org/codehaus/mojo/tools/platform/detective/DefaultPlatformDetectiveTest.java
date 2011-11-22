package org.codehaus.mojo.tools.platform.detective;

import java.io.File;
import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.plexus.PlexusTestCase;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class DefaultPlatformDetectiveTest
    extends PlexusTestCase
{
    private static final File FEDORA_RELEASE_FILE = new File( "/etc/fedora-release" );

    private PlatformDetective platformDetective;
    private MavenSession session;

    @Before public void setUp()
        throws Exception
    {
        super.setUp();
        session = new MavenSession( this.getContainer(), null, null, null
            , null, null, null, null, null
        );
        platformDetective = (PlatformDetective) lookup( PlatformDetective.ROLE, DefaultPlatformDetective.ROLE_HINT );
        Model model = new Model();
        model.setGroupId( "groupIdPlatform" );
        model.setArtifactId( "artifactId" );
        model.setVersion( "1.0" );
        MavenProject project = new MavenProject( model );
        // System.out.println(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        session.setCurrentProject(project);
    }

    @Test public void testShouldDetectFCPrefixUsingDefaultPatternsWhenOnFedora()
        throws PlatformDetectionException, ComponentLookupException
    {
        if ( FEDORA_RELEASE_FILE.exists() )
        {
            String result = platformDetective.getOperatingSystemToken( session );

            System.out.println( "result= " + result );
            assertTrue( "Incorrect OS token detected; should begin with 'fc'.", result.startsWith( "fc" ) );
        }
        else
        {
            System.out.println( "Skipping test for Fedora - /etc/fedora-release file is not present." );
        }
    }


    @Test public void testShouldDetectOSXPrefixUsingDefaultPatternsWhenOnMAC()
        throws PlatformDetectionException, ComponentLookupException
    {
        if ( "Mac OS X".equals( System.getProperty( "os.name" ) ) )
        {
            String result = platformDetective.getOperatingSystemToken( session );

            System.out.println( "result= " + result );
            assertTrue( "Incorrect OS token detected; should begin with 'osx'.", result.startsWith( "osx" ) );
        }
        else
        {
            System.out.println( "Skipping test for Mac OS X." );
        }
    }
}
