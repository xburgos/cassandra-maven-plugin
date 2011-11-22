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

package org.codehaus.mojo.legaltools

import org.codehaus.mojo.groovy.GroovyMojoSupport

import java.util.zip.ZipFile
import java.util.zip.ZipException

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject
import org.apache.maven.artifact.Artifact

/**
 * Verify that legal files are in all attached zip-encoded artifacts.
 *
 * @goal verify-legal-files
 * @phase verify
 *
 * @version $Id$
 */
class VerifyLegalFilesMojo
    extends GroovyMojoSupport
{
    /**
     * When set to true, fail the build when no legal files are found.
     *
     * @parameter default-value="false"
     */
    private boolean strict

    /**
     * The list of required legal files.
     *
     * @parameter
     */
    private String[] requiredFiles = [ 'LICENSE.txt', 'NOTICE.txt' ]

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project
    
    void execute() {
        def artifacts = []
        
        artifacts.add(project.artifact)
        artifacts.addAll(project.attachedArtifacts)
        
        artifacts.each { artifact ->
            // Some artifacts might not have files, so skip them
            if (artifact.file == null) {
                log.debug("Skipping artifact; no attached file: $artifact")
            }
            else {
                try {
                    ZipFile zip = new ZipFile(artifact.file)
                    // If not a zip file, then an exception would have been thrown
                    
                    log.info("Checking legal files in: ${artifact.file.name}")
                    
                    def containsLegalFiles = { basedir ->
                        for (name in requiredFiles) {
                            def filename = "${basedir}/${name}"
                            log.debug("Checking for: ${filename}")
                            
                            def entry = zip.getEntry(filename)
                            if (!entry) {
                                return false
                            }
                        }
                        
                        return true
                    }
                    
                    def checkLegalFiles = {
                        return containsLegalFiles('META-INF') ||
                               containsLegalFiles("${project.build.finalName}")
                    }
                    
                    if (!checkLegalFiles()) {
                        String msg = "Artifact does not contain any legal files: ${artifact.file.name}"
                        if (strict) {
                            throw new MojoExecutionException(msg)
                        }
                        else {
                            log.warn(msg)
                        }
                    }
                }
                catch (ZipException e) {
                    log.debug("Failed to check file for legal muck; ignoring: ${artifact.file}", e)
                }
            }
        }
    }
}
