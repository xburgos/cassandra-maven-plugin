package org.apache.maven.diagrams.gui.sidebar;

import java.awt.BorderLayout;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.gui.bindings.connectors.ResolvedBinding;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.connector.classes.ClassesConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.controller.MainController;
import org.apache.maven.diagrams.gui.model.Model;

public class SidebarPanel extends JPanel
{
    private MainController controller;;

    private List<JPanel> panels = new LinkedList<JPanel>();

    private JTabbedPane tabs;

    public SidebarPanel( MainController a_controller )
    {
        super();
        controller = a_controller;
        this.setLayout( new BorderLayout() );

        tabs = new JTabbedPane();

        // tabs.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
        // tabs.setTabPlacement( JTabbedPane.LEFT );
        createTabs();

        this.add( tabs );
    }

    @SuppressWarnings( "unchecked" )
    public void createTabs()
    {        
        for(JPanel p:controller.calculatePanelsForSideBar())
        {
            panels.add( p );
            tabs.insertTab( p.getName(), null, p, p.getName(), 0 );
        }
        
    }
}
