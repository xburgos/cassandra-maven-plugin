package org.apache.maven.diagrams.connectors.classes.filter;

import java.util.regex.Pattern;

/**
 * Single compiled regexp pattern
 * 
 * @author Piotr Tabor
 *
 */
public class FilterPattern
{
    Pattern pattern;

    public FilterPattern( String a_pattern )
    {
        pattern = Pattern.compile( a_pattern );
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public void setPattern( Pattern pattern )
    {
        this.pattern = pattern;
    }

    public boolean match( String s )
    {
        return pattern.matcher( s ).matches();
    }
}
