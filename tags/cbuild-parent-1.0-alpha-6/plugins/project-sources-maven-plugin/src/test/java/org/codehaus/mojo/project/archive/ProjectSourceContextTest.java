package org.codehaus.mojo.project.archive;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.easymock.MockControl;

public class ProjectSourceContextTest
    extends PlexusTestCase
{
    private static MavenSession session;
    private BuildAdvisor buildAdvisor;
    private MavenProject project;

    public void setUp()
        throws Exception
    {
        super.setUp();
        // TODO: Delete the 2nd line for Maven 3.0
        session = new MavenSession( new DefaultPlexusContainer(), null, null, null
            , null, null, null, null, null
        );
        buildAdvisor = (BuildAdvisor) lookup( BuildAdvisor.ROLE, BuildAdvisor.ROLE_HINT );
        Model model = new Model();
        model.setGroupId( "groupId" );
        model.setArtifactId( "artifactId" );
        model.setVersion( "1.0" );
        project = new MavenProject( model );
        // System.out.println(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        session.setCurrentProject(project);
    }

    public void testIsSourceArtifactResolved_ShouldReturnFalseWhenNothingSet()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx.read( session );
        assertFalse( ctx.isSourceArtifactResolved() );
    }

    public void testIsSourceArtifactResolved_ShouldReturnTrueAfterSettingSourceArtifactResolved()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);

        ctx.read( session );
        ctx.setSourceArtifactResolved( true );
        ctx.store( session );
        ctx.read( session );

        // should obviously return true
        assertTrue( ctx.isSourceArtifactResolved() );
    }

    public void testIsSourceArtifactResolved_ShouldReturnTrueAfterSet()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx.read( session );
        assertTrue( ctx.isSourceArtifactResolved() );
    }

    public void testHasProjectSourceDirectory_ShouldReturnFalseWhenNothingSet()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx.read( session );
        assertFalse( ctx.hasProjectSourceDirectory() );
    }

    public void testHasProjectSourceDirectory_ShouldReturnTrueWhenProjectSourceDirSet() 
        throws ComponentLookupException, IOException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);

        File tmpDir = File.createTempFile( "project-dir", ".test" );
        tmpDir.deleteOnExit();

        ctx.read( session );
        ctx.setProjectSourceDirectory( tmpDir );
        ctx.store( session );

        assertTrue( ctx.hasProjectSourceDirectory() );
        ctx.read( session );
        assertTrue( ctx.hasProjectSourceDirectory() );
    }

    public void testHasProjectSourceDirectory_ShouldReturnTrueAfterSet()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx.read( session );
        assertTrue( ctx.hasProjectSourceDirectory() );
    }

    public void testGetProjectSourceArtifact_RetrieveArtifactAfterSet()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx.read( session );

        ArtifactFactory artifactFactory = (ArtifactFactory) lookup(
            ArtifactFactory.ROLE );
        Artifact artifact = artifactFactory.createArtifactWithClassifier(
            "mygroup", "myArtifact", "2.1.0-M1", "zip", "project-sources" );

        //MockControl ctl = MockControl.createControl( Artifact.class );
        //Artifact artifact = (Artifact) ctl.getMock();
        assertNotNull( artifact );

        //ctl.replay();

        ctx.setProjectSourceArtifact( artifact );
        assertNotNull( ctx.getProjectSourceArtifact() );

        assertSame( artifact, ctx.getProjectSourceArtifact() );

        ctx.store( session );
        assertSame( artifact, ctx.getProjectSourceArtifact() );
        ctx.read( session );
        assertNotNull( ctx.getProjectSourceArtifact() );

        assertEquals( artifact, ctx.getProjectSourceArtifact() );

        //ctl.verify();
    }

    public void testGetProjectSourceArtifact_RetrieveNullAfterReset()
        throws ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        // reset the cookie
        ctx.store( session );
        ctx.read( session );
        assertNull( ctx.getProjectSourceArtifact() );
        assertNull( ctx.getOriginalProjectSourceLocation() );
    }

    public void testGetOriginalProjectSourceLocation_RetrieveFileAfterSet()
        throws IOException, ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);

        File tmp = File.createTempFile( "original-source-location", ".test" );
        tmp.deleteOnExit();

        ctx.setOriginalProjectSourceLocation( tmp );
        ctx.store( session );

        assertSame( tmp, ctx.getOriginalProjectSourceLocation() );
        ctx.read( session );

        // TODO: should be same in a memory based store with BuildAdvisor.java
        assertEquals( tmp, ctx.getOriginalProjectSourceLocation() );
    }

    public void testStoreAndRead_RetrieveProjectSourceDirectoryAfterStoring()
        throws IOException, ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);

        File tmp = File.createTempFile( "projectContext-store", ".test" );
        tmp.deleteOnExit();

        ctx.setProjectSourceDirectory( tmp );

        ctx.store( session );

        ProjectSourceContext ctx2 = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);
        ctx2.read( session );

        assertEquals( tmp, ctx2.getProjectSourceDirectory() );

        // TODO: This fails with the Serialized Object /tmp store currently
        // implemented for Maven 2.x.  When we go back to Maven 3.x
        // and store context in the MavenSession, this will work again
        // @Ignore assertSame( tmp, ctx2.getProjectSourceDirectory() );
    }
/*
    public void testStoreDeleteAndRead_RetrieveNullProjectSourceDirectoryAfterStoringAndDeleting()
        throws IOException, ComponentLookupException, Exception
    {
        ProjectSourceContext ctx = (ProjectSourceContext) lookup(
            ProjectSourceContext.ROLE, ProjectSourceContext.ROLE_HINT);

        File tmp = File.createTempFile( "projectContext-store", ".test" );
        tmp.deleteOnExit();

        ctx.setProjectSourceDirectory( tmp );

        ctx.store( session );
        
        ProjectSourceContext.delete( buildContextManager, project );

        ProjectSourceContext ctx2 = ProjectSourceContext.read( buildContextManager, project );

        assertTrue( ctx2 == null || ctx2.getProjectSourceDirectory() == null );
    }
*/
}
