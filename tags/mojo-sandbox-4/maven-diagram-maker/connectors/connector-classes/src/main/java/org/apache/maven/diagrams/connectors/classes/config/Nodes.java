package org.apache.maven.diagrams.connectors.classes.config;

public class Nodes
{
    /**
     * Should field,getter and optionally setter be transformed into single &lt;&lt;property&gt;&gt; scope.
     */
    private Boolean compressJavaBeanProperties = true;

    /**
     * Should be inherited (public and protected) methods added to the class's node.
     */
    private Boolean propagateInheritedMethods = false;

    /**
     * Should be inherited (public and protected) fields added to the class's node.
     */
    private Boolean propagateInheritedFields = false;

    /**
     * Should field,getter and optionally setter be transformed into single &lt;&lt;property&gt;&gt; scope.
     */
    public Boolean getCompressJavaBeanProperties()
    {
        return compressJavaBeanProperties;
    }

    /**
     * Should field,getter and optionally setter be transformed into single &lt;&lt;property&gt;&gt; scope.
     */
    public void setCompressJavaBeanProperties( Boolean compressJavaBeanProperties )
    {
        this.compressJavaBeanProperties = compressJavaBeanProperties;
    }

    /**
     * Should be inherited (public and protected) methods added to the class's node.
     */
    public Boolean getPropagateInheritedMethods()
    {
        return propagateInheritedMethods;
    }

    /**
     * Should be inherited (public and protected) methods added to the class's node.
     */
    public void setPropagateInheritedMethods( Boolean propagateInheritedMethods )
    {
        this.propagateInheritedMethods = propagateInheritedMethods;
    }

    /**
     * Should be inherited (public and protected) fields added to the class's node.
     */
    public Boolean getPropagateInheritedFields()
    {
        return propagateInheritedFields;
    }

    /**
     * Should be inherited (public and protected) fields added to the class's node.
     */
    public void setPropagateInheritedFields( Boolean propagateInheritedFields )
    {
        this.propagateInheritedFields = propagateInheritedFields;
    }

}
