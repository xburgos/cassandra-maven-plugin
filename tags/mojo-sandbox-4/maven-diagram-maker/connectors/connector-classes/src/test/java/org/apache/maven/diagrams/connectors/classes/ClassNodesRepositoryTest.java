package org.apache.maven.diagrams.connectors.classes;

import java.net.URL;
import java.util.List;

import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.connector_api.manager.ConnectorManager;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.codehaus.plexus.PlexusTestCase;

public class ClassNodesRepositoryTest extends PlexusTestCase
{

    public void testGetClassNode()
        throws Exception
    {
        ConnectorManager cm = new ConnectorManager();
        ClassesConnector classesConnector = (ClassesConnector) lookup( DiagramConnector.class, "connector-classes" );
        ClassesConnectorConfiguration config =
            (ClassesConnectorConfiguration) cm.fromXML(
                                                        DefaultClassNodesRepository.class.getResourceAsStream( "testGetClassNode-configuration.xml" ),
                                                        classesConnector.getConnectorDescriptor() );

        // URL[] classpathItems=new RL[]{new
        // File("/home/ptab/gsoc/Maveny_dev/plugins/maven-assembly-plugin/target/classes/").toURL()};
        URL[] classpathItems = new URL[] { DefaultClassNodesRepository.class.getResource( "/log4j.jar" ) };

        List<String> lista = PackageUtils.getClassNamesOnClassPathItem( classpathItems[0] );
        ClassModelsRepository classModelsRepository = new ClassModelsRepository( classpathItems );
        ClassNodesRepository classNodesRepository = new DefaultClassNodesRepository( classModelsRepository, config );

        for ( String s : lista )
        {
            System.out.println( "-------------------------------------------------------" );
            System.out.println( s );
            ClassNode classNode = classNodesRepository.getClassNode( s );
            classNode.print( System.out );
        }

        System.out.println( "-------------------------------------------------------" );
        ClassNode classNode = classNodesRepository.getClassNode( "java.lang.Object" );
        classNode.print( System.out );
    }

}
