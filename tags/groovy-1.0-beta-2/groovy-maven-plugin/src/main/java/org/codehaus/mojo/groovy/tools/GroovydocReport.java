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
import java.util.Locale;

import org.codehaus.doxia.sink.Sink;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import org.codehaus.groovy.tools.groovydoc.FileOutputTool;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Generates a report for the Groovy API documentation.
 *
 * @goal groovydoc
 * @phase generate-sources
 * @since 1.0-beta-1
 *
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class GroovydocReport
    extends ToolMojoSupport
    implements MavenReport
{
    //
    // TODO: Expose FileSets to allow for custom templates to be used instead of the defaults.
    //
    
    private static final String[] DEFAULT_DOC_TEMPLATES = {
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/index.html",
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/overview-frame.html",
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/allclasses-frame.html",
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/overview-summary.html",
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/stylesheet.css"
    };

    private static final String[] DEFAULT_PACKAGE_TEMPLATES = {
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/package-level/package-frame.html",
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/package-level/package-summary.html"
    };

    private static final String[] DEFAULT_CLASS_TEMPLATES = {
        "org/codehaus/groovy/tools/groovydoc/gstring-templates/class-level/classDocName.html"
    };

    /**
     * The directory where Groovy API documentation will be placed.
     *
     * @parameter default-value="${project.build.directory}/gapi"
     * @required
     */
    private File outputDirectory;

    /**
     * Source files to generate documentation for.  Defaults to all <tt>*.groovy</tt> files under <tt>src/main/groovy</tt>.
     *
     * @parameter
     */
    protected FileSet sources;

    /**
     * The name of the Groovydoc report.
     *
     * @parameter expression="${name}" default-value="Groovy API"
     */
    private String name;

    /**
     * The description of the Groovydoc report.
     *
     * @parameter expression="${description}" default-value="Groovy API Documentation."
     */
    private String description;

    /**
     * Specifies the destination directory where Groovydoc saves the generated HTML files.
     *
     * @parameter expression="${project.reporting.outputDirectory}/gapi"
     * @required
     */
    private File reportOutputDirectory;

    /**
     * The name of the destination directory under the site tree where reports will be stored.
     *
     * @parameter expression="${destDir}" default-value="gapi"
     */
    private String destDir;

    //
    // Components
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

    /**
     * @component
     */
    private Renderer siteRenderer;

    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        SiteRendererSink sink = siteRenderer.createSink(getReportOutputDirectory(), getOutputName() + ".html");
        generate(sink, Locale.getDefault());
    }

    //
    // MavenReport
    //

    public void generate(final Sink sink, final Locale locale) throws MavenReportException {
        try {
            init();
            doGenerate(sink, locale);
        }
        catch (Exception e) {
            throw new MavenReportException(e.getMessage(), e);
        }
    }

    private void doGenerate(final Sink sink, final Locale locale) throws Exception {
        // This will setup the default sources if not configured, so need to do that first
        String[] files = getFiles();

        GroovyDocTool groovydoc = new GroovyDocTool(
            new ClasspathResourceManager(), sources.getDirectory(), DEFAULT_DOC_TEMPLATES, DEFAULT_PACKAGE_TEMPLATES, DEFAULT_CLASS_TEMPLATES);

        for (int i=0; i < files.length; i++) {
            groovydoc.add(files[i]);
        }

        if (files.length > 0) {
            //
            // FIXME: Need to resolve this mess with outputDirectory, kinda copied in part from the javadoc plugin
            //        leaving asis for now since it works, but its whack still
            //
            
            outputDirectory = getReportOutputDirectory();
            
            log.info("Rendering API for " + files.length + " Groovy source file" + (files.length > 1 ? "s" : "") + " to " + outputDirectory);

            FileOutputTool output = new FileOutputTool();
            groovydoc.renderToOutput(output, outputDirectory.getCanonicalPath());
        }
        else {
            log.info("No Groovy sources for rendering API");
        }
    }

    private FileSet getDefaultSources() {
        FileSet set = new FileSet();

        File basedir = new File(project.getBasedir(), "src/main/groovy");
        set.setDirectory(basedir.getAbsolutePath());
        set.addInclude("**/*.groovy");

        return set;
    }

    private String[] getFiles() {
        if (sources == null) {
            sources = getDefaultSources();
        }

        FileSetManager fsm = new FileSetManager(getLog(), getLog().isDebugEnabled());
        return fsm.getIncludedFiles(sources);
    }

    public String getOutputName() {
        return outputDirectory.getName() + "/index";
    }

    public String getName(final Locale locale) {
        return name;
    }

    public String getCategoryName() {
        return CATEGORY_PROJECT_REPORTS;
    }

    public String getDescription(final Locale locale) {
        return description;
    }

    public File getReportOutputDirectory() {
        if (reportOutputDirectory == null) {
            return outputDirectory;
        }
        return reportOutputDirectory;
    }

    public void setReportOutputDirectory(File reportOutputDirectory) {
        if ((reportOutputDirectory != null) && (!reportOutputDirectory.getAbsolutePath().endsWith(destDir))) {
            this.reportOutputDirectory = new File(reportOutputDirectory, destDir);
        }
        else {
            this.reportOutputDirectory = reportOutputDirectory;
        }
    }

    public boolean isExternalReport() {
        return true;
    }

    public boolean canGenerateReport() {
        return getFiles().length > 0;
    }
}
