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
import java.util.ArrayList;
import java.net.URL;
import java.net.URLClassLoader;

import groovy.lang.GroovyClassLoader;

import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Support for compiliation mojos.
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public abstract class CompilationMojoSupport
    extends MojoSupport
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

    /**
     * Additional artifacts to add to the classpath (in addition to the classpath
     * which is picked up from the executing poms configuration).
     *
     * @parameter
     */
    private ArtifactItem[] classpath;

    /**
     * One or more filesets of Groovy sources to be compiled.  If not specified, then
     * the default will be used.
     *
     * @parameter
     */
    protected FileSet[] sources;

    //
    // MojoSupport Hooks
    //

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = null;

    protected MavenProject getProject() {
        return project;
    }

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository artifactRepository;

    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    //
    // Support
    //

    protected abstract List getProjectClasspathElements() throws DependencyResolutionRequiredException;

    protected abstract File getOutputDirectory() throws Exception;

    protected abstract FileSet[] getDefaultSources();

    private CompilerConfiguration createCompilerConfiguration() throws Exception {
        CompilerConfiguration config = new CompilerConfiguration();

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

    private URL[] getClasspath() throws Exception {
        List list = new ArrayList();

        // Add the plugins dependencies
        List classpathFiles = getProjectClasspathElements();
        for (int i = 0; i < classpathFiles.size(); ++i) {
            list.add(new File((String)classpathFiles.get(i)).toURL());
        }

        // Add custom dependencies
        if (classpath != null) {
            for (int i=0; i < classpath.length; i++) {
                Artifact artifact = getArtifact(classpath[i]);
                list.add(artifact.getFile().toURL());
            }
        }

        URL[] urls = (URL[])list.toArray(new URL[list.size()]);

        // Dump the classpath
        if (log.isDebugEnabled()) {
            log.debug("Compilation classpath:");
            for (int i=0; i < urls.length; i++) {
                log.debug("    " + urls[i]);
            }
        }

        return urls;
    }

    private void compile(final FileSet[] sources) throws Exception {
        assert sources != null;
        assert sources.length > 0;

        // Setupt the Groovy compiler
        CompilerConfiguration config = createCompilerConfiguration();
        ClassLoader parent = new URLClassLoader(getClasspath(), getClass().getClassLoader());
        GroovyClassLoader gcl = new GroovyClassLoader(parent);
        CompilationUnit compilation = new CompilationUnit(config, /* CodeSource security */ null, gcl);

        // Add each fileset to the compilation
        FileSetManager fsm = new FileSetManager(log, log.isDebugEnabled());

        for (int i=0; i<sources.length; i++) {
            File basedir = new File(sources[i].getDirectory());
            String[] includes = fsm.getIncludedFiles(sources[i]);

            for (int j=0; j < includes.length; j++) {
                compilation.addSource(new File(basedir, includes[j]));
            }
        }
        
        compilation.compile();
    }

    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        compile(sources != null ? sources : getDefaultSources());
    }
}
