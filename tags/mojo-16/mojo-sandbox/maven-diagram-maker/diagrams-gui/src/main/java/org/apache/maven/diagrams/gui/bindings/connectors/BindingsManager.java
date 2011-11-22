package org.apache.maven.diagrams.gui.bindings.connectors;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.diagrams.connector_api.DiagramConnector;

import com.thoughtworks.xstream.XStream;

public class BindingsManager
{
    /* Map from graph type into resolved binding */
    public Map<String, ResolvedBinding<DiagramConnector>> bindingsMapByGraphType =
        new HashMap<String, ResolvedBinding<DiagramConnector>>();

    public Map<String, ResolvedBinding<DiagramConnector>> bindingsMapByConnectorName =
        new HashMap<String, ResolvedBinding<DiagramConnector>>();

    public void loadAndResolveBindings( InputStream is )
    {
        XStream xstream = new XStream();
        xstream.aliasType( "diagrams-gui", DiagramGUIMapping.class );
        xstream.aliasType( "binding", Binding.class );

        // List<Binding> bindings_list = new LinkedList<Binding>();
        DiagramGUIMapping diagramGUIMapping = new DiagramGUIMapping();
        xstream.fromXML( is, diagramGUIMapping );

        List<ResolvedBinding<DiagramConnector>> resolvedBindings = resolveBindings( diagramGUIMapping.getBindings() );

        for ( ResolvedBinding<DiagramConnector> resolvedBinding : resolvedBindings )
        {
            bindingsMapByGraphType.put( resolvedBinding.getName(), resolvedBinding );
            bindingsMapByConnectorName.put( resolvedBinding.getConnectorClass().getName(), resolvedBinding );
        }
    }

    private List<ResolvedBinding<DiagramConnector>> resolveBindings( List<Binding> bindings_list )
    {
        List<ResolvedBinding<DiagramConnector>> res = new LinkedList<ResolvedBinding<DiagramConnector>>();
        for ( Binding binding : bindings_list )
        {

            try
            {
                res.add( binding.resolveBinding() );
            }
            catch ( ResolveBindingException e )
            {
                // TODO: Logger
                e.printStackTrace( System.err );
            }

        }
        return res;
    }

    public Collection<ResolvedBinding<DiagramConnector>> getResolvedBindings()
    {
        return bindingsMapByConnectorName.values();
    }

    public ResolvedBinding<DiagramConnector> getBindingsForConnector( Class<? extends DiagramConnector> class_ )
    {
        return bindingsMapByConnectorName.get( class_.getName() );
    }

    public ResolvedBinding<DiagramConnector> getBindingsForConnector( DiagramConnector con )
    {
        return getBindingsForConnector( con.getClass() );
    }

}
