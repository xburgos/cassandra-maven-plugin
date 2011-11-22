package org.apache.maven.diagrams.connectors.classes.graph;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.diagrams.connectors.classes.model.FieldModel;
import org.apache.maven.diagrams.connectors.classes.model.MethodModel;
import org.apache.maven.diagrams.graph_api.Node;

/**
 * The class represents single Class as graph node (implementing Graph-api Node interface)
 * 
 * @author Piotr Tabor
 * 
 */
public class ClassNode implements Node
{
    private String fullName;

    private boolean interf;

    private List<MethodModel> methods;

    private List<FieldModel> fields;

    private List<FieldModel> properties;

    private String superclassName;

    private List<String> interfaceNames;

    public ClassNode()
    {
        interfaceNames = new LinkedList<String>();
        fields = new LinkedList<FieldModel>();
        methods = new LinkedList<MethodModel>();
        properties = new LinkedList<FieldModel>();
    }

    public ClassNode( String a_fullName )
    {
        fullName = a_fullName;
        interfaceNames = new LinkedList<String>();
        fields = new LinkedList<FieldModel>();
        methods = new LinkedList<MethodModel>();
        properties = new LinkedList<FieldModel>();
    }

    public String getId()
    {
        return getFull_name();
    }

    /*----------------------- Class name ----------------------*/

    public String getFull_name()
    {
        return fullName;
    }

    public void setClass_name( String fullName )
    {
        this.fullName = fullName;
    }

    public String getPackageName()
    {
        return packageName( fullName );
    }

    public String getSimpleName()
    {
        return simpleClassName( fullName );
    }

    /* ----------------------- superclass name ---------------------- */

    public String getSuperclassName()
    {
        return superclassName;
    }

    public void setSuperclassName( String superclassName )
    {
        this.superclassName = superclassName;
    }

    public String getSimpleSuperclassName()
    {
        return simpleClassName( superclassName );
    }

    /* ------------------------ interfaces --------------------------- */

    public List<String> getInterfaceNames()
    {
        return interfaceNames;
    }

    public void setInterfaceNames( List<String> interfaceNames )
    {
        this.interfaceNames = interfaceNames;
    }

    /* ----------------------- methods ----------------------------- */

    public List<MethodModel> getMethods()
    {
        return methods;
    }

    public void setMethods( List<MethodModel> methods )
    {
        this.methods = methods;
    }

    /* ------------------------- fields ------------------------------ */

    public List<FieldModel> getFields()
    {
        return fields;
    }

    public void setFields( List<FieldModel> fields )
    {
        this.fields = fields;
    }

    /* ---------------------- properties --------------------------- */

    public List<FieldModel> getProperties()
    {
        return properties;
    }

    public void setProperties( List<FieldModel> properties )
    {
        this.properties = properties;
    }

    /* ----------------------- interface ---------------------------- */

    public boolean isInterface()
    {
        return interf;
    }

    public void setInterface( boolean interf )
    {
        this.interf = interf;
    }

    /* ====================== helpers ================================ */

    /*
     * TODO: Move to single place
     */
    private static String packageName( String name )
    {
        int last = name.lastIndexOf( "." );
        return last > 0 ? name.substring( 0, last ) : "";
    }

    /*
     * TODO: Move to single place
     */
    private static String simpleClassName( String name )
    {
        int last = name.lastIndexOf( "." );
        return name.substring( last + 1 );
    }

    private String getEOL()
    {
        String sp = System.getProperty( "line.separator" );
        return sp == null ? "\n" : sp;
    }

    public String toString()
    {
        String EOL = getEOL();

        StringBuffer sb = new StringBuffer();

        sb.append( getFull_name() + " extends " + getSuperclassName() + " implemments " + getInterfaceNames() + EOL );

        sb.append( "PROPERTIES:" + EOL );
        for ( FieldModel fm : properties )
        {
            sb.append( " - " + fm.toString() + EOL );
        }
        sb.append( "FIELDS:" + EOL );
        for ( FieldModel fm : fields )
        {
            sb.append( " - " + fm.toString() + EOL );
        }
        sb.append( "METHODS:" + EOL );
        for ( MethodModel fm : methods )
        {
            sb.append( " - " + fm.toString() + EOL );
        }

        return sb.toString();
    }

    public void print( PrintStream printStream )
    {
        printStream.println( this.toString() );
    }
}
