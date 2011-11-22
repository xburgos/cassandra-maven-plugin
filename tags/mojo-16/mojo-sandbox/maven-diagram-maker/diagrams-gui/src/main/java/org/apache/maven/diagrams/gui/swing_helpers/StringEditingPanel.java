package org.apache.maven.diagrams.gui.swing_helpers;

import java.awt.BorderLayout;
import javax.swing.JTextField;

public class StringEditingPanel extends ObjectEdititingPanel<String>
{
    /**
     * 
     */
    private static final long serialVersionUID = -6162061251688453299L;
    private JTextField textField;
    
    public StringEditingPanel()
    {
        textField=new JTextField();
        setLayout( new BorderLayout() );
        add(textField);
    }

    @Override
    public String getObject()
    {
        return textField.getText();
    }

    @Override
    public void setObject( String state )
    {
        textField.setText( state );
    }

}
