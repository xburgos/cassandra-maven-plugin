package org.apache.maven.diagrams.gui.renderers.umlClass;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationItem;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationPanel;

public class UmlClassRendererConfigurationPanel extends RendererConfigurationPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 3985633179077245840L;
    private List<UmlClassRendererConfigurationItemPanel> items;
    
    public UmlClassRendererConfigurationPanel()
    {
    }

    @Override
    public void setCurrentRendererConfiguration( RendererConfiguration configuration )
    {
        this.removeAll();
        rebuildUI( configuration );

    }

    private void rebuildUI( RendererConfiguration configuration )
    {
        setLayout( new GridBagLayout() );
        GridBagConstraints c =
            new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                                    new Insets( 2, 2, 2, 2 ), 0, 0 );
        
        items=new LinkedList<UmlClassRendererConfigurationItemPanel>();
        
        for ( RendererConfigurationItem item : configuration.getRenderConfigurationItems().values() )
        {
            UmlClassRendererConfigurationItemPanel itemPanel = new UmlClassRendererConfigurationItemPanel();
            itemPanel.setCurrentRendererConfigurationItem(  item );
            items.add( itemPanel );
            add( itemPanel, c );
            c.gridy++;
        }
    }

    @Override
    public RendererConfiguration getCurrentRendererConfiguration()
    {
        UmlClassRendererConfiguration ucrc=new UmlClassRendererConfiguration();
        ucrc.getRenderConfigurationItems().clear();
        for(UmlClassRendererConfigurationItemPanel itemPanel:items)
        {
          RendererConfigurationItem item=itemPanel.getCurrentRendererConfiguration();
          ucrc.getRenderConfigurationItems().put( item.getName(),item );   
        }
        return ucrc;
    }

}
