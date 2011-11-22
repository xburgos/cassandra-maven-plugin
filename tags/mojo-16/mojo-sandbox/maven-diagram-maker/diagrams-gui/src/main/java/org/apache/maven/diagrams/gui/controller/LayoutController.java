package org.apache.maven.diagrams.gui.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.diagrams.gui.bindings.layouts.LayoutBinding;
import org.apache.maven.diagrams.gui.bindings.layouts.LayoutsManager;
import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfiguration;
import org.apache.maven.diagrams.gui.layouts.AbstractLayoutConfigurationPanel;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.RendererNodeCache;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.layout.Layout;
import prefuse.visual.VisualItem;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class LayoutController
{
    private LayoutsManager layoutsManager;

    private Layout currentLayout;

    private MainController controller;

    private boolean autoApplyMode;

    private boolean layoutRunning;

    public LayoutController( MainController a_controller )
    {
        layoutsManager = new LayoutsManager();
        controller = a_controller;
        layoutRunning = false;
    }

    @SuppressWarnings( "unchecked" )
    public synchronized void updateLayoutConfiguration( AbstractLayoutConfiguration new_configuration )
    {
        Visualization visualization = controller.getVisualization();

        Layout layout = getCurrentLayout();
        if ( new_configuration.canUpdateLayout( layout ) )
            new_configuration.updateLayout( layout );

        layout.setDuration( Layout.DEFAULT_STEP_TIME );
        visualization.run( "layout" );
        // visualization.putAction( "repaint", new RepaintAction() );

    }

    // ========================= AutoApply mode =================================

    public boolean isAutoApplyMode()
    {
        return autoApplyMode;
    }

    public void setAutoApplyMode( boolean autoApplyMode )
    {
        this.autoApplyMode = autoApplyMode;
    }

    // ======================= Bindings ==========================================
    @SuppressWarnings( "unchecked" )
    public List<LayoutBinding> getAvailableLayouts()
    {
        return layoutsManager.getLayoutBindings();
    }

    @SuppressWarnings( "unchecked" )
    public AbstractLayoutConfigurationPanel getPanelFor( String layoutName )
    {
        Class<? extends AbstractLayoutConfigurationPanel> panelClass = layoutsManager.getEditPanelFor( layoutName );
        try
        {
            AbstractLayoutConfigurationPanel<? extends AbstractLayoutConfiguration> res = panelClass.newInstance();
            res.setController( this );
            return res;

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

    @SuppressWarnings( "unchecked" )
    public boolean isCurrentLayoutNamed( String layoutName )
    {
        Layout currenLayout = getCurrentLayout();
        LayoutBinding lb = layoutsManager.getBindingForName( layoutName );
        if ( lb == null )
            return false;
        return ( lb.getLayoutClass().isInstance( currenLayout ) );
    }

    public void setCurrentLayout( String layoutName, AbstractLayoutConfiguration configuration )
    {
        LayoutBinding lb = layoutsManager.getBindingForName( layoutName );
        try
        {
            Constructor c = lb.getLayoutClass().getConstructor( new Class[] { String.class } );

            currentLayout = (Layout) c.newInstance( new Object[] { "graph" } );
            if ( ( configuration != null ) && ( lb.getConfigurationClass().isInstance( configuration ) ) )
                configuration.updateLayout( currentLayout );

            Visualization visualization = controller.getVisualization();
            visualization.cancel( "layout" );
            ActionList layoutList = new ActionList( layoutRunning ? Action.INFINITY : Action.DEFAULT_STEP_TIME );
            layoutList.add( currentLayout );
            layoutList.add( new RepaintAction() );
            visualization.putAction( "layout", layoutList );
            
            visualization.run( "layout" );
            
            visualization.putAction( "refresh", new RepaintAction());

        }
        catch ( InstantiationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IllegalAccessException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( SecurityException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( NoSuchMethodException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IllegalArgumentException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( InvocationTargetException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Layout getCurrentLayout()
    {
        if ( currentLayout == null )
        {
            Visualization visualization = controller.getVisualization();
            if ( visualization != null )
                currentLayout = searchForLayout( visualization.getAction( "layout" ) );
            else
                return null;
        }
        return currentLayout;
    }

    private Layout searchForLayout( Action action )
    {
        if ( Layout.class.isInstance( action ) )
            return (Layout) action;
        if ( ActionList.class.isInstance( action ) )
        {
            ActionList al = (ActionList) action;
            for ( int i = 0; i < al.size(); i++ )
            {
                Layout res = searchForLayout( al.get( i ) );
                if ( res != null )
                    return res;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public AbstractLayoutConfiguration provideConfigurationForLayoutNamed( String layoutName )
        throws InstantiationException, IllegalAccessException
    {
        Layout currenLayout = getCurrentLayout();
        LayoutBinding lb = layoutsManager.getBindingForName( layoutName );
        AbstractLayoutConfiguration<Layout> conf =
            (AbstractLayoutConfiguration<Layout>) lb.getConfigurationClass().newInstance();
        if ( lb.getLayoutClass().isInstance( currenLayout ) )
        {
            conf.readFromLayout( currenLayout );
        }
        else
        {
            conf.setDefaultConfiguration();
        }
        return conf;
    }

    public void startLayoutRunning()
    {
        Visualization visualization = controller.getVisualization();
        Action layout = visualization.getAction( "layout" );
        layout.setDuration( Action.INFINITY );
        if ( !layout.isRunning() )
            layout.run();
        layoutRunning = true;
    }

    public void stopLayoutRunning()
    {
        Visualization visualization = controller.getVisualization();
        Action layout = visualization.getAction( "layout" );
        layout.setDuration( 0 );
        layoutRunning = false;
    }

    public void updateRendererConfiguration( RendererConfiguration currentRendererConfiguration )
    {
        controller.getRenderer().setConfiguration( currentRendererConfiguration );
        clearRenderersCache();
        controller.getVisualization().run( "refresh");
    }

    private void clearRenderersCache()
    {        
        for(Iterator<VisualItem> i=controller.getVisualization().items(); i.hasNext();)
        {
            VisualItem item=i.next();
            item.set( RendererNodeCache.CACHE_ITEM_COLUMN_NAME, null);
        }
    }
}
