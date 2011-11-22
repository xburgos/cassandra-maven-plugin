package org.apache.maven.diagrams.gui.swing_helpers;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class DelegateClassRenderer implements ListCellRenderer
{

    private ListCellRenderer lcr;

    private ObjectToStringConverter<Object> objectToStringConverter;

    @SuppressWarnings("unchecked")
    public DelegateClassRenderer( ListCellRenderer a_lcr, ObjectToStringConverter<?> obToStringConverter )
    {
        lcr = a_lcr;
        objectToStringConverter=(ObjectToStringConverter<Object>) obToStringConverter;
    }

    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
                                                   boolean cellHasFocus )
    {
        return lcr.getListCellRendererComponent( list, objectToStringConverter.convert( value ), index,
                                                 isSelected, cellHasFocus );
    }
}
