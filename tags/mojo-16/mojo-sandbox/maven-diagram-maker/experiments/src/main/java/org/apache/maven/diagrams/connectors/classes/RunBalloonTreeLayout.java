package org.apache.maven.diagrams.connectors.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.maven.diagrams.prefuse.PrefuseWindow;

import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.data.Graph;

public class RunBalloonTreeLayout
{

    /**
     * @param args
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException {
            
            InputStream is=PackageUtilsOldTest.class.getResourceAsStream( "/log4j.jar");
            
            if (is==null)
                System.err.println("Couldn't find source '*.jar' file");
            Graph g=PackageUtilsOldTest.createGraph(is  ,new URL[]{PackageUtilsOldTest.class.getResource( "/log4j.jar" )} );
            PrefuseWindow.show(g.getSpanningTree( g.getNode( 0 ) ),new BalloonTreeLayout("graph"),0);
    
            
            
    }   

}
