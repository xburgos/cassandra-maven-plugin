package org.apache.maven.diagrams.gui.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.maven.diagrams.gui.controller.LayoutController;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationPanel;

public class RendererConfigurationContainerPanel extends JPanel implements ActionListener
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6689108990495767294L;

	private static final String ACTION_APPLY = "APPLY";

    private JButton applyButton;

    private RendererConfigurationPanel rendererConfigurationPanel;

    private LayoutController controller;

    public RendererConfigurationContainerPanel(LayoutController a_controller)
    {
        controller=a_controller;
        rebuildUI();
    }

    public void setRendererConfigurationPanel( RendererConfigurationPanel rendererConfigurationPanel )
    {
        this.rendererConfigurationPanel = rendererConfigurationPanel;
        removeAll();
        rebuildUI();
    }

    protected void rebuildUI()
    {
        GridBagLayout gbl = new GridBagLayout();
        setLayout( gbl );
        if (rendererConfigurationPanel!=null)
            add( new JScrollPane(rendererConfigurationPanel), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                                                                 GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0,
                                                                 0 ) );
        applyButton = new JButton( "Apply" );
        applyButton.setActionCommand( ACTION_APPLY );
        applyButton.addActionListener( this );
        add( applyButton, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                                  GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    }

    public void actionPerformed( ActionEvent e )
    {
        if ( e.getActionCommand().equals( ACTION_APPLY ) )
        {
            controller.updateRendererConfiguration( rendererConfigurationPanel.getCurrentRendererConfiguration() );
        }
    }

    public void setController( LayoutController controller )
    {
        this.controller = controller;
    }

}
