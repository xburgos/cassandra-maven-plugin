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

package org.codehaus.mojo.groovy.tools;

import java.io.File;

import org.codehaus.groovy.antlr.java.Java2GroovyMain;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Converts Java source files to Groovy sources.
 *
 * @goal java2groovy
 * @requiresProject false
 * @since 1.0-beta-2
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class Java2GroovyMojo
    extends ToolMojoSupport
{
    /**
     * The directory where converted source files will be generated into.  If
     * not specified, then converted source will be dumpted to console.
     *
     * @parameter expression="${outputDirectory}"
     */
    private File outputDirectory;
    
    /**
     * One or more file sets of Java source files to convert to Groovy sources.
     *
     * @parameter
     */
    private FileSet[] sources;
    
    /**
     * A single source file to convert.
     *
     * @parameter expression="${source}"
     */
    private File source;
    
    private void convert(final FileSet[] sources) throws Exception {
        assert sources != null && sources.length > 0;
        
        FileSetManager fsm = new FileSetManager(log, log.isDebugEnabled());
        
        for (int i=0; i<sources.length; i++) {
            File basedir = new File(sources[i].getDirectory());
            String[] includes = fsm.getIncludedFiles(sources[i]);
            
            log.debug("Converting sources from: " + basedir);
            
            for (int j=0; j < includes.length; j++) {
                log.debug("    " + includes[j]);
                
                File file = new File(basedir, includes[j]);
                
                String input = DefaultGroovyMethods.getText(file);
                String output = Java2GroovyMain.convert(includes[j], input, true, true);
                
                if (outputDirectory != null) {
                    File outputFile;
                    
                    if (includes[j].endsWith(".java")) {
                        String filename = includes[j].substring(0, includes[j].length() - 5) + ".groovy";
                        outputFile = new File(outputDirectory, filename);
                    }
                    else {
                        throw new MojoExecutionException("Unable to determine new file name of Groovy source file from: " + includes[j]);
                    }
                    
                    log.info("Writing: " + outputFile);
                    
                    outputFile.getParentFile().mkdirs();
                    DefaultGroovyMethods.write(outputFile, output);
                }
                else {
                    System.out.println("----8<----");
                    System.out.println(output);
                    System.out.println("---->8----");
                }
            }
        }
    }
    
    protected void doExecute() throws Exception {
        if (sources == null && source == null) {
            throw new MojoExecutionException("Must configure either 'source' or 'sources'.");
        }
        if (sources != null && source != null) {
            throw new MojoExecutionException("Only one of 'source' or 'sources' may be configured.");
        }
        
        if (source != null) {
            FileSet fileset = new FileSet();
            fileset.setDirectory(source.getParentFile().getCanonicalPath());
            fileset.addInclude(source.getName());
            sources = new FileSet[] { fileset };
        }
        
        convert(sources);
    }
}
