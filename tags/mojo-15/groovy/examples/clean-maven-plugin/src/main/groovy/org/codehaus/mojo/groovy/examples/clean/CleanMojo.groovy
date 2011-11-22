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

package org.codehaus.mojo.groovy.examples.clean

import org.codehaus.mojo.groovy.GroovyMojoSupport

import org.apache.maven.project.MavenProject

import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

/**
 * Cleans build generated output.
 *
 * @goal clean
 *
 * @version $Id$
 */
class CleanMojo
    extends GroovyMojoSupport
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project
    
    /**
     * Extra files to be deleted in addition to the default directories.
     *
     * @parameter
     */
    FileSet[] filesets
    
    /**
     * Sets whether the plugin runs in verbose mode.
     *
     * @parameter expression="${clean.verbose}" default-value="false"
     */
    boolean verbose
    
    /**
     * Sets whether the plugin should follow Symbolic Links to delete files.
     *
     * @parameter expression="${clean.followSymLinks}" default-value="false"
     */
    boolean followSymLinks
    
    void execute() {
        def fsm = new FileSetManager(log, verbose)
        
        def rmdir = { dir ->
            if (dir) {
                def fileset = new FileSet(directory: dir.path, followSymlinks: followSymLinks)
                fileset.addInclude('**/**')
                
                try {
                    log.info("Deleting directory: $dir")
                    fsm.delete(fileset)
                }
                catch (Exception e) {
                    fail("Failed to delete directory: ${dir}; Reason: ${e.message}", e)
                }
            }
        }
        
        // First delete the standard directories
        rmdir(project.build.directory)
        rmdir(project.build.outputDirectory)
        rmdir(project.build.testOutputDirectory)
        
        // Not sure if reporting is always here, so safe navigate to it
        rmdir(project.reporting?.outputDirectory)
        
        // Then if given delete the additional files specified by the filesets
        if (filesets) {
            filesets.each { fileset ->
                log.info("Deleting $fileset")
                try {
                    fsm.delete(fileset)
                }
                catch (Exception e) {
                    fail("Failed to delete directory: ${fileset.directory}; Reason: ${e.message}", e)
                }
            }
        }
    }
}
