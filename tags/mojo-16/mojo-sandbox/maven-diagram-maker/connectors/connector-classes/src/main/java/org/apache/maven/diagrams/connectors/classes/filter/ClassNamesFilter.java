package org.apache.maven.diagrams.connectors.classes.filter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.diagrams.connectors.classes.filter.ClassFilterStatus;

public class ClassNamesFilter
{
    private List<IncludePattern> includes;

    private List<ExcludePattern> excludes;

    /* ------------------- Getters and setters ---------------------------- */
    public List<IncludePattern> getIncludes()
    {
        return includes;
    }

    public void setIncludes( List<IncludePattern> includes )
    {
        this.includes = includes;
    }

    public List<ExcludePattern> getExcludes()
    {
        return excludes;
    }

    public void setExcludes( List<ExcludePattern> excludes )
    {
        this.excludes = excludes;
    }

    /* ------------------- Logic ---------------------------- */

    /**
     * Filters given collection of classNames (full-dot-qualified) and produces map from ClassName to
     * {@link ClassFilterStatus}
     */
    public Map<String, ClassFilterStatus> scan( Collection<String> col )
    {
        Map<String, ClassFilterStatus> result = new LinkedHashMap<String, ClassFilterStatus>( col.size() );
        for ( String className : col )
        {
            result.put( className, filter( className ) );
        }
        return result;
    }

    /**
     * Filters single full-dot-qualified class name in context of includes and excludes and returns
     * {@link ClassFilterStatus}
     * 
     * @param className
     * @return
     */
    public ClassFilterStatus filter( String className )
    {
        if ( isIncluded( className ) )
        {
            for ( ExcludePattern exclude : excludes )
            {
                if ( exclude.match( className ) )
                    return ( exclude.getWithKeepEdges() ) ? ClassFilterStatus.EXCLUDED_WITH_KEEP_EDGES
                                    : ClassFilterStatus.EXCLUDED_WITHOUT_KEEP_EDGES;
            }
            return ClassFilterStatus.INCLUDED;
        }
        else
            return ClassFilterStatus.NOT_INCLUDED;

    }

    /**
     * Returns true if given full-dot-qualified className is included by any includePattern
     * 
     * @param className
     * @return
     */
    private boolean isIncluded( String className )
    {
        for ( IncludePattern includePattern : includes )
        {
            if ( includePattern.match( className ) )
                return true;
        }
        return false;
    }
}
