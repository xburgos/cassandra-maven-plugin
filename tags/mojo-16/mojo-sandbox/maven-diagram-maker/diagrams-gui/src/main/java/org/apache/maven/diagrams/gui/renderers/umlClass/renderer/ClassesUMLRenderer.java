package org.apache.maven.diagrams.gui.renderers.umlClass.renderer;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.connectors.classes.model.FieldModel;
import org.apache.maven.diagrams.connectors.classes.model.MethodModel;
import org.apache.maven.diagrams.connectors.classes.model.ModifierModel;
import org.apache.maven.diagrams.gui.renderers.RendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.umlClass.UmlClassRendererConfiguration;
import org.apache.maven.diagrams.gui.renderers.umlClass.UmlClassRendererConfigurationItem;

import prefuse.visual.VisualItem;

public class ClassesUMLRenderer extends ListRenderer
{
    private EnumSet<AccessModifier> methodAccessModifiers;

    private EnumSet<AccessModifier> fieldAccessModifiers;

    public ClassesUMLRenderer()
    {
        super();
        methodAccessModifiers = EnumSet.of( AccessModifier.PUBLIC );
        fieldAccessModifiers = EnumSet.of( AccessModifier.PUBLIC );
    }

    public ClassesUMLRenderer( EnumSet<AccessModifier> methodAccessModifiers,
                               EnumSet<AccessModifier> fieldAccessModifiers )
    {
        super();
        this.methodAccessModifiers = methodAccessModifiers;
        this.fieldAccessModifiers = fieldAccessModifiers;
    }

    @SuppressWarnings( "unchecked" )
    protected List<RendererListItem> getList( VisualItem vi )
    {
        List<RendererListItem> list;
        // = (List<RendererListItem>) vi.get( "vcache" );
        // if ( list == null )
        {
            list = new LinkedList<RendererListItem>();

            ClassNode node = (ClassNode) vi.get( "node" );
            UmlClassRendererConfigurationItem name_config =
                (UmlClassRendererConfigurationItem) getConfiguration().getRenderConfigurationItems().get(
                                                                                                          UmlClassRendererConfiguration.ATT_NAME );
            if ( name_config.isVisible() )
            {
                list.add( new TextItem( name_config.getFull_class_names() ? node.getFull_name() : node.getSimpleName(),
                                        true, false, true ) );
            }
            list.add( new SeparatorItem() );

            for ( FieldModel field : node.getFields() )
            {
                if ( AccessModifier.hasOneOfSuchModifiers( fieldAccessModifiers, field.getModifiers() ) )
                    list.add( new TextItem( field.toUMLString( true ), false,
                                            field.getModifiers().contains( ModifierModel.STATIC ), false ) );
            }
            for ( FieldModel field : node.getProperties() )
            {
                // if ( AccessModifier.hasOneOfSuchModifiers( fieldAccessModifiers, field.getModifiers() ) )
                if ( getConfiguration().isVisible( UmlClassRendererConfiguration.ATT_PROPERTIES ) )
                    list.add( new TextItem( field.toUMLString( true ) + " <<property>>", false,
                                            field.getModifiers().contains( ModifierModel.STATIC ), false ) );
            }
            list.add( new SeparatorItem() );

            for ( MethodModel method : node.getMethods() )
            {
                if ( AccessModifier.hasOneOfSuchModifiers( methodAccessModifiers, method.getModifiers() ) )
                    list.add( new TextItem( method.toUMLString( true ), false,
                                            method.getModifiers().contains( ModifierModel.STATIC ), false ) );
            }
            // vi.set( "vcache", list );
        }

        return list;
    }

    @Override
    public void setConfiguration( RendererConfiguration newRendererConfiguration )
    {
        super.setConfiguration( newRendererConfiguration );
        methodAccessModifiers = EnumSet.noneOf( AccessModifier.class );
        fieldAccessModifiers = EnumSet.noneOf( AccessModifier.class );
        RendererConfiguration ucrm = (RendererConfiguration) newRendererConfiguration;

        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PRIVATE_FIELDS ) )
            fieldAccessModifiers.add( AccessModifier.PRIVATE );
        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PUBLIC_FIELDS ) )
            fieldAccessModifiers.add( AccessModifier.PUBLIC );
        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PROTECTED_FIELDS ) )
            fieldAccessModifiers.add( AccessModifier.PROTECTED );

        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PRIVATE_METHODS ) )
            methodAccessModifiers.add( AccessModifier.PRIVATE );
        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PUBLIC_METHODS ) )
            methodAccessModifiers.add( AccessModifier.PUBLIC );
        if ( ucrm.isVisible( UmlClassRendererConfiguration.ATT_PROTECTED_METHODS ) )
            methodAccessModifiers.add( AccessModifier.PROTECTED );

    }
}
