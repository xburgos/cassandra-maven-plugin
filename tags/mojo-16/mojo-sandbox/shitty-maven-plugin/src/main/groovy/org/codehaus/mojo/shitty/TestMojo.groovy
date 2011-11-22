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

import org.apache.maven.settings.Settings

import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

import org.apache.commons.lang.time.StopWatch

import org.codehaus.plexus.util.Os

/**
 * Invoke child Maven builds to perform super helpful integration testing.
 *
 * @goal test
 * @phase integration-test
 *
 * @version $Id$
 */
class TestMojo
    extends ShittyMojoSupport
{
    /**
     * Definition of the projects to execute.
     *
     * @parameter
     */
    FileSet[] projects
    
    /**
     * The name of the project-specific file that contains the list of goals
     * to execute for that test.
     * 
     * @parameter default-value="goals.txt"
     */
    String goalsFile
    
    /**
     * The name of the project-specific file that contains the execution (system) 
     * properties to be defined for the test.
     * 
     * @parameter default-value="test.properties"
     */
    String propertiesFile
    
    /**
     * @parameter expression="${settings}"
     */
    Settings settings
    
    //
    // Mojo
    //
    
    void execute() {
        def fsm = new FileSetManager(log, log.debugEnabled)
        def failures = []
        def count = 0
        def suiteWatch = new StopWatch(); suiteWatch.start()
        
        // Run a build for each configured project
        getProjects().each { fileset ->
            fileset = resolveFileSet(fileset)
            def basedir = new File(fileset.directory)
            def includes = fsm.getIncludedFiles(fileset)
            def watch = new StopWatch()
            
            includes.each { filename ->
                count++
                watch.reset()
                
                def pomFile = new File(basedir, filename)
                def projectDir = pomFile.parentFile
                def buildLog = new File(projectDir, 'build.log')
                def goals = getGoals(projectDir)
                def properties = getProperties(projectDir)
                
                try {
                    ant.exec(
                        executable: mavenExecutable,
                        dir: projectDir,
                        failIfExecutionFails: true,
                        failonerror: true)
                    {
                        redirector(output: buildLog)
                        
                        arg(value: '-f')
                        arg(file: pomFile)
                        arg(value: '--batch-mode')
                        arg(value: '--errors')
                        
                        // If the current build is offline, then make the children builds offline too
                        if (settings.offline) {
                            arg(value: '--offline')
                        }
                        
                        // Add properties
                        properties.each { key, value ->
                            arg(value: "-D${key}=${value}")
                        }
                        
                        // Add goals
                        goals.each {
                            arg(value: it)
                        }
                        
                        // About to launch, provide some user feedback
                        log.info("Building $pomFile")
                        watch.start()
                    }
                    
                    //
                    // TODO: Add support for expected build status, so we can design tests
                    //       intended to fail, and verify they don't pass
                    //
                    
                    log.info("... SUCCESS ($watch)")
                }
                catch (Exception e) {
                    log.info("... FAILED ($watch): $e.message; See $buildLog for details")
                    failures << pomFile
                }
            }
        }
        
        // Only display the summary if at least one test was run
        if (count > 0) {
            log.info('')
            log.info('-' * 79)
            log.info("Test Summary ($suiteWatch)")
            log.info("    Passed: ${count - failures.size()}")
            log.info("    Failed: ${failures.size()}")
            log.info('-' * 79)
            log.info('')
            
            if (failures.size() != 0) {
                log.info('The following builds failed:')
                
                failures.each {
                    log.info("    * $it")
                }
                
                log.info('')
                
                fail("${failures.size()} of $count builds failed")
            }
        }
    }
    
    private FileSet[] getProjects() {
        // If no projects were configured, then setup the default
        if (!projects) {
            def fileset = new FileSet()
            fileset.directory = 'src/it'
            fileset.addInclude('*/pom.xml')
            
            return [ fileset ]
        }
        
        return projects
    }
    
    private File getMavenExecutable() {
        def path = System.properties['maven.home']
        if (!path) {
            // This should really never happen
            fail("Missing maven.home system property")
        }
        
        def home = new File(path)
        def cmd
        
        if (Os.isFamily('windows')) {
            cmd = new File(home, 'bin/mvn.bat')
        }
        else {
            cmd = new File(home, 'bin/mvn')
        }
        
        if (!cmd.exists()) {
            fail("Maven executable not found at: $cmd")
        }

        return cmd.canonicalFile
    }
    
    private List getGoals(File basedir) {
        assert basedir
        
        def file = new File(basedir, goalsFile)
        
        if (!file.exists()) {
            fail("Missing goals file ($goalsFile) in project directory: $basedir")
        }
        
        log.debug("Loading goals from: $file")
        
        def goals = []
        file.text.tokenize().each {
            goals << it
        }
        
        return goals
    }
    
    private Properties getProperties(File basedir) {
        assert basedir
        
        def props = new Properties()
        def file = new File(basedir, propertiesFile)
        
        if (file.exists()) {
            log.debug("Loading test properties from: $file")
            
            props.load(file.newInputStream())
        }
        
        return props
    }
}
