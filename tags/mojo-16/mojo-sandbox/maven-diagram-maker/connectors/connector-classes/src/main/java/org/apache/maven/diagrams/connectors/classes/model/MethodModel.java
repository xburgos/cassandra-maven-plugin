package org.apache.maven.diagrams.connectors.classes.model;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Represents single Method
 * 
 * @author Piotr Tabor
 * 
 */
public class MethodModel
{
    /**
     * Returned data type from method.
     */
    private String type;

    /**
     * Method name
     */
    private String name;

    /**
     * List of parameter's types
     */
    private List<String> params;

    /**
     * Set of (access) modifiers
     */
    private EnumSet<ModifierModel> modifiers;

    /**
     * Returned data type from method.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returned data type from method.
     */
    public void setType( String type )
    {
        this.type = type;
    }

    /**
     * Method name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method name
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * List of parameter's types
     */
    public List<String> getParams()
    {
        return params;
    }

    /**
     * List of parameter's types
     */
    public void setParams( List<String> params )
    {
        this.params = params;
    }

    /**
     * Set of (access) modifiers
     */
    public EnumSet<ModifierModel> getModifiers()
    {
        return modifiers;
    }

    /**
     * Set of (access) modifiers
     */
    public void setModifiers( EnumSet<ModifierModel> modifiers )
    {
        this.modifiers = modifiers;
    }

    @Override
    public String toString()
    {
        return modifiers + " " + type + " " + name + "(" + params + ")";
    }

    /*--------- Java Bean getters and setters identification ----------*/
    /**
     * @return if the method is getter
     */
    public boolean isGetter()
    {
        return ( ( ( name.startsWith( "get" ) || ( name.startsWith( "is" ) ) ) && ( !type.equals( "void" ) )
                        && ( params.size() == 0 ) && ( modifiers.contains( ModifierModel.PUBLIC ) ) && ( !modifiers.contains( ModifierModel.STATIC ) ) ) );
    }

    /**
     * @return if the method is setter
     */
    public boolean isSetter()
    {
        return name.startsWith( "set" ) && ( params.size() == 1 ) && ( !modifiers.contains( ModifierModel.STATIC ) )
                        && ( modifiers.contains( ModifierModel.PUBLIC ) );
    }

    /**
     * Return's the "fieldName" part of getter or setter (for example: abcDef for getAbcDef)
     * 
     * @return
     */
    public String getPropertyName()
    {
        if ( ( name.startsWith( "get" ) ) || ( name.startsWith( "set" ) ) )
            return lowerFirstChar( name.substring( 3 ) );
        if ( name.startsWith( "is" ) )
            return lowerFirstChar( name.substring( 2 ) );
        return null;
    }

    /*------------------------ helpers -------------------------------*/

    /**
     * Translates the MethodModel into UML string representation
     * 
     * @param short_ - use short (simple / not qualified) class names
     * @return
     */
    public String toUMLString( boolean short_ )
    {
        StringBuffer res = new StringBuffer();
        if ( modifiers.contains( ModifierModel.PRIVATE ) )
            res.append( "- " );
        else if ( modifiers.contains( ModifierModel.PUBLIC ) )
            res.append( "+ " );
        else if ( modifiers.contains( ModifierModel.PROTECTED ) )
            res.append( "# " );
        else
            res.append( "~ " );

        res.append( name );
        res.append( '(' );
        Iterator<String> params_iterator = params.iterator();
        while ( params_iterator.hasNext() )
        {
            res.append( shortClassName( params_iterator.next() ) );
            if ( params_iterator.hasNext() )
                res.append( ',' );
        }
        res.append( ')' );

        if ( !type.equals( "void" ) )
        {
            res.append( ":" );
            if ( short_ )
                res.append( shortClassName( type ) );
            else
                res.append( type );
        }

        return res.toString();
    }

    /**
     * Translates qualified class name into simple class name
     * 
     * @param className
     * @return
     */
    // TODO: Move to one place all "String/class management methods"
    private static String shortClassName( String className )
    {
        int last = className.lastIndexOf( '.' );
        return last >= 0 ? className.substring( last + 1 ) : className;
    }

    /**
     * Change the first char into lower case char. For empty string - returns itself.
     * 
     * @param string
     * @return
     */
    // TODO: Move to one place all "String/class management methods"
    static private String lowerFirstChar( String string )
    {
        if ( string.length() > 0 )
        {
            StringBuffer res = new StringBuffer();
            res.append( Character.toLowerCase( string.charAt( 0 ) ) );
            res.append( string.substring( 1 ) );
            return res.toString();
        }
        else
            return string;
    }
}
