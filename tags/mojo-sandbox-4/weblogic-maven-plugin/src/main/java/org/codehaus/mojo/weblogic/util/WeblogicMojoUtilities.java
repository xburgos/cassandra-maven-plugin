package org.codehaus.mojo.weblogic.util;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
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
 */

import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

/**
 * This class contains some utilities that are useful during use of the Weblogic Mojo.
 * 
 * @author <a href="mailto:scott@theryansplace.com">Scott Ryan</a>
 * @version $Id: ListAppsMojo.java 2166 2006-07-18 21:32:16Z carlos $
 */
public class WeblogicMojoUtilities
{

    /**
     * Creates a new WeblogicMojoUtilities object.
     */
    private WeblogicMojoUtilities()
    {
        super();
    }

    /**
     * This method will contstruct the Admin URL to the given server.
     * 
     * @param inProtocol
     *            The protocol to contact the server with (i.e. t3 or http)
     * @param inServerName
     *            The name of the server to contact.
     * @param inServerPort
     *            The listen port for the server to contact.
     * @return The value of admin url.
     */
    public static String getAdminUrl( final String inProtocol, final String inServerName, final String inServerPort )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( inProtocol ).append( "://" );
        buffer.append( inServerName );
        buffer.append( ":" ).append( inServerPort );

        return buffer.toString();
    }

    /**
     * This method will make sure there is a type appended to the file name and if it is the appropriate type for the
     * project packaging. If the project packaging is ear the artifact must end in .ear. If the project packaging is war
     * then the artifact must end in .war. If the project packaging is ejb then the artifact must end in .jar.
     * 
     * @param inName
     *            The name of the artifact.
     * @param inProjectPackaging
     *            The type of packaging for this project.
     * 
     * @return The updated artifact name.
     */
    public static String updateArtifactName( final String inName, final String inProjectPackaging )
    {
        String newName = inName;
        // If project type is ear then artifact name must end in .ear
        if ( inProjectPackaging.equalsIgnoreCase( "ear" ) )
        {
            if ( !inName.endsWith( ".ear" ) )
            {
                newName = inName.concat( ".ear" );
            }
        }
        // If project type is war then artifact name must end in .war
        else if ( inProjectPackaging.equalsIgnoreCase( "war" ) )
        {
            if ( !inName.endsWith( ".war" ) )
            {
                newName = inName.concat( ".war" );
            }
        }

        // If project type is ejb then artifact name must end in .jar
        else if ( inProjectPackaging.equalsIgnoreCase( "ejb" ) )
        {
            if ( inName.endsWith( ".ejb" ) )
            {
                newName = inName.replaceAll( "\\.ejb", ".jar" );
            }
            else if ( !inName.endsWith( ".jar" ) )
            {
                newName = inName.concat( ".jar" );
            }
        }
        // Unsupported project type
        else
        {
            throw new IllegalArgumentException( "Unsupported project packaging " + inProjectPackaging );
        }
        return newName;

    }

    /**
     * This method will get the dependencies from the pom and construct a classpath string to be used to run a mojo
     * where a classpath is required.
     * 
     * @param inArtifacts
     *            The Set of artifacts for the pom being run.
     * @return A string representing the current classpath for the pom.
     */
    public static String getDependencies( final Set inArtifacts )
    {

        if ( inArtifacts == null || inArtifacts.isEmpty() )
        {
            return "";
        }
        // Loop over all the artifacts and create a classpath string.
        Iterator iter = inArtifacts.iterator();

        StringBuffer buffer = new StringBuffer();
        if ( iter.hasNext() )
        {
            Artifact artifact = ( Artifact ) iter.next();
            buffer.append( artifact.getFile() );

            while ( iter.hasNext() )
            {
                artifact = ( Artifact ) iter.next();
                buffer.append( System.getProperty( "path.separator" ) );
                buffer.append( artifact.getFile() );
            }
        }

        return buffer.toString();
    }

}
