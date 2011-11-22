package org.apache.maven.diagrams.gui;

import org.apache.maven.diagrams.gui.controller.MainController;

public class StartGui
{

//    protected Model createModel()
//    {
//        return createModel( createConnectorContext() );
//    }

   

    public static void main( String[] args )
    {
        //StartGui startGui = new StartGui();
        MainController controller=new MainController();
        //controller.setModel( startGui.createModel() );
        //MainWindow window = new MainWindow( startGui.createModel() );
        controller.run();
       // Thread.sleep( 1000 );
    }
}
