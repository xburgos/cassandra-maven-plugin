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

package org.codehaus.mojo.shitty

import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

/**
 * Cleans generated output from super helpful integration tests.
 *
 * @goal clean
 * @phase validate
 *
 * @version $Id$
 */
class CleanMojo
    extends ShittyMojoSupport
{
    /**
     * Extra files to be deleted in addition to the default directories.
     *
     * @parameter
     */
    FileSet[] filesets
    
    //
    // Mojo
    //
    
    void execute() {
        def fsm = new FileSetManager(log, log.debugEnabled)
        
        // Then if given delete the additional files specified by the filesets
        getFilesets().each { fileset ->
            fileset = resolveFileSet(fileset)
            
            try {
                fsm.delete(fileset)
            }
            catch (Exception e) {
                fail("Failed to clean files from: $baesdir; $e.message", e)
            }
        }
    }
    
    private FileSet[] getFilesets() {
        // If no filesets were configured, then setup the default
        if (!filesets) {
            def fileset = new FileSet()
            fileset.directory = 'src/it'
            fileset.addInclude('**/target')
            fileset.addInclude('**/build.log')
            
            return [ fileset ]
        }
        
        return filesets
    }
}
