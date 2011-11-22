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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.PlexusTestCase;

public class PlatformPropertyPatternsTest extends PlexusTestCase
{
    
    public void testSanity()
    {
        Pattern pattern = Pattern.compile( "Fedora Core release ([-.0-9]+)" );
        Matcher matcher = pattern.matcher( "Fedora Core release 4 (Stentz)" );
        
        assertFalse( matcher.matches() );
        assertTrue( matcher.find( 0 ) );
    }
    
    public void testDefaults_ShouldMatchFedoraOSPattern()
    {
        assertDefault( "fedora-release", "Fedora Core release 4 (Stentz)", "fc4" );
    }

    public void testDefaults_ShouldMatchRHEnterpriseOSPattern()
    {
        assertDefault( "redhat-release", "Red Hat Enterprise Linux AS release 3 (Taroon Update 7)", "rhel3" );
    }

    public void testDefaults_ShouldMatchGentooOSPattern()
    {
        assertDefault( "gentoo-release", "version 3", "gentoo3" );
    }
    
    public void testDefaults_ShouldMatchUbuntuOSPattern()
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
    
    public void testContextPutAndGet()
    {
        PlatformPropertyPatterns patterns = new PlatformPropertyPatterns();
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( getContainer().getContext() );
        
        PlatformPropertyPatterns retrieved = PlatformPropertyPatterns.readFromContext( getContainer().getContext() );
        
        assertEquals( "token", retrieved.getOperatingSystemToken( "source", "expression" ) );
    }

    public void testContextShouldPutRemoveThenFailToGet()
    {
        PlatformPropertyPatterns patterns = new PlatformPropertyPatterns();
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( getContainer().getContext() );
        PlatformPropertyPatterns.deleteFromContext( getContainer().getContext() );
        
        PlatformPropertyPatterns retrieved = PlatformPropertyPatterns.readFromContext( getContainer().getContext() );
        
        assertNull( retrieved );
    }

    public void testContextShouldPutRemovePutThenGetSecondSetup()
    {
        PlatformPropertyPatterns patterns = new PlatformPropertyPatterns();
        patterns.addOsPattern( "source", "expression", "token" );
        
        patterns.saveToContext( getContainer().getContext() );
        PlatformPropertyPatterns.deleteFromContext( getContainer().getContext() );
        
        PlatformPropertyPatterns patterns2 = new PlatformPropertyPatterns();
        patterns2.addOsPattern( "source", "expression", "token2" );
        
        patterns2.saveToContext( getContainer().getContext() );
        
        PlatformPropertyPatterns retrieved = PlatformPropertyPatterns.readFromContext( getContainer().getContext() );
        
        assertEquals( "token2", retrieved.getOperatingSystemToken( "source", "expression" ) );
    }

}
