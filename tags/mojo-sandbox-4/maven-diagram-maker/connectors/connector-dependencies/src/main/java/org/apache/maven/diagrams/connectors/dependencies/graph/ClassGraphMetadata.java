package org.apache.maven.diagrams.connectors.dependencies.graph;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.graph_api.GraphMetadata;


/**
 * Graph metadata (in meaning of Graph-api)
 * 
 * The class describes properties of nodes and edges in
 * classes graph.  
 *  
 * @author Piotr Tabor
 *
 */
public class ClassGraphMetadata implements GraphMetadata
{
    private static List<String> nodePropertiesNames; 
    private static List<String> edgePropertiesNames;

    public List<String> getNodePropertiesNames()
    {
        synchronized (DependencyGraphMetadata.class )
        {
            if (nodePropertiesNames==null)
            {
                nodePropertiesNames=new LinkedList<String>();
                nodePropertiesNames.add("fullName");            
                nodePropertiesNames.add("interf");
                nodePropertiesNames.add("methods");
                nodePropertiesNames.add("fields");
                nodePropertiesNames.add("properties");
                nodePropertiesNames.add("superclassName");
                nodePropertiesNames.add("interfaceNames");
            }
            return nodePropertiesNames;
        }
    }
    
    public List<String> getEdgePropertiesNames()
    {
        synchronized (DependencyGraphMetadata.class)
        {
            if (edgePropertiesNames==null)
            {
                edgePropertiesNames=new LinkedList<String>();            
            }
            return edgePropertiesNames;    
        }        
    }


    public boolean isDirected()
    {
        return true;
    }

}
