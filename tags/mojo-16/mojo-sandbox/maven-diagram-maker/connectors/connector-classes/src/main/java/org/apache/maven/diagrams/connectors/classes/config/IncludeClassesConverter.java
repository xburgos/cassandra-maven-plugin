package org.apache.maven.diagrams.connectors.classes.config;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Converts &lt;includePattern&gt;string&lt;/includePattern&gt; into IncludePattern object
 * (for &lt;pattern&gt;) 
 * 
 * @author Piotr Tabor
 *
 */
public class IncludeClassesConverter extends AbstractSingleValueConverter
{

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert( Class arg0 )
    {
        return arg0.isAssignableFrom( IncludeClasses.class );
    }

    @Override
    public Object fromString( String arg0 )
    {
        return new IncludeClasses(arg0);
    }

    @Override
    public String toString( Object obj )
    {
        return ((IncludeClasses)obj).getPattern();
    }
}
