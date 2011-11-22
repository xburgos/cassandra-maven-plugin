package org.codehaus.mojo.resolver.bod.pom;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.MockManager;
import org.codehaus.mojo.resolver.bod.pom.DefaultDependencyPOMResolver;
import org.codehaus.plexus.logging.Logger;
import org.easymock.MockControl;

public class DefaultDependencyPOMResolverTest
    extends TestCase
{

    private MockManager mockManager;

    private MockControl artifactMetadataSourceControl;

    private ArtifactMetadataSource artifactMetadataSource;

    private MockControl artifactFactoryControl;

    private ArtifactFactory artifactFactory;

    private MockControl artifactCollectorControl;

    private ArtifactCollector artifactCollector;

    private MockControl projectBuilderControl;

    private MavenProjectBuilder projectBuilder;

    public void setUp()
    {
        mockManager = new MockManager();

        artifactMetadataSourceControl = MockControl.createControl( ArtifactMetadataSource.class );
        mockManager.add( artifactMetadataSourceControl );

        artifactMetadataSource = (ArtifactMetadataSource) artifactMetadataSourceControl.getMock();

        artifactFactoryControl = MockControl.createControl( ArtifactFactory.class );
        mockManager.add( artifactFactoryControl );

        artifactFactory = (ArtifactFactory) artifactFactoryControl.getMock();

        artifactCollectorControl = MockControl.createControl( ArtifactCollector.class );
        mockManager.add( artifactCollectorControl );

        artifactCollector = (ArtifactCollector) artifactCollectorControl.getMock();

        projectBuilderControl = MockControl.createControl( MavenProjectBuilder.class );
        mockManager.add( projectBuilderControl );

        projectBuilder = (MavenProjectBuilder) projectBuilderControl.getMock();
    }

    public void testShouldConstructWithMetadataSourceProjectBuilderAndLogLevel()
    {
        mockManager.replayAll();

        new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder, artifactFactory, artifactCollector,
                                    Logger.LEVEL_DISABLED );

        mockManager.verifyAll();
    }

    public void testShouldConstructWithNoParams()
    {
        new DefaultDependencyPOMResolver();
    }

    public void testShouldRemoveOneBuildInProgress()
    {
        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        MavenProject project = new MavenProject( model );

        Map candidates = new HashMap( Collections.singletonMap( ArtifactUtils.versionlessKey( "group", "artifact" ),
                                                                project ) );
        List inProgress = Collections.singletonList( project );

        resolver.removeBuildsInProgress( candidates, inProgress );

        assertTrue( candidates.isEmpty() );

        mockManager.verifyAll();
    }

    public void testShouldRemoveOneCompletedBuild()
    {
        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        MavenProject project = new MavenProject( model );

        Map candidates = new HashMap( Collections.singletonMap( ArtifactUtils.versionlessKey( "group", "artifact" ),
                                                                project ) );
        Set completed = Collections.singleton( "group:artifact" );

        resolver.removeCompletedBuilds( candidates, completed );

        assertTrue( candidates.isEmpty() );

        mockManager.verifyAll();
    }

    public void testShouldReturnEmptyMapIfCreateInstancesCalledWithEmptyArtifactSet()
    {
        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        Map result = resolver.createProjectInstances( Collections.EMPTY_SET, null, null );

        assertNotNull( result );
        assertTrue( result.isEmpty() );

        mockManager.verifyAll();
    }

    public void testShouldReturnEmptyMapIfCreateInstancesCalledWithNullArtifactSet()
    {
        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        Map result = resolver.createProjectInstances( null, null, null );

        assertNotNull( result );
        assertTrue( result.isEmpty() );

        mockManager.verifyAll();
    }

    public void testShouldReturnEmptyMapAndErrorIfCreateInstancesFailsToBuildOneProject()
    {
        try
        {
            projectBuilder.buildFromRepository( null, null, null );
            projectBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            projectBuilderControl.setThrowable( new ProjectBuildingException( "one", "two" ) );
        }
        catch ( ProjectBuildingException e )
        {
            fail( "This should never happen." );
        }

        MockControl artifactControl = MockControl.createControl( Artifact.class );
        mockManager.add( artifactControl );

        Artifact artifact = (Artifact) artifactControl.getMock();

        artifact.getGroupId();
        artifactControl.setReturnValue( "group" );
        artifact.getArtifactId();
        artifactControl.setReturnValue( "artifact" );

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();
        
        ResolutionNode node = new ResolutionNode( artifact, Collections.EMPTY_LIST );

        Map result = resolver.createProjectInstances( Collections.singleton( node ), null, mh );

        assertNotNull( result );
        assertTrue( result.isEmpty() );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "Failed to build" ) > -1 );
        assertTrue( mh.render().indexOf( "group:artifact" ) > -1 );

        mockManager.verifyAll();
    }

    public void testShouldReturnMapWhenCreateInstancesBuildsOneProject()
    {
        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        MavenProject project = new MavenProject( model );

        try
        {
            projectBuilder.buildFromRepository( null, null, null );
            projectBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            projectBuilderControl.setReturnValue( project );
        }
        catch ( ProjectBuildingException e )
        {
            fail( "This should never happen." );
        }

        MockControl artifactControl = MockControl.createControl( Artifact.class );
        mockManager.add( artifactControl );

        Artifact artifact = (Artifact) artifactControl.getMock();

        artifact.getGroupId();
        artifactControl.setReturnValue( "group" );
        artifact.getArtifactId();
        artifactControl.setReturnValue( "artifact" );

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();
        
        ResolutionNode node = new ResolutionNode( artifact, Collections.EMPTY_LIST );

        Map result = resolver.createProjectInstances( Collections.singleton( node ), null, mh );

        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertNotNull( result.get( "group:artifact" ) );

        mockManager.verifyAll();
    }

    public void testShouldFailToIncludeUnresolvableArtifactsDuringCandidateCreation()
    {
        MavenProject project = setupBasicTestProject();

        Artifact dependencyArtifact = setupTestDependencyArtifact();

        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );
            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setThrowable( new ArtifactResolutionException( "cannot resolve",
                                                                                    dependencyArtifact ) );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertTrue( result.isEmpty() );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "cannot resolve" ) > -1 );

        mockManager.verifyAll();
    }

    public void testShouldFailToCreateDependencyArtifacts()
    {
        MavenProject project = setupBasicTestProject();

        Dependency dep = (Dependency) project.getDependencies().get( 0 );
        dep.setVersion( "[1," );

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( null, null, artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertTrue( result.isEmpty() );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "Failed to create dependency artifacts" ) > -1 );

        mockManager.verifyAll();
    }

    public void testShouldResolveTwoProjectsForOneDependencyDuringCandidateCreation()
    {
        MavenProject project = setupBasicTestProject();

        Artifact dependencyArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
            .createFromVersion( "version2" ), "compile", "jar", null, new DefaultArtifactHandler( "jar" ), false );

        Artifact dependencyPomArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
            .createFromVersion( "version2" ), "compile", "pom", null, new DefaultArtifactHandler( "pom" ), false );

        Artifact transdepPomArtifact = new DefaultArtifact( "group3", "artifact3", VersionRange
            .createFromVersion( "version3" ), "compile", "pom", null, new DefaultArtifactHandler( "pom" ), false );

        project.setDependencyArtifacts( Collections.singleton( dependencyArtifact ) );

        ResolutionNode topNode = new ResolutionNode( dependencyPomArtifact, Collections.EMPTY_LIST );
        ResolutionNode childNode = new ResolutionNode( transdepPomArtifact, Collections.EMPTY_LIST, topNode );

        Set nodes = new HashSet( 2 );
        nodes.add( topNode );
        nodes.add( childNode );

        ArtifactResolutionResult arResult = new ArtifactResolutionResult();
        arResult.setArtifactResolutionNodes( nodes );

        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );

            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setReturnValue( arResult );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "should never happen" );
        }

        MavenProject placeholder = new MavenProject( new Model() );

        try
        {
            projectBuilder.buildFromRepository( null, null, null );
            projectBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
            projectBuilderControl.setReturnValue( placeholder, MockControl.ONE_OR_MORE );
        }
        catch ( ProjectBuildingException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertFalse( result.isEmpty() );
        assertTrue( result.containsKey( "group2:artifact2" ) );
        assertTrue( result.containsKey( "group3:artifact3" ) );
        assertEquals( 0, mh.size() );

        mockManager.verifyAll();
    }

    public void testShouldResolveZeroProjectsForNoDependenciesDuringCandidateCreation()
    {
        MavenProject project = setupBasicTestProject();
        project.setDependencies( Collections.EMPTY_LIST );

        ArtifactResolutionResult arResult = new ArtifactResolutionResult();
        arResult.setArtifactResolutionNodes( Collections.EMPTY_SET );

        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );

            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setReturnValue( arResult );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "should never happen" );
        }

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertTrue( result.isEmpty() );
        assertEquals( 0, mh.size() );

        mockManager.verifyAll();
    }

    public void testShouldBuildProjectArtifactAndResolveZeroProjectsDuringCandidateCreation()
    {
        MavenProject project = setupBasicTestProject();
        project.setDependencies( Collections.EMPTY_LIST );

        Artifact projectArtifact = project.getArtifact();
        project.setArtifact( null );

        artifactFactory.createBuildArtifact( projectArtifact.getGroupId(), projectArtifact.getArtifactId(),
                                             projectArtifact.getVersion(), "pom" );
        artifactFactoryControl.setReturnValue( projectArtifact );

        ArtifactResolutionResult arResult = new ArtifactResolutionResult();
        arResult.setArtifactResolutionNodes( Collections.EMPTY_SET );

        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );

            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setReturnValue( arResult );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "should never happen" );
        }

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertTrue( result.isEmpty() );
        assertEquals( 0, mh.size() );

        mockManager.verifyAll();
    }

    public void testShouldNotBuildDependencyArtifactAndResolveZeroProjectsDuringCandidateCreation()
        throws InvalidDependencyVersionException
    {
        MavenProject project = setupBasicTestProject();

        Artifact dependencyArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
            .createFromVersion( "version2" ), "compile", "jar", null, new DefaultArtifactHandler( "jar" ), false );

        artifactFactory.createDependencyArtifact( "group2", "artifact2", VersionRange.createFromVersion( "version2" ),
                                                  "jar", null, Artifact.SCOPE_COMPILE, null, false );

        artifactFactoryControl.setMatcher( MockControl.ALWAYS_MATCHER );

        // only match once.
        // this is the real meat of the test, since it should only be called once,
        // about 10 lines below this location (before we enter the resolver code).
        artifactFactoryControl.setReturnValue( dependencyArtifact );

        // this will simply short-circuit the rest of the code, to keep the
        // mock configuration for the test relatively simple.
        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );

            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setThrowable( new ArtifactResolutionException( "short circuit.",
                                                                                    dependencyArtifact ) );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "should never happen" );
        }

        mockManager.replayAll();

        project.setDependencyArtifacts( project.createArtifacts( artifactFactory, null,
                                                                 new ScopeArtifactFilter( Artifact.SCOPE_TEST ) ) );

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        Map result = resolver.resolveBuildCandidateMap( project, null, mh );

        assertTrue( result.isEmpty() );
        assertEquals( 1, mh.size() );

        mockManager.verifyAll();
    }

    public void testShouldResolveOneCandidateEndToEnd()
        throws BuildOnDemandResolutionException
    {
        testEndToEnd( Collections.EMPTY_LIST, Collections.EMPTY_SET, true );
    }

    public void testShouldResolveOneCandidateEndToEndWithNullCurrentBuildsList()
        throws BuildOnDemandResolutionException
    {
        testEndToEnd( null, Collections.EMPTY_SET, true );
    }

    public void testShouldResolveOneCandidateEndToEndWithNullCompletedBuilds()
        throws BuildOnDemandResolutionException
    {
        testEndToEnd( Collections.EMPTY_LIST, null, true );
    }

    public void testShouldResolveOneCandidateEndToEndWithNoDependencyBuilds()
        throws BuildOnDemandResolutionException
    {
        testEndToEnd( Collections.EMPTY_LIST, Collections.EMPTY_SET, false );
    }

    public void testShouldResolveOneCandidateEndToEndWithAllDependencyBuildsCached()
        throws BuildOnDemandResolutionException
    {
        testEndToEnd( Collections.EMPTY_LIST, Collections.singleton( "group2:artifact2" ), true );
    }

    private void testEndToEnd( List currentBuilds, Set completedBuilds, boolean includeDependency )
        throws BuildOnDemandResolutionException
    {
        MavenProject project = setupBasicTestProject();

        ArtifactResolutionResult arResult = new ArtifactResolutionResult();
        
        if ( includeDependency )
        {
            Artifact dependencyArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
                .createFromVersion( "version2" ), "compile", "jar", null, new DefaultArtifactHandler( "jar" ), false );
            Artifact dependencyPomArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
                .createFromVersion( "version2" ), "compile", "pom", null, new DefaultArtifactHandler( "pom" ), false );
            
            ResolutionNode topNode = new ResolutionNode( dependencyPomArtifact, Collections.EMPTY_LIST );
            
            artifactFactory.createDependencyArtifact( null, null, null, null, null, null, null, false );
            artifactFactoryControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactFactoryControl.setReturnValue( dependencyArtifact );
            
            arResult.setArtifactResolutionNodes( Collections.singleton( topNode ) );
            
            MavenProject placeholder = new MavenProject( new Model() );

            try
            {
                projectBuilder.buildFromRepository( null, null, null );
                projectBuilderControl.setMatcher( MockControl.ALWAYS_MATCHER );
                projectBuilderControl.setReturnValue( placeholder, MockControl.ONE_OR_MORE );
            }
            catch ( ProjectBuildingException e )
            {
                fail( "Should never happen." );
            }
        }        
        else
        {
            project.setDependencies( Collections.EMPTY_LIST );
            
            arResult.setArtifactResolutionNodes( Collections.EMPTY_SET );
        }

        try
        {
            artifactCollector.collect( null, null, null, null, null, null, null );

            artifactCollectorControl.setMatcher( MockControl.ALWAYS_MATCHER );
            artifactCollectorControl.setReturnValue( arResult );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "should never happen" );
        }

        mockManager.replayAll();

        DefaultDependencyPOMResolver resolver = new DefaultDependencyPOMResolver( artifactMetadataSource, projectBuilder,
                                                                      artifactFactory, artifactCollector,
                                                                      Logger.LEVEL_DISABLED );

        MessageHolder mh = new DefaultMessageHolder();

        List result = resolver.resolveDependencyPoms( project, currentBuilds, null, completedBuilds );

        if ( includeDependency )
        {
            int expectedSize = 1;
            
            if ( currentBuilds != null )
            {
                expectedSize -= currentBuilds.size();
            }
            
            if ( completedBuilds != null )
            {
                expectedSize -= completedBuilds.size();
            }
            
            assertEquals( expectedSize, result.size() );
        }
        else
        {
            assertTrue( result.isEmpty() );
        }

        assertEquals( 0, mh.size() );

        mockManager.verifyAll();
    }

    private Artifact setupTestDependencyArtifact()
    {
        Artifact dependencyArtifact = new DefaultArtifact( "group2", "artifact2", VersionRange
            .createFromVersion( "version2" ), "compile", "jar", null, new DefaultArtifactHandler( "jar" ), false );

        artifactFactory.createDependencyArtifact( "group2", "artifact2", VersionRange.createFromVersion( "version2" ),
                                                  "jar", null, Artifact.SCOPE_COMPILE, null, false );

        artifactFactoryControl.setMatcher( MockControl.ALWAYS_MATCHER );
        artifactFactoryControl.setReturnValue( dependencyArtifact );

        return dependencyArtifact;
    }

    private MavenProject setupBasicTestProject()
    {
        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        Dependency dependency = new Dependency();

        dependency.setGroupId( "group2" );
        dependency.setArtifactId( "artifact2" );
        dependency.setVersion( "version2" );

        model.addDependency( dependency );

        MavenProject project = new MavenProject( model );

        MockControl projectArtifactControl = MockControl.createControl( Artifact.class );
        mockManager.add( projectArtifactControl );

        Artifact projectArtifact = new DefaultArtifact( "group", "artifact",
                                                        VersionRange.createFromVersion( "version" ), null, "pom", null,
                                                        new DefaultArtifactHandler( "pom" ), false );

        project.setArtifact( projectArtifact );

        return project;
    }
}
