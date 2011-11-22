package org.codehaus.mojo.tools.project.extras;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.PlexusTestCase;

public class DerivedArtifactTest extends PlexusTestCase
{
    
    private ArtifactFactory artifactFactory;
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
    }
    
    public void testShouldUseInjectedJARArtifactHandler() throws Exception
    {
        Artifact parent = artifactFactory.createProjectArtifact( "group", "artifact", "version" );
        
        ArtifactHandler handler = (ArtifactHandler) lookup( ArtifactHandler.ROLE, "jar" );
        
        DerivedArtifact artifact = new DerivedArtifact( parent, "classifier", "jar", handler );
        
        assertSame( handler, artifact.getArtifactHandler() );
    }

    public void testShouldUseInjectedWARArtifactHandler() throws Exception
    {
        Artifact parent = artifactFactory.createProjectArtifact( "group", "artifact", "version" );
        
        ArtifactHandler handler = (ArtifactHandler) lookup( ArtifactHandler.ROLE, "war" );
        
        DerivedArtifact artifact = new DerivedArtifact( parent, "classifier", "war", handler );
        
        assertSame( handler, artifact.getArtifactHandler() );
    }

}
