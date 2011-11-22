package org.apache.maven.diagrams.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.apache.maven.diagrams.gui.controller.MainController;
import org.apache.maven.diagrams.gui.sidebar.SidebarPanel;

public class MainWindow
{
    private Component statusPanel;

    private SidebarPanel settingsPanel;

    private Component diagramPanel;

    JSplitPane rightPanel,mainSplitPanel;

    private MainController controller;
    
    

    /*------------------------- Construction ---------------*/
    public MainWindow( MainController a_controller )
    {
        controller = a_controller;

        createStatusPanel();
        createSettingsPanel();
        createDiagramPanel();

        rightPanel = new JSplitPane( JSplitPane.VERTICAL_SPLIT, diagramPanel, statusPanel );
        rightPanel.setOneTouchExpandable( true );
        rightPanel.setDividerLocation( 500 );
        rightPanel.setResizeWeight( 1.0 );

        mainSplitPanel = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, settingsPanel, rightPanel );
        mainSplitPanel.setOneTouchExpandable( true );
        mainSplitPanel.setDividerLocation( 270 );
        mainSplitPanel.setPreferredSize( new Dimension( 780, 580 ) );
        

    }

    private void createDiagramPanel()
    {
        try
        {
          //  diagramPanel=new Display();
            // ClassesConnector cc=new ClassesConnector();
            // ConnectorDescriptor desc=cc.getConnectorDescriptor();
            // ConnectorManager cm=new ConnectorManager();
            // ClassesConnectorConfiguration conf=(ClassesConnectorConfiguration) cm.fromXML(
            // RunFruchtermanReingoldLayout2.class.getResourceAsStream("/RunNodeLinkTreeLayout2-config.xml" ), desc );
            // cc.setConnectorContext( model.getConnectorContext() );;
//
//            if ( ( model.getConnectorContext() != null ) && ( model.getConnectorContext() != null )
//                            && ( model.getConnector() != null ) && ( model.getConnectorConfiguration() != null ) )
//
//            {
//                DiagramConnector diagramConnector = model.getConnector();
//
//                org.apache.maven.diagrams.graph_api.Graph graphapi_g =
//                    diagramConnector.calculateGraph( model.getConnectorConfiguration() );
//                Graph g = GraphToPrafuseGraph.graph2PrefuseGraph( graphapi_g );
//                diagramPanel =
//                    PrefuseWindow.prepareDisplay( g, new ForceDirectedLayout( "graph" ), ForceDirectedLayout.INFINITY,
//                                                  null );
//            }
//            else
  //          {
                diagramPanel = new JLabel( "The connector is not selected nor configurated" );
    //        }

        }
        catch ( Exception e )
        {
            // TODO: handle exception
        }
    }

    private void createSettingsPanel()
    {
        settingsPanel = new SidebarPanel(controller);
    }

    private void createStatusPanel()
    {
        statusPanel = new JLabel( "status" );
    }

    public Component getMainPanel()
    {
        return mainSplitPanel;
    }

    /*-------------------------------------------------*/

    private void createAndShowGUI()
    {

        // Create and set up the window.
        JFrame frame = new JFrame( "Maven diagram GUI" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().add( getMainPanel() );
        frame.setJMenuBar( new MainMenuBar(controller));

        // Display the window.
        frame.pack();
        frame.setVisible( true );
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                closed();
              }
            });
    }

    public void run()
    {
        System.out.println("Main window - run");
        try
        {
            javax.swing.SwingUtilities.invokeAndWait( new Runnable()
            {
                public void run()
                {
                    System.out.println("Create and show GUI");
                    createAndShowGUI();
                }
            } );
        }
        catch ( InterruptedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( InvocationTargetException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setDiagramPanel(Component prepareDisplay )
    {
        diagramPanel=prepareDisplay;
        rightPanel.setLeftComponent( diagramPanel );
    }
    
    public Component getDiagramPanel()
    {
        return diagramPanel;
    }
     
      private synchronized void closed() {
        notify();
      }
     
      /**
       * MUST not be called on the event dispatch thread!!!
       */
      public synchronized void waitUntilClosed() {
        try {
          wait();
        } catch (InterruptedException ex) {
          // stop waiting on interruption
        }
      }

}
