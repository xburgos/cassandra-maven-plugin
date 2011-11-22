package org.codehaus.mojo.tools.project.extras;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.mojo.tools.project.extras.DependencyPathResolver;
import org.codehaus.mojo.tools.project.extras.PrefixPropertyPathResolver;
import org.codehaus.plexus.PlexusTestCase;


public class DependencyPathResolverTest
    extends PlexusTestCase
{
    
    public void testShouldResolveSinglePathOfStatement() throws Exception
    {
        File tmpFile = new File( "/tmp" );
        
        ArtifactFactory factory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
        Artifact artifact = factory.createArtifact("groupId", "artifactId", "version", "compile", "jar");
        artifact.setFile( tmpFile );
        
        Log log = new DefaultLog( getContainer().getLogger() );
        Collection<Artifact> artifacts = Collections.singletonList( artifact );
        
        String firstPart = "This is the path: ";
        String lastPart = " that we're looking for...";
        
        String src = firstPart + "@pathOf(groupId:artifactId)@" + lastPart;
        String check = firstPart + tmpFile.getCanonicalPath() + lastPart;
        
        MavenProjectBuilder projectBuilder = new DummyProjectBuilder( artifact );
        
        List<ArtifactRepository> remoteRepos = Collections.emptyList();
        
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) lookup( ArtifactRepositoryLayout.ROLE, "default" );
        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
        
        ArtifactRepository localRepo = new DefaultArtifactRepository( "local", "file:///tmp", layout );
        
        PrefixPropertyPathResolver pathResolver = new PrefixPropertyPathResolver( projectBuilder, remoteRepos, localRepo, artifactFactory, log );
        
        DependencyPathResolver resolver = new DependencyPathResolver( artifacts, pathResolver, log );
        
        assertEquals( check, resolver.resolveDependencyPaths( src ) );
    }

    public void testShouldResolveTwoPathOfStatements() throws Exception
    {
        File tmpFile = new File( "/tmp" );
        File tmpFile2 = new File( "/tmp2" );
        
        ArtifactFactory factory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
        Artifact artifact = factory.createArtifact("groupId", "artifactId", "version", "compile", "jar");
        artifact.setFile( tmpFile );
        
        Artifact artifact2 = factory.createArtifact("groupId2", "artifactId2", "version2", "compile", "jar");
        artifact2.setFile( tmpFile2 );
        
        Log log = new DefaultLog( getContainer().getLogger() );
        
        Collection<Artifact> artifacts = new ArrayList<Artifact>();
        artifacts.add( artifact );
        artifacts.add( artifact2 );
        
        String firstPart = "This is the path: --path=";
        String middlePart = " --path2=";
        String lastPart = " that we're looking for...";
        
        String src = firstPart + "@pathOf(groupId:artifactId)@" + middlePart + "@pathOf(groupId2:artifactId2)@" + lastPart;
        String check = firstPart + tmpFile.getCanonicalPath() + middlePart + tmpFile2.getCanonicalPath() + lastPart;
        
        MavenProjectBuilder projectBuilder = new DummyProjectBuilder( artifacts );
        
        List<ArtifactRepository> remoteRepos = Collections.emptyList();
        
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) lookup( ArtifactRepositoryLayout.ROLE, "default" );
        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
        
        ArtifactRepository localRepo = new DefaultArtifactRepository( "local", "file:///tmp", layout );
        
        PrefixPropertyPathResolver pathResolver = new PrefixPropertyPathResolver( projectBuilder, remoteRepos, localRepo, artifactFactory, log );
        
        DependencyPathResolver resolver = new DependencyPathResolver( artifacts, pathResolver, log );
        
        assertEquals( check, resolver.resolveDependencyPaths( src ) );
    }
    
    private static final class DummyProjectBuilder extends DefaultMavenProjectBuilder
    {
        private Map<String, File> artifactMap = new HashMap<String, File>();
        
        DummyProjectBuilder( Artifact artifact )
        {
            this( Collections.singleton( artifact ) );
        }
        
        DummyProjectBuilder( Collection<Artifact> artifacts )
        {
            for ( Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = it.next();
                artifactMap.put( ArtifactUtils.versionlessKey( artifact ), artifact.getFile() );
            }
        }

        public MavenProject buildFromRepository( Artifact artifact, List remoteArtifactRepositories, ArtifactRepository localRepository, boolean allowStubModel )
            throws ProjectBuildingException
        {
            return buildFromRepository( artifact, remoteArtifactRepositories, localRepository );
        }

        public MavenProject buildFromRepository( Artifact artifact, List remoteArtifactRepositories, ArtifactRepository localRepository )
            throws ProjectBuildingException
        {
            File file = (File) artifactMap.get( ArtifactUtils.versionlessKey( artifact ) );
            
            Model model = new Model();
            model.setGroupId( artifact.getGroupId() );
            model.setArtifactId( artifact.getArtifactId() );
            model.setVersion( artifact.getVersion() );
            
            Properties props = new Properties();
            props.setProperty( "prefix", file.getPath() );
            
            model.setProperties( props );
            
            return new MavenProject( model );
        }
        
        
    }

}
