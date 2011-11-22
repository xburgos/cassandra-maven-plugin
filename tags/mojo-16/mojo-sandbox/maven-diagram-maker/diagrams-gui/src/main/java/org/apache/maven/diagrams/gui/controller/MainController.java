package org.apache.maven.diagrams.gui.controller;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.maven.diagrams.connector_api.ConnectorConfiguration;
import org.apache.maven.diagrams.connector_api.ConnectorException;
import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.connector_api.context.ConnectorContext;
import org.apache.maven.diagrams.connector_api.context.RunMavenConnectorContext;
import org.apache.maven.diagrams.connectors.classes.ClassesConnector;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.graph_api.Graph;
import org.apache.maven.diagrams.gui.MainWindow;
import org.apache.maven.diagrams.gui.bindings.connectors.BindingsManager;
import org.apache.maven.diagrams.gui.bindings.connectors.ResolvedBinding;
import org.apache.maven.diagrams.gui.connector.AbstractConnectorConfigurationPanel;
import org.apache.maven.diagrams.gui.graph_adapter.GraphToPrafuseGraph;
import org.apache.maven.diagrams.gui.model.Model;
import org.apache.maven.diagrams.gui.renderers.ConfigurableRenderer;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererConfigurationPanel;
import org.apache.maven.diagrams.gui.renderers.umlClass.renderer.ClassesUMLRenderer;
import org.apache.maven.diagrams.gui.sidebar.ConnectorConfigurationContainerPanel;
import org.apache.maven.diagrams.gui.sidebar.LayoutConfigurationContainerPanel;
import org.apache.maven.diagrams.gui.sidebar.RendererConfigurationContainerPanel;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class MainController
{
    private Model model;

    private MainWindow view;

    private BindingsManager bindingsManager;

    private LayoutController layoutController;

    private ConfigurableRenderer renderer;

    public MainController()
    {
        this( createConnectorContext() );
    }

    public MainController( ConnectorContext connectorContext )
    {
        model = createModel( connectorContext );
        layoutController = new LayoutController( this );
        view = new MainWindow( this );

    }

    public Model getModel()
    {
        return model;
    }

    public void setModel( Model model )
    {
        this.model = model;
    }

    public MainWindow getView()
    {
        return view;
    }

    public void setView( MainWindow view )
    {
        this.view = view;
    }

    public BindingsManager getBindingsManager()
    {
        if ( bindingsManager == null )
        {
            bindingsManager = new BindingsManager();
            InputStream is = this.getClass().getResourceAsStream( "/diagrams-gui-bindings.xml" );
            if ( is == null )
            {
                // TODO: Logger
                return null;
            }
            bindingsManager.loadAndResolveBindings( is );
        }
        return bindingsManager;
    }

    public void setBindingsManager( BindingsManager bindingsManager )
    {
        this.bindingsManager = bindingsManager;
    }

    public static Display prepareDisplay( prefuse.data.Graph graph, Layout lay, long activity, Node root, Renderer r )
    {
        Visualization vis = new Visualization();

        vis.add( "graph", graph );
        vis.setInteractive( "graph.edges", null, false );

        if ( ( root != null ) && ( TreeLayout.class.isInstance( lay ) ) )
        {
            VisualItem vi = vis.getVisualItem( "graph", root );
            ( (TreeLayout) lay ).setLayoutRoot( (NodeItem) vi );
        }

        // -- 3. the renderers and renderer factory ---------------------------

        // draw the "name" label for NodeItems
        // Renderer r = new LabelRenderer("name");
        // r.setRoundedCorner(8, 8); // round the corners

        // Renderer r = new ClassesUMLRenderer();

        // .setRoundedCorner(8, 8); // round the corners

        DefaultRendererFactory drf = new DefaultRendererFactory( r );
        drf.setDefaultRenderer( r );
        vis.setRendererFactory( drf );

        // -- 4. the processing actions ---------------------------------------

        // create our nominal color palette
        // pink for females, baby blue for males
        int[] palette = new int[] { ColorLib.rgb( 255, 255, 255 ), ColorLib.rgb( 190, 190, 255 ) };
        // map nominal data values to colors using our provided palette
        DataColorAction fill =
            new DataColorAction( "graph.nodes", "interface", Constants.NOMINAL, VisualItem.FILLCOLOR, palette );

        FontAction font = new FontAction( "graph.nodes", FontLib.getFont( "Arial", 4 ) );

        // use light grey for edges
        ColorAction edges = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 200 ) );

        ColorAction edges0 = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 200 ) );

        // create an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( fill );
        color.add( edges );
        color.add( edges0 );
        color.add( font );

        ActionList layout = new ActionList( activity );
        layout.add( lay );

        // JForcePanel fpanel =
        // ( ForceDirectedLayout.class.isInstance( lay ) ) ? new JForcePanel(
        // ( (ForceDirectedLayout) lay ).getForceSimulator() )
        // : null;

        layout.add( new RepaintAction() );

        vis.putAction( "color", color );
        vis.putAction( "layout", layout );

        // -- 5. the display and interactive controls -------------------------

        Display d = new Display( vis );
        d.setSize( 720, 500 ); // set display size
        // drag individual items around
        DragControl dc=new DragControl();
        dc.setFixPositionOnMouseOver( false );
        d.addControlListener(dc);
        // pan with left-click drag on background
        d.addControlListener( new PanControl() );
        // zoom with right-click drag
        d.addControlListener( new ZoomControl() );
        d.addControlListener( new ZoomToFitControl() );
        d.addControlListener( new FixNodeControl() );

        // -- 6. launch the visualization -------------------------------------

        // assign the colors
        vis.run( "color" );
        // start up the animated layout
        vis.run( "layout" );
        
        vis.putAction( "refresh", new RepaintAction());

        return d;
    }

    @SuppressWarnings( "unchecked" )
    public List<JPanel> calculatePanelsForSideBar()
    {

        List<JPanel> result = new LinkedList<JPanel>();
        if ( model.getConnector() != null )
        {
            ResolvedBinding<DiagramConnector> binding =
                getBindingsManager().getBindingsForConnector( model.getConnector() );
            try
            {
                AbstractConnectorConfigurationPanel p = binding.getConnectorConfigurationPanelClass().newInstance();
                p.setCurrentConfiguration( model.getConnectorConfiguration() );
                ConnectorConfigurationContainerPanel cccp = new ConnectorConfigurationContainerPanel( this, p );
                cccp.setName( "Connector" );
                result.add( cccp );

                if ( ( binding.getRendererConfigurationPanel() != null )
                                || ( binding.getRendererConfiguration() != null ) )
                {
                    RendererConfigurationContainerPanel rccp =
                        new RendererConfigurationContainerPanel( this.layoutController );
                    rccp.setName( "Node apperance" );
                    RendererConfigurationPanel rcp =
                        (RendererConfigurationPanel) ( (Class) binding.getRendererConfigurationPanel() ).newInstance();
                    RendererConfiguration rendererConfiguration = binding.getRendererConfiguration().newInstance();
                    rcp.setCurrentRendererConfiguration( rendererConfiguration );
                    rccp.setRendererConfigurationPanel( rcp );
                    result.add( rccp );
                }

            }
            catch ( InstantiationException e )
            {
                // TODO: Logger
                e.printStackTrace();
            }
            catch ( IllegalAccessException e )
            {
                // TODO: Logger
                e.printStackTrace();
            }
        }

        LayoutConfigurationContainerPanel lccp = new LayoutConfigurationContainerPanel( this.layoutController );
        lccp.setName( "Layout" );
        result.add( lccp );

        return result;
    }

    // ============================================================

    private static ConnectorContext createConnectorContext()
    {
        try
        {
            RunMavenConnectorContext context = new RunMavenConnectorContext();
            File f = new File( MainController.class.getResource( "/maven_test" ).toURI() );
            context.setBaseDir( f );
            context.setLogger( new org.apache.maven.diagrams.gui.log.SimpleLogger() );
            return context;
        }
        catch ( URISyntaxException e )
        {
            e.printStackTrace( System.err );
        }
        return null;
    }

    protected Model createModel( ConnectorContext connectorContext )
    {
        Model model = new Model();
        model.setConnectorContext( connectorContext );
        model.setConnector( new ClassesConnector() );
        model.setConnectorConfiguration( new ClassesConnectorConfiguration() );
        return model;
    }

    // ==============================================================

    public void updateConnectorConfiguration( ConnectorConfiguration new_connector_configuration, boolean refresh )
    {
        model.setConnectorConfiguration( new_connector_configuration );
        if ( refresh )
            refreshGraph();
    }

    public void refreshGraph()
    {
        model.getConnector().setConnectorContext( model.getConnectorContext() );
        try
        {
            Graph g = model.getConnector().calculateGraph( model.getConnectorConfiguration() );
            prefuse.data.Graph prefuse_graph = GraphToPrafuseGraph.graph2PrefuseGraph( g );

            if ( !Display.class.isInstance( view.getDiagramPanel() ) )
            {
                view.setDiagramPanel( prepareDisplay( prefuse_graph, new NodeLinkTreeLayout( "graph" ),
                                                      NodeLinkTreeLayout.DEFAULT_STEP_TIME, null, getRenderer() ) );
            }
            else
            {
                getVisualization().removeGroup( "graph" );
                getVisualization().add( "graph", prefuse_graph );
                getVisualization().run( "color" );
            }

        }
        catch ( ConnectorException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ConfigurableRenderer getRenderer()
    {
        if ( renderer == null )
        {
            ResolvedBinding<DiagramConnector> binding =
                getBindingsManager().getBindingsForConnector( model.getConnector() );
            try
            {
                renderer =  binding.getRenderer().newInstance();
                renderer.setConfiguration( binding.getRendererConfiguration().newInstance() );
            }
            catch ( InstantiationException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            catch ( IllegalAccessException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

        }
        return renderer;
    }

    public Visualization getVisualization()
    {
        if ( ( view != null ) && ( view.getDiagramPanel() != null ) )
            return ( (Display) view.getDiagramPanel() ).getVisualization();
        else
            return null;
    }

    // ==============================================================
    public void run()
    {
        System.out.println("MainController.run()");
        view.run();
    }

}
