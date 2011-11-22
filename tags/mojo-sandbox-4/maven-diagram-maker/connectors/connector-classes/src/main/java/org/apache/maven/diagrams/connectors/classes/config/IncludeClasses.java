package org.apache.maven.diagrams.connectors.classes.config;

public class IncludeClasses 
{
    /**
     * Regexp pattern for included classes. 
     */
    private String pattern;
  
    public IncludeClasses()
    {
        super();
    }
    public IncludeClasses( String pattern )
    {
        super();
        this.pattern = pattern;
    }
    
    
    /**
     * Gets the regexp pattern
     * @return
     */
    public String getPattern()
    {
        return pattern;
    }
    
    /**
     * Sets the regexp pattern
     * @param pattern
     */
    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }
}
