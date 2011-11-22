package org.apache.maven.diagrams.gui.connector.classes;

import java.awt.BorderLayout;

import javax.swing.JTextField;

import org.apache.maven.diagrams.connectors.classes.config.IncludeClasses;
import org.apache.maven.diagrams.gui.swing_helpers.ObjectEdititingPanel;

public class IncludeClassesEditingPanel extends ObjectEdititingPanel<IncludeClasses>
{
    /**
     * 
     */
    private static final long serialVersionUID = 4081543812180184125L;
    private JTextField textField;
    
    public IncludeClassesEditingPanel()
    {
        textField=new JTextField();
        setLayout( new BorderLayout() );
        add(textField);
    }

    @Override
    public IncludeClasses getObject()
    {
        return new IncludeClasses(textField.getText());
    }

    @Override
    public void setObject( IncludeClasses state )
    {
        textField.setText( state.getPattern() );
    }

}
