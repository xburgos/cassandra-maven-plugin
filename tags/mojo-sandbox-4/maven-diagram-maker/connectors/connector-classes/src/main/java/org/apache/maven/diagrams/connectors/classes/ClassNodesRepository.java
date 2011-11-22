package org.apache.maven.diagrams.connectors.classes;

import java.util.Map;

import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;

public interface ClassNodesRepository
{
    public static String ROLE = ClassNodesRepository.class.getName();

    /**
     * The method checks if the classNode for given className ((fully qualified, dot separated) already exists (and
     * returns it). If not - the method calculates it and stores in the cache the result.
     */
    public abstract ClassNode getClassNode( String className ) throws ClassDataSourceException;

    /**
     * Returns the current state of the cache (as a map from ClassName to ClassNode)
     * 
     * @return
     */
    public abstract Map<String, ClassNode> getMap();

}