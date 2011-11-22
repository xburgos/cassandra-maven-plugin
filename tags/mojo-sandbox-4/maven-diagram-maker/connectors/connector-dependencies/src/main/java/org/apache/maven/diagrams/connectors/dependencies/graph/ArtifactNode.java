package org.apache.maven.diagrams.connectors.dependencies.graph;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.diagrams.graph_api.Node;

public class ArtifactNode implements Node
{
    public Artifact artifact;

    public String getId()
    {
        return artifact.getDependencyConflictId();
    }
    
    public Artifact getArtifact()
    {
        return artifact;
    }
    
    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

}
