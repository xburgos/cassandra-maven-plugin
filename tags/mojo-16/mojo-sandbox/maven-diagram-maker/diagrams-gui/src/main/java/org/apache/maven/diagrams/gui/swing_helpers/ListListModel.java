package org.apache.maven.diagrams.gui.swing_helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListListModel<Type> implements ListModel
{
    private List<Type> items;

    private Set<ListDataListener> listDataListeners = new HashSet<ListDataListener>();

    public ListListModel( List<Type> a_items )
    {
        items = a_items;
    }

    public void addListDataListener( ListDataListener l )
    {
        listDataListeners.add( l );
    }

    public void removeListDataListener( ListDataListener l )
    {
        listDataListeners.remove( l );
    }

    public void notify( ListDataEvent e )
    {
        for ( ListDataListener l : listDataListeners )
        {
            l.contentsChanged( e );
        }
    }

    public Object getElementAt( int index )
    {
        return items.get( index );
    }

    public Object setElementAt( int index )
    {
        return items.get( index );
    }

    public int getSize()
    {
        if ( items == null )
            return 0;
        return items.size();
    }

}
