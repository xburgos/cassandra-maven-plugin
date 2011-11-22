package org.codehaus.mojo.resolver.bod.pom.rewrite;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;

public class PomRewriteConfiguration
{
    
    private List includes = new ArrayList();
    
    private List excludes = new ArrayList();
    
    private Properties properties = new Properties();
    
    private String parentArtifactId;
    
    public boolean isEmpty()
    {
        return properties == null || properties.isEmpty();
    }

    public List getExcludes()
    {
        return excludes;
    }

    public void setExcludes( List excludes )
    {
        this.excludes = excludes;
    }

    public List getIncludes()
    {
        return includes;
    }

    public void setIncludes( List includes )
    {
        this.includes = includes;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    public String getIncludesAsCSV()
    {
        return StringUtils.join( getIncludes().iterator(), "," );
    }

    public String getExcludesAsCSV()
    {
        return StringUtils.join( getExcludes().iterator(), "," );
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( "POM Rewrite Configuration\n----------------------" );
        
        if ( includes != null )
        {
            buffer.append( "\nIncludes: " ).append( getIncludesAsCSV() );
        }
        
        if ( excludes != null )
        {
            buffer.append( "\nExcludes: " ).append( getExcludesAsCSV() );
        }
        
        if ( properties != null )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            
            properties.list( pw );
            
            buffer.append( "\nProperties: " ).append( sw.toString() );
        }
        
        buffer.append( '\n' );
        
        return buffer.toString();
    }

    public String getParentArtifactId()
    {
        return parentArtifactId;
    }

    public void setParentArtifactId( String parentArtifactId )
    {
        this.parentArtifactId = parentArtifactId;
    }

}
