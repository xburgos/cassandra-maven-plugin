package org.codehaus.mojo.project.archive;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.easymock.MockControl;

public class ProjectSourceContextTest
    extends TestCase
{

    private Context context;

    public void setUp()
        throws Exception
    {
        super.setUp();

        context = new DefaultContext();
    }

    public void testIsSourceArtifactResolved_ShouldReturnTrueAfterSettingSourceArtifactResolved()
    {
        ProjectSourceContext ctx = new ProjectSourceContext();

        ctx.setSourceArtifactResolved( true );

        assertTrue( ctx.isSourceArtifactResolved() );
    }

    public void testIsSourceArtifactResolved_ShouldReturnFalseWhenNothingSet()
    {
        assertFalse( new ProjectSourceContext().isSourceArtifactResolved() );
    }

    public void testHasProjectSourceDirectory_ShouldReturnTrueWhenProjectSourceDirSet()
        throws IOException
    {
        ProjectSourceContext ctx = new ProjectSourceContext();

        File tmpDir = File.createTempFile( "project-dir", ".test" );
        tmpDir.deleteOnExit();

        ctx.setProjectSourceDirectory( tmpDir );

        assertTrue( ctx.hasProjectSourceDirectory() );
    }

    public void testHasProjectSourceDirectory_ShouldReturnFalseWhenNothingSet()
    {
        assertFalse( new ProjectSourceContext().hasProjectSourceDirectory() );
    }

    public void testGetProjectSourceArtifact_RetrieveArtifactAfterSet()
    {
        ProjectSourceContext ctx = new ProjectSourceContext();

        MockControl ctl = MockControl.createControl( Artifact.class );
        Artifact artifact = (Artifact) ctl.getMock();

        ctl.replay();

        ctx.setProjectSourceArtifact( artifact );

        assertSame( artifact, ctx.getProjectSourceArtifact() );

        ctl.verify();
    }

    public void testGetProjectSourceArtifact_RetrieveNullWhenNothingSet()
    {
        assertNull( new ProjectSourceContext().getProjectSourceArtifact() );
    }

    public void testGetOriginalProjectSourceLocation_RetrieveFileAfterSet()
        throws IOException
    {
        ProjectSourceContext ctx = new ProjectSourceContext();

        File tmp = File.createTempFile( "original-source-location", ".test" );
        tmp.deleteOnExit();

        ctx.setOriginalProjectSourceLocation( tmp );

        assertSame( tmp, ctx.getOriginalProjectSourceLocation() );
    }

    public void testGetOriginalProjectSourceLocation_RetrieveNullWhenNothingSet()
    {
        assertNull( new ProjectSourceContext().getOriginalProjectSourceLocation() );
    }

    public void testStoreAndRead_RetrieveProjectSourceDirectoryAfterStoring()
        throws IOException
    {
        MavenProject project = buildProject( "group", "artifact", "1.0" );

        ProjectSourceContext ctx = new ProjectSourceContext();

        File tmp = File.createTempFile( "projectContext-store", ".test" );
        tmp.deleteOnExit();

        ctx.setProjectSourceDirectory( tmp );

        ctx.store( context, project );

        ProjectSourceContext ctx2 = ProjectSourceContext.read( context, project );

        assertSame( tmp, ctx2.getProjectSourceDirectory() );
    }

    public void testStoreDeleteAndRead_RetrieveNullProjectSourceDirectoryAfterStoringAndDeleting()
        throws IOException
    {
        MavenProject project = buildProject( "group", "artifact", "1.0" );

        ProjectSourceContext ctx = new ProjectSourceContext();

        File tmp = File.createTempFile( "projectContext-store", ".test" );
        tmp.deleteOnExit();

        ctx.setProjectSourceDirectory( tmp );

        ctx.store( context, project );
        
        ProjectSourceContext.delete( context, project );

        ProjectSourceContext ctx2 = ProjectSourceContext.read( context, project );

        assertNull( ctx2.getProjectSourceDirectory() );
    }

    private MavenProject buildProject( String groupId, String artifactId, String version )
    {
        Model model = new Model();

        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );

        return new MavenProject( model );
    }

}
