package org.codehaus.mojo.bod.build;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.codehaus.mojo.bod.BuildConfiguration;
import org.codehaus.mojo.bod.MockManager;
import org.codehaus.mojo.bod.build.BuildException;
import org.codehaus.mojo.bod.build.DefaultDependencyBuilder;
import org.codehaus.mojo.bod.build.DependencyBuildRequest;
import org.codehaus.mojo.bod.candidate.BuildCandidateResolver;
import org.codehaus.mojo.bod.rewrite.PomRewriter;
import org.codehaus.mojo.bod.source.ProjectSourceResolver;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.easymock.MockControl;

public class DefaultDependencyBuilderTest
    extends TestCase
{

    private String javaIOTmpDir = System.getProperty( "java.io.tmpdir" );

    private MockManager mockManager;

    private MockControl candidateResolverControl;

    private BuildCandidateResolver candidateResolver;

    private MockControl sourceResolverControl;

    private ProjectSourceResolver sourceResolver;

    private MockControl invokerControl;

    private Invoker invoker;

    private MockControl rewriterControl;

    private PomRewriter rewriter;

    public void setUp()
    {
        mockManager = new MockManager();

        candidateResolverControl = MockControl.createControl( BuildCandidateResolver.class );
        mockManager.add( candidateResolverControl );

        candidateResolver = (BuildCandidateResolver) candidateResolverControl.getMock();

        sourceResolverControl = MockControl.createControl( ProjectSourceResolver.class );
        mockManager.add( sourceResolverControl );

        sourceResolver = (ProjectSourceResolver) sourceResolverControl.getMock();

        invokerControl = MockControl.createControl( Invoker.class );
        mockManager.add( invokerControl );

        invoker = (Invoker) invokerControl.getMock();

        rewriterControl = MockControl.createControl( PomRewriter.class );
        mockManager.add( rewriterControl );

        rewriter = (PomRewriter) rewriterControl.getMock();
    }

    public void testShouldConstructWithNoParamsToSupportPlexus()
    {
        new DefaultDependencyBuilder();
    }

    public void testShouldConstructWithCandidateAndSourceResolversAndInvoker()
    {
        mockManager.replayAll();

        newMockEnabledDefaultDependencyBuilder();

        mockManager.verifyAll();
    }

    private DefaultDependencyBuilder newMockEnabledDefaultDependencyBuilder()
    {
        return new DefaultDependencyBuilder( candidateResolver, sourceResolver, rewriter, invoker );
    }

    public void testShouldOrderNormalDependencyRelationshipProperly()
        throws BuildException
    {
        MavenProject project1 = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );

        addDependency( project2, project1 );

        List candidates = new ArrayList();
        candidates.add( project2 );
        candidates.add( project1 );

        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( candidates );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( project1 ) );
        assertEquals( 1, ordered.indexOf( project2 ) );

        mockManager.verifyAll();
    }

    public void testShouldOrderProjectsWhenDependencyIsNotAmongCandidates()
        throws BuildException
    {
        MavenProject project1 = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );
        MavenProject project3 = buildProject( "group3", "artifact3", "version3" );

        addDependency( project2, project1 );

        List candidates = new ArrayList();
        candidates.add( project2 );
        candidates.add( project3 );

        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( candidates );

        assertEquals( 0, ordered.indexOf( project2 ) );
        assertEquals( 1, ordered.indexOf( project3 ) );

        mockManager.verifyAll();
    }

    public void testOrderingShouldReturnEmptyListWhenCandidatesNull()
        throws BuildException
    {
        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( null );

        assertNotNull( ordered );
        assertEquals( 0, ordered.size() );

        mockManager.verifyAll();
    }

    public void testOrderingShouldReturnEmptyListWhenCandidatesEmpty()
        throws BuildException
    {
        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( Collections.EMPTY_LIST );

        assertNotNull( ordered );
        assertEquals( 0, ordered.size() );

        mockManager.verifyAll();
    }

    public void testShouldOrderNormalParentRelationshipProperly()
        throws BuildException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );

        addParent( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( parent );

        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( candidates );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( parent ) );
        assertEquals( 1, ordered.indexOf( child ) );

        mockManager.verifyAll();
    }

    public void testShouldFailToOrderWhenTwoProjectsWithSameGroupIdArtifactIdExist()
    {
        MavenProject project1 = buildProject( "group", "parent", "version" );
        MavenProject project2 = buildProject( "group", "child", "version" );
        MavenProject project3 = buildProject( "group", "child", "version2" );

        List candidates = new ArrayList();
        candidates.add( project1 );
        candidates.add( project2 );
        candidates.add( project3 );

        mockManager.replayAll();

        try
        {
            new DefaultDependencyBuilder().orderCandidates( candidates );

            fail( "Should fail to sort two projects with same groupId:artifactId." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "is duplicated" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testShouldFailToOrderCyclicalDependencyRelationship()
    {
        MavenProject project1 = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );

        addDependency( project2, project1 );
        addDependency( project1, project2 );

        List candidates = new ArrayList();
        candidates.add( project2 );
        candidates.add( project1 );

        mockManager.replayAll();

        try
        {
            new DefaultDependencyBuilder().orderCandidates( candidates );

            fail( "Should fail to order in the case where a dependency-induced cycle exists." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "Cycle detected with dependency" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testShouldOrderCyclicalParentDependencyRelationshipProperly()
        throws BuildException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );

        addParent( child, parent );
        addDependency( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( parent );

        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( candidates );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( parent ) );
        assertEquals( 1, ordered.indexOf( child ) );

        mockManager.verifyAll();
    }

    public void testShouldHandleOrderingCaseWhereParentIsNotInCandidateList()
        throws BuildException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );
        MavenProject child2 = buildProject( "group", "child2", "version" );

        addParent( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( child2 );

        mockManager.replayAll();

        List ordered = new DefaultDependencyBuilder().orderCandidates( candidates );

        assertEquals( 0, ordered.indexOf( child ) );
        assertEquals( 1, ordered.indexOf( child2 ) );

        mockManager.verifyAll();
    }

    private void addParent( MavenProject childProject, MavenProject parentProject )
    {
        Parent parent = new Parent();
        parent.setGroupId( parentProject.getGroupId() );
        parent.setArtifactId( parentProject.getArtifactId() );
        parent.setVersion( parentProject.getVersion() );

        childProject.getModel().setParent( parent );
        childProject.setParent( parentProject );
    }

    private void addDependency( MavenProject dependentProject, MavenProject dependencyProject )
    {
        Dependency dep = new Dependency();
        dep.setGroupId( dependencyProject.getGroupId() );
        dep.setArtifactId( dependencyProject.getArtifactId() );
        dep.setVersion( dependencyProject.getVersion() );

        dependentProject.getDependencies().add( dep );
    }

    private MavenProject buildProject( String groupId, String artifactId, String version )
    {
        Model model = new Model();
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );

        return new MavenProject( model );
    }

    public void testBuildProjectShouldReturnFalseIfExitCodeIsNonZero()
    {
        MockControl resultCtl = MockControl.createControl( InvocationResult.class );
        mockManager.add( resultCtl );

        InvocationResult result = (InvocationResult) resultCtl.getMock();

        result.getExecutionException();
        resultCtl.setReturnValue( null );

        result.getExitCode();
        resultCtl.setReturnValue( -1 );

        result.getExitCode();
        resultCtl.setReturnValue( -1 );

        BuildConfiguration config = new BuildConfiguration();

        try
        {
            invoker.execute( config );
            invokerControl.setReturnValue( result );
        }
        catch ( MavenInvocationException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        assertFalse( builder.buildProject( null, null, config, new DefaultMessageHolder() ) );

        mockManager.verifyAll();
    }

    public void testBuildProjectShouldReturnTrueIfExitCodeIsZero()
    {
        MockControl resultCtl = MockControl.createControl( InvocationResult.class );
        mockManager.add( resultCtl );

        InvocationResult result = (InvocationResult) resultCtl.getMock();

        result.getExecutionException();
        resultCtl.setReturnValue( null );

        result.getExitCode();
        resultCtl.setReturnValue( 0 );

        BuildConfiguration config = new BuildConfiguration();

        try
        {
            invoker.execute( config );
            invokerControl.setReturnValue( result );
        }
        catch ( MavenInvocationException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        assertTrue( builder.buildProject( null, null, config, new DefaultMessageHolder() ) );

        mockManager.verifyAll();
    }

    public void testBuildProjectShouldReturnFalseIfCommandLineExceptionOccurs()
    {
        MockControl resultCtl = MockControl.createControl( InvocationResult.class );
        mockManager.add( resultCtl );

        InvocationResult result = (InvocationResult) resultCtl.getMock();

        result.getExecutionException();
        resultCtl.setReturnValue( new CommandLineException( "cli problem" ) );

        BuildConfiguration config = new BuildConfiguration();

        try
        {
            invoker.execute( config );
            invokerControl.setReturnValue( result );
        }
        catch ( MavenInvocationException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        assertFalse( builder.buildProject( null, null, config, new DefaultMessageHolder() ) );

        mockManager.verifyAll();
    }

    public void testBuildProjectShouldReturnFalseIfInvocationExceptionOccurs()
    {
        BuildConfiguration config = new BuildConfiguration();

        try
        {
            invoker.execute( config );
            invokerControl.setThrowable( new MavenInvocationException( "invocation error" ) );
        }
        catch ( MavenInvocationException e )
        {
            fail( "Should never happen." );
        }

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        assertFalse( builder.buildProject( null, null, config, new DefaultMessageHolder() ) );

        mockManager.verifyAll();
    }

    public void testBuildCandidatesShouldSucceedWithOneProjectResolvedSourceDir()
        throws BuildException
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        BuildConfiguration config = setupBuildCandidatesForOneCandidate( project, projectDir, true, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        builder.buildCandidates( Collections.singletonList( project ), new HashSet(), null, null, config,
                                 new File( javaIOTmpDir ), null );

        mockManager.verifyAll();
    }

    public void testBuildCandidatesShouldFailWhenOneProjectOfTwoFailsToBuild()
    {
        MavenProject project = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        setupBuildCandidatesForOneCandidate( project, projectDir, true, -1 );
        BuildConfiguration config = setupBuildCandidatesForOneCandidate( project2, projectDir, true, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        List candidates = new ArrayList();
        candidates.add( project );
        candidates.add( project2 );

        try
        {
            builder.buildCandidates( candidates, new HashSet(), null, null, config, new File( javaIOTmpDir ), null );

            fail( "Should fail due to failed first build." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "Build for project: group:artifact:jar:version" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testBuildCandidatesShouldFailWithOneProjectUnResolvedSourceDir()
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        BuildConfiguration config = setupBuildCandidatesForOneCandidate( project, null, false, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        try
        {
            builder.buildCandidates( Collections.singletonList( project ), Collections.EMPTY_SET, null, null, config,
                                     new File( javaIOTmpDir ), null );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "Failed to resolve project sources" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testFailedBuildCandidatesShouldNotBeCached()
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        // we want to register two build runs with the mocks...
        // if the mocks don't pickup on exactly two runs for the project, it
        // means it was cached.
        setupBuildCandidatesForOneCandidate( project, null, false, 0 );
        BuildConfiguration config = setupBuildCandidatesForOneCandidate( project, null, false, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        try
        {
            builder.buildCandidates( Collections.singletonList( project ), Collections.EMPTY_SET, null, null, config,
                                     new File( javaIOTmpDir ), null );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "Failed to resolve project sources" ) > -1 );
        }

        try
        {
            builder.buildCandidates( Collections.singletonList( project ), Collections.EMPTY_SET, null, null, config,
                                     new File( javaIOTmpDir ), null );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildException e )
        {
            assertTrue( e.getMessage().indexOf( "Failed to resolve project sources" ) > -1 );
        }

        mockManager.verifyAll();
    }

    private BuildConfiguration setupBuildCandidatesForOneCandidate( MavenProject project, File projectDir,
                                                                    boolean expectInvocation, int invocationResult )
    {
        sourceResolver.resolveProjectSources( project, null, null, null );
        sourceResolverControl.setMatcher( MockControl.ALWAYS_MATCHER );
        sourceResolverControl.setReturnValue( projectDir );

        BuildConfiguration config = new BuildConfiguration();

        if ( expectInvocation )
        {
            MockControl resultCtl = MockControl.createControl( InvocationResult.class );
            mockManager.add( resultCtl );

            InvocationResult result = (InvocationResult) resultCtl.getMock();

            result.getExecutionException();
            resultCtl.setReturnValue( null );

            result.getExitCode();
            resultCtl.setReturnValue( invocationResult );

            if ( invocationResult != 0 )
            {
                result.getExitCode();
                resultCtl.setReturnValue( invocationResult );
            }

            try
            {
                invoker.execute( config );
                invokerControl.setMatcher( MockControl.ALWAYS_MATCHER );
                invokerControl.setReturnValue( result );
            }
            catch ( MavenInvocationException e )
            {
                fail( "Should never happen." );
            }
        }

        return config;
    }

    public void testShouldBuildOneMissingDependency()
        throws BuildException
    {
        MavenProject mainProject = buildProject( "group", "top", "1" );

        MavenProject depProject = buildProject( "group", "dep", "1" );

        List candidates = Collections.singletonList( depProject );

        try
        {
            candidateResolver
                .resolveCandidates( mainProject, Collections.EMPTY_LIST, null, Collections.EMPTY_SET, true );

            candidateResolverControl.setMatcher( MockControl.ALWAYS_MATCHER );
            candidateResolverControl.setReturnValue( candidates );
        }
        catch ( BuildException e )
        {
            fail( "Should never happen!" );
        }

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        BuildConfiguration config = setupBuildCandidatesForOneCandidate( depProject, projectDir, true, 0 );

        rewriter.rewrite( null, null, null, null );
        rewriterControl.setMatcher( MockControl.ALWAYS_MATCHER );
        rewriterControl.setReturnValue( candidates );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        DependencyBuildRequest request = new DependencyBuildRequest();
        request.setProject( mainProject );
        request.setBuildPrototype( config );
        request.setCompletedBuilds( new HashSet() );
        request.setCurrentPendingProjects( Collections.EMPTY_LIST );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );

        builder.buildMissingDependencies( request );

        mockManager.verifyAll();
    }

    public void testShouldBuildOneMissingDependencyWithNullPrototypeConfig()
        throws BuildException
    {
        MavenProject mainProject = buildProject( "group", "top", "1" );

        MavenProject depProject = buildProject( "group", "dep", "1" );

        List candidates = Collections.singletonList( depProject );

        try
        {
            candidateResolver
                .resolveCandidates( mainProject, Collections.EMPTY_LIST, null, Collections.EMPTY_SET, true );

            candidateResolverControl.setMatcher( MockControl.ALWAYS_MATCHER );
            candidateResolverControl.setReturnValue( candidates );
        }
        catch ( BuildException e )
        {
            fail( "Should never happen!" );
        }

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        setupBuildCandidatesForOneCandidate( depProject, projectDir, true, 0 );

        rewriter.rewrite( null, null, null, null );
        rewriterControl.setMatcher( MockControl.ALWAYS_MATCHER );
        rewriterControl.setReturnValue( candidates );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        DependencyBuildRequest request = new DependencyBuildRequest();
        request.setProject( mainProject );
        request.setCompletedBuilds( new HashSet() );
        request.setCurrentPendingProjects( Collections.EMPTY_LIST );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );

        builder.buildMissingDependencies( request );

        mockManager.verifyAll();
    }

}
