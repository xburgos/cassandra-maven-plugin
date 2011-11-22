package org.apache.maven.diagrams.gui.layouts.forces;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfigurationPanel;

import prefuse.util.force.ForceSimulator;

public class ForcesLayoutConfigurationPanel extends AbstractLayoutConfigurationPanel<ForcesLayoutConfiguration>
    implements ChangeListener
{
    private static final long serialVersionUID = 1636416084494453583L;

    private MyJForcePanel panel;

    private ForceSimulator sim;

    public ForcesLayoutConfigurationPanel()
    {
        sim = new ForceSimulator();
        GridBagLayout gbl=new GridBagLayout();
        gbl.columnWeights=new double[]{1.0};
        this.setLayout( gbl );
        createJForcePanel();        
        this.add( panel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
    }

    private void createJForcePanel()
    {
        panel = new MyJForcePanel( sim );
        panel.setChangeListener( this );
    }

    @Override
    public ForcesLayoutConfiguration getCurrentConfiguration()
    {
        ForcesLayoutConfiguration config = new ForcesLayoutConfiguration();
        config.setInstanceOfForceSimulator( sim );
        return config;
    }

    @Override
    public void setCurrentConfiguration( ForcesLayoutConfiguration configuration )
    {
        sim = configuration.getInstanceOfForceSimulator();
        this.removeAll();
        createJForcePanel();
        this.add( panel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
    }

    public void stateChanged( ChangeEvent e )
    {
        if ( isAutoApply() )
            apply();
    }

}
