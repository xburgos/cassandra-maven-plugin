package org.apache.maven.diagrams.connectors.classes.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;

/**
 * The class is configuration for ClassesConnector
 * 
 * @author Piotr Tabor
 * 
 */
public class ClassesConnectorConfiguration implements ConnectorConfiguration
{
    /**
     * Should be added all classes that are ancestors of classes included into result
     */
    private Boolean fullInheritancePath = false;

    /**
     * List of includePatterns for class's names
     */
    private List<IncludeClasses> includes = new ArrayList<IncludeClasses>();

    /**
     * List of excludePatterns for class's names
     */
    private List<ExcludeClasses> excludes = new ArrayList<ExcludeClasses>();

    /**
     * Configuration for information provided in single node (about one class)
     */
    private Nodes nodes = new Nodes();

    /**
     * Configuration for information provided in edges
     */
    private List<EdgeType> edges = new ArrayList<EdgeType>();    
    /* ============================================================================= */
    
    public ClassesConnectorConfiguration()
    {
        includes.add( new IncludeClasses(".*") );
        fullInheritancePath=true;
        getEdges().add( new InheritanceEdgeType() );
        getEdges().add( new ImplementEdgeType() );
    }
    
    /* =========================== Getters and setters ============================= */
    /**
     * Gets information if should be added all classes that are ancestors of classes included into result
     */
    public Boolean getFullInheritancePaths()
    {
        return fullInheritancePath;
    }

    /**
     * Sets information if should be added all classes that are ancestors of classes included into result
     */
    public void setFullInheritancePaths( Boolean fullInheritancePath )
    {
        this.fullInheritancePath = fullInheritancePath;
    }

    /**
     * Gets list of includePatterns for class's names
     */
    public List<IncludeClasses> getIncludes()
    {
        return includes;
    }

    /**
     * Sets list of includePatterns for class's names
     */
    public void setIncludes( List<IncludeClasses> includes )
    {
        this.includes = includes;
    }

    /**
     * Gets list of includePatterns for class's names
     */
    public List<ExcludeClasses> getExcludes()
    {
        return excludes;
    }

    /**
     * Sets list of includePatterns for class's names
     */
    public void setExcludes( List<ExcludeClasses> excludes )
    {
        this.excludes = excludes;
    }

    /**
     * Gets configuration for information provided in single node (about one class)
     */
    public Nodes getNodes()
    {
        return nodes;
    }

    /**
     * Sets configuration for information provided in single node (about one class)
     */
    public void setNodes( Nodes nodes )
    {
        this.nodes = nodes;
    }

    /**
     * Gets configuration for information provided in edges
     */
    public List<EdgeType> getEdges()
    {
        return edges;
    }

    /**
     * Sets configuration for information provided in edges
     */
    public void setEdges( List<EdgeType> edges )
    {
        this.edges = edges;
    }

}
