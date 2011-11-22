package org.codehaus.mojo.tomcat;

/*
 * Copyright 2005 Mark Hobson.
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

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Lists session information for a WAR in Tomcat.
 *
 * @goal sessions
 *
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class SessionsMojo
    extends AbstractWarCatalinaMojo
{
    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /*
     * @see org.codehaus.mojo.tomcat.AbstractCatalinaMojo#invokeManager()
     */
    protected void invokeManager()
        throws MojoExecutionException, TomcatManagerException, IOException
    {
        getLog().info( getMessage( "SessionsMojo.listSessions", getDeployedURL() ) );

        log( getManager().getSessions( getPath() ) );
    }
}
