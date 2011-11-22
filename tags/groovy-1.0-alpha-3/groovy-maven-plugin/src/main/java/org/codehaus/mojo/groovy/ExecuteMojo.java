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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyResourceLoader;
import groovy.lang.GroovyRuntimeException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.project.MavenProject;

import org.apache.maven.execution.MavenSession;

import org.codehaus.plexus.component.factory.groovy.GroovyComponentFactory;
import org.codehaus.plexus.component.factory.groovy.GroovyResourceLoaderImpl;

import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

/**
 * Executes a <a href="http://groovy.codehaus.org">Groovy</a> script.
 *
 * @goal execute
 * @requiresDependencyResolution
 * @configurator override
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class ExecuteMojo
    extends MojoSupport
{
    /**
     * The source of the script to execute.  Must provide exactly one of <tt>body</tt>, <tt>file</tt> or <tt>url</tt>.
     *
     * @parameter
     * @required
     */
    private CodeSource source;

    /**
     * Additional artifacts to add to the scripts classpath.
     *
     * @parameter
     */
    private ArtifactItem[] classpath;

    /**
     * Path to search for imported scripts.
     *
     * @parameter expression
     */
    private File[] scriptpath;

    /**
     * A set of default project properties, which the values will be used only if
     * the project or system does not override.
     *
     * @parameter
     */
    private Map defaults;

    /**
     * A set of additional project properties.
     * 
     * @parameter
     */
    private Map properties;
    
    /**
     * Trap assertion errors and rethrow them as execution failures.
     *
     * @parameter default-value="true"
     */
    private boolean trapAssertionErrors;
    
    //
    // Components
    //
    
    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository artifactRepository;
    
    /**
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;
    
    /**
     * @component role="org.codehaus.plexus.component.factory.ComponentFactory" role-hint="groovy"
     * @readonly
     * @required
     */
    private GroovyComponentFactory componentFactory;

    //
    // MojoSupport Hooks
    //

    protected MavenProject getProject() {
        return project;
    }
    
    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        boolean debug = log.isDebugEnabled();

        GroovyObject obj = loadGroovyObject(source);
        
        // Expose logging
        obj.setProperty("log", log);

        // Add a custom project to resolve properties
        MavenProject projectAdapter = new GroovyMavenProjectAdapter(project, session, properties, defaults);
        obj.setProperty("project", projectAdapter);
        obj.setProperty("pom", projectAdapter);

        // Stuff in an Ant helper
        obj.setProperty("ant", new AntBuilder(log));

        // Execute the script
        if (debug) {
            log.debug("Invoking run() on: " + obj);
        }
        
        try {
            obj.invokeMethod("run", new Object[0]);
        }
        catch (AssertionError e) {
            // Special handling for assert, translates them into execution exceptions
            if (trapAssertionErrors) {
                if (debug) {
                    // Yes, log error if debug is enabled
                    log.error("Groovy script assertion failure", e);
                }
                
                throw new MojoExecutionException("Assertion failed: " + e.getMessage(), e);
            }
            else {
                throw e;
            }
        }
        catch (GroovyRuntimeException e) {
            if (debug) {
                // Yes, log error if debug is enabled
                log.error("Groovy script execution failure", e);
            }
            
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            
            throw new MojoExecutionException(cause.getMessage(), cause);
        }
    }

    private GroovyObject loadGroovyObject(final CodeSource source) throws Exception {
        assert source != null;

        boolean debug = log.isDebugEnabled();

        // Setup the class loader to use
        ClassLoader parent = getClass().getClassLoader();
        URL[] urls = getClasspath();
        URLClassLoader classLoader = new URLClassLoader(urls, parent);

        // Validate and dump the scriptpath
        if (scriptpath != null) {
            log.debug("Scriptpath:");
            for (int i=0; i < scriptpath.length; i++) {
                if (scriptpath[i] == null) {
                    throw new MojoExecutionException("Null element found in scriptpath at index: " + i);
                }

                if (debug) {
                    log.debug("    " + scriptpath[i]);
                }
            }
        }

        // Setup the resource loader
        GroovyResourceLoader resourceLoader = new GroovyResourceLoaderImpl(classLoader) {
            protected URL resolveGroovySource(final String className, final ClassLoader classLoader) throws MalformedURLException {
                assert className != null;
                assert classLoader != null;

                String resource = classToResourceName(className);
                
                // First check the scriptpath
                if (scriptpath != null) {
                    for (int i=0; i<scriptpath.length; i++) {
                        assert scriptpath[i] != null;

                        File file = new File(scriptpath[i], resource);
                        if (file.exists()) {
                            return file.toURL();
                        }
                    }
                }

                // Then look for a resource in the classpath
                URL url = classLoader.getResource(resource);

                //
                // HACK: Try w/o leading '/'... ???  Seems that when loading resources the '/' prefix messes things up?
                //
                if (url == null) {
                    if (resource.startsWith("/")) {
                        String tmp = resource.substring(1, resource.length());
                        url = classLoader.getResource(tmp);
                    }
                }

                if (url == null) {
                    // And finally check for a class defined in a file next to the main script file
                    File script = source.getFile();
                    if (script != null) {
                        File file = new File(script.getParentFile(), resource);
                        if (file.exists()) {
                            return file.toURL();
                        }
                    }
                }
                else {
                    return url;
                }

                return super.resolveGroovySource(className, classLoader);
            }
        };
        
        return componentFactory.loadGroovyObject(source.toGroovyCodeSource(), classLoader, resourceLoader);
    }

    private URL[] getClasspath() throws DependencyResolutionRequiredException, MalformedURLException, MojoExecutionException {
        List list = new ArrayList();

        // Add the plugins dependencies
        List classpathFiles = project.getCompileClasspathElements();
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
            log.debug("Classpath:");
            for (int i=0; i < urls.length; i++) {
                log.debug("    " + urls[i]);
            }
        }

        return urls;
    }
}
