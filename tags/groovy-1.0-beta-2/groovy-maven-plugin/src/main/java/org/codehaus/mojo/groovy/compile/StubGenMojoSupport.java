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

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Support for Java stub generation mojos.
 *
 * <p>
 * Stub generation basically parses Groovy sources, and then creates the bare-minimum
 * Java source equivilent so that the maven-compiler-plugin's compile and testCompile
 * goals can execute and resolve Groovy classes that may be referenced by Java sources.
 * </p>
 *
 * <p>
 * This is important, since our compile and testCompile goals execute *after* the 
 * normal Java compiler does.
 * </p>
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public abstract class StubGenMojoSupport
    extends CompileMojoSupport
{
    private void generate(final FileSet[] sources) throws Exception {
        assert sources != null;
        assert sources.length > 0;

        CompilerConfiguration config = getCompilerConfiguration();
        GroovyClassLoader gcl = createGroovyClassLoader();
        JavaStubCompilationUnit compilation = new JavaStubCompilationUnit(config, gcl, getOutputDirectory());

        FileSetManager fsm = new FileSetManager(log, log.isDebugEnabled());

        int count = 0;

        for (int i=0; i<sources.length; i++) {
            FileSetUtils.validate(sources[i]);

            File basedir = new File(sources[i].getDirectory());

            //
            // NOTE: We have to add the src dirs here now... so that compiler:* can find them...
            //
            //       Blah, might have to drop the fileset thing and just go with includes/excludes
            //       and/or just do a plexus compiler impl
            //

            //
            // TODO: Make this optional (but enabled by default)
            //

            // Hook up as a source root so other plugins (like the m-compiler-p) can process anything in here if needed
            addSourceRoot(basedir);

            String[] includes = fsm.getIncludedFiles(sources[i]);

            log.debug("Adding sources from: " + basedir);

            for (int j=0; j < includes.length; j++) {
                log.debug("    " + includes[j]);

                File file = new File(basedir, includes[j]);
                compilation.addSourceFile(file);
                
                // Increment the count for each non/java src we found
                if (!includes[j].endsWith(".java")) {
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Generating " + count + " Java stub" + (count > 1 ? "s" : "") + " to " + getOutputDirectory());

            // Generate the stubs
            compilation.compile(Phases.CONVERSION);

            // And hook up for normal compile
            addSourceRoot(getOutputDirectory());
        }
        else {
            log.info("No Groovy sources found for stub generation");
        }
    }

    protected void doExecute() throws Exception {
        generate(sources != null ? sources : getDefaultSources());
    }
}
