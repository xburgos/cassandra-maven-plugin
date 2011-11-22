package org.apache.maven.diagrams.connectors.classes.config;

public class ExcludeClasses
{
    /**
     * Regular expression pattern for the class name
     */
    private String pattern;

    /**
     * If the keepEdges is true, and there is such a situation: A--B--C and B is excluded the result is A--C, if
     * keepEdges ware false, the result would be A and C separetly.
     */
    private Boolean keepEdges;

    public ExcludeClasses( String pattern, Boolean keepEdges )
    {
        super();
        this.pattern = pattern;
        this.keepEdges = keepEdges;
    }

    /**
     * Gets the regexp pattern for classes to be excluded
     * @return
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Sets the regexp pattern for classes to be excluded
     * @return
     */
    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }

    /**
     * Gets keepEdges parameter.
     * 
     * If the keepEdges is true, and there is such a situation: A--B--C and B is excluded the result is A--C, if
     * keepEdges ware false, the result would be A and C separetly.
     */
    public Boolean getKeepEdges()
    {
        return keepEdges;
    }

    /**
     * Sets keepEdges parameter.
     * 
     * If the keepEdges is true, and there is such a situation: A--B--C and B is excluded the result is A--C, if
     * keepEdges ware false, the result would be A and C separetly.
     */
    public void setKeepEdges( Boolean keepEdges )
    {
        this.keepEdges = keepEdges;
    }
}
