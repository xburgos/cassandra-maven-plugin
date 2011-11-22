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

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Support for compile mojos that generate classes.
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public abstract class ClassGenMojoSupport
    extends CompileMojoSupport
{
    /**
     * Sets the encoding to be used when reading source files.
     *
     * @parameter expression="${sourceEncoding}" default-value="${file.encoding}"
     */
    private String sourceEncoding;

    /**
     * Turns verbose operation on or off.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Turns debugging operation on or off.
     *
     * @parameter expression="${debug}" default-value="false"
     */
    private boolean debug;

    /**
     * Enable compiler to report stack trace information if a problem occurs.
     *
     * @parameter expression="${stacktrace}" default-value="false"
     */
    private boolean stacktrace;

    /**
     * Sets the error tolerance, which is the number of non-fatal errors (per unit)
     * that should be tolerated before compilation is aborted.
     *
     * @parameter expression="${tolerance}" default-value="0"
     */
    private int tolerance;

    /**
     * Sets the name of the base class for scripts. It must be a subclass of <tt>groovy.lang.Script</tt>.
     *
     * @parameter expression="${scriptBaseClassname}"
     */ 
    private String scriptBaseClassname;

    /**
     * Set the default extention for Groovy script source files.
     *
     * @parameter expression="${defaultScriptExtension}" default-value=".groovy"
     */
    private String defaultScriptExtension;

    protected CompilerConfiguration createCompilerConfiguration() throws Exception {
        CompilerConfiguration config = super.createCompilerConfiguration();
        
        //
        // TODO: See what else from: http://groovy.codehaus.org/apidocs/org/codehaus/groovy/control/CompilerConfiguration.html
        //       we might want to allow to be configured here
        //

        config.setDebug(stacktrace);
        config.setSourceEncoding(sourceEncoding);
        config.setVerbose(verbose);
        config.setDebug(debug);
        config.setTolerance(tolerance);
        config.setTargetDirectory(getOutputDirectory());

        if (scriptBaseClassname != null) {
            config.setScriptBaseClass(scriptBaseClassname);
        }
        if (defaultScriptExtension != null) {
            config.setDefaultScriptExtension(defaultScriptExtension);
        }

        return config;
    }

    private void compile(final FileSet[] sources) throws Exception {
        assert sources != null;
        assert sources.length > 0;
        
        CompilerConfiguration config = getCompilerConfiguration();
        GroovyClassLoader gcl = createGroovyClassLoader();
        CompilationUnit compilation = new CompilationUnit(config, null, gcl);

        FileSetManager fsm = new FileSetManager(log, log.isDebugEnabled());

        int count = 0;

        for (int i=0; i<sources.length; i++) {
            FileSetUtils.validate(sources[i]);
            
            File basedir = new File(sources[i].getDirectory());
            String[] includes = fsm.getIncludedFiles(sources[i]);

            log.debug("Adding sources from: " + basedir);

            for (int j=0; j < includes.length; j++) {
                log.debug("    " + includes[j]);
                
                File file = new File(basedir, includes[j]);
                compilation.addSource(file);
                count++;
            }
        }

        if (count > 0) {
            log.info("Compiling " + count + " Groovy source file" + (count > 1 ? "s" : "") + " to " + config.getTargetDirectory());
            compilation.compile();
        }
        else {
            log.info("No Groovy sources to compile");
        }
    }
    
    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        compile(sources != null ? sources : getDefaultSources());
    }
}
