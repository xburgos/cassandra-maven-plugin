/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codehaus.mojo.anthill3.codestation

import org.codehaus.mojo.groovy.GroovyMojoSupport

/**
 * Support for Anthill3 Codestation mojos.
 *
 * @version $Id$
 */
abstract class CodestationMojoSupport
    extends GroovyMojoSupport
{
    /**
     * The URL of the Anthill3 server.
     *
     * @parameter
     * @required
     */
    protected URL serverUrl
    
    /**
     * Flag to indicate if the Codestation client should perform SSL certificate checking.
     *
     * @parameter default-value="true"
     */
    protected boolean checkCertificate
    
    //
    // TODO: Figure out how to pull this from settings...
    //
    
    /**
     * The username to authenticate with the server.
     *
     * @parameter
     */
    protected String username
    
    /**
     * The password to authenticate with the server.
     *
     * @parameter
     */
    protected String password
    
    /**
     * Flag to enable debug output.
     *
     * @parameter default-value="false"
     */
    protected boolean debug
    
    //
    // TODO: Add the other connection-related params: noCache, offline, verifyArtifacts, force, proxy*
    //
    
    /**
     * Create a Codestation client.
     */
    protected CodestationClient createClient() {
        CodestationClient client = new CodestationClient(serverUrl, !checkCertificate)
        client.debug = debug
        
        if (username) {
            client.username = username
        }
        if (password) {
            client.password = password
        }
        
        return client
    }
}
