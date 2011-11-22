package org.apache.maven.diagrams;

import java.io.IOException;
import java.util.Iterator;

import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.descriptor.ConnectorDescriptor;
import org.apache.maven.diagrams.connector_api.manager.ConnectorManager;
import org.apache.maven.diagrams.connectors.classes.ClassesConnector;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.gui.graph_adapter.GraphToPrafuseGraph;
import org.apache.maven.diagrams.prefuse.PrefuseWindow;

import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.parser.ExpressionParser;

public class RunRadialLayout2
{

    /**
     * @param args
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @throws ConnectorException 
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws ClassNotFoundException, IOException, ConnectorException {
        ClassesConnector cc = new ClassesConnector();
        ConnectorDescriptor desc = cc.getConnectorDescriptor();
        ConnectorManager cm = new ConnectorManager();
        ClassesConnectorConfiguration conf =
            (ClassesConnectorConfiguration) cm.fromXML(
                                                        RunRadialLayout2.class.getResourceAsStream( "/RunNodeLinkTreeLayout2-config.xml" ),
                                                        desc );

        org.apache.maven.diagrams.graph_api.Graph graphapi_g = cc.calculateGraph( conf );
        Graph g = GraphToPrafuseGraph.graph2PrefuseGraph( graphapi_g );
        System.out.println( "Nodes: " + graphapi_g.getNodes().size() );
        RadialTreeLayout layout = new RadialTreeLayout( "graph" );
        //layout.setOrientation( Constants.ORIENT_TOP_BOTTOM );
        Iterator<Tuple> it = g.getNodes().tuples( ExpressionParser.predicate( "name='java.lang.Object'" ) );
        Tuple t = it.next();
        Node n = (Node) g.getNode( t.getRow() );

        PrefuseWindow.show( g, layout, RadialTreeLayout.INFINITY, n );
            
    }   

}
