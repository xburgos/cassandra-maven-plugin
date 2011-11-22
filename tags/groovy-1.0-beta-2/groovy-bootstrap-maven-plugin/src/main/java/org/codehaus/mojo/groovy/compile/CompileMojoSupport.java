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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Support for compile mojos (class and stub gen).
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public abstract class CompileMojoSupport
    extends MojoSupport
{
    /**
     * Additional artifacts to add to the classpath (in addition to the classpath
     * which is picked up from the executing poms configuration).
     *
     * @parameter
     */
    protected ArtifactItem[] classpath;

    /**
     * Source files to be included.  If not specified, then the default will be used.
     *
     * @parameter
     */
    protected FileSet[] sources;

    //
    // MojoSupport Hooks
    //

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    protected MavenProject getProject() {
        return project;
    }

    //
    // NOTE: This 'artifactRepository' is needed for getClasspath()'s artifact resolution.
    //
    
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

    protected abstract List getSourceRoots();

    protected void addSourceRoot(final File dir) throws IOException {
        assert dir != null;

        List roots = getSourceRoots();
        assert roots != null;
        
        String path = dir.getCanonicalPath();

        if (!roots.contains(path)) {
            log.debug("Adding source root: " + path);
            roots.add(path);
        }
    }

    protected abstract FileSet[] getDefaultSources();

    protected URL[] getClasspath() throws Exception {
        List list = new ArrayList();

        // Add the projects dependencies
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

        //
        // TODO: See if we should try and automatically add groovy:groovy-all
        //

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

    protected CompilerConfiguration createCompilerConfiguration() throws Exception {
        CompilerConfiguration config = new CompilerConfiguration();

        //
        // TODO: Anything in here common to both compile and stub gen?
        //
        
        return config;
    }
    
    private CompilerConfiguration cachedConfig;
    
    protected CompilerConfiguration getCompilerConfiguration() throws Exception {
        if (cachedConfig == null) {
            cachedConfig = createCompilerConfiguration();
        }
        
        return cachedConfig;
    }
    
    protected GroovyClassLoader createGroovyClassLoader() throws Exception {
        CompilerConfiguration config = createCompilerConfiguration();
        
        //
        // NOTE: Do not use the CL from this class or it will mess up resolution
        //       when using classes from groovy* which depend on other artifacts,
        //       also don't really want to pollute the classpath with our dependencies.
        //
        
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        GroovyClassLoader gcl = new GroovyClassLoader(parent, config);

        // Add custom classpath elements
        URL[] classpath = getClasspath();
        for (int i=0; i<classpath.length; i++) {
            gcl.addURL(classpath[i]);
        }
        
        return gcl;
    }
    
    protected static class FileSetUtils
    {
        //
        // FIXME: This is a very feeble attempt at better error handling when pom config is whacky...
        //        Should update and improve this.
        //
        
        public static void validate(final FileSet fileSet) throws Exception {
            if (fileSet == null) {
                throw new MojoExecutionException("FileSet contains null element");
            }
            
            String dirname = fileSet.getDirectory();
            if (dirname == null) {
                throw new MojoExecutionException("FileSet missing <directory> (or resolved to null value)");
            }
            
            File dir = new File(dirname);
            if (!dir.exists()) {
                throw new MojoExecutionException("FileSet <directory> does not exist: " + dir);
            }
            if (!dir.isDirectory()) {
                throw new MojoExecutionException("FileSet <directory> does not reference a directory: " + dir);
            }
        }
    }
}
