package org.codehaus.mojo.tools.platform.detective;

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

import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class PlatformPropertyPatternsTest extends PlexusTestCase {
    private static MavenSession session;
    private MavenProject project;
    private static int testcnt = 0;

    @Before public void setUp() throws Exception {
        testcnt += 1;
        super.setUp();
        session = new MavenSession( this.getContainer(), null, null, null
            , null, null, null, null, null
        );

        // Create a different project each time because maven caches old projects
        Model model = new Model();
        model.setGroupId( "groupIdPlatform" );
        model.setArtifactId( "artifactId." + testcnt );
        model.setVersion( "1.0" );
        project = new MavenProject( model );
        // System.out.println(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        session.setCurrentProject(project);
    }

    @Test public void testSanity()
    {
	try { 
	    System.out.println( "testSanity()" ); 
	    //Pattern pattern = Pattern.compile( "Fedora Core release ([-.0-9]+)" );
	    Pattern pattern = Pattern.compile( "Fedora Core release ([0-9]+) .*" );
	    Matcher matcher = pattern.matcher( "Fedora Core release 4 (Stentz)" );
	    
	    System.out.println( "assert1" ); 
	    
	    boolean match = matcher.matches();
	    System.out.println( "match1= " + match );
	    
	    //assertFalse( matcher.matches() );
	    assertTrue( matcher.matches() );

	    System.out.println( "assert2" ); 
	    match = matcher.find( 0 );
	    System.out.println( "match2= " + match );
	    assertTrue( matcher.find( 0 ) );
	    
	    System.out.println( "Calling replaceReferences" ); 
            PlatformPropertyPatterns patterns = (PlatformPropertyPatterns)
                lookup( PlatformPropertyPatterns.ROLE, PlatformPropertyPatterns.ROLE_HINT );
	    String ext = patterns.replaceReferences( "fc$1", matcher );
	    System.out.println( "*************** ext= " + ext ); 
	}
	catch ( Exception ee ) {
	    ee.printStackTrace();
	    fail();
	}
    }
    
    @Test public void testDefaults_ShouldMatchFedoraOSPattern()
    {
	System.out.println( "testDefaults_ShouldMatchFedoraOSPattern()" ); 
        assertDefault( "fedora-release", "Fedora Core release 4 (Stentz)", "fc4" );
    }

    @Test public void testDefaults_ShouldMatchRHEnterpriseOSPattern()
    {
        assertDefault( "redhat-release", "Red Hat Enterprise Linux AS release 3 (Taroon Update 7)", "rhel3" );
    }

    @Test public void testDefaults_ShouldMatchGentooOSPattern()
    {
        assertDefault( "gentoo-release", "version 3", "gentoo3" );
    }
    
    @Test public void testDefaults_ShouldMatchUbuntuOSPattern()
    {
        String fileContents = "DISTRIB_ID=Ubuntu" +
                "\nDISTRIB_RELEASE=6.06" +
                "\nDISTRIB_CODENAME=dapper" +
                "\nDISTRIB_DESCRIPTION=\"Ubuntu 6.06.1 LTS\"";
        
        assertDefault( "lsb-release", fileContents, "lsb6.06" );
        
    }

    private void assertDefault( String source, String value, String expectedResult )
    {
        PlatformPropertyPatterns patterns = PlatformPropertyPatterns.DEFAULT_PATTERNS;
        
        String result = patterns.getOperatingSystemToken( source, value );
        
        assertEquals( expectedResult, result );
    }
    
    @Test public void testContextPutAndGet() throws Exception, ComponentLookupException
    {
        PlatformPropertyPatterns patterns = (PlatformPropertyPatterns) lookup( PlatformPropertyPatterns.ROLE,
            PlatformPropertyPatterns.ROLE_HINT );
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( session );
        
        PlatformPropertyPatterns retrieved = patterns.readFromContext( session );
        
        assertEquals( "token", retrieved.getOperatingSystemToken( "source", "expression" ) );
    }
/* Pulled the deleteFromContext method....
    @Test public void testContextShouldPutRemoveThenFailToGet()
    {
        PlatformPropertyPatterns patterns = new PlatformPropertyPatterns();
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( session );
        PlatformPropertyPatterns.deleteFromContext( session );
        
        PlatformPropertyPatterns retrieved = PlatformPropertyPatterns.readFromContext( session );
        
        assertNull( retrieved );
    }

    @Test public void testContextShouldPutRemovePutThenGetSecondSetup()
    {
        PlatformPropertyPatterns patterns = new PlatformPropertyPatterns();
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( session );
        PlatformPropertyPatterns.deleteFromContext( session );
        
        PlatformPropertyPatterns patterns2 = new PlatformPropertyPatterns();
        patterns2.addOsPattern( "source", "expression", "token2" );
        
        patterns2.saveToContext( session );
        
        PlatformPropertyPatterns retrieved = PlatformPropertyPatterns.readFromContext( session );
        
        assertEquals( "token2", retrieved.getOperatingSystemToken( "source", "expression" ) );
    }
*/
}
