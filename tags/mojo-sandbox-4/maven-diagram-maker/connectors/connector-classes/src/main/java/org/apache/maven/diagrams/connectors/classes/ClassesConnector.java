package org.apache.maven.diagrams.connectors.classes;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.connector_api.DynamicDiagramConnector;
import org.apache.maven.diagrams.connector_api.descriptor.ConnectorDescriptor;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.config.EdgeType;
import org.apache.maven.diagrams.connectors.classes.config.ExcludeClasses;
import org.apache.maven.diagrams.connectors.classes.config.IncludeClasses;
import org.apache.maven.diagrams.connectors.classes.edge_source.EdgeSource;
import org.apache.maven.diagrams.connectors.classes.filter.ClassNamesFilter;
import org.apache.maven.diagrams.connectors.classes.filter.ExcludePattern;
import org.apache.maven.diagrams.connectors.classes.filter.FilterRepository;
import org.apache.maven.diagrams.connectors.classes.filter.IncludePattern;
import org.apache.maven.diagrams.connectors.classes.graph.ClassEdge;
import org.apache.maven.diagrams.connectors.classes.graph.ClassGraphMetadata;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.graph_api.Graph;
import org.apache.maven.diagrams.graph_api.impl.GraphImpl;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * This class is main class of the Classes connector.
 * 
 * @author Piotr Tabor
 * 
 */
public class ClassesConnector extends AbstractLogEnabled implements DiagramConnector
{
    private ConnectorDescriptor connectorDescriptor;

    private Map<String, EdgeSource> edgeSources;

    private MavenProject mavenProject;

    public DynamicDiagramConnector getDynamicDiagramConnector()
    {
        throw new IllegalStateException( "ClassesConnector doesn't suppert dynamic diagram connector" );
    }

    public Graph calculateGraph( ConnectorConfiguration configuration ) throws ConnectorException
    {
        /* Build classpath for all available classes (from maven dependencies list) */
        URL[] classpathItems =
            getClassPathFromConfiguration( (ClassesConnectorConfiguration) configuration, mavenProject );

        /* Create (helping) objects */
        ClassModelsRepository classModelsRepository = new ClassModelsRepository( classpathItems );
        ClassNodesRepository classNodesRepository =
            new DefaultClassNodesRepository( classModelsRepository, (ClassesConnectorConfiguration) configuration );
        ClassNamesFilter classNamesFilter =
            createClassNamesFilterFromConfiguration( (ClassesConnectorConfiguration) configuration );
        FilterRepository filterRepository = new FilterRepository( classNamesFilter );

        /* Find all available classes's names */
        List<String> classesOnClasspath = calculateClassesOnClasspath( classpathItems );

        /* Do the "real" stuff of creating graph */
        return calculateGraph( (ClassesConnectorConfiguration) configuration, classesOnClasspath, filterRepository,
                               classNodesRepository );
    }

    /**
     * Calculates graph using provided "helping" objects.
     * 
     * @param configuration
     * @param classesOnClasspath
     * @param filterRepository
     * @param classNodesRepository
     * @return
     * @throws ConnectorException
     */
    private Graph calculateGraph( ClassesConnectorConfiguration configuration, List<String> classesOnClasspath,
                                  FilterRepository filterRepository, ClassNodesRepository classNodesRepository )
        throws ConnectorException
    {
        /* ----------------------- creating nodes ---------------------- */
        Set<ClassNode> resultNodes = new HashSet<ClassNode>();
        /* For every not filtered class - obtains information about it and store into resultNodes */
        for ( String className : classesOnClasspath )
        {
            if ( !filterRepository.getStatus( className ).toSkip() )
            {
                try
                {
                    resultNodes.add( classNodesRepository.getClassNode( className ) );
                }
                catch ( ClassDataSourceException e )
                {
                    getLogger().warn( "Cannot get full informations for " + className, e );
                }
            }
        }

        /* ----------------------- creating edges ---------------------- */
        Set<ClassEdge> resultEdges = new HashSet<ClassEdge>();
        /* For every available (configured) edges source - calculate edges and add them to the resultEdge */
        for ( EdgeType edgeType : configuration.getEdges() )
        {
            EdgeSource edgeSource = getEdgeSourceByEdgeType( edgeType );
            if ( edgeSource == null )
            {
                throw new ConnectorException( "Edge source :" + edgeType.getClass().getSimpleName() + " does not exist" );
            }
            edgeSource.configure( filterRepository, classNodesRepository, configuration );
            resultEdges.addAll( edgeSource.calculateEdges( resultNodes ) );
        }

        /* Combine the resultNodes and resultEdges into the graph */
        GraphImpl graph = new GraphImpl( new ClassGraphMetadata() );
        graph.addNodes( resultNodes );
        graph.addEdges( resultEdges );
        return graph;
    }

    private EdgeSource getEdgeSourceByEdgeType( EdgeType edgeType )
    {
        return edgeSources.get( edgeType.getClass().getSimpleName() );
    }

    /** Returns all fully-dot-qualified classes's and interfaces's names that are available in the given classpath */
    private List<String> calculateClassesOnClasspath( URL[] classpathItems )
    {
        List<String> classesOnClasspath = new LinkedList<String>();
        for ( URL url : classpathItems )
        {
            try
            {
                classesOnClasspath.addAll( PackageUtils.getClassNamesOnClassPathItem( url ) );
            }
            catch ( URISyntaxException e )
            {
                if ( getLogger() != null )
                    getLogger().warn( e.getMessage() + "- skipping", e );
            }
        }
        return classesOnClasspath;
    }

    /** Creates (using the configuration) the filter (to classificate the classes to add or not to add to the graph) */
    private ClassNamesFilter createClassNamesFilterFromConfiguration( ClassesConnectorConfiguration configuration )
    {
        ClassNamesFilter result = new ClassNamesFilter();

        List<IncludePattern> includes = new LinkedList<IncludePattern>();
        for ( IncludeClasses include : configuration.getIncludes() )
        {
            includes.add( new IncludePattern( include.getPattern() ) );
        }

        List<ExcludePattern> excludes = new LinkedList<ExcludePattern>();
        for ( ExcludeClasses exclude : configuration.getExcludes() )
        {
            excludes.add( new ExcludePattern( exclude.getPattern(), exclude.getKeepEdges() == null ? false
                            : exclude.getKeepEdges() ) );
        }
        result.setIncludes( includes );
        result.setExcludes( excludes );
        return result;
    }

    /**
     * Gets classpath from the currentContext's mavenProject. It goes through all dependencies (all scopes) and puts
     * each dependency into the classpath. The project itself's source is also added.
     */
    private URL[] getClassPathFromConfiguration( ClassesConnectorConfiguration configuration, MavenProject mavenProject )
        throws ConnectorException
    {
        ArrayList<URL> result = new ArrayList<URL>();
        for ( Object artifact : mavenProject.getArtifactMap().values() )
        {
            try
            {
                result.add( ( (Artifact) artifact ).getFile().toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
        }
        try
        {
            result.add( mavenProject.getArtifact().getFile().toURI().toURL() );
        }
        catch ( MalformedURLException e )
        {
            throw new ConnectorException( e );
        }

        /* Translates list into arrays */
        URL[] a = new URL[result.size()];
        int i = 0;
        for ( URL u : result )
        {
            a[i++] = u;
        }
        return a;
    }

    public ConnectorDescriptor getConnectorDescriptor() throws ConnectorException
    {
        return connectorDescriptor;
    }

    public void setMavenProject( MavenProject mavenProject )
    {
        this.mavenProject = mavenProject;
    }
}