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
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.bod.BuildConfiguration;
import org.codehaus.mojo.bod.BuildOnDemandMojo;
import org.codehaus.mojo.bod.build.BuildException;
import org.codehaus.mojo.bod.build.DependencyBuilder;
import org.easymock.MockControl;

import junit.framework.TestCase;

public class BuildOnDemandMojoTest
    extends TestCase
{
    
    private MockManager mockManager;
    
    private MockControl dependencyBuilderControl;
    
    private DependencyBuilder dependencyBuilder;
    
    public void setUp()
    {
        mockManager = new MockManager();
        
        dependencyBuilderControl = MockControl.createControl( DependencyBuilder.class );
        mockManager.add( dependencyBuilderControl );
        
        dependencyBuilder = (DependencyBuilder) dependencyBuilderControl.getMock();
    }
    
    public void testShouldBuildDependency() throws MojoFailureException
    {
        MockControl logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );
        
        Log log = (Log) logCtl.getMock();
        
        log.isDebugEnabled();
        logCtl.setReturnValue( false );
        
        try
        {
            dependencyBuilder.buildMissingDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( BuildException e )
        {
            fail( "Should never happen" );
        }
        
        mockManager.replayAll();
        
        BuildOnDemandMojo mojo = new BuildOnDemandMojo();
        
        mojo.setDependencyBuilder( dependencyBuilder );
        mojo.setLog( log );
        
        mojo.execute();
        
        mockManager.verifyAll();
    }
    
    public void testShouldBuildDependencyWithFullScaleConfiguration() throws MojoFailureException
    {
        BuildConfiguration config = new BuildConfiguration();
        
        MavenProject project = new MavenProject( new Model() );
        List reactorProjects = Collections.EMPTY_LIST;
        
        MockControl lrCtl = MockControl.createControl( ArtifactRepository.class );
        mockManager.add( lrCtl );
        
        ArtifactRepository localRepository = (ArtifactRepository) lrCtl.getMock();
        
        boolean force = true;
        
        File projectsDir = new File( "/path/to/projects/dir" );
        
        MockControl logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );
        
        Log log = (Log) logCtl.getMock();
        
        log.isDebugEnabled();
        logCtl.setReturnValue( false );
        
        try
        {
            dependencyBuilder.buildMissingDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( BuildException e )
        {
            fail( "Should never happen" );
        }
        
        mockManager.replayAll();
        
        BuildOnDemandMojo mojo = new BuildOnDemandMojo();
        
        mojo.setDependencyBuilder( dependencyBuilder );
        mojo.setLog( log );
        mojo.setBuild( config );
        mojo.setDependencyProjectsDir( projectsDir );
        mojo.setForce( force );
        mojo.setUseGlobalBuildCache( false );
        mojo.setLocalRepository( localRepository );
        mojo.setProject( project );
        mojo.setReactorProjects( reactorProjects );
        
        mojo.execute();
        
        mockManager.verifyAll();
    }
    
    public void testShouldThrowMojoFailureExceptionWhenDependencyBuildsFail()
    {
        MockControl logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );
        
        Log log = (Log) logCtl.getMock();
        
        log.isDebugEnabled();
        logCtl.setReturnValue( false );
        
        try
        {
            dependencyBuilder.buildMissingDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            dependencyBuilderControl.setThrowable( new BuildException( "test error" ) );
        }
        catch ( BuildException e )
        {
            fail( "Should never happen" );
        }
        
        mockManager.replayAll();
        
        BuildOnDemandMojo mojo = new BuildOnDemandMojo();
        
        mojo.setDependencyBuilder( dependencyBuilder );
        mojo.setLog( log );
        
        try
        {
            mojo.execute();
            
            fail( "Execution should have failed due to failure in building missing dependencies." );
        }
        catch ( MojoFailureException e )
        {
            assertTrue( e.getLongMessage().indexOf( "test error" ) > -1 );
        }
        
        mockManager.verifyAll();
    }
    
}
