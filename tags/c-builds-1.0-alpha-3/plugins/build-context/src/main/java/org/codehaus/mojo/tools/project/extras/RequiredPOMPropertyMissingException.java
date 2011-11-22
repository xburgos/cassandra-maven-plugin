package org.codehaus.mojo.tools.project.extras;

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

import org.apache.maven.project.MavenProject;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


public class RequiredPOMPropertyMissingException
    extends Exception
{

    private static final long serialVersionUID = 1L;
    private final MavenProject project;
    private final Set missingProperties;
    private final String longMessage;

    public RequiredPOMPropertyMissingException( MavenProject project, String property, Throwable cause )
    {
        super( "POM property: " + property + " is missing in project: " + project.getId(), cause );
        this.project = project;
        this.missingProperties = Collections.singleton( property );
        this.longMessage = formatLongMessage( this.missingProperties, project );
    }

    public RequiredPOMPropertyMissingException( MavenProject project, String property )
    {
        super( "POM property: " + property + " is missing in project: " + project.getId() );
        this.project = project;
        this.missingProperties = Collections.singleton( property );
        this.longMessage = formatLongMessage( this.missingProperties, project );
    }
    
    public RequiredPOMPropertyMissingException( MavenProject project, Set missingProperties )
    {
        super( "POM property: " + join( missingProperties, "," ) + " are missing in project: " + project.getId() );
        this.project = project;
        this.missingProperties = missingProperties;
        this.longMessage = formatLongMessage( missingProperties, project );
    }
    
    public MavenProject getProject()
    {
        return project;
    }
    
    public Set getMissingProperties()
    {
        return missingProperties;
    }
    
    public String getLongMessage()
    {
        return longMessage;
    }
    
    private static String formatLongMessage( Set missingProperties, MavenProject project )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "Missing one or more POM properties. The following is required in your POM:\n\n" );
        buffer.append( "<properties>\n" );
        
        for ( Iterator it = missingProperties.iterator(); it.hasNext(); )
        {
            String property = (String) it.next();
            buffer.append("  <").append(property).append(">VALUE</").append(property).append( ">\n" );
        }
        
        buffer.append( "</properties>\n" );
        
        buffer.append( "\n*** NOTE: Please verify that the POM for: " + project.getId() + " is in at least one of your repositories. ***\n" );
        
        return buffer.toString();
    }

    private static String join( Set properties, String separator )
    {
        StringBuffer buffer = new StringBuffer();
        
        for ( Iterator it = properties.iterator(); it.hasNext(); )
        {
            String property = (String) it.next();
            
            buffer.append( property );
            
            if ( it.hasNext() )
            {
                buffer.append( separator );
            }
        }
        
        return buffer.toString();
    }

}
