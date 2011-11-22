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

package org.codehaus.mojo.groovy.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.ClassLibrary;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.DocletTag;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryScanner;

import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.extractor.ExtractionException;

/**
 * Extracts Mojo descriptors from Groovy sources.
 *
 * @version $Id$
 */
public class GroovyMojoDescriptorExtractor
    extends JavaMojoDescriptorExtractor
{
    private Logger log;

    public void enableLogging(final Logger logger) {
        super.enableLogging(logger);

        // Grab a hold of the logger
        this.log = logger;
        assert log != null;
    }
    
    protected JavaClass[] discoverClasses(final MavenProject project) throws ExtractionException {
        assert project != null;

        MojoMetaDataParser parser = new MojoMetaDataParser();
        JavaDocBuilder builder = new JavaDocBuilder();

        // Allow QDox to resolve classes
        ClassLibrary lib = builder.getClassLibrary();
        lib.addClassLoader(Thread.currentThread().getContextClassLoader());

        List sourceRoots = new ArrayList();
        sourceRoots.addAll(project.getCompileSourceRoots());
        sourceRoots.addAll(project.getScriptSourceRoots());

        //
        // FIXME: Shouldn't need to hard-code this...
        //
        sourceRoots.add("src/main/groovy");
        
        Iterator iter = sourceRoots.iterator();
        while (iter.hasNext()) {
            File basedir = new File((String)iter.next());

            // Resolve source dir if needed
            if (!basedir.isAbsolute()) {
                basedir = new File(project.getBasedir(), basedir.getPath()).getAbsoluteFile();
            }

            // Skip if dir is missing
            if (!basedir.exists() || !basedir.isDirectory()) {
                log.debug("Skipping missing source directory: " + basedir);
                continue;
            }
            else {
                log.debug("Scanning for sources in: " + basedir);
            }

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(basedir);
            scanner.addDefaultExcludes();

            //
            // FIXME: Shouldn't need to hard-code this...
            //
            scanner.setIncludes(new String[]{ "**/*.java", "**/*.groovy" });
            scanner.scan();
            
            String[] includes = scanner.getIncludedFiles();
            for (int i=0; i < includes.length; i++) {
                File file = new File(basedir, includes[i]).getAbsoluteFile();
                log.debug("Prcessing: " + file);
                
                //
                // If the file looks like Java, then just add it directly to QDox
                // otherwise, pass it to the metadata parser and then render it
                // as something QDox can read.
                //
                
                try {
                    if (file.getName().endsWith(".java")) {
                        builder.addSource(file);
                    }
                    else {
                        MojoMetaData md = parser.parse(file.toURL());
                        MojoClassQDoxRenderer.addSources(md, builder);
                    }
                }
                catch (Exception e) {
                    log.error("Failed to process: " + file, e);
                }
            }
        }

        return builder.getClasses();
    }
    
    protected boolean canExtractMojoDescriptor(final JavaClass javaClass) {
        assert javaClass != null;
        
        boolean extract = super.canExtractMojoDescriptor(javaClass);
        
        if (extract) {
            // Only extract if this is a Groovy class, don't extract from Java.
            // use the synthetic source type field to check
            
            JavaField field = javaClass.getFieldByName(MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
            if (field != null) {
                DocletTag tag = field.getTagByName(MojoClassQDoxRenderer.SOURCE_TYPE_TAG);
                if (tag != null) {
                    // If there is a field and tag, then its a source we have parsed,
                    // use the source-type field to determine if its Groovy or not
                    
                    extract = MojoClassQDoxRenderer.SOURCE_TYPE_GROOVY.equals(tag.getValue());
                }
                else {
                    // This shouldn't really happen
                    throw new RuntimeException("Expected to find @" +
                        MojoClassQDoxRenderer.SOURCE_TYPE_TAG + " tag on synthetic marker field: " +
                        MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
                }
            }
            else {
                // If there is no synthetic field, its not a Groovy source that we have processed
                extract = false;
            }
            
            if (!extract) {
                log.debug("Skipping extraction for non-Groovy class: " + javaClass.getFullyQualifiedName());
            }
        }
        
        if (extract) {
            log.debug("Extracting mojo from class: " + javaClass.getFullyQualifiedName());
        }
        
        return extract;
    }
}