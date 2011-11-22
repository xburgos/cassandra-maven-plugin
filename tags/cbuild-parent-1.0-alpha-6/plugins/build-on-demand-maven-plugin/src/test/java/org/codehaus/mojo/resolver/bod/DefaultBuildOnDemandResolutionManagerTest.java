package org.codehaus.mojo.resolver.bod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.resolver.bod.pom.DependencyPOMResolver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.dag.DAG;
import org.easymock.MockControl;

public class DefaultBuildOnDemandResolutionManagerTest
    extends TestCase
{

    private String javaIOTmpDir = System.getProperty( "java.io.tmpdir" );

    private File repoBasedir = new File( javaIOTmpDir, "repository" );

    private MockManager mockManager;

    private MockControl candidateResolverControl;

    private DependencyPOMResolver candidateResolver;

    public void setUp()
    {
        mockManager = new MockManager();

        candidateResolverControl = MockControl.createControl( DependencyPOMResolver.class );
        mockManager.add( candidateResolverControl );

        candidateResolver = (DependencyPOMResolver) candidateResolverControl.getMock();
    }

    public void testShouldConstructWithNoParamsToSupportPlexus()
    {
        new TestBuildOnDemandResolutionManager();
    }

    public void testShouldConstructWithCandidateAndSourceResolversAndInvoker()
    {
        mockManager.replayAll();

        newMockEnabledDefaultDependencyBuilder();

        mockManager.verifyAll();
    }

    private TestBuildOnDemandResolutionManager newMockEnabledDefaultDependencyBuilder()
    {
        return new TestBuildOnDemandResolutionManager( candidateResolver );
    }

    public void testShouldOrderNormalDependencyRelationshipProperly()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject project1 = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );

        addDependency( project2, project1 );

        List deps = new ArrayList();
        deps.add( project2 );
        deps.add( project1 );

        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( deps );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( project1 ) );
        assertEquals( 1, ordered.indexOf( project2 ) );

        mockManager.verifyAll();
    }

    public void testShouldOrderProjectsWhenDependencyIsNotAmongCandidates()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject project1 = buildProject( "group", "artifact", "version" );
        MavenProject project2 = buildProject( "group2", "artifact2", "version2" );
        MavenProject project3 = buildProject( "group3", "artifact3", "version3" );

        addDependency( project2, project1 );

        List deps = new ArrayList();
        deps.add( project2 );
        deps.add( project3 );

        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( deps );

        assertEquals( 0, ordered.indexOf( project2 ) );
        assertEquals( 1, ordered.indexOf( project3 ) );

        mockManager.verifyAll();
    }

    public void testOrderingShouldReturnEmptyListWhenCandidatesNull()
        throws BuildOnDemandResolutionException
    {
        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( null );

        assertNotNull( ordered );
        assertEquals( 0, ordered.size() );

        mockManager.verifyAll();
    }

    public void testOrderingShouldReturnEmptyListWhenCandidatesEmpty()
        throws BuildOnDemandResolutionException
    {
        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( Collections.EMPTY_LIST );

        assertNotNull( ordered );
        assertEquals( 0, ordered.size() );

        mockManager.verifyAll();
    }

    public void testShouldOrderNormalParentRelationshipProperly()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );

        addParent( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( parent );

        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( candidates );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( parent ) );
        assertEquals( 1, ordered.indexOf( child ) );

        mockManager.verifyAll();
    }

    public void testShouldFailToOrderWhenTwoProjectsWithSameGroupIdArtifactIdExist()
        throws IOException
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
            new TestBuildOnDemandResolutionManager().orderDependencyProjects( candidates );

            fail( "Should fail to sort two projects with same groupId:artifactId." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "is duplicated" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testShouldFailToOrderCyclicalDependencyRelationship()
        throws IOException
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
            new TestBuildOnDemandResolutionManager().orderDependencyProjects( candidates );

            fail( "Should fail to order in the case where a dependency-induced cycle exists." );
        }
        catch ( BuildOnDemandResolutionException e )
        {
            assertTrue( e.getMessage().indexOf( "Cycle detected with dependency" ) > -1 );
        }

        mockManager.verifyAll();
    }

    public void testShouldOrderCyclicalParentDependencyRelationshipProperly()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );

        addParent( child, parent );
        addDependency( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( parent );

        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( candidates );

        // project2 has to be built last, since it needs to reference project1 binaries.
        assertEquals( 0, ordered.indexOf( parent ) );
        assertEquals( 1, ordered.indexOf( child ) );

        mockManager.verifyAll();
    }

    public void testShouldHandleOrderingCaseWhereParentIsNotInCandidateList()
        throws BuildOnDemandResolutionException, IOException
    {
        MavenProject parent = buildProject( "group", "parent", "version" );
        MavenProject child = buildProject( "group", "child", "version" );
        MavenProject child2 = buildProject( "group", "child2", "version" );

        addParent( child, parent );

        List candidates = new ArrayList();
        candidates.add( child );
        candidates.add( child2 );

        mockManager.replayAll();

        List ordered = new TestBuildOnDemandResolutionManager().orderDependencyProjects( candidates );

        assertEquals( 0, ordered.indexOf( child ) );
        assertEquals( 1, ordered.indexOf( child2 ) );

        mockManager.verifyAll();
    }

    // FIXME: Get this top-level test working again!
//    public void testShouldBuildOneMissingDependency()
//        throws BuildOnDemandResolutionException, IOException
//    {
//        MavenProject mainProject = buildProject( "group", "top", "1" );
//
//        MavenProject depProject = buildProject( "group", "dep", "1" );
//
//        List candidates = Collections.singletonList( depProject );
//
//        File projectDir = new File( javaIOTmpDir, "test-project-dir" );
//
//        BuildConfiguration config = setupbuildDependenciesForOneCandidate( depProject, projectDir, true, 0 );
//
//        rewriter.rewrite( null, null, null, null );
//        rewriterControl.setMatcher( MockControl.ALWAYS_MATCHER );
//        rewriterControl.setReturnValue( candidates );
//
//        mockManager.replayAll();
//
//        DefaultDependencyBuilder builder = newMockEnabledDefaultDependencyBuilder();
//
//        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
//        request.setProject( mainProject );
//        request.setBuildPrototype( config );
//        request.setCompletedBuilds( new HashSet() );
//        request.setCurrentPendingProjects( Collections.EMPTY_LIST );
//        request.setProjectsDirectory( new File( javaIOTmpDir ) );
//        request.setLocalRepository( localRepository );
//
//        builder.resolveDependencies( request );
//
//        mockManager.verifyAll();
//    }
//
//    public void testShouldBuildOneMissingDependencyWithNullPrototypeConfig()
//        throws BuildOnDemandResolutionException, IOException
//    {
//        MavenProject mainProject = buildProject( "group", "top", "1" );
//
//        MavenProject depProject = buildProject( "group", "dep", "1" );
//
//        List candidates = Collections.singletonList( depProject );
//
//        File projectDir = new File( javaIOTmpDir, "test-project-dir" );
//
//        setupbuildDependenciesForOneCandidate( depProject, projectDir, true, 0 );
//
//        rewriter.rewrite( null, null, null, null );
//        rewriterControl.setMatcher( MockControl.ALWAYS_MATCHER );
//        rewriterControl.setReturnValue( candidates );
//
//        mockManager.replayAll();
//
//        DefaultBuildOnDemandResolutionManager builder = newMockEnabledDefaultDependencyBuilder();
//
//        BuildOnDemandResolutionRequest request = new BuildOnDemandResolutionRequest();
//        request.setProject( mainProject );
//        request.setCompletedBuilds( new HashSet() );
//        request.setCurrentPendingProjects( Collections.EMPTY_LIST );
//        request.setProjectsDirectory( new File( javaIOTmpDir ) );
//
//        builder.resolveDependencies( request );
//
//        mockManager.verifyAll();
//    }

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
        throws IOException
    {
        Model model = new Model();
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );

        MockControl artifactCtl = MockControl.createControl( Artifact.class );
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

        MockControl handlerCtl = MockControl.createControl( ArtifactHandler.class );
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

    private static final class TestBuildOnDemandResolutionManager
        extends DefaultBuildOnDemandResolutionManager
    {

        public TestBuildOnDemandResolutionManager()
        {
            super();
        }

        public TestBuildOnDemandResolutionManager( DependencyPOMResolver pomResolver )
        {
            super( pomResolver );
        }

        public void graphRelationships( DAG dag, List candidates )
            throws BuildOnDemandResolutionException
        {
            super.graphRelationships( dag, candidates );
        }

        public List orderDependencyProjects( List candidates )
            throws BuildOnDemandResolutionException
        {
            return super.orderDependencyProjects( candidates );
        }

        public Map populateGraphVerticesAndProjectMap( DAG dag, List candidates )
            throws BuildOnDemandResolutionException
        {
            return super.populateGraphVerticesAndProjectMap( dag, candidates );
        }

    }

}
