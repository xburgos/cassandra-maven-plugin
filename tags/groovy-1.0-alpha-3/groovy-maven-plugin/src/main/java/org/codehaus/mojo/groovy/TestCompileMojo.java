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

package org.codehaus.mojo.groovy;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Compiles <a href="http://groovy.codehaus.org">Groovy</a> <em>test</em> sources.
 *
 * @goal testCompile
 * @phase test-compile
 * @requiresDependencyResolution test
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class TestCompileMojo
    extends CompilationMojoSupport
{
    /**
     * The directory where generated Java class files will be placed.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Flag to allow test compiliation to be skipped.
     *
     * @parameter expression="${maven.test.skip}" default-value="false";
     */
    private boolean skip;
    
    //
    // CompilationMojoSupport Hooks
    //
    
    protected List getProjectClasspathElements() throws DependencyResolutionRequiredException {
        return project.getTestClasspathElements();
    }

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    protected FileSet[] getDefaultSources() {
        FileSet set = new FileSet();

        File basedir = new File(project.getBasedir(), "src/test/groovy");
        set.setDirectory(basedir.getAbsolutePath());
        set.addInclude("**/*.groovy");

        return new FileSet[] { set };
    }
    
    protected void doExecute() throws Exception {
        if (skip) {
            log.info("Test compiliation is skipped");
        }
        else {
            super.doExecute();
        }
    }
}
