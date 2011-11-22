package org.apache.maven.diagrams.connectors.classes.model;

import java.util.List;

/**
 * The class represents all information available in
 * single class file for class diagram
 *  
 * @author Piotr Tabor (ptab@newitech.com)
 *
 */
public class ClassModel
{
    /**
     * Full class name with dots as package separators
     */
    private String classifiedName;

    private List<MethodModel> methods;

    private List<FieldModel> fields;

    private String superClassName;

    private List<String> interfaces;

    /**
     * Is the class interface or ordinal "class"
     */
    private Boolean interf;

    public String getClassifiedName()
    {
        return classifiedName;
    }

    public void setClassifiedName( String classifiedName )
    {
        this.classifiedName = classifiedName;
    }

    public List<MethodModel> getMethods()
    {
        return methods;
    }

    public void setMethods( List<MethodModel> methods )
    {
        this.methods = methods;
    }

    public List<FieldModel> getFields()
    {
        return fields;
    }

    public void setFields( List<FieldModel> fields )
    {
        this.fields = fields;
    }

    public String getSuperClassName()
    {
        return superClassName;
    }

    public void setSuperClassName( String superClassName )
    {
        this.superClassName = superClassName;
    }

    public List<String> getInterfaces()
    {
        return interfaces;
    }

    public void setInterfaces( List<String> interfaces )
    {
        this.interfaces = interfaces;
    }

    public Boolean isInterface()
    {
        return interf;
    }

    public void setInterface( Boolean interf )
    {
        this.interf = interf;
    }
}
