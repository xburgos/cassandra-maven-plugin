package org.apache.maven.diagrams.connectors.classes.filter;

import java.util.LinkedList;
import java.util.List;

/**
 * Compiled pattern for className inclussion  
 * 
 * @author Piotr Tabor
 * 
 */
public class IncludePattern extends FilterPattern
{

    public IncludePattern( String a_pattern )
    {
        super( a_pattern );
    }

    static List<IncludePattern> createList( String[] inputs )
    {
        List<IncludePattern> result = new LinkedList<IncludePattern>();
        for ( String pattern : inputs )
        {
            result.add( new IncludePattern( pattern ) );
        }
        return result;
    }

}
