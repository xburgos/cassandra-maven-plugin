package org.codehaus.mojo.resolver.bod.build;

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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionRequest;
import org.codehaus.mojo.resolver.bod.MockManager;
import org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

public class DefaultDependencyBuilderTest
    extends TestCase
{

    private String javaIOTmpDir = System.getProperty( "java.io.tmpdir" );

    private File repoBasedir = new File( javaIOTmpDir, "repository" );

    private MockManager mockManager;

    private MockControl<Invoker> invokerControl;

    private Invoker invoker;

    private MockControl<PomRewriter> rewriterControl;

    private PomRewriter rewriter;

    private MockControl<ArtifactRepository> localRepositoryCtl;

    private ArtifactRepository localRepository;

    private MockControl<ArtifactFactory> artifactFactoryCtl;

    private ArtifactFactory artifactFactory;

    private boolean localRepoPathOfMatcherSet = false;

    public void setUp()
    {
        mockManager = new MockManager();

        // candidateResolverControl = MockControl.createControl( DependencyPOMResolver.class );
        // mockManager.add( candidateResolverControl );
        //
        // candidateResolver = (DependencyPOMResolver) candidateResolverControl.getMock();

        invokerControl = MockControl.createControl( Invoker.class );
        mockManager.add( invokerControl );

        invoker = (Invoker) invokerControl.getMock();

        rewriterControl = MockControl.createControl( PomRewriter.class );
        mockManager.add( rewriterControl );

        rewriter = (PomRewriter) rewriterControl.getMock();
    }

    public void testBuildProjectShouldReturnFalseIfExitCodeIsNonZero()
    {
        MockControl<InvocationResult> resultCtl = MockControl.createControl( InvocationResult.class );
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

    private DefaultDependencyBuilder newMockEnabledDefaultDependencyBuilder()
    {
        return new DefaultDependencyBuilder( invoker, rewriter, artifactFactory );
    }

    public void testBuildProjectShouldReturnTrueIfExitCodeIsZero()
    {
        MockControl<InvocationResult> resultCtl = MockControl.createControl( InvocationResult.class );
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
        MockControl<InvocationResult> resultCtl = MockControl.createControl( InvocationResult.class );
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

    public void testbuildDependenciesShouldSucceedWithOneProjectResolvedSourceDir()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        BuildConfiguration config = setupbuildDependenciesForOneCandidate( project, projectDir, true, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();
        
        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
        request.setLocalRepository( localRepository );
        request.setBuildPrototype( config );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );

        List<MavenProject> projects = new ArrayList<MavenProject>( 1 );
        projects.add( project );
        
        builder.buildDependencies( projects, new HashSet(), request );

        mockManager.verifyAll();
    }

    public void testbuildDependenciesShouldFailWhenOneProjectOfTwoFailsToBuild()
        throws IOException
    {
        MavenProject project = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );

        File projectDir = new File( javaIOTmpDir, "test-project-dir" );

        setupbuildDependenciesForOneCandidate( project, projectDir, true, -1 );
        BuildConfiguration config = setupbuildDependenciesForOneCandidate( project2, projectDir, true, 0 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        List<MavenProject> candidates = new ArrayList<MavenProject>();
        candidates.add( project );
        candidates.add( project2 );

        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
        request.setLocalRepository( localRepository );
        request.setBuildPrototype( config );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );
        
        try
        {
            builder.buildDependencies( candidates, new HashSet(), request );

            fail( "Should fail due to failed first build." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "Build for project: group:artifact:jar:version" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testbuildDependenciesShouldFailWithOneProjectUnResolvedSourceDir()
        throws IOException
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        BuildConfiguration config = setupbuildDependenciesForOneCandidate( project, null, true, -1 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
        request.setLocalRepository( localRepository );
        request.setBuildPrototype( config );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );
        
        try
        {
            builder.buildDependencies( Collections.singletonList( project ), Collections.EMPTY_SET, request );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "exit code: -1" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testFailedbuildDependenciesShouldNotBeCached()
        throws IOException
    {
        MavenProject project = buildProject( "group", "artifact", "version" );

        // we want to register two build runs with the mocks...
        // if the mocks don't pickup on exactly two runs for the project, it
        // means it was cached.
        setupbuildDependenciesForOneCandidate( project, null, true, -1 );
        BuildConfiguration config = setupbuildDependenciesForOneCandidate( project, null, true, -1 );

        mockManager.replayAll();

        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();

        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
        request.setLocalRepository( localRepository );
        request.setBuildPrototype( config );
        request.setProjectsDirectory( new File( javaIOTmpDir ) );
        
        try
        {
            builder.buildDependencies( Collections.singletonList( project ), Collections.EMPTY_SET, request );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "exit code: -1" ) > -1 );
        }

        try
        {
            builder.buildDependencies( Collections.singletonList( project ), Collections.EMPTY_SET, request );

            fail( "Should fail when a build candidate's project-sources directory is null." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "exit code: -1" ) > -1 );
        }

        mockManager.verifyAll();
    }

    private BuildConfiguration setupbuildDependenciesForOneCandidate( MavenProject project, File projectDir,
                                                                    boolean expectInvocation, int invocationResult )
    {
        if ( localRepository == null )
        {
            localRepositoryCtl = MockControl.createControl( ArtifactRepository.class );
            localRepository = (ArtifactRepository) localRepositoryCtl.getMock();

            mockManager.add( localRepositoryCtl );

            localRepository.getBasedir();
            localRepositoryCtl.setReturnValue( repoBasedir.getAbsolutePath(), MockControl.ZERO_OR_MORE );
        }

        if ( artifactFactory == null )
        {
            artifactFactoryCtl = MockControl.createControl( ArtifactFactory.class );
            artifactFactory = (ArtifactFactory) artifactFactoryCtl.getMock();

            mockManager.add( artifactFactoryCtl );
        }

        MockControl<Artifact> pomArtifactCtl = MockControl.createControl( Artifact.class );
        Artifact pomArtifact = (Artifact) pomArtifactCtl.getMock();

        mockManager.add( pomArtifactCtl );

        pomArtifact.getGroupId();
        pomArtifactCtl.setReturnValue( project.getGroupId(), MockControl.ZERO_OR_MORE );

        pomArtifact.getArtifactId();
        pomArtifactCtl.setReturnValue( project.getArtifactId(), MockControl.ZERO_OR_MORE );

        pomArtifact.getVersion();
        pomArtifactCtl.setReturnValue( project.getVersion(), MockControl.ZERO_OR_MORE );

        pomArtifact.getClassifier();
        pomArtifactCtl.setReturnValue( null, MockControl.ZERO_OR_MORE );

        pomArtifact.getType();
        pomArtifactCtl.setReturnValue( "pom", MockControl.ZERO_OR_MORE );

        MockControl<ArtifactHandler> handlerCtl = MockControl.createControl( ArtifactHandler.class );
        ArtifactHandler handler = (ArtifactHandler) handlerCtl.getMock();

        mockManager.add( handlerCtl );

        handler.getClassifier();
        handlerCtl.setReturnValue( null, MockControl.ZERO_OR_MORE );

        handler.getExtension();
        handlerCtl.setReturnValue( "pom", MockControl.ZERO_OR_MORE );

        pomArtifact.getArtifactHandler();
        pomArtifactCtl.setReturnValue( handler, MockControl.ZERO_OR_MORE );

        pomArtifact.getId();
        pomArtifactCtl.setReturnValue( project.getGroupId() + ":" + project.getArtifactId() + ":"
                        + project.getVersion() + ":pom", MockControl.ZERO_OR_MORE );

        artifactFactory.createProjectArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion() );
        artifactFactoryCtl.setReturnValue( pomArtifact );

        String path =
            project.getGroupId().replace( '.', '/' ) + "/" + project.getArtifactId() + "/" + project.getVersion() + "/"
                            + project.getArtifactId() + "-" + project.getVersion() + ".pom";

        localRepository.pathOf( pomArtifact );

        if ( !localRepoPathOfMatcherSet )
        {
            localRepositoryCtl.setMatcher( new ArgumentsMatcher()
            {

                public boolean matches( Object[] expected, Object[] actual )
                {
                    Artifact one = (Artifact) expected[0];
                    Artifact two = (Artifact) actual[0];
                    return one.getId().equals( two.getId() );
                }

                public String toString( Object[] arguments )
                {
                    return "Matcher: {" + ( (Artifact) arguments[0] ).getId() + "}";
                }

            } );

            localRepoPathOfMatcherSet = true;
        }

        localRepositoryCtl.setReturnValue( path );

        rewriter.rewriteOnDisk( null, null, null );
        rewriterControl.setMatcher( MockControl.ALWAYS_MATCHER );

        BuildConfiguration config = new BuildConfiguration();

        if ( expectInvocation )
        {
            MockControl<InvocationResult> resultCtl = MockControl.createControl( InvocationResult.class );
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

    private MavenProject buildProject( String groupId, String artifactId, String version )
        throws IOException
    {
        Model model = new Model();
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );

        MockControl<Artifact> artifactCtl = MockControl.createControl( Artifact.class );
        Artifact artifact = (Artifact) artifactCtl.getMock();

        mockManager.add( artifactCtl );

        artifact.getGroupId();
        artifactCtl.setReturnValue( groupId, MockControl.ZERO_OR_MORE );

        artifact.getArtifactId();
        artifactCtl.setReturnValue( artifactId, MockControl.ZERO_OR_MORE );

        artifact.getVersion();
        artifactCtl.setReturnValue( version, MockControl.ZERO_OR_MORE );

        artifact.getClassifier();
        artifactCtl.setReturnValue( null, MockControl.ZERO_OR_MORE );

        artifact.getType();
        artifactCtl.setReturnValue( "jar", MockControl.ZERO_OR_MORE );

        MockControl<ArtifactHandler> handlerCtl = MockControl.createControl( ArtifactHandler.class );
        ArtifactHandler handler = (ArtifactHandler) handlerCtl.getMock();

        mockManager.add( handlerCtl );

        handler.getClassifier();
        handlerCtl.setReturnValue( null, MockControl.ZERO_OR_MORE );

        handler.getExtension();
        handlerCtl.setReturnValue( "jar", MockControl.ZERO_OR_MORE );

        artifact.getArtifactHandler();
        artifactCtl.setReturnValue( handler, MockControl.ZERO_OR_MORE );

        String path =
            groupId.replace( '.', '/' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";

        File file = new File( repoBasedir, path );

        FileUtils.forceDelete( file );
        file.getParentFile().mkdirs();

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( file );

            new MavenXpp3Writer().write( writer, model );
        }
        finally
        {
            IOUtil.close( writer );
        }

        MavenProject project = new MavenProject( model );

        project.setArtifact( artifact );

        return project;
    }

}
