package org.apache.maven.diagrams.connectors.dependencies.graph;

import org.apache.maven.diagrams.graph_api.Edge;
import org.apache.maven.diagrams.graph_api.Node;

public class DependencyEdge implements Edge
{
    private ArtifactNode use;
    private ArtifactNode used;

    public DependencyEdge(ArtifactNode a_use, ArtifactNode a_used)
    {
       use=a_use;
       used=a_used;
    }
    
    public Node getEndNode()
    {
        return use;
    }

    public String getId()
    {
        return use.getId()+"-"+used.getId();
    }

    public Node getStartNode()
    {
        return used;
    }

}
