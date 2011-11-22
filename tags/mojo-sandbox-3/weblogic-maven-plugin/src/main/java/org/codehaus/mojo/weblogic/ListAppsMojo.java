package org.codehaus.mojo.weblogic;

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

import org.apache.maven.plugin.MojoExecutionException;

import weblogic.Deployer;

/**
 * List the atifacts on Weblogic server(s) or cluster(s).
 * 
 * @author <a href="mailto:scott@theryansplace.com">Scott Ryan</a>
 * @version $Id$
 * @description This mojo will start a component (EAR, WAR) on a server or group of servers.
 * @goal listapps
 */
public class ListAppsMojo extends DeployMojoBase
{

    /**
     * This method will list the artifacts on a server(s) or cluster(s).
     * 
     * @throws MojoExecutionException
     *             Thrown if we fail to obtain a Weblogic deployment instance.
     */
    public void execute() throws MojoExecutionException
    {

        if ( getLog().isInfoEnabled() )
        {
            getLog().info( "Weblogic list apps beginning with parameters " + this.toString() );
        }

        // get the basic parameters
        String[] parameters = this.getInputParameters( "listapps" );

        try
        {

            // Deploy with the parameters
            Deployer deployer = new Deployer( parameters );
            deployer.run();
        }
        catch ( Exception ex )
        {
            getLog().error( "Exception encountered during list apps ", ex );
            throw new MojoExecutionException( "Exception encountered during listapps", ex );
        }

        if ( getLog().isInfoEnabled() )
        {
            getLog().info( "Weblogic list apps successful " );
        }

    }

}
