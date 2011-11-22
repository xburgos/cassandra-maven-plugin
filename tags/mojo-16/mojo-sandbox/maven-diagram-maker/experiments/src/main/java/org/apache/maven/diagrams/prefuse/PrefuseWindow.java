package org.apache.maven.diagrams.prefuse;

import javax.swing.JFrame;

import org.apache.maven.diagrams.prefuse.list_renderer.ClassesUMLRenderer;

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
import prefuse.action.layout.graph.TreeLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.controls.ZoomingPanControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class PrefuseWindow
{

    public static void show( Graph graph, Layout lay, long activity )
    {
        show( graph, lay, activity, null );
    }

    public static void show( Graph graph, Layout lay, long activity, Node root )
    {

        // -- 1. load the data ------------------------------------------------

        // load the socialnet.xml file. it is assumed that the file can be
        // found at the root of the java classpath
        /*
         * Graph graph = null; try { graph = new GraphMLReader().readGraph("/socialnet.xml"); } catch ( DataIOException
         * e ) { e.printStackTrace(); System.err.println("Error loading graph. Exiting..."); System.exit(1); }
         */

        // -- 2. the visualization --------------------------------------------
        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization vis = new Visualization();
        // graph.addColumn( "height" , double.class );
        // graph.addColumn( "width" , double.class );
        vis.add( "graph", graph );
        vis.setInteractive( "graph.edges", null, false );

        if ( ( root != null ) && ( TreeLayout.class.isInstance( lay ) ) )
        {
            VisualItem vi=vis.getVisualItem( "graph", root );
            ((TreeLayout)lay).setLayoutRoot( (NodeItem) vi);
        }

        // vis.getV

        // -- 3. the renderers and renderer factory ---------------------------

        // draw the "name" label for NodeItems
        // Renderer r = new LabelRenderer("name");
        // r.setRoundedCorner(8, 8); // round the corners

        Renderer r = new ClassesUMLRenderer();
        // .setRoundedCorner(8, 8); // round the corners

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default

        DefaultRendererFactory drf = new DefaultRendererFactory( r );
        drf.setDefaultRenderer( r );
        // EdgeRenderer er=new EdgeRenderer();
        // er.setArrowType( prefuse.Constants.EDGE_ARROW_REVERSE );
        // er.setA( prefuse.Constants.EDGE_ARROW_REVERSE );
        // drf.setDefaultEdgeRenderer( er);
        vis.setRendererFactory( drf );

        // -- 4. the processing actions ---------------------------------------

        // create our nominal color palette
        // pink for females, baby blue for males
        int[] palette = new int[] { ColorLib.rgb( 255, 255, 255 ), ColorLib.rgb( 190, 190, 255 ) };
        // map nominal data values to colors using our provided palette
        DataColorAction fill =
            new DataColorAction( "graph.nodes", "interface", Constants.NOMINAL, VisualItem.FILLCOLOR, palette );
        // use black for node text
        // ColorAction text = new ColorAction("graph.nodes",
        // VisualItem.TEXTCOLOR, ColorLib.gray(0));

        FontAction font = new FontAction( "graph.nodes", FontLib.getFont( "Arial", 4 ) );

        // use light grey for edges
        ColorAction edges = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 200 ) );

        ColorAction edges0 = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 200 ) );
        
    
        //        
        // ColorAction edges2 = new ColorAction("graph.edges",
        // VisualItem.STARTFILLCOLOR, ColorLib.gray(200));
        //        
        // ColorAction edges3 = new ColorAction("graph.edges",
        // VisualItem.ENDFILLCOLOR, ColorLib.gray(200));
        //
        // ColorAction edges2b = new ColorAction("graph.edges",
        // VisualItem.STARTSTROKECOLOR, ColorLib.gray(200));
        //         
        // ColorAction edges3b = new ColorAction("graph.edges",
        // VisualItem.ENDSTROKECOLOR, ColorLib.gray(200));

        // ColorAction edges2 = new ColorAction("graph.edges",
        // VisualItem.STARTFILLCOLOR, ColorLib.gray(200));

        // create an action list containing all color assignments
        
        
        ActionList color = new ActionList();
        color.add( fill );
        color.add( edges );
        color.add( edges0 );
        color.add( font );

        ActionList layout = new ActionList( activity );
        layout.add( lay );
        layout.add( new RepaintAction() );

        JForcePanel fpanel =
            ( ForceDirectedLayout.class.isInstance( lay ) ) ? new JForcePanel(                                                                               ( (ForceDirectedLayout) lay ).getForceSimulator() )
                            : null;

        // add the actions to the visualization
        vis.putAction( "color", color );
        vis.putAction( "layout", layout );

        // -- 5. the display and interactive controls -------------------------

        Display d = new Display( vis );
        d.setSize( 720, 500 ); // set display size
        // drag individual items around
        d.addControlListener( new DragControl() );
        // pan with left-click drag on background
        d.addControlListener( new PanControl() );
        // zoom with right-click drag
        d.addControlListener( new ZoomingPanControl() );
        
        //d.addControlListener( new ZoomToFitControl() );

        // -- 6. launch the visualization -------------------------------------

        // create a new window to hold the visualization
        JFrame frame = new JFrame( "prefuse example" );
        // ensure application exits when window is closed
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.add( d );
        frame.pack(); // layout components in window
        frame.setVisible( true ); // show the window

        if ( fpanel != null )
        {
            JFrame frame2 = new JFrame( "2" );
            frame2.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            frame2.add( fpanel );
            frame2.pack(); // layout components in window
            frame2.setVisible( true ); // show the window
        }

        // assign the colors
        vis.run( "color" );
        // start up the animated layout
        vis.run( "layout" );
    }
    
    public static Display prepareDisplay( Graph graph, Layout lay, long activity, Node root )
    {

        // -- 1. load the data ------------------------------------------------

        // load the socialnet.xml file. it is assumed that the file can be
        // found at the root of the java classpath
        /*
         * Graph graph = null; try { graph = new GraphMLReader().readGraph("/socialnet.xml"); } catch ( DataIOException
         * e ) { e.printStackTrace(); System.err.println("Error loading graph. Exiting..."); System.exit(1); }
         */

        // -- 2. the visualization --------------------------------------------
        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization vis = new Visualization();
        // graph.addColumn( "height" , double.class );
        // graph.addColumn( "width" , double.class );
        vis.add( "graph", graph );
        vis.setInteractive( "graph.edges", null, false );

        if ( ( root != null ) && ( TreeLayout.class.isInstance( lay ) ) )
        {
            VisualItem vi=vis.getVisualItem( "graph", root );
            ((TreeLayout)lay).setLayoutRoot( (NodeItem) vi);
        }

        // vis.getV

        // -- 3. the renderers and renderer factory ---------------------------

        // draw the "name" label for NodeItems
        // Renderer r = new LabelRenderer("name");
        // r.setRoundedCorner(8, 8); // round the corners

        Renderer r = new ClassesUMLRenderer();
        // .setRoundedCorner(8, 8); // round the corners

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default

        DefaultRendererFactory drf = new DefaultRendererFactory( r );
        drf.setDefaultRenderer( r );
        // EdgeRenderer er=new EdgeRenderer();
        // er.setArrowType( prefuse.Constants.EDGE_ARROW_REVERSE );
        // er.setA( prefuse.Constants.EDGE_ARROW_REVERSE );
        // drf.setDefaultEdgeRenderer( er);
        vis.setRendererFactory( drf );

        // -- 4. the processing actions ---------------------------------------

        // create our nominal color palette
        // pink for females, baby blue for males
        int[] palette = new int[] { ColorLib.rgb( 255, 255, 255 ), ColorLib.rgb( 190, 190, 255 ) };
        // map nominal data values to colors using our provided palette
        DataColorAction fill =
            new DataColorAction( "graph.nodes", "interface", Constants.NOMINAL, VisualItem.FILLCOLOR, palette );
        // use black for node text
        // ColorAction text = new ColorAction("graph.nodes",
        // VisualItem.TEXTCOLOR, ColorLib.gray(0));

        FontAction font = new FontAction( "graph.nodes", FontLib.getFont( "Arial", 4 ) );

        // use light grey for edges
        ColorAction edges = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 200 ) );

        ColorAction edges0 = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 200 ) );
        
    
        //        
        // ColorAction edges2 = new ColorAction("graph.edges",
        // VisualItem.STARTFILLCOLOR, ColorLib.gray(200));
        //        
        // ColorAction edges3 = new ColorAction("graph.edges",
        // VisualItem.ENDFILLCOLOR, ColorLib.gray(200));
        //
        // ColorAction edges2b = new ColorAction("graph.edges",
        // VisualItem.STARTSTROKECOLOR, ColorLib.gray(200));
        //         
        // ColorAction edges3b = new ColorAction("graph.edges",
        // VisualItem.ENDSTROKECOLOR, ColorLib.gray(200));

        // ColorAction edges2 = new ColorAction("graph.edges",
        // VisualItem.STARTFILLCOLOR, ColorLib.gray(200));

        // create an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( fill );
        // color.add(text);
        color.add( edges );
        color.add( edges0 );
        // color.add(edges2);
        // color.add(edges3);
        // color.add(edges2b);
        // color.add(edges3b);
        color.add( font );

        ActionList layout = new ActionList( activity );
        layout.add( lay );

        JForcePanel fpanel =
            ( ForceDirectedLayout.class.isInstance( lay ) ) ? new JForcePanel(
                                                                               ( (ForceDirectedLayout) lay ).getForceSimulator() )
                            : null;

        layout.add( new RepaintAction() );

        // add the actions to the visualization
        vis.putAction( "color", color );
        vis.putAction( "layout", layout );
       // vis.cancel( "layout" );
        

        // -- 5. the display and interactive controls -------------------------

        Display d = new Display( vis );
        d.setSize( 720, 500 ); // set display size
        // drag individual items around
        d.addControlListener( new DragControl() );
        // pan with left-click drag on background
        d.addControlListener( new PanControl() );
        // zoom with right-click drag
        d.addControlListener( new ZoomControl() );
        
        d.addControlListener( new  ZoomToFitControl());

        // -- 6. launch the visualization -------------------------------------
        

        // assign the colors
        vis.run( "color" );
        // start up the animated layout
        vis.run( "layout" );

        return d;

        // create a new window to hold the visualization
//        JFrame frame = new JFrame( "prefuse example" );
//        // ensure application exits when window is closed
//        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//        frame.add( d );
//        frame.pack(); // layout components in window
//        frame.setVisible( true ); // show the window
//
//        if ( fpanel != null )
//        {
//            JFrame frame2 = new JFrame( "2" );
//            frame2.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//            frame2.add( fpanel );
//            frame2.pack(); // layout components in window
//            frame2.setVisible( true ); // show the window
//        }
    }


}
