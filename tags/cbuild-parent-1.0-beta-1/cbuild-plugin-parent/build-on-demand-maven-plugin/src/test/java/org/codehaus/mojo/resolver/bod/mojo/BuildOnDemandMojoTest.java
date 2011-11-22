package org.codehaus.mojo.resolver.bod.mojo;

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

import junit.framework.TestCase;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionManager;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionRequest;
import org.codehaus.mojo.resolver.bod.MockManager;
import org.codehaus.mojo.resolver.bod.build.BuildConfiguration;
import org.easymock.MockControl;

public class BuildOnDemandMojoTest
    extends TestCase
{

    private MockManager mockManager;

    private MockControl dependencyBuilderControl;

    private BuildOnDemandResolutionManager buildOnDemandResolutionManager;

    public void setUp()
    {
        mockManager = new MockManager();

        dependencyBuilderControl = MockControl.createControl( BuildOnDemandResolutionManager.class );
        mockManager.add( dependencyBuilderControl );

        buildOnDemandResolutionManager = (BuildOnDemandResolutionManager) dependencyBuilderControl.getMock();
    }

    public void testShouldBuildDependency()
        throws MojoFailureException, MojoExecutionException
    {
        MockControl<Log> logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );

        Log log = (Log) logCtl.getMock();

        log.isDebugEnabled();
        logCtl.setReturnValue( false );

        try
        {
            buildOnDemandResolutionManager.resolveDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            fail( "Should never happen" );
        }

        mockManager.replayAll();

        BuildOnDemandMojo mojo = new BuildOnDemandMojo();

        mojo.setDependencyBuilder( buildOnDemandResolutionManager );
        mojo.setLog( log );
        mojo.setSettings( new Settings() );
        mojo.setResolutionMode( BuildOnDemandResolutionRequest.MODE_BUILD_ON_DEMAND );

        mojo.execute();

        mockManager.verifyAll();
    }

    public void testShouldBuildDependencyWithFullScaleConfiguration()
        throws MojoFailureException, MojoExecutionException
    {
        BuildConfiguration config = new BuildConfiguration();

        MavenProject project = new MavenProject( new Model() );
        List reactorProjects = Collections.EMPTY_LIST;

        MockControl<ArtifactRepository> lrCtl = MockControl.createControl( ArtifactRepository.class );
        mockManager.add( lrCtl );

        ArtifactRepository localRepository = (ArtifactRepository) lrCtl.getMock();

        File projectsDir = new File( "/path/to/projects/dir" );

        MockControl<Log> logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );

        Log log = (Log) logCtl.getMock();

        log.isDebugEnabled();
        logCtl.setReturnValue( false );

        try
        {
            buildOnDemandResolutionManager.resolveDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            fail( "Should never happen" );
        }

        mockManager.replayAll();

        BuildOnDemandMojo mojo = new BuildOnDemandMojo();

        mojo.setDependencyBuilder( buildOnDemandResolutionManager );
        mojo.setLog( log );
        mojo.setBuild( config );
        mojo.setDependencyProjectsDir( projectsDir );
        mojo.setUseGlobalBuildCache( false );
        mojo.setLocalRepository( localRepository );
        mojo.setProject( project );
        mojo.setReactorProjects( reactorProjects );
        mojo.setSettings( new Settings() );
        mojo.setResolutionMode( BuildOnDemandResolutionRequest.MODE_BUILD_ON_DEMAND );

        mojo.execute();

        mockManager.verifyAll();
    }

    public void testShouldThrowMojoFailureExceptionWhenDependencyBuildsFail()
        throws MojoExecutionException
    {
        MockControl<Log> logCtl = MockControl.createControl( Log.class );
        mockManager.add( logCtl );

        Log log = (Log) logCtl.getMock();

        log.isDebugEnabled();
        logCtl.setReturnValue( false );

        try
        {
            buildOnDemandResolutionManager.resolveDependencies( null );
            dependencyBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            dependencyBuilderControl.setThrowable( new BuildOnDemandResolutionException( "test error" ) );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            fail( "Should never happen" );
        }

        mockManager.replayAll();

        BuildOnDemandMojo mojo = new BuildOnDemandMojo();

        mojo.setDependencyBuilder( buildOnDemandResolutionManager );
        mojo.setLog( log );
        mojo.setSettings( new Settings() );
        mojo.setResolutionMode( BuildOnDemandResolutionRequest.MODE_BUILD_ON_DEMAND );

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
