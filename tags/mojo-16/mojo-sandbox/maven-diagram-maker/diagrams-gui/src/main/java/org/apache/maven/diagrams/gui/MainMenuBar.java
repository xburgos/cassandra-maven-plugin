package org.apache.maven.diagrams.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.maven.diagrams.gui.controller.MainController;

import prefuse.Display;
import prefuse.util.display.ExportDisplayAction;

public class MainMenuBar extends JMenuBar
{
    /**
     * 
     */
    private static final long serialVersionUID = -4770551478277405323L;

    private JMenu file;

    private MainController controller;

    public MainMenuBar( MainController a_controller )
    {
        controller = a_controller;

        /*----------------- FILE -------------------- */
        file = new JMenu( "File" );
        file.setMnemonic( 'f' );

        JMenuItem item = new JMenuItem( "New diagram..." );
        file.add( item );

        file.addSeparator();
        file.add( new JMenuItem( "Save view configuration..." ) );
        file.add( new JMenuItem( "Restore view configuration..." ) );
        file.addSeparator();

        item = new JMenuItem( "Export to graphical file..." );
        item.setMnemonic( 'x' );

        item.addActionListener( new ExportToGraphicalFileAction() );

        file.add( item );

        file.addSeparator();

        item = new JMenuItem( "Exit" );
        item.setMnemonic( 'e' );
        item.addActionListener( new ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                System.exit( 0 );
            };
        } );
        file.add( item );

        this.add( file );
    }

    private class ExportToGraphicalFileAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            ExportDisplayAction exportDisplayAction =
                new ExportDisplayAction( controller.getVisualization().getDisplay( 0 ) );
            exportDisplayAction.actionPerformed( e );
        }
    }
}
