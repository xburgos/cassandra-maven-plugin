package org.apache.maven.diagrams.connectors.classes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.diagrams.connector_api.logger.Logger;
import org.apache.maven.diagrams.connectors.classes.config.ClassesConnectorConfiguration;
import org.apache.maven.diagrams.connectors.classes.graph.ClassNode;
import org.apache.maven.diagrams.connectors.classes.model.ClassModel;
import org.apache.maven.diagrams.connectors.classes.model.FieldModel;
import org.apache.maven.diagrams.connectors.classes.model.MethodModel;
import org.apache.maven.diagrams.connectors.classes.model.ModifierModel;

/**
 * The class creates {@link ClassNode} objects for given class names. Translated objects are cached for feature asks.
 * 
 * @author Piotr Tabor
 */
public class ClassNodesRepository
{
    private Logger logger;

    private ClassModelsRepository classModels;

    private Map<String, ClassNode> classNodes;

    private ClassesConnectorConfiguration configuration;

    public ClassNodesRepository( ClassModelsRepository models, ClassesConnectorConfiguration a_configuration )
    {
        classModels = models;
        classNodes = new HashMap<String, ClassNode>();
        configuration = a_configuration;
    }

    /**
     * The method checks if the classNode for given className ((fully qualified, dot separated) already exists (and
     * returns it). If not - the method calculates it and stores in the cache the result.
     */
    public ClassNode getClassNode( String className ) throws ClassDataSourceException
    {
        ClassNode res = classNodes.get( className );
        if ( res == null )
        {
            res = calculateClassNode( className );
            classNodes.put( className, res );
        }
        return res;
    }

    /**
     * Returns the current state of the cache (as a map from ClassName to ClassNode)
     * 
     * @return
     */
    public Map<String, ClassNode> getMap()
    {
        return classNodes;
    }

    /**
     * Does the real calculation of classNode from the className (fully qualified, dot separated)
     * 
     * @param className
     * @return
     * @throws ClassDataSourceException
     */
    protected ClassNode calculateClassNode( String className ) throws ClassDataSourceException
    {
        /* Calculate class model for the className */
        ClassModel model = classModels.getClassModel( className );

        ClassNode parent = null;
        ClassNode result = new ClassNode();

        /* Calculate (recursive) the classNode for the super class of the className (if needed for feature calculations)*/
        if ( ( model.getSuperClassName() != null )
                        && ( configuration.getFullInheritancePaths()
                                        || configuration.getNodes().getPropagateInheritedFields() || configuration.getNodes().getPropagateInheritedMethods() ) )
        {
            parent = getClassNode( model.getSuperClassName() );
        }

        result.setClass_name( model.getClassifiedName() );
        result.setSuperclassName( model.getSuperClassName() );
        result.setInterface( model.isInterface() );

        /*
         * if propagateInheritedFields is on (and parent is available) 
         * copy not private fields from them to the current class)  
         */
        if ( ( parent != null ) && ( configuration.getNodes().getPropagateInheritedFields() ) )
        {
            copyNotPrivateFields( parent, result );
            copyNotPrivateProperties( parent, result );
        }

        /*
         * if propagateInheritedMethods is on (and parent is available) 
         * copy not private methods from them to the current class)  
         */
        if ( ( parent != null ) && ( configuration.getNodes().getPropagateInheritedMethods() ) )
            copyNotPrivateMethods( parent, result );

        result.setInterfaceNames( model.getInterfaces() );

        copyMethodsFromModel( model, result );
        copyFieldsFromModel( model, result );

        if ( configuration.getNodes().getCompressJavaBeanProperties() )
            compressProperties( result );
        else
            result.setProperties( new ArrayList<FieldModel>() );

        return result;
    }

    /**
     * Translates getters, setters and fields into single "property" object inside
     * the given node;
     * @param node - node to work on
     */
    private void compressProperties( ClassNode node )
    {
        /* prepare list of possible properties (propertyName->type) */
        HashMap<String, String> propertiesCandidates = new LinkedHashMap<String, String>();
        for ( MethodModel method : node.getMethods() )
        {
            if ( method.isGetter() )
                propertiesCandidates.put( method.getPropertyName(), method.getType() );
        }

        /* filter methods (we don't want getter's and setter's on methods list) */
        List<MethodModel> newMethodsList = new ArrayList<MethodModel>();
        for ( MethodModel method : node.getMethods() )
        {
            if ( !method.isGetter() && !method.isSetter() )
                newMethodsList.add( method );
            else if ( propertiesCandidates.get( method.getPropertyName() ) == null )
                newMethodsList.add( method );
        }
        node.setMethods( newMethodsList );

        /* filter fields */
        List<FieldModel> newFieldsList = new ArrayList<FieldModel>();
        for ( FieldModel field : node.getFields() )
        {
            if ( propertiesCandidates.get( field.getName() ) == null )
                newFieldsList.add( field );
        }
        node.setFields( newFieldsList );

        /* create properties list */
        List<FieldModel> newPropertiesList = new ArrayList<FieldModel>();
        for ( String fieldName : propertiesCandidates.keySet() )
        {
            FieldModel fm = new FieldModel();
            fm.setName( fieldName );
            fm.setModifiers( EnumSet.of( ModifierModel.PUBLIC ) );
            fm.setType( propertiesCandidates.get( fieldName ) );
            newPropertiesList.add( fm );
        }
        node.setProperties( newPropertiesList );
    }

    private void copyFieldsFromModel( ClassModel model, ClassNode result )
    {
        for ( FieldModel field : model.getFields() )
        {
            result.getFields().add( field );
        }
    }

    private void copyMethodsFromModel( ClassModel model, ClassNode result )
    {
        for ( MethodModel method : model.getMethods() )
        {
            result.getMethods().add( method );
        }

    }

    private void copyNotPrivateMethods( ClassNode from, ClassNode to )
    {
        for ( MethodModel method : from.getMethods() )
        {
            if ( !method.getModifiers().contains( ModifierModel.PRIVATE ) )
                to.getMethods().add( method );
        }
    }

    private void copyNotPrivateFields( ClassNode from, ClassNode to )
    {
        for ( FieldModel field : from.getFields() )
        {
            if ( !field.getModifiers().contains( ModifierModel.PRIVATE ) )
                to.getFields().add( field );
        }
    }

    private void copyNotPrivateProperties( ClassNode from, ClassNode to )
    {
        for ( FieldModel field : from.getProperties() )
        {
            if ( !field.getModifiers().contains( ModifierModel.PRIVATE ) )
                to.getProperties().add( field );
        }
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }
}
