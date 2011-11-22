package org.codehaus.mojo.tools.context;

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

/*
NOTES!!

John first wrote this using a Maven "BuildContextManager" around Feb '07
That internal object on the maven 2.1 (aka 3.0) codebase was deleted mid '07.
Lee reimplemented this using MavenSession in early '08.
When 2.1 was renamed to 3.0, Lee and his users decided to backport to 
Maven 2.0.x and found MavenSession to be quite useless.  Maven 2.1.0-M1
was similar to 2.0.x so a cheezy "pride free" cookie file was created to
store necessary build state items.  When MavenSession works, it is quite
easy to flip back to using memory instead of a filesystem and this 
explanation/comment can be removed.

Function over form...

Works on Mac and Linux.
*/

import org.apache.maven.execution.MavenSession;
//import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
//import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
//import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.context.Context;
//import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.HashMap;
//import java.util.Map;
//import java.util.Date;
//import java.text.DateFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * BuildAdvisor is a simple object storage/retrieval mechanism typically used
 * to share state between cooperating plugins.  Currently implemented as a temp file
 * which will be converted into a MavenSession implementation with Maven 3.0.
 * No support for delete.
 * @plexus.component role="org.codehaus.mojo.tools.context.BuildAdvisor" role-hint="default"
 */
public class BuildAdvisor implements Contextualizable, LogEnabled
{
    /**
     * Defines the Plexus ROLE, which is a standard plexus setup requirement
     */
    public static final String ROLE = BuildAdvisor.class.getName();

    /**
     * Defines the Plexus ROLE_HINT which allows plexus to chose one implementation
     * of a component vs another implementation
     */
    public static final String ROLE_HINT = "default";

    /**
     * Constant
     */
    private static final String IS_PROJECT_BUILD_SKIPPED = "is-project-build-skipped";

    /**
     * cookie storage temp file, used for Maven 2.x.  Will move to MavenSession for Maven 3.0
     */
    private static File myKeyFile = null;

    /**
     * The plexus container context
     */
    private Context context;

    /**
     * The plexus container that is invoking BuildAdvisor
     */
    private PlexusContainer container;
    
    private Logger logger;

    /**
     * Method called during plexus container initialization
     */
    public BuildAdvisor()
    {
        // used for Plexus init.
    }
    /**
     * Silly temp file implementation of object storage
     */
    private String getFilename( MavenSession session ) throws IOException
    {
        if ( myKeyFile == null )
        {
            myKeyFile = File.createTempFile( "cbuild-keyfile-", ".bin" );
            myKeyFile.deleteOnExit();
        }
        String name = myKeyFile.getName();
        String key = name.substring( 15, name.length() - 4 );
        return "/tmp/cbuild-context-" + key;
    }

    /**
     * Get the cookie file setup for readback
     */
    private ObjectInputStream getInput( MavenSession session ) throws IOException
    {
        FileInputStream fis = new FileInputStream( getFilename( session ) + ".bin" );
        ObjectInputStream ois = new ObjectInputStream( fis );
        return ois;
    }

    /**
     * Get the cookie file setup for writting
     */
    private ObjectOutputStream getOutput( MavenSession session ) throws IOException
    {
        FileOutputStream fos = new FileOutputStream( getFilename( session ) + ".bin" );
        ObjectOutputStream oos = new ObjectOutputStream( fos );
        return oos;
    }

    /**
     * Helper function to store the need to skip the build this time around
     */
    public void skipProjectBuild( MavenSession session )
    {
        this.store( session, IS_PROJECT_BUILD_SKIPPED, Boolean.TRUE );
    }

    /**
     * Helper function to check if the project build is to be skipped
     */
    public boolean isProjectBuildSkipped( MavenSession session )
    {
        return Boolean.TRUE.equals( (Boolean) this.retrieve( session, IS_PROJECT_BUILD_SKIPPED ) );
    }

    /**
     * Store an object which can be read back by a pluggin later
     */
    public void store( MavenSession session, String key, Object val )
    {
        // Silly temp file implementation
        HashMap ctx = null;
        try
        {
            ObjectInputStream ois = getInput( session );
            ctx = ( HashMap ) ois.readObject();
            ois.close();
        }
        catch ( IOException e )
        {
            getLogger().debug( "BuildAdvisor: empty session" );
        }
        catch ( ClassNotFoundException e )
        {
            getLogger().debug( "BuildAdvisor: empty session" );
        }

        try
        {
            ObjectOutputStream oos = getOutput( session );
            if ( ctx == null )
            {
                ctx = new HashMap();
            }
            ctx.put( "cbuilds:" + session.getCurrentProject().getId() + ":" + key, val );
            oos.writeObject( ctx );

            if ( getLogger().isDebugEnabled() )
            {
                // Courtesy text dump of cookies for you developer types
                FileOutputStream fos = new FileOutputStream( getFilename( session ) + ".txt" );
                fos.write( ctx.toString().getBytes() );
                fos.close();
            }

            oos.close();
        }
        catch ( IOException e )
        {
            return;
        }

        String logval = "null";
        if ( val != null )
        {
            logval = val.toString();
            if ( logval == null ) 
            {
                logval = val.getClass().getName();
            }
        }
        getLogger().debug( "BuildAdvisor: set cookie -> \"cbuilds:"
            + session.getCurrentProject().getId() + ":" + key
            + "\"  :  \"" + logval + "\"" );
    }

    /**
     * Retrieve an object that was stored previously into BuildAdvisor
     */
    public Object retrieve( MavenSession session, String key )
    {
        Object retval = null;
        try
        {
            ObjectInputStream ois = getInput( session );
            HashMap ctx = (HashMap) ois.readObject();
            retval = ctx.get( "cbuilds:" + session.getCurrentProject().getId() + ":" + key );
            ois.close();
            // TODO: Maven 3.0 implementation
            //retval = context.get( "cbuilds:" + session.getCurrentProject().getId() + ":" + key );
            String myval = retval == null ? "null" : retval.toString();
            getLogger().debug( "BuildAdvisor: read cookie -> \"cbuilds:"
                + session.getCurrentProject().getId() + ":" + key
                + "\"  :  \"" + myval + "\"" );
        }
        //catch (ContextException e) { return null; }
        catch ( FileNotFoundException e )
        {
            return null;
        }
        catch ( IOException e )
        {
            return null;
        }
        catch ( ClassNotFoundException e )
        {
            return null;
        }

        return retval;
    }

    /**
     * Standard plexus mechanism to retrieve the invoking Plexus container calling BuildAdvisor
     */
    public void contextualize( Context mycontext ) throws ContextException
    {
        this.context = mycontext;
        this.container = (PlexusContainer) mycontext.get( PlexusConstants.PLEXUS_KEY );
    }

    /**
     * Standard plexus mechanism to enable logging
     */
    public void enableLogging( Logger mylogger )
    {
        this.logger = mylogger;
    }

    /**
     * Standard plexus mechanism to get the logging object
     */
    protected Logger getLogger()
    {
        return logger;
    }

}
