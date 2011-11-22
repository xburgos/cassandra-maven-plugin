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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


public class RequiredPOMPropertyChecker
{
    
    public static void checkForRequiredPOMProperty( MavenProject project, String property )
        throws RequiredPOMPropertyMissingException
    {
        Properties projectProperties = project.getProperties();
        
        if ( projectProperties == null || !projectProperties.containsKey( property ) )
        {
            throw new RequiredPOMPropertyMissingException( project, property );
        }
    }
    
    public static void checkForRequiredPOMProperties( MavenProject project, Collection properties )
    throws RequiredPOMPropertyMissingException
    {
        // don't check a given property twice.
        Set propsToCheck = new HashSet( properties );
        
        Set missingProperties = new HashSet();
        
        Properties projectProperties = project.getProperties();
        
        if ( projectProperties == null )
        {
            missingProperties.addAll( propsToCheck );
        }
        else
        {
            for ( Iterator it = propsToCheck.iterator(); it.hasNext(); )
            {
                String property = (String) it.next();
                
                if ( !projectProperties.containsKey( property ) )
                {
                    missingProperties.add( property );
                }
            }
        }
        
        if ( !missingProperties.isEmpty() )
        {
            throw new RequiredPOMPropertyMissingException( project, missingProperties );
        }
    }

}
