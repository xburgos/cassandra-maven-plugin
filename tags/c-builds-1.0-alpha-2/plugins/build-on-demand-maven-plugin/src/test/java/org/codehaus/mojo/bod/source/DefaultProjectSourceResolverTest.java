package org.codehaus.mojo.bod.source;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.location.ArtifactLocatorStrategy;
import org.apache.maven.shared.io.location.FileLocation;
import org.apache.maven.shared.io.location.FileLocatorStrategy;
import org.apache.maven.shared.io.location.Location;
import org.apache.maven.shared.io.location.Locator;
import org.apache.maven.shared.io.location.URLLocatorStrategy;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.MockManager;
import org.codehaus.mojo.bod.source.DefaultProjectSourceResolver;
import org.codehaus.mojo.bod.source.ProjectSourceResolver;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpander;
import org.codehaus.mojo.tools.fs.archive.ArchiveExpansionException;
import org.codehaus.plexus.util.FileUtils;
import org.easymock.MockControl;


public class DefaultProjectSourceResolverTest
    extends TestCase
{
    
    private List dirsToDelete = new ArrayList();
    
    public void tearDown()
        throws IOException
    {
        for ( Iterator it = dirsToDelete.iterator(); it.hasNext(); )
        {
            File dir = (File) it.next();
            
            FileUtils.deleteDirectory( dir );
        }
    }

    public void testShouldCreateLocator()
    {
        MockManager mgr = new MockManager();

        MockControl resolverCtl = MockControl.createControl( ArtifactResolver.class );
        mgr.add( resolverCtl );

        ArtifactResolver resolver = (ArtifactResolver) resolverCtl.getMock();

        MockControl factoryCtl = MockControl.createControl( ArtifactFactory.class );
        mgr.add( factoryCtl );

        ArtifactFactory factory = (ArtifactFactory) factoryCtl.getMock();

        MockControl lrCtl = MockControl.createControl( ArtifactRepository.class );
        mgr.add( lrCtl );

        ArtifactRepository lr = (ArtifactRepository) lrCtl.getMock();

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver( factory, resolver, null );

        Locator locator = r.createLocator( lr, Collections.EMPTY_LIST, "tar.gz", new DefaultMessageHolder() );

        assertNotNull( locator );

        List strategies = locator.getStrategies();

        assertEquals( 3, strategies.size() );
        assertEquals( FileLocatorStrategy.class, strategies.get( 0 ).getClass() );
        assertEquals( URLLocatorStrategy.class, strategies.get( 1 ).getClass() );
        assertEquals( ArtifactLocatorStrategy.class, strategies.get( 2 ).getClass() );

        mgr.verifyAll();
    }

    public void testShouldFailToResolveSourcesWhenLocationGetFileThrowsIOException()
    {
        MockManager mgr = new MockManager();

        MockControl locationCtl = MockControl.createControl( Location.class );
        mgr.add( locationCtl );

        Location location = (Location) locationCtl.getMock();

        try
        {
            location.getFile();
            locationCtl.setThrowable( new IOException( "test error" ) );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver();

        MessageHolder mh = new DefaultMessageHolder();

        File result = r.getProjectSourceDirectory( location, mh, null, null, null );

        assertNull( result );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "test error" ) > -1 );

        mgr.verifyAll();
    }

    public void testShouldResolveToLocationFileWhenLocationReturnsDirectory()
        throws IOException
    {
        MockManager mgr = new MockManager();

        MockControl locationCtl = MockControl.createControl( Location.class );
        mgr.add( locationCtl );

        Location location = (Location) locationCtl.getMock();

        File file = File.createTempFile( "project-source.", ".test" );
        file.delete();
        file.mkdirs();
        dirsToDelete.add( file );

        try
        {
            location.getFile();
            locationCtl.setReturnValue( file );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver();

        MessageHolder mh = new DefaultMessageHolder();

        File result = r.getProjectSourceDirectory( location, mh, null, null, null );

        assertNotNull( result );
        assertSame( file, result );
        assertTrue( result.isDirectory() );

        mgr.verifyAll();
    }

    public void testShouldFailToResolveWhenSourceFileUnpackThrowsIOException()
        throws IOException
    {
        MockManager mgr = new MockManager();

        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
            expanderCtl.setThrowable( new IOException( "test error" ) );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File result = r.getProjectSourceDirectory( new FileLocation( file, "test" ), mh, null, null, null );

        assertNull( result );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "IOException" ) > -1 );
        assertTrue( mh.render().indexOf( "test error" ) > -1 );

        mgr.verifyAll();
    }

    public void testShouldFailToResolveWhenSourceFileUnpackThrowsArchiveExpansionException()
        throws IOException
    {
        MockManager mgr = new MockManager();

        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
            expanderCtl.setThrowable( new ArchiveExpansionException( "test error" ) );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File result = r.getProjectSourceDirectory( new FileLocation( file, "test" ), mh, null, null, null );

        assertNull( result );
        assertEquals( 1, mh.size() );
        assertTrue( mh.render().indexOf( "ArchiveExpansionException" ) > -1 );
        assertTrue( mh.render().indexOf( "test error" ) > -1 );

        mgr.verifyAll();
    }

    public void testShouldFailToResolveWhenUnpackedDirDoesntMatchPomSourceDirProperty()
        throws IOException
    {
        MockManager mgr = new MockManager();

        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        String testProjectSubdir = "test-project";

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        MavenProject project = new MavenProject( model );

        File result = r.getProjectSourceDirectory( new FileLocation( file, "test" ), mh, projectDir, project,
                                                   testProjectSubdir );

        assertNull( result );
        assertEquals( 1, mh.size() );

        assertTrue( mh.render().indexOf( projectDir.getAbsolutePath() ) > -1 );
        assertTrue( mh.render().indexOf( testProjectSubdir ) > -1 );

        mgr.verifyAll();
    }

    public void testShouldResolveToUnpackedDirWhenResolvedSourceIsArchiveFile()
        throws IOException
    {
        MockManager mgr = new MockManager();

        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        TestSourceResolver r = new TestSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        String testProjectSubdir = "test-project";

        File projectSourcesDir = new File( projectDir, testProjectSubdir );
        projectSourcesDir.mkdirs();

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );

        MavenProject project = new MavenProject( model );

        File result = r.getProjectSourceDirectory( new FileLocation( file, "test" ), mh, projectDir, project,
                                                   testProjectSubdir );

        assertNotNull( result );
        assertEquals( 0, mh.size() );
        assertEquals( projectSourcesDir, result );

        mgr.verifyAll();
    }

    public void testShouldResolveProjectSourcesEndToEndForFilePath()
        throws IOException
    {
        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        String testProjectSubdir = "test-project";
        String url = file.getAbsolutePath();

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_ARCHIVE_EXTENSION, "zip" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_DIRECTORY_NAME, testProjectSubdir );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_URL, url );

        MavenProject project = new MavenProject( model );

        MockManager mgr = new MockManager();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        ProjectSourceResolver r = new DefaultProjectSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        File projectSourcesDir = new File( projectDir, testProjectSubdir );

        // this is part of pretending that the expander does its job...testing of which is out of scope.
        projectSourcesDir.mkdirs();

        File result = r.resolveProjectSources( project, projectDir, null, mh );

        assertNotNull( result );
        assertEquals( 0, mh.size() );
        assertEquals( projectSourcesDir, result );

        mgr.verifyAll();
    }

    public void testShouldResolveProjectSourcesEndToEndForURL()
        throws IOException
    {
        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        String testProjectSubdir = "test-project";
        String url = file.toURL().toExternalForm();

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_ARCHIVE_EXTENSION, "zip" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_DIRECTORY_NAME, testProjectSubdir );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_URL, url );

        MavenProject project = new MavenProject( model );

        MockManager mgr = new MockManager();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        ProjectSourceResolver r = new DefaultProjectSourceResolver( null, null, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        File projectSourcesDir = new File( projectDir, testProjectSubdir );

        // this is part of pretending that the expander does its job...testing of which is out of scope.
        projectSourcesDir.mkdirs();

        File result = r.resolveProjectSources( project, projectDir, null, mh );

        assertNotNull( result );
        assertEquals( 0, mh.size() );
        assertEquals( projectSourcesDir, result );

        mgr.verifyAll();
    }

    public void testShouldResolveProjectSourcesEndToEndForImplicitArtifact()
        throws IOException
    {
        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        String testProjectSubdir = "test-project";

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_ARCHIVE_EXTENSION, "zip" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_DIRECTORY_NAME, testProjectSubdir );

        MavenProject project = new MavenProject( model );
        project.setRemoteArtifactRepositories( Collections.EMPTY_LIST );

        MockManager mgr = new MockManager();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        MockControl artifactCtl = MockControl.createControl( Artifact.class );
        mgr.add( artifactCtl );

        Artifact artifact = (Artifact) artifactCtl.getMock();

        artifact.getFile();
        artifactCtl.setReturnValue( file );
        artifact.getFile();
        artifactCtl.setReturnValue( file );

        MockControl localRepositoryCtl = MockControl.createControl( ArtifactRepository.class );
        mgr.add( localRepositoryCtl );

        ArtifactRepository localRepository = (ArtifactRepository) localRepositoryCtl.getMock();

        MockControl factoryCtl = MockControl.createControl( ArtifactFactory.class );
        mgr.add( factoryCtl );

        ArtifactFactory factory = (ArtifactFactory) factoryCtl.getMock();

        factory.createArtifactWithClassifier( "group", "artifact", "version", "zip",
                                              ProjectSourceResolver.DEFAULT_PROJECT_ARCHIVE_ARTIFACT_CLASSIFIER );
        factoryCtl.setReturnValue( artifact );

        MockControl resolverCtl = MockControl.createControl( ArtifactResolver.class );
        mgr.add( resolverCtl );

        ArtifactResolver resolver = (ArtifactResolver) resolverCtl.getMock();

        try
        {
            resolver.resolve( artifact, Collections.EMPTY_LIST, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( ArtifactNotFoundException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        ProjectSourceResolver r = new DefaultProjectSourceResolver( factory, resolver, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        File projectSourcesDir = new File( projectDir, testProjectSubdir );

        // this is part of pretending that the expander does its job...testing of which is out of scope.
        projectSourcesDir.mkdirs();

        File result = r.resolveProjectSources( project, projectDir, localRepository, mh );

        assertNotNull( result );
        assertEquals( 0, mh.size() );
        assertEquals( projectSourcesDir, result );

        mgr.verifyAll();
    }

    public void testShouldResolveProjectSourcesEndToEndForExplicitArtifact()
        throws IOException
    {
        File file = File.createTempFile( "project-source.", ".test.zip" );
        file.deleteOnExit();

        String testProjectSubdir = "test-project";
        String classifier = "project-sources";

        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "version" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_ARCHIVE_EXTENSION, "zip" );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_DIRECTORY_NAME, testProjectSubdir );
        model.addProperty( ProjectSourceResolver.PROJECT_SOURCE_URL, "group:artifact:version:zip:" + classifier );
        
        MavenProject project = new MavenProject( model );
        project.setRemoteArtifactRepositories( Collections.EMPTY_LIST );

        MockManager mgr = new MockManager();

        MockControl expanderCtl = MockControl.createControl( ArchiveExpander.class );
        mgr.add( expanderCtl );

        ArchiveExpander expander = (ArchiveExpander) expanderCtl.getMock();

        try
        {
            expander.expand( null );
            expanderCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        }
        catch ( ArchiveExpansionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( IOException e )
        {
            fail( "Should never happen!!" );
        }

        MockControl artifactCtl = MockControl.createControl( Artifact.class );
        mgr.add( artifactCtl );

        Artifact artifact = (Artifact) artifactCtl.getMock();

        artifact.getFile();
        artifactCtl.setReturnValue( file );
        artifact.getFile();
        artifactCtl.setReturnValue( file );

        MockControl localRepositoryCtl = MockControl.createControl( ArtifactRepository.class );
        mgr.add( localRepositoryCtl );

        ArtifactRepository localRepository = (ArtifactRepository) localRepositoryCtl.getMock();

        MockControl factoryCtl = MockControl.createControl( ArtifactFactory.class );
        mgr.add( factoryCtl );

        ArtifactFactory factory = (ArtifactFactory) factoryCtl.getMock();

        factory.createArtifactWithClassifier( "group", "artifact", "version", "zip", classifier );
        factoryCtl.setReturnValue( artifact );

        MockControl resolverCtl = MockControl.createControl( ArtifactResolver.class );
        mgr.add( resolverCtl );

        ArtifactResolver resolver = (ArtifactResolver) resolverCtl.getMock();

        try
        {
            resolver.resolve( artifact, Collections.EMPTY_LIST, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            fail( "Should never happen!!" );
        }
        catch ( ArtifactNotFoundException e )
        {
            fail( "Should never happen!!" );
        }

        mgr.replayAll();

        ProjectSourceResolver r = new DefaultProjectSourceResolver( factory, resolver, expander );

        MessageHolder mh = new DefaultMessageHolder();

        File projectDir = File.createTempFile( "project-dir.", ".test" );
        projectDir.delete();
        projectDir.mkdirs();
        dirsToDelete.add( projectDir );

        File projectSourcesDir = new File( projectDir, testProjectSubdir );

        // this is part of pretending that the expander does its job...testing of which is out of scope.
        projectSourcesDir.mkdirs();

        File result = r.resolveProjectSources( project, projectDir, localRepository, mh );

        assertNotNull( result );
        assertEquals( 0, mh.size() );
        assertEquals( projectSourcesDir, result );

        mgr.verifyAll();
    }

    private static final class TestSourceResolver
        extends DefaultProjectSourceResolver
    {
        TestSourceResolver()
        {
        }

        TestSourceResolver( ArtifactFactory factory, ArtifactResolver resolver, ArchiveExpander expander )
        {
            super( factory, resolver, expander );
        }
    }

}
