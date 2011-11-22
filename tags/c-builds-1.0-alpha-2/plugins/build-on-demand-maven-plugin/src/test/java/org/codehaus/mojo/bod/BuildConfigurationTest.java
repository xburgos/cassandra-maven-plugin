package org.codehaus.mojo.bod;

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
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.codehaus.mojo.bod.BuildConfiguration;

import junit.framework.TestCase;

public class BuildConfigurationTest
    extends TestCase
{

    public void testShouldCopyAllFields()
    {
        BuildConfiguration in = createConfigurationWithAllFieldsFilled();
        copy( in );
    }
    
    public void testShouldCopyWithNullLists()
    {
        BuildConfiguration in = createConfigurationWithAllFieldsFilled();
        
        in.setGoals( null );
        
        copy( in );
    }
    
    private BuildConfiguration createConfigurationWithAllFieldsFilled()
    {
        BuildConfiguration in = new BuildConfiguration();
        
        File basedir = new File( "/test/basedir" );
        in.setBaseDirectory( basedir );
        
        in.setDebug( true );
        
        InvocationOutputHandler errHandler = new SystemOutHandler(); 
        in.setErrorHandler( errHandler );
        
        in.setFailureBehavior( BuildConfiguration.REACTOR_FAIL_AT_END );
        in.setGlobalChecksumPolicy( BuildConfiguration.CHECKSUM_POLICY_FAIL );
        in.setGoals( Collections.singletonList( "eclipse:eclipse" ) );
        
        InputStream stream = getInputStream();
        
        in.setInputStream( stream );
        
        in.setInteractive( true );
        
        File localRepo = new File( "/test/local-repo" );
        in.setLocalRepositoryDirectory( localRepo );
        
        in.setOffline( true );
        
        InvocationOutputHandler outHandler = new SystemOutHandler();
        in.setOutputHandler( outHandler );
        
        File pom = new File( "pom.xml" );
        in.setPomFile( pom );
        
        String pomName = "my-pom.xml";
        in.setPomFileName( pomName );
        
        Properties props = new Properties();
        in.setProperties( props );
        
        in.setShellEnvironmentInherited( true );
        in.setShowErrors( true );
        in.setUpdateSnapshots( true );
        
        File userSettings = new File( "/test/user-settings.xml" );
        in.setUserSettingsFile( userSettings );
        
        return in;
    }

    private BuildConfiguration copy( BuildConfiguration in )
    {
        
        BuildConfiguration out = in.copy();
        
        assertSame( in.getBaseDirectory(), out.getBaseDirectory() );
        assertTrue( out.isDebug() );
        assertSame( in.getErrorHandler( null ), out.getErrorHandler( null ) );
        assertEquals( BuildConfiguration.REACTOR_FAIL_AT_END, out.getFailureBehavior() );
        assertEquals( BuildConfiguration.CHECKSUM_POLICY_FAIL, out.getGlobalChecksumPolicy() );
        assertEquals( in.getGoals(), out.getGoals() );
        assertSame( in.getInputStream( null ), out.getInputStream( null ) );
        assertTrue( out.isInteractive() );
        assertSame( in.getLocalRepositoryDirectory( null ), out.getLocalRepositoryDirectory( null ) );
        assertTrue( out.isOffline() );
        assertSame( in.getOutputHandler( null ), out.getOutputHandler( null ) );
        assertSame( in.getPomFile(), out.getPomFile() );
        assertEquals( in.getPomFileName(), out.getPomFileName() );
        assertEquals( in.getProperties(), out.getProperties() );
        assertTrue( out.isShellEnvironmentInherited() );
        assertTrue( out.isShowErrors() );
        assertTrue( out.isUpdateSnapshots() );
        assertSame( in.getUserSettingsFile(), out.getUserSettingsFile() );
        
        return out;
    }

    private InputStream getInputStream()
    {
        ClassLoader cloader = getClass().getClassLoader();
        String className = getClass().getName().replace( '.', '/' ) + ".class";
        
        return cloader.getResourceAsStream( className );
    }
}
