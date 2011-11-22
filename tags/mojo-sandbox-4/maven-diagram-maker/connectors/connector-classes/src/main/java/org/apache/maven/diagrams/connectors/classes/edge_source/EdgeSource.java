package org.apache.maven.diagrams.connectors.classes.edge_source;

import java.util.Set;

import org.apache.maven.diagrams.connectors.classes.ClassNodesRepository;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.filter.FilterRepository;
import org.apache.maven.diagrams.connectors.classes.graph.ClassEdge;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;

public interface EdgeSource
{
    public static String ROLE = EdgeSource.class.getName();

    /**
     * Sets the main (dependent components) in one call.
     * 
     * @param a_filterRepository
     * @param a_classNodesRepository
     * @param a_configuration
     */
    public abstract void configure( FilterRepository a_filterRepository, ClassNodesRepository a_classNodesRepository,
                                    ClassesConnectorConfiguration a_configuration );

    /**
     * The method returns created edges. It can also add new nodes to "resultNodes"
     */
    public abstract Set<ClassEdge> calculateEdges( Set<ClassNode> resultNodes );

    public abstract FilterRepository getFilterRepository();

    public abstract ClassNodesRepository getClassNodesRepository();

    public abstract ClassesConnectorConfiguration getConfiguration();

}