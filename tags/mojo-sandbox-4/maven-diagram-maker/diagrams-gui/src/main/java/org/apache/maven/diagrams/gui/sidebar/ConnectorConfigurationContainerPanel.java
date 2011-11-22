package org.apache.maven.diagrams.gui.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.controller.MainController;

public class ConnectorConfigurationContainerPanel extends JPanel implements ActionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -7589049137585188595L;
    private static final String APPLY="APPLY";
    
    private AbstractConnectorConfigurationPanel<? extends ConnectorConfiguration> panel;
    private JButton applyButton;

    
    private MainController controller;
    
    public ConnectorConfigurationContainerPanel(MainController a_controller,AbstractConnectorConfigurationPanel<? extends ConnectorConfiguration> a_panel)
    {
        panel=a_panel;
        controller=a_controller;
        
        GridBagLayout gridBagLayout=new GridBagLayout();
        gridBagLayout.rowWeights=new double[]{1.0,0.0};
        this.setLayout(gridBagLayout);
        
        
        GridBagConstraints c=new GridBagConstraints();
        c.gridheight=1;
        c.gridwidth=1;
        c.gridx=0;
        c.gridy=0;
        c.fill=GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        
    
        this.add(new JScrollPane(panel),c);
        
        c.gridy++;
        c.fill=GridBagConstraints.NONE;
        c.weightx=0.0;
        c.weighty=0.0;
        applyButton=new JButton("Apply");
        applyButton.addActionListener( this );
        applyButton.setActionCommand( APPLY );
        
        
        this.add(applyButton,c);
    }
    
    public void actionPerformed( ActionEvent e )
    {
        if (e.getActionCommand().equals( APPLY ))
        {
            controller.updateConnectorConfiguration(panel.getCurrentConfiguration(),true);
//            try
//            {
                //model.getConnector().setConnectorContext( model.getConnectorContext());               
     
                //model.getConnector().calculateGraph( model.getConnectorConfiguration() );
           // }
//            catch ( ConnectorException e1 )
//            {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
        }        
    }
    
    
    
    
}
