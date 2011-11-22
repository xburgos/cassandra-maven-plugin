package org.apache.maven.diagrams.connectors.classes.filter;

/**
 * Every class (interface) can be in one of such a states (in terms of filtering classes)
 * 
 * @author Piotr Tabor
 * 
 */
public enum ClassFilterStatus
{
    /**
     * Does not match any includePattern
     */
    NOT_INCLUDED,

    /**
     * Matches one of includePattern and doesn't match any excludePattern
     */
    INCLUDED,

    /**
     * Matches one of includePattern and matched excludePattern with keep edges first
     */
    EXCLUDED_WITH_KEEP_EDGES,

    /**
     * Matches one of includePattern and matched excludePattern without keep edges first
     */
    EXCLUDED_WITHOUT_KEEP_EDGES;

    /**
     * NOT_INCLUDED and EXCLUDED_WITHOUT_KEEP_EDGES classes are skipped
     */
    public boolean toSkip()
    {
        return this.equals( NOT_INCLUDED ) || this.equals( EXCLUDED_WITHOUT_KEEP_EDGES );
    }

}
