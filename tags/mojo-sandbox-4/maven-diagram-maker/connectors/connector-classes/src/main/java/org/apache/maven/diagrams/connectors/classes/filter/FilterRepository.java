package org.apache.maven.diagrams.connectors.classes.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache of filtered classNames.
 * (after filtering className it is added to the cache and
 * the next answer for the same class is faster)
 *  
 * @author Piotr Tabor
 *
 */
public class FilterRepository
{
    private ClassNamesFilter filter;

    private Map<String, ClassFilterStatus> filtered;

    public FilterRepository( ClassNamesFilter a_filter )
    {
        filter = a_filter;
        filtered = new HashMap<String, ClassFilterStatus>();
    }

    public ClassFilterStatus getStatus( String className )
    {
        ClassFilterStatus result = filtered.get( className );
        if ( result == null )
        {
            result = filter.filter( className );
            filtered.put( className, result );
        }
        return result;
    }
}
