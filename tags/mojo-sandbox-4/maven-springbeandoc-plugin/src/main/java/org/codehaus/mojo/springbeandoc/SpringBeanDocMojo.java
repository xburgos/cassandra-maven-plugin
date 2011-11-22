package org.codehaus.mojo.springbeandoc;

/*
   The MIT License
   .
   Copyright (c) 2005, Ghent University (UGent)
   .
   Permission is hereby granted, free of charge, to any person obtaining a copy of
   this software and associated documentation files (the "Software"), to deal in
   the Software without restriction, including without limitation the rights to
   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
   of the Software, and to permit persons to whom the Software is furnished to do
   so, subject to the following conditions:
   .
   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.
   .
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
 */

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * Spring BeanDoc report generator that can be used as a Maven 2 plugin or a
 * Maven 2 site report.
 *
 * @author Jurgen De Landsheer
 * @author Marat Radchenko
 * @goal springbeandoc
 * @phase generate-sources
 * @see <a href="http://opensource.atlassian.com/confluence/spring/display/BDOC/Home">Spring
 *      BeanDoc</a>
 * @see <a href="http://www.graphviz.org/">GraphViz</a>
 */
public class SpringBeanDocMojo extends AbstractMavenReport {

  /**
   * Subdirectory for report.
   */
  protected static final String SUBDIRECTORY = "springbeandoc";

  /**
   * Base output directory.
   *
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  private File buildDirectory;

  /**
   * Stylesheet file to use for generated report.
   *
   * @parameter
   */
  private File cssUrl;

  /**
   * Base output directory for reports.
   *
   * @parameter expression="${project.reporting.outputDirectory}"
   * @readonly
   * @required
   */
  private File outputDirectory;

  /**
   * List of {@link JavadocLocation} objects.
   *
   * @parameter
   */
  private List<JavadocLocation> javadocLocations = Collections.emptyList();

  /**
   * Input files can be one or more (comma-separated if multiple)
   * resource locations. Resources are standard Spring resources. Note that
   * beandoc defaults to FILE sysytem resources, not classpath resources if
   * no qualifier is listed.
   *
   * @parameter
   * @required
   */
  private List<String> resources = Collections.emptyList();

  /**
   * Reference to Maven 2 Project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Doxia SiteRender.
   *
   * @component
   */
  private Renderer renderer;

  /**
   * GraphViz executable location; visualization (images) will be
   * generated only if you install this program and set this property to the
   * executable dot (dot.exe on Win).
   *
   * @parameter alias="graphViz"
   * @required
   */
  private File executable;

  /**
   * Graph output types. Default is png. Possible values: png, jpg, gif, svg.
   *
   * @parameter default-value="png"
   * @required
   */
  private String graphsOutputType;

  /**
   * Documentation title used in the HTML output.
   *
   * @parameter
   */
  private String title;

  /**
   * You can have the XML parser not bother to validate the input
   * files against the DTD/XSD if you so wish. True by default.
   *
   * @parameter default-value="true"
   */
  private boolean validate;

  /**
   * @param locale report locale.
   * @return report description.
   * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
   */
  public String getDescription(final Locale locale) {
    return getBundle(locale).getString("report.description");
  }

  /**
   * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
   */
  public String getName(final Locale locale) {
    return getBundle(locale).getString("report.name");
  }

  /**
   * @see org.apache.maven.reporting.MavenReport#getOutputName()
   */
  public String getOutputName() {
    return SUBDIRECTORY + "/index.html";
  }

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException {
    this.execute(this.buildDirectory, Locale.getDefault());
  }

  /**
   * Builds beandoc config file.
   *
   * @param outputDir target directory
   * @param locale    locale
   * @return path to config file.
   * @throws IOException exception
   */
  protected File buildConfig(final File outputDir, final Locale locale) throws IOException {
    outputDir.mkdirs();
    Properties p = new Properties();
    p.setProperty("graphs.outputType", this.graphsOutputType);
    final String title;
    if (this.title != null) {
      title = this.title;
    } else {
      title = MessageFormat.format(
          this.getBundle(locale).getString("report.site.title"),
          this.project.getName(),
          this.project.getVersion()
      );
    }
    p.setProperty("html.title", title);
    p.setProperty("compiler.dotExe", this.executable.getAbsolutePath());
    if (this.cssUrl != null) {
      p.setProperty("html.cssUrl", this.cssUrl.getAbsolutePath());
    }

    for (final JavadocLocation location : this.javadocLocations) {
      p.setProperty("javadoc.locations[" + location.getPackagename() + "]", location.getLocation());
    }

    p.setProperty("processor.validateFiles", String.valueOf(this.validate));

    Iterator<String> it = resources.iterator();
    StringBuilder ipb = new StringBuilder(16);

    while (it.hasNext()) {
      File f = new File(getProject().getBasedir(), it.next());

      if (f.isDirectory()) {
        ipb.append(f.getAbsolutePath()).append(File.separator).append("*.xml");
      } else {
        ipb.append(f.getAbsolutePath());
      }

      if (it.hasNext()) {
        ipb.append(",");
      }
    }

    p.setProperty("input.files", ipb.toString());
    p.setProperty("output.dir", outputDir.getAbsolutePath());

    final File outputFile = new File(outputDir, "beandoc.properties");
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(outputFile);
      p.store(os, "");
      os.close();
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (final IOException e) {
          this.getLog().error("Could not close " + outputFile.getAbsolutePath(), e);
        }
      }
    }
    return outputFile;
  }

  /**
   * Executes BeanDoc generator.
   *
   * @param outputDirectory report output directory.
   * @param locale          report locale.
   * @throws MojoExecutionException if there were any execution errors.
   */
  private void execute(final File outputDirectory, final Locale locale) throws MojoExecutionException {
    final File config;
    try {
      config = buildConfig(new File(outputDirectory, SUBDIRECTORY), locale);
    } catch (final IOException e) {
      throw new MojoExecutionException("Could not generate beandoc config", e);
    }
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      //TODO: fix SpringLoader classloader bug
      //TODO: avoid using BeanDocClient because it calls System.exit
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      org.springframework.beandoc.client.BeanDocClient.main(new String[]{
          "--properties",
          config.getAbsolutePath()
      });
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  /**
   * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
   */
  protected void executeReport(final Locale locale) throws MavenReportException {
    try {
      this.execute(this.outputDirectory, locale);
    } catch (MojoExecutionException e) {
      final MavenReportException ex = new MavenReportException(e.getMessage());
      ex.initCause(e.getCause());
      throw ex;
    }
  }

  /**
   * Gets resource bundle for given locale.
   *
   * @param locale locale
   * @return resource bundle
   */
  protected ResourceBundle getBundle(final Locale locale) {
    return ResourceBundle.getBundle(
        "maven-springbeandoc-plugin",
        locale,
        this.getClass().getClassLoader());
  }

  protected Renderer getSiteRenderer() {
    return this.renderer;
  }

  /**
   * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
   */
  protected String getOutputDirectory() {
    return this.outputDirectory.getAbsolutePath();
  }

  protected MavenProject getProject() {
    return this.project;
  }
}

