package org.codehaus.mojo.settings;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.plexus.util.interpolation.ValueSource;

public class SplitterValueSource
    implements ValueSource
{
    
    private final String splitSequence;
    private final List valueSourceSegments;

    private Map localCache = new HashMap();
    
    public SplitterValueSource( String splitSequence, List valueSourceSegments )
    {
        this.splitSequence = splitSequence;
        this.valueSourceSegments = valueSourceSegments;
    }

    public Object getValue( String expression )
    {
        String[] segments = expression.split( splitSequence );
        
        Object value = null;
        
	String key = expression;

        if ( segments.length > 0 )
        {
            key = segments[0];
        }

        value = localCache.get( key );

        if ( value == null )
        {
	
            for ( int i = 0; value == null && i < segments.length && i < valueSourceSegments.size(); i++ )
            {
                String segment = segments[i];
            
                List valueSources = (List) valueSourceSegments.get( i );
            
                if ( valueSources != null && !valueSources.isEmpty() )
                {
                    for ( Iterator it = valueSources.iterator(); value == null && it.hasNext(); )
                    {
                        ValueSource valueSource = (ValueSource) it.next();
                    
                        value = valueSource.getValue( segment );
                    }
                }
            }
            
            if ( value != null )
            {
                localCache.put( key, value );
            }
        }
        
        return value;
    }

}
