package org.apache.maven.diagrams.gui.connector.classes;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.apache.maven.diagrams.connectors.classes.config.ExcludeClasses;
import org.apache.maven.diagrams.gui.swing_helpers.ObjectEdititingPanel;

public class ExcludeClassesEditingPanel extends ObjectEdititingPanel<ExcludeClasses>
{
    /**
     * 
     */
    private static final long serialVersionUID = 4081543812180184125L;
    private JTextField textField;
    private JCheckBox  keepEdges;
    
    public ExcludeClassesEditingPanel()
    {
        textField=new JTextField();
        setLayout( new GridLayout(2,1) );
        add(textField);
        keepEdges=new JCheckBox("Keep edges");
        add(keepEdges);
    }

    @Override
    public ExcludeClasses getObject()
    {
        return new ExcludeClasses(textField.getText(), keepEdges.isSelected());
    }

    @Override
    public void setObject( ExcludeClasses state )
    {
        keepEdges.setSelected( state.getKeepEdges() );
        textField.setText( state.getPattern() );
    }
    
    

}
