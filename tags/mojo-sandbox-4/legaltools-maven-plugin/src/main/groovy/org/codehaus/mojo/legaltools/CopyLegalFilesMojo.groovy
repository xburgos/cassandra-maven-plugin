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
 * Copy legal files for inclusion into generated jars.
 *
 * @goal copy-legal-files
 * @phase validate
 *
 * @version $Id$
 */
class CopyLegalFilesMojo
    extends GroovyMojoSupport
{
    /**
     * The default includes when no fileset is configured.
     */
    private static final List DEFAULT_INCLUDES = [
        'LICENSE.txt',
        'LICENSE',
        'NOTICE.txt',
        'NOTICE',
        'DISCLAIMER.txt',
        'DISCLAIMER'
    ]

    /**
     * Directory to copy legal files into.
     *
     * @parameter expression="${project.build.outputDirectory}/META-INF"
     * @required
     */
    private File outputDirectory
    
    /**
     * The set of legal files to be copied.  Default fileset includes: LICENSE[.txt], NOTICE[.txt] and DISCLAIMER[.txt].
     *
     * @parameter
     */
    private FileSet fileset
    
    /**
     * When set to true, fail the build when no legal files are found.
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
        if (project.packaging == 'pom') {
            log.debug('Skipping copy for packaging: pom')
            return
        }
        
        def fsm = new FileSetManager(log, log.debugEnabled)
        
        // If no fileset was configured, then use the default
        if (fileset == null) {
            fileset = new FileSet()
            fileset.setIncludes(DEFAULT_INCLUDES)
        }
        
        // Default to the projects directory if non given
        if (fileset.directory == null) {
            fileset.directory = project.basedir
        }
        
        def includes = fsm.getIncludedFiles(fileset)
        
        if (includes.length == 0) {
            def msg = 'No legal files found to copy'
            if (strict) {
                throw new MojoExecutionException(msg)
            }
            else {
                log.warn(msg)
            }
            
            return
        }
        
        ant.mkdir(dir: outputDirectory)
        
        ant.copy(todir: outputDirectory) {
            fileset(dir: fileset.directory) {
                includes.each {
                    include(name: "$it")
                }
            }
        }
    }
}
