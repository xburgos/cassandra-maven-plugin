package org.apache.maven.diagrams.connectors.classes;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.manager.ConnectorManager;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;

public class ClassNodesRepositoryTest extends TestCase
{

    public void testGetClassNode() throws ConnectorException, MalformedURLException, URISyntaxException, ClassDataSourceException
    {
        ConnectorManager cm=new ConnectorManager();
        ClassesConnectorConfiguration config;
        ClassesConnector classesConnector=new ClassesConnector();
        config=(ClassesConnectorConfiguration) cm.fromXML( ClassNodesRepository.class.getResourceAsStream( "testGetClassNode-configuration.xml" ), classesConnector.getConnectorDescriptor() );
        
        //URL[] classpathItems=new RL[]{new File("/home/ptab/gsoc/Maveny_dev/plugins/maven-assembly-plugin/target/classes/").toURL()};
        URL[] classpathItems=new URL[]{ClassNodesRepository.class.getResource( "/log4j.jar" )};
        
        List<String> lista=PackageUtils.getClassNamesOnClassPathItem(classpathItems[0]);
        ClassModelsRepository classModelsRepository=new ClassModelsRepository(classpathItems);
        ClassNodesRepository classNodesRepository=new ClassNodesRepository(classModelsRepository,config);
        
        for(String s:lista)
        {
            System.out.println("-------------------------------------------------------");
            System.out.println(s);
            ClassNode classNode=classNodesRepository.getClassNode(s);
            classNode.print(System.out);
        }
        
        System.out.println("-------------------------------------------------------");        
        ClassNode classNode=classNodesRepository.getClassNode( "java.lang.Object" );
        classNode.print(System.out);
    }

}
