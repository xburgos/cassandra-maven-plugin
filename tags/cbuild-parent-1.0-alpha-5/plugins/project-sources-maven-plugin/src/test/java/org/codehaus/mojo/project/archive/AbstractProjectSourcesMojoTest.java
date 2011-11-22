package org.codehaus.mojo.project.archive;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.DefaultContext;
import org.easymock.MockControl;

public class AbstractProjectSourcesMojoTest
    extends TestCase
{
    private Context context;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.context = new DefaultContext();
    }

    public void testGetProjectSourcesArtifact_RetrieveArtifactWithTypeAndClassifierUsingStdVersion()
        throws ContextException, MojoExecutionException
    {
        checkProjectSourcesArtifact( "1.0", "1.0" );
    }

    public void testGetProjectSourcesArtifact_RetrieveArtifactWithTypeAndClassifierUsingReleaseVersion()
        throws ContextException, MojoExecutionException
    {
        checkProjectSourcesArtifact( "1.0-2", "1.0" );
    }

    private void checkProjectSourcesArtifact( String projectVersion, String projectSourcesVersion )
        throws ContextException, MojoExecutionException
    {
        String classifier = "project-sources";
        String type = "zip";

        // there is a ton of mock setup, but I had some trouble with PlexusTestCase,
        // so I'm mocking everything.
        MockControl containerCtl = MockControl.createControl( PlexusContainer.class );
        PlexusContainer container = (PlexusContainer) containerCtl.getMock();

        context.put( PlexusConstants.PLEXUS_KEY, container );

        MockControl handlerCtl = MockControl.createControl( ArtifactHandler.class );
        ArtifactHandler handler = (ArtifactHandler) handlerCtl.getMock();

        try
        {
            container.lookup( ArtifactHandler.ROLE, type );
            containerCtl.setReturnValue( handler, MockControl.ZERO_OR_MORE );
        }
        catch ( ComponentLookupException e )
        {
            fail( "Should never happen: " + e.getMessage() );
        }

        handler.getClassifier();
        handlerCtl.setReturnValue( null, MockControl.ZERO_OR_MORE );

        containerCtl.replay();
        handlerCtl.replay();
        // end of mock setup.

        Artifact projectArtifact =
            new DefaultArtifact( "group", "artifact", VersionRange.createFromVersion( projectVersion ), null, "rpm",
                                 null, handler );

        // Delete the 2nd line for maven 3.0
        MavenSession session = new MavenSession( container, null, null, null
            , null, null, null, null, null
        );

        MavenProject project = buildProject( "group", "artifact", projectVersion );

        TestMojo mojo = new TestMojo();
        mojo.setSourceArtifactClassifier( classifier );
        mojo.setSourceArtifactType( type );
        mojo.setSession( session );
        mojo.setProject( project );
        mojo.setProjectArtifact( projectArtifact );

        mojo.contextualize( context );

        Artifact sourcesArtifact = mojo.getProjectSourcesArtifact();

        assertEquals( classifier, sourcesArtifact.getClassifier() );
        assertEquals( type, sourcesArtifact.getType() );
        assertEquals( projectSourcesVersion, sourcesArtifact.getVersion() );

        // verify the mock behaviors.
        containerCtl.verify();
        handlerCtl.verify();
    }

    private MavenProject buildProject( String groupId, String artifactId, String version )
    {
        Model model = new Model();

        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );

        return new MavenProject( model );
    }

    private static final class TestMojo
        extends AbstractProjectSourcesMojo
    {

        public void execute()
            throws MojoExecutionException, MojoFailureException
        {
            throw new UnsupportedOperationException( "Test-only class; not for execution." );
        }

        public Artifact getProjectSourcesArtifact()
            throws MojoExecutionException
        {
            return super.getProjectSourcesArtifact();
        }

    }

}
