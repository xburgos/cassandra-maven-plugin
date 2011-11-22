package org.codehaus.mojo.tools.project.extras;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.project.MavenProject;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Exceptions for POM property checking
 *
 */
public class RequiredPOMPropertyMissingException
    extends Exception
{

    /**
     * Serializable objects define <code>serialVersionUID</code> per convention
     */
    private static final long serialVersionUID = 1L;
    /**
     * Maven project which had the missing properties
     */
    private final MavenProject project;
    /**
     * The properties found to be missing from the project
     */
    private final Set < String > missingProperties;
    /**
     * A formatted message detailing the detected property deficiency in the maven project
     */
    private final String longMessage;

    /**
     * Exception
     * 
     * @param property property found in the project to be missing
     * @param project project which encountered the problem
     * @param cause The original exception thrown
     */
    public RequiredPOMPropertyMissingException( MavenProject project, String property, Throwable cause )
    {
        super( "POM property: " + property + " is missing in project: " + project.getId(), cause );
        this.project = project;
        this.missingProperties = Collections.singleton( property );
        this.longMessage = formatLongMessage( this.missingProperties, project );
    }

    /**
     * Exception
     * 
     * @param property property found in the project to be missing
     * @param project project which encountered the problem
     */
    public RequiredPOMPropertyMissingException( MavenProject project, String property )
    {
        super( "POM property: " + property + " is missing in project: " + project.getId() );
        this.project = project;
        this.missingProperties = Collections.singleton( property );
        this.longMessage = formatLongMessage( this.missingProperties, project );
    }

    /**
     * Exception
     * 
     * @param missingProperties properties found in the project to be missing
     * @param project project which encountered the problem
     */

    public RequiredPOMPropertyMissingException( MavenProject project, Set < String > missingProperties )
    {
        super( "POM property: " + join( missingProperties, "," ) + " are missing in project: " + project.getId() );
        this.project = project;
        this.missingProperties = missingProperties;
        this.longMessage = formatLongMessage( missingProperties, project );
    }

    /**
     * Returns the MavenProject
     * 
     * @return the project which had the missing properties
     */
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * returns a set of missing properties
     *  
     * @return the missing properties found to be missing in the project
     */
    public Set < String > getMissingProperties()
    {
        return missingProperties;
    }

    /**
     * Returns the long message for the exception being thrown
     * 
     * @return a long message detailing the reason an exception was thrown
     */
    public String getLongMessage()
    {
        return longMessage;
    }

    /**
     * Formats a long format message for the exception
     * 
     * @param missingProperties properties found in the project to be missing
     * @param project project which encountered the problem
     * @return A long formatted message
     */
    private static String formatLongMessage( Set < String > missingProperties, MavenProject project )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "Missing one or more POM properties. The following is required in your POM:\n\n" );
        buffer.append( "<properties>\n" );
        
        for ( Iterator < String > it = missingProperties.iterator(); it.hasNext(); )
        {
            String property = it.next();
            buffer.append( "  <" ).append( property ).append( ">VALUE</" ).append( property ).append( ">\n" );
        }
        
        buffer.append( "</properties>\n" );
        
        buffer.append( "\n*** NOTE: Please verify that the POM for: "
            + project.getId()
            + " is in at least one of your repositories. ***\n" );
        
        return buffer.toString();
    }

    /**
     * Takes a list of properties and formats it into a String
     * 
     * @param properties String value used to join together with a separater inserted between elements of the Set
     * @param separator String used between property
     * @return String of joined properties with separators
     */
    private static String join( Set < String > properties, String separator )
    {
        StringBuffer buffer = new StringBuffer();
        
        for ( Iterator < String > it = properties.iterator(); it.hasNext(); )
        {
            String property = it.next();
            
            buffer.append( property );
            
            if ( it.hasNext() )
            {
                buffer.append( separator );
            }
        }
        
        return buffer.toString();
    }

}
