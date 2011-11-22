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

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject

import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

/**
 * Verify that legal headers on source files are valid.
 *
 * @goal verify-legal-headers
 * @phase verify
 *
 * @version $Id$
 */
class VerifyLegalHeadersMojo
    extends GroovyMojoSupport
{
    /**
     * Set of header styles to verify sources.
     *
     * @parameter
     * @requried
     */
    private HeaderStyle[] styles
    
    /**
     * When set to true, fail the build when no legal headers are found.
     *
     * @parameter default-value="false"
     */
    private boolean strict

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project
    
    void execute() {
        def fsm = new FileSetManager(log, log.debugEnabled)
        
        styles.each { style ->
            style.validate()
            
            def headerLines = style.loadLines()
            
            def hasValidHeader = { file ->
                def reader = file.newReader()
                
                if (style.preHeaderIgnoreLines > 0) {
                    for (i in 1..style.preHeaderIgnoreLines) {
                        def line = reader.readLine()
                        
                        println "Ignoring line: $line"
                    }
                }
                
                try {
                    for (expectLine in headerLines) {
                        def line = reader.readLine()
                        if (line == null) {
                            return false
                        }
                        if (line != expectLine) {
                            return false
                        }
                    }
                }
                finally {
                    reader.close()
                }
                
                return true
            }
            
            style.sources.each { fileSet ->
                def basedir = new File(fileSet.directory)
                def includes = fsm.getIncludedFiles(fileSet)
                
                includes.each { filename ->
                    log.info("Checking: $filename")
                    
                    if (!hasValidHeader(new File(basedir, filename))) {
                        String msg = "Source does not contain valid legal headers: $filename"
                        if (strict) {
                            throw new MojoExecutionException(msg)
                        }
                        else {
                            log.warn(msg)
                        }
                    }
                }
            }
        }
    }
}
