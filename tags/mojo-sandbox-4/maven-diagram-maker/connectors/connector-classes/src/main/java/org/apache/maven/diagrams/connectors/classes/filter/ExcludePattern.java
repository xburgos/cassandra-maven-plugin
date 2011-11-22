package org.apache.maven.diagrams.connectors.classes.filter;

/**
 * Compiled pattern for className exclussion  
 * 
 * @author Piotr Tabor
 * 
 */
public class ExcludePattern extends FilterPattern
{
    private Boolean withKeepEdges;

    public ExcludePattern( String a_pattern )
    {
        super( a_pattern );
        withKeepEdges = false;
    }

    public ExcludePattern( String a_pattern, boolean a_withKeepEdges )
    {
        super( a_pattern );
        withKeepEdges = a_withKeepEdges;
    }

    public Boolean getWithKeepEdges()
    {
        return withKeepEdges;
    }

    public void setWithEdges( Boolean withKeepEdges )
    {
        this.withKeepEdges = withKeepEdges;
    }

}
