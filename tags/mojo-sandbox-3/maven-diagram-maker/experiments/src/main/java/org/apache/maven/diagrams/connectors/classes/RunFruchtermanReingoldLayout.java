package org.apache.maven.diagrams.connectors.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.maven.diagrams.prefuse.PrefuseWindow;

import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.data.Graph;

public class RunFruchtermanReingoldLayout
{

    /**
     * @param args
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException {
            
            InputStream is=PackageUtilsTest.class.getResourceAsStream( "/log4j.jar");
            
            if (is==null)
                System.err.println("Couldn't find source '*.jar' file");
            Graph g=PackageUtilsTest.createGraph(is  ,new URL[]{PackageUtilsTest.class.getResource( "/log4j.jar" )} );
            PrefuseWindow.show(g,new FruchtermanReingoldLayout("graph"),0);                    
    }   

}
