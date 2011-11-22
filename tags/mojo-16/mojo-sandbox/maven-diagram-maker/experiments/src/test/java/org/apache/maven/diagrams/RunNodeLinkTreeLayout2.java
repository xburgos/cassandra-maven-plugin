package org.apache.maven.diagrams;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.context.ConnectorContext;
import org.apache.maven.diagrams.connector_api.context.RunMavenConnectorContext;
import org.apache.maven.diagrams.connector_api.descriptor.ConnectorDescriptor;
import org.apache.maven.diagrams.connector_api.manager.ConnectorManager;
import org.apache.maven.diagrams.connectors.classes.ClassesConnector;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.gui.graph_adapter.GraphToPrafuseGraph;
import org.apache.maven.diagrams.gui.log.SimpleLogger;
import org.apache.maven.diagrams.prefuse.PrefuseWindow;

import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.parser.ExpressionParser;

public class RunNodeLinkTreeLayout2
{

    
    protected static ConnectorContext getContext() throws URISyntaxException
    {
        RunMavenConnectorContext context=new RunMavenConnectorContext();
        File f=new File(RunNodeLinkTreeLayout2.class.getResource("/maven_test").toURI());
        context.setBaseDir(f) ;
        context.setLogger( new SimpleLogger() );
        return context;
    }
    /**
     * @param args
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws ConnectorException
     * @throws URISyntaxException 
     */
    @SuppressWarnings( "unchecked" )
    public static void main( String[] args ) throws ClassNotFoundException, IOException, ConnectorException, URISyntaxException
    {

        ClassesConnector cc = new ClassesConnector();
        ConnectorDescriptor desc = cc.getConnectorDescriptor();
        ConnectorManager cm = new ConnectorManager();
        ClassesConnectorConfiguration conf =
            (ClassesConnectorConfiguration) cm.fromXML(
                                                        RunFruchtermanReingoldLayout2.class.getResourceAsStream( "/RunNodeLinkTreeLayout2-config.xml" ),
                                                        desc );
        
        cc.setConnectorContext( getContext() );
        
        org.apache.maven.diagrams.graph_api.Graph graphapi_g = cc.calculateGraph( conf );
        Graph g = GraphToPrafuseGraph.graph2PrefuseGraph( graphapi_g );
        System.out.println( "Nodes: " + graphapi_g.getNodes().size() );
        NodeLinkTreeLayout layout = new NodeLinkTreeLayout( "graph" );
        //layout.setOrientation( Constants.ORIENT_TOP_BOTTOM );
        Iterator<Tuple> it = g.getNodes().tuples( ExpressionParser.predicate( "name='java.lang.Object'" ) );
        Tuple t = it.next();
        Node n = (Node) g.getNode( t.getRow() );

        PrefuseWindow.show( g, layout, 0, n );

    }

}
