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

/**
 * Resolve artifacts from an Anthill3 Codestation repository.
 *
 * @goal resolve-artifacts
 *
 * @version $Id$
 */
class ResolveArtifactsMojo
    extends CodestationMojoSupport
{
    /**
     * The name of the project to resolve artifacts from.
     *
     * @parameter
     * @required
     */
    private String projectName
    
    /**
     * The name of the workflow to resolve artifacts from.
     *
     * @parameter
     * @required
     */
    private String workflowName
    
    /**
     * The stamp of the buildlife to resolve artifacts from. Can not be used if <tt>buildLifeId</tt> is specified.
     *
     * @parameter
     */
    private String stamp
    
    /**
     * The id of the buildlife to resolve artifacts from. Can not be used if <tt>stamp</tt> is specified.
     *
     * @parameter
     */
    private Long buildLifeId
    
    /**
     * The name of the artifact set to resolve artifacts from.
     *
     * @parameter
     * @required
     */
    private String setName
    
    /**
     * The directory where resolved artifacts are put.
     *
     * @parameter expression="${project.build.directory}/artifacts"
     * @required
     */
    private File outputDirectory
    
    /**
     * Fetches the project document based on either stamp or buildlife id.
     */
    private def lookupProject(client) {
        assert client
        
        // Complain of both stamp and buildLifeId are set
        if (stamp && buildLifeId) {
            fail("Only one of 'stamp' or 'buildLifeId' is permitted")
        }
        
        if (stamp) {
            return client.projectLookup(projectName, workflowName, stamp)
        }
        else if (buildLifeId) {
            return client.projectLookup(projectName, workflowName, buildLifeId)
        }
        else {
            fail("Missing either 'stamp' or 'buildLifeId' parameters")
        }
    }
    
    void execute() {
        def client = createClient()
        
        // The client works on a workingDir, and then relative target directories, so to avoid
        // needing to configure both, set the parent as the workingDir, and then use the child's
        // name for the relative bits below
        client.workingDir = outputDirectory.parentFile.canonicalPath
        
        def doc = lookupProject(client)
        def profileId = client.extractProfileId(doc)
        log.debug("Profile ID: ${profileId}")
        
        def _buildLifeId = client.extractBuildlifeId(doc)
        log.debug("BuildLife ID: ${_buildLifeId}")
        
        //
        // FIXME: This is bound to cause OOME for large artifact sets
        //
        
        def files = client.retrieveArtifacts(projectName, profileId, null, _buildLifeId, setName, outputDirectory.name)
        
        log.info("Resolved ${files.size()} file(s)")
        
        if (log.debugEnabled) {
            int i=0
            files.each {
                log.debug("[${i++}] $it")
            }
        }
    }
}

