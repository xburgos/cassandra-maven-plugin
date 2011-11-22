package org.codehaus.mojo.profile;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.PlexusTestCase;
import org.apache.maven.project.build.model.ModelLineage;
import org.apache.maven.project.build.model.ModelLineageBuilder;
import org.apache.maven.project.build.model.DefaultModelLineage;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.activation.DefaultProfileActivationContext;
import org.codehaus.plexus.context.DefaultContext;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ModelPropertyProfileActivatorTest
    extends PlexusTestCase
{
    
    private MavenSession session;
    private MavenProject project;

    private DefaultProfileActivationContext ctx;

    private List filesToDelete = new ArrayList();

    private File localRepoDir;

    private DefaultMavenProjectBuilder projectBuilder;
    
    private ArtifactRepositoryLayout repoLayout;

    public void setUp() throws Exception
    {
        super.setUp();

        projectBuilder = (DefaultMavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        // create a temp repo
        localRepoDir = new File( System.getProperty( "java.io.tmpdir" ), "local-repo." + System.currentTimeMillis() );
        localRepoDir.mkdirs();
        filesToDelete.add( localRepoDir );

        // create the parent pom into the temp repo
        Model ancestor = new Model();
        ancestor.setModelVersion( "4.0.0" );
        ancestor.setGroupId( "group" );
        ancestor.setArtifactId( "ancestor" );
        ancestor.setVersion( "1" );
        ancestor.setPackaging( "pom" );
        Properties props = new Properties();
        props.setProperty( "ancestorname", "ancestorvalue" );
        props.setProperty( "overlap", "value2" );
        ancestor.setProperties( props );
        File ancestorFile = new File( localRepoDir, "group/ancestor/1/ancestor-1.pom" );
        Writer writer = null;
        try
        {
            ancestorFile.getParentFile().mkdirs();
            writer = WriterFactory.newXmlWriter( ancestorFile );
            new MavenXpp3Writer().write( writer, ancestor );
        }
        finally
        {
            IOUtil.close( writer );
        }

        // load up a pom which is setup to have a parent
        repoLayout = (ArtifactRepositoryLayout) lookup( ArtifactRepositoryLayout.ROLE, "default" );
        props = System.getProperties();
        ctx = new DefaultProfileActivationContext( props, false );

        // Load up a pom and session
        File f1 = getTestFile( "src/test/resources/profile/test_parent.pom" );
        project = projectBuilder.build( f1, getLocalRepository(), new DefaultProfileManager( getContainer(), ctx ));

        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setLocalRepository( new DefaultArtifactRepository( "myrepo", localRepoDir.getAbsolutePath(), repoLayout ));
        request.setPom(f1);
        session = new MavenSession( getContainer(), request, null, null );
        session.setCurrentProject(project);

    }
    
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        if ( !filesToDelete.isEmpty() )
        {
            for ( Iterator it = filesToDelete.iterator(); it.hasNext(); )
            {
                File file = (File) it.next();

                if ( file.exists() )
                {
                    if ( file.isDirectory() )
                    {
                        FileUtils.deleteDirectory( file );
                    }
                    else
                    {
                        file.delete();
                    }
                }
            }
        }
    }

    public void testCanDetermineActivation_ShouldReturnFalseWhenNameNotSet()
    {
        assertEquals( "group:artifactId:pom:1.0", session.getCurrentProject().getId() );
        Profile profile = new Profile();
        profile.setId( "test-profile" );

        assertFalse( new ModelPropertyProfileActivator( session, project, null, "value", ctx ).canDetermineActivation( profile ) );
    }

    public void testCanDetermineActivation_ShouldReturnExceptionWithBadParent()
    {
        // This seems to show that if the lineage is bad, you will get an exception when you build the project
        // Prior to refactoring, the lineage was set by the now obsolete BuildContextManager class
        // Now the lineage is tested by building a test repo of POMs...
        Profile profile = new Profile();
        profile.setId( "test-profile" );

        boolean caught = false;
        try
        {
            File f1 = getTestFile( "src/test/resources/profile/test_badparent.pom" );
            project = projectBuilder.build( f1, getLocalRepository(), new DefaultProfileManager( getContainer(), ctx ));
        }
        catch( ProjectBuildingException e )
        {
            caught = true;
        }
        catch( Exception e )
        {
            System.out.println("Wrong exception caught " + e );
        }
        assertTrue(caught);
    }

    public void testCanDetermineActivation_ShouldReturnTrueWithSetupPom()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "name", ctx ).canDetermineActivation( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenNameNotSet()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, null, "value", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenProjectContextIsMissing()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, "name", null ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenValueIsMissing()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, "noname", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenModelPropertyNamePresentAndValueNotConfigured()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "name", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenModelPropertyNamePresentValueNotConfigedAndNameConfigNegated()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, "!name", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenModelPropertyNameMissingValueNotConfigedAndNameConfigNegated()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "!noname", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenNameAndValueMatch()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "name", "value", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenNameAndValueMatchInParentModel()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "ancestorname", "ancestorvalue", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenNameMatchesAndValueDoesntMatch()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, "name", "valueless", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenNameNegatedAndValueDoesntMatch()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertTrue( new ModelPropertyProfileActivator( session, project, "!name", "valueless", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnTrueWhenNameNegatedAndValueDoesntMatchButValueInParentDoesMatch()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        // This is a child overloading the parent I suppose.
        assertTrue( new ModelPropertyProfileActivator( session, project, "!overlap", "value2", ctx ).isActive( profile ) );
    }

    public void testIsActive_ShouldReturnFalseWhenNameNegatedAndParentHasValue()
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );
        assertFalse( new ModelPropertyProfileActivator( session, project, "!ancestorname", ctx ).isActive( profile ) );
    }

    public void testSoloPom()
        throws Exception
    {
        Profile profile = new Profile();
        profile.setId( "test-profile" );

         // Load up a pom and session
        File f1 = getTestFile( "src/test/resources/profile/test_solo.pom" );
        MavenProject myProj = projectBuilder.build( f1, getLocalRepository(), new DefaultProfileManager( getContainer(), ctx ));

        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setLocalRepository( new DefaultArtifactRepository( "myrepo", localRepoDir.getAbsolutePath(), repoLayout ));
        request.setPom(f1);
        MavenSession mySes = new MavenSession( getContainer(), request, null, null );
        mySes.setCurrentProject(myProj);

        assertTrue( new ModelPropertyProfileActivator( mySes, myProj, "name", ctx ).canDetermineActivation( profile ) );
        assertFalse( new ModelPropertyProfileActivator( mySes, myProj, "name", ctx ).isActive( profile ) );
        assertTrue( new ModelPropertyProfileActivator( mySes, myProj, "junk", "wonderful", ctx ).isActive( profile ) );
        assertFalse( new ModelPropertyProfileActivator( mySes, myProj, "!junk", "wonderful", ctx ).isActive( profile ) );
        assertTrue( new ModelPropertyProfileActivator( mySes, myProj, "!junk", "awful", ctx ).isActive( profile ) );
    }

    // This overloads the parent getLocalRepository() so that our scratch pad repo gets used
    protected ArtifactRepository getLocalRepository()
        throws Exception
    {
        // System.out.println("Setting repo to " + localRepoDir.getAbsolutePath());
        ArtifactRepository r = new DefaultArtifactRepository( "local", "file://" + localRepoDir.getAbsolutePath(),
                                                              repoLayout );
        return r;
    }

}
