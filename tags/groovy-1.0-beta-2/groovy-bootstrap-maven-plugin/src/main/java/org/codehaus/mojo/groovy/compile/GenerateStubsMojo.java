/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.mojo.groovy.compile;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Generate Java stubs from Groovy sources.
 *
 * @goal generateStubs
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @since 1.0-beta-2
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class GenerateStubsMojo
    extends StubGenMojoSupport
{
    /**
     * The directory where generated Java stub files will be placed.
     *
     * @parameter default-value="${project.build.directory}/groovy-stubs/main"
     * @required
     */
    private File outputDirectory;

    //
    // StubGenMojoSupport Hooks
    //

    protected List getProjectClasspathElements() throws DependencyResolutionRequiredException {
        return project.getCompileClasspathElements();
    }

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    protected List getSourceRoots() {
        return project.getCompileSourceRoots();
    }

    protected FileSet[] getDefaultSources() {
        FileSet set = new FileSet();

        File basedir = new File(project.getBasedir(), "src/main/groovy");
        set.setDirectory(basedir.getAbsolutePath());
        set.addInclude("**/*.groovy");
        set.addInclude("**/*.java");

        return new FileSet[] { set };
    }
}
