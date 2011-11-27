package org.codehaus.mojo.bod.rewrite;

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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.MockManager;
import org.codehaus.mojo.bod.PomRewriteConfiguration;
import org.codehaus.mojo.bod.rewrite.DefaultPomRewriter;
import org.codehaus.plexus.PlexusContainer;
import org.easymock.MockControl;

public class DefaultPomRewriterTest
    extends TestCase
{
    
    private MockManager mockManager;
    
    private MockControl projectBuilderControl;
    
    private MavenProjectBuilder projectBuilder;
    
    private MockControl containerControl;
    
    private PlexusContainer container;
    
    private MockControl localRepoControl;
    
    private ArtifactRepository localRepo;
    
    public void setUp()
    {
        mockManager = new MockManager();
        
        projectBuilderControl = MockControl.createControl( MavenProjectBuilder.class );
        mockManager.add( projectBuilderControl );
        
        projectBuilder = (MavenProjectBuilder) projectBuilderControl.getMock();
        
        containerControl = MockControl.createControl( PlexusContainer.class );
        mockManager.add( containerControl );
        
        container = (PlexusContainer) containerControl.getMock();
        
        localRepoControl = MockControl.createControl( ArtifactRepository.class );
        mockManager.add( localRepoControl );
        
        localRepo = (ArtifactRepository) localRepoControl.getMock();
    }
    
    public void testShouldSkipProjectWhichIsExcluded()
    {
        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        
        MavenProject project = new MavenProject( model );
        
        PomRewriteConfiguration config = new PomRewriteConfiguration();
        config.setExcludes( Collections.singletonList( "group:artifact" ) );
        
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        
        config.setProperties( props );
        
        mockManager.replayAll();
        
        DefaultPomRewriter rewriter = new DefaultPomRewriter( projectBuilder, container );
        
        MessageHolder mh = new DefaultMessageHolder();
        
        List result = rewriter.rewrite( Collections.singletonList( project ), config, localRepo, mh );
        
        assertNotNull( result );
        assertTrue( result.contains( project ) );
        
        mockManager.verifyAll();
    }

    public void testShouldNotSkipProjectWhichIsIncluded()
    {
        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        
        model.setProperties( new Properties() );
        
        MavenProject project = new MavenProject( model );
        
        project.setOriginalModel( model );
        
        MavenProject rewritten = new MavenProject( model );
        
        PomRewriteConfiguration config = new PomRewriteConfiguration();
        config.setIncludes( Collections.singletonList( "group:artifact" ) );
        
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        
        config.setProperties( props );
        
        try
        {
            projectBuilder.build( null, null, null );
            projectBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            projectBuilderControl.setReturnValue( rewritten );
        }
        catch ( ProjectBuildingException e )
        {
            fail( "Should never happen" );
        }
        
        mockManager.replayAll();
        
        DefaultPomRewriter rewriter = new DefaultPomRewriter( projectBuilder, container );
        
        MessageHolder mh = new DefaultMessageHolder();
        
        List result = rewriter.rewrite( Collections.singletonList( project ), config, localRepo, mh );
        
        assertNotNull( result );
        assertTrue( result.contains( rewritten ) );
        
        MavenProject out = (MavenProject) result.get( 0 );
        
        assertEquals( "value", out.getProperties().getProperty( "key" ) );
        
        mockManager.verifyAll();
    }

}