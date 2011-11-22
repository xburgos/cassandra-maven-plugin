package org.codehaus.mojo.xmlbeans;

/*
 * Copyright 2001-2005 The Apache Software Foundation. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.codehaus.plexus.util.DirectoryScanner;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.xml.sax.EntityResolver;

/**
 * <p>
 * A Maven 2 plugin which parses xsd files and produces a corresponding object
 * model based on the Apache XML Beans parser.
 * </p>
 * <p>
 * The plugin produces two sets of output files referred to as generated sources
 * and generated classes. The former is then compiled to the build
 * <code>outputDirectory</code>. The latter is generated in this directory.
 * </p>
 * <p>
 * Note that the descriptions for the goal's parameters have been blatently
 * copied from http://xmlbeans.apache.org/docs/2.0.0/guide/antXmlbean.html for
 * convenience.
 * </p>
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:kris.bravo@corridor-software.us">Kris Bravo</a>
 * @version $Id$
 */
public abstract class AbstractXmlBeansPlugin extends AbstractMojo implements PluginProperties {

    /**
     * Define the name of the jar file created. For instance, "myXMLBean.jar"
     * will output the results of this task into a jar with the same name.
     * 
     * @parameter
     */
    private File outputJar;

    /**
     * Set to true to permit the compiler to download URLs for imports and
     * includes. Defaults to false, meaning all imports and includes must be
     * copied locally.
     * 
     * @parameter default-value="false"
     */
    private boolean download;

    /**
     * Indicates whether source should be compiled with debug information;
     * defaults to off. If set to off, -g:none will be passed on the command
     * line for compilers that support it (for other compilers, no command line
     * argument will be used). If set to true, the value of the debug level
     * attribute determines the command line argument.
     * 
     * @parameter default-value="false"
     */
    private boolean debug;

    /**
     * The initial size of the memory for the underlying VM, if javac is run
     * externally; ignored otherwise. Defaults to the standard VM memory
     * setting. (Examples: 83886080, 81920k, or 80m)
     * 
     * @parameter
     */
    private String memoryInitialSize;

    /**
     * The maximum size of the memory for the underlying VM, if javac is run
     * externally; ignored otherwise. Defaults to the standard VM memory
     * setting. (Examples: 83886080, 81920k, or 80m)
     * 
     * @parameter
     */
    private String memoryMaximumSize;

    /**
     * The compiler implementation to use. If this attribute is not set, the
     * value of the build.compiler property, if set, will be used. Otherwise,
     * the default compiler for the current VM will be used.
     * 
     * @parameter
     */
    private String compiler;

    /**
     * Controls the amount of build message output.
     * 
     * @parameter default-value="false"
     */
    private boolean verbose;

    /**
     * Supress the normal amount of console output.
     * 
     * @parameter default-value="true"
     */
    private boolean quiet = true;

    /**
     * Do not enforce the unique particle attribution rule.
     * 
     * @parameter default-value="false"
     */
    private boolean noUpa;

    /**
     * Do not enforce the particle valid (restriction) rule.
     * 
     * @parameter default-value="false"
     */
    private boolean noPvr;

    /**
     * Todo: Unkown use.
     * 
     * @parameter default-value="false"
     */
    private boolean jaxb;

    /**
     * Don't compile the generated source files.
     * 
     * @parameter default-value="false"
     */
    private boolean noJavac;

    /**
     * The location of the catalog used to resolve xml entities. 
     *
     * @parameter expression="${basedir}/src/main/catalog/resolver-catalog.xml"
     */
    protected File catalogLocation;

    /**
     * A file that points to a directory of files.
     * 
     * @parameter
     */
    private String sourceSchemas;

    /**
     * Configuration files used by the object generator. For more information
     * about the format of these files, see Todo.
     * 
     * @parameter
     */
    private List xmlConfigs;

    /**
     * Returns the javasource parameter which specifies an option to the
     * XmlBeans code generator.
     * 
     * @parameter
     * @return null.
     */
    private String javaSource;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     */
    private List pluginArtifacts;

    /**
     * @parameter expression="${project.artifactMap}"
     * @required
     * @readonly
     */
    private Map artifactMap;

    /**
     * The repository for libraries we depend on.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private ArtifactFactory factory;

    /**
     * A reference to the Maven Project metadata.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Used to find resources used by the XML compiler. Currently not passed to
     * the compiler, since everything is on the classpath.
     */
    private EntityResolver entityResolver = null;

    /**
     * 
     */
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * Files to parse and generate models for.
     */
    private File[] xsdFiles;

    /**
     * Empty constructor for the XML Beans plugin.
     */
    public AbstractXmlBeansPlugin() {
    }

    /**
     * <p>
     * Map the parameters to the schema compilers parameter object, make sure
     * the necessary output directories exist, then call on the schema compiler
     * to produce the java objects and supporting resources.
     * </p>
     * 
     * @number MOJO-270
     * 
     * @throws MojoExecutionException Errors occurred during compile.
     */
    public final void execute() throws org.apache.maven.plugin.MojoExecutionException {

        if (hasSchemas()) {
            try {
                SchemaCompiler.Parameters compilerParams = ParameterAdapter
                        .getCompilerParameters(this);
                if (isOutputStale()) {
                    try {
                        compilerParams.getSrcDir().mkdirs();

                        boolean result = SchemaCompiler.compile(compilerParams);

                        if (!result) {
                            StringBuffer errors = new StringBuffer();
                            for (Iterator iterator = compilerParams.getErrorListener().iterator(); iterator
                                    .hasNext();) {
                                Object o = (Object) iterator.next();
                                errors.append("xml Error" + o);
                                errors.append("\n");
                            }
                            throw new XmlBeansException(XmlBeansException.COMPILE_ERRORS, errors
                                    .toString());
                        }

                        touchStaleFile();
                    } catch (IOException ioe) {
                        throw new XmlBeansException(XmlBeansException.STALE_FILE_TOUCH,
                                getStaleFile().getAbsolutePath(), ioe);
                    }
                } else {
                    getLog().info("All schema objects are up to date.");
                }
                updateProject(project, compilerParams);
            } catch (DependencyResolutionRequiredException drre) {
                throw new XmlBeansException(XmlBeansException.CLASSPATH_DEPENDENCY, drre);
            }
        } else {
            getLog().info("Nothing to generate.");
        }
    }

    /**
     * Indicates whether or not there are schemas to compile.
     * 
     * @return true if there are schema files in the source or artifacts.
     * @throws XmlBeansException
     */
    private boolean hasSchemas() throws XmlBeansException {
        return getXsdFiles().length > 0;
    }

    protected abstract void updateProject(MavenProject project,
            SchemaCompiler.Parameters compilerParams) throws DependencyResolutionRequiredException;

    protected abstract List getXsdJars();

    protected abstract File getGeneratedSchemaDirectory();

    private void touchStaleFile() throws IOException {
        File staleFile = getStaleFile();

        if (!staleFile.exists()) {
            staleFile.getParentFile().mkdirs();
            staleFile.createNewFile();
            getLog().debug("Stale flag file created.");
        } else {
            staleFile.setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * Returns true of any one of the files in the XSD array are more new than
     * the <code>staleFlag</code> file.
     * 
     * @return True if xsd files have been modified since the last build.
     */
    private boolean isOutputStale() throws XmlBeansException {
        File[] sourceXsds = getXsdFiles();
        File staleFile = getStaleFile();
        boolean stale = !staleFile.exists();

        if (!stale) {
            getLog().debug("Stale flag file exists, comparing to xsd's.");
            long staleMod = staleFile.lastModified();

            for (int i = 0; i < sourceXsds.length; i++) {
                if (sourceXsds[i].lastModified() > staleMod) {
                    getLog().debug(sourceXsds[i].getName() + " is newer than the stale flag file.");
                    stale = true;
                }
            }
        }
        return stale;
    }

    /**
     * Gives the plugin a reference to the local repository.
     * 
     * @param repository The local repository.
     */
    public final void setLocalRepository(final ArtifactRepository repository) {
        localRepository = repository;
    }

    public abstract File getBaseDir();

    public abstract File getStaleFile();

    public abstract File getDefaultXmlConfigDir();

    /**
     * Returns the directory where the schemas are located. Note that this is
     * the base directory of the schema compiler, not the maven project.
     * 
     * @return The schema directory.
     */
    public abstract File getSchemaDirectory();

    /**
     * Returns a classpath for the compiler made up of artifacts from the
     * project.
     * 
     * @return Array of classpath entries.
     */
    public final File[] getClasspath() {
        List results = new ArrayList(project.getArtifacts().size()
                + project.getPluginArtifacts().size());
        for (Iterator i = project.getArtifacts().iterator(); i.hasNext();) {
            Artifact a = (Artifact) i.next();
            if (a.getFile() != null) {
                results.add(a.getFile());
            }
        }

        // TODO: use addArtifacts
        Set set = new HashSet(project.getDependencyArtifacts());

        if (pluginArtifacts != null) {
            for (Iterator i = pluginArtifacts.iterator(); i.hasNext();) {
                Artifact a = (Artifact) i.next();
                if (a.getFile() != null) {
                    results.add(a.getFile());

                    a = factory.createArtifact(a.getGroupId(), a.getArtifactId(), a.getVersion(),
                            Artifact.SCOPE_COMPILE, a.getType());
                    set.add(a);
                }
            }
        }

        project.setDependencyArtifacts(set);

        return (File[]) results.toArray(EMPTY_FILE_ARRAY);
    }

    /**
     * Returns null. Currently the compiler preference isn't passwed to the xml
     * beans compiler.
     * 
     * @return null.
     */
    public final String getCompiler() {
        return compiler;
    }

    /**
     * Returns configuration files identified in the xmlConfigs string passed by
     * the project configuration. If none were identified, a check is made for
     * the default xsd config directory src/xsdconfig.
     * 
     * @return An array of configuration files.
     */
    public final File[] getConfigFiles() throws XmlBeansException {
        File defaultXmlConfigDir = getDefaultXmlConfigDir();
        getLog().debug("Creating a list of config files.");
        try {
            if (xmlConfigs != null) {
                return (File[]) getFileList(xmlConfigs).toArray(new File[] {});
            } else if (defaultXmlConfigDir.exists()) {
                List defaultDir = new ArrayList();
                defaultDir.add(defaultXmlConfigDir);
                return (File[]) getFileList(defaultDir).toArray(new File[] {});
            } else {
                return null;
            }
        } catch (XmlBeansException xmlbe) {
            throw new XmlBeansException(XmlBeansException.INVALID_CONFIG_FILE, xmlbe);
        }
    }

    /**
     * Recursively travers the file list and it's subdirs and produce a single
     * flat list of the files.
     * 
     * @param fileList
     * @return files
     */
    private final List getFileList(List fileList) throws XmlBeansException {

        if (fileList != null) {
            getLog().debug("A list was given.");
            List files = new ArrayList();

            File nextFile = null;
            ArrayList nextDir = new ArrayList();
            for (Iterator i = fileList.iterator(); i.hasNext();) {
                nextFile = (File) i.next();
                if (nextFile.exists()) {
                    // scrub for "hidden" files beginning with '.'
                    if (nextFile.getName().indexOf('.') != 0) {
                        if (nextFile.isDirectory()) {
                            getLog().debug("One entry was a directory. Getting its children too.");
                            File[] children = nextFile.listFiles();
                            for (int j = 0; j < children.length; j++) {
                                nextDir.clear();
                                nextDir.add(children[j]);
                                files.addAll(getFileList(nextDir));
                            }
                        } else {
                            getLog().debug("Adding file " + nextFile.getAbsolutePath());
                            files.add(nextFile);
                        }
                    }
                } else {
                    throw new XmlBeansException(XmlBeansException.MISSING_FILE, nextFile
                            .getAbsolutePath());
                }
            }
            return files;
        } else {
            getLog().debug("No list was given. Returning.");
            return null;
        }

    }

    /**
     * Returns a null entity resolver.
     * 
     * @return entityResolver set to null.
     */
    public final EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Returns an empty collection the compiler will store error message Strings
     * in.
     * 
     * @return An empty ArrayList.
     */
    public final Collection getErrorListeners() {
        Collection listener = new ArrayList();
        return listener;
    }

    /**
     * Todo: Not certain of the purpose of this.
     * 
     * @return null at this time.
     */
    public final List getExtensions() {
        return null;
    }

    /**
     * Used during testing.
     * 
     * @param project
     */
    final void setProject(MavenProject newProject) {
        project = newProject;
    }

    /**
     * An array of other source files. Currently an empty array.
     * 
     * @return An empty file array.
     */
    public final File[] getJavaFiles() {
        return new File[] {};
    }

    /**
     * Returns null at this time. Passed to the schema compiler.
     * 
     * @return null.
     */
    public final Set getMdefNamespaces() {
        return null;
    }

    /**
     * 
     * 
     * 
     */
    public final String getJavaSource() {
        return javaSource;
    }

    /**
     * Returns the initial size of the memory allocation for the schema compile
     * process.
     * 
     * @return The initial memory size value.
     */
    public final String getMemoryInitialSize() {
        return memoryInitialSize;
    }

    /**
     * Returns the maximum size of the memory allocation for the schema compile
     * process.
     * 
     * @return The max memory size value.
     */
    public final String getMemoryMaximumSize() {
        return memoryMaximumSize;
    }

    /**
     * Returns null at this time. This is passed to the schema compiler.
     * 
     * @return null.
     */
    public final String getName() {
        return null;
    }

    /**
     * Returns the location of the output jar file should one be produced. If
     * it has been set, make sure the directories exist before passing it
     * to the xml beans compiler.
     * 
     * @number MXMLBEANS-17
     * @return The jar file location.
     */
    public final File getOutputJar() {
        if (outputJar != null) {
            outputJar.getParentFile().mkdirs();
        }
        return outputJar;
    }

    /**
     * Todo: Not certain of the purpose of this.
     * 
     * @return null at this time.
     */
    public final String getRepackage() {
        return null;
    }

    /**
     * Currently returns an empty file array.
     * 
     * @return An empty file array.
     */
    public final File[] getWsdlFiles() {
        return new File[] {};
    }

    /**
     * Returns the name of the file used to resolve xml entities.
     *
     * @number MXMLBEANS-3
     * @return The entity resolver catalog file location.
     */
    public final boolean hasCatalogFile() {
        getLog().debug("looking for resolver catalog at " + catalogLocation.getAbsolutePath());
        return catalogLocation.exists();
    }
    
    /**
     * Returns the name of the file used to resolve xml entities.
     *
     * @number MXMLBEANS-3
     * @return The entity resolver catalog file location.
     */
    public final String getCatalogFile() {
        getLog().debug("Using resolver catalog.");
        return catalogLocation.getAbsolutePath();
    }
    
    /**
     * Returns a file array of xsd files to translate to object models.
     * 
     * @return An array of schema files to be parsed by the schema compiler.
     */
    public final File[] getXsdFiles() throws XmlBeansException {

        if (xsdFiles == null) {
            File schemaDirectory = getSchemaDirectory();
            getLog().debug("The schema Directory is " + schemaDirectory);

            // take care of artifacts first.
            List schemas = getArtifactSchemas();

            // take care of the schema directory next.
            if (sourceSchemas != null) {
                for (StringTokenizer st = new StringTokenizer(sourceSchemas, ","); st
                        .hasMoreTokens();) {
                    String schemaName = st.nextToken();
                    schemas.add(new File(schemaDirectory, schemaName));
                }
            } else if (schemaDirectory.exists()) {
                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setBasedir(schemaDirectory);

                String[] includes = {"**/*.xsd"};
                scanner.setIncludes(includes);
                
                scanner.setCaseSensitive(false);
                scanner.scan();

                String[] files = scanner.getIncludedFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        getLog().debug("Adding " + files[i]);
                        schemas.add(new File(schemaDirectory, files[i]));
                    }
                }
            }

            xsdFiles = (File[]) schemas.toArray(new File[] {});
        }

        return xsdFiles;
    }
    
    /**
     * Sweep through the jar artifacts which contain xsds and produce a list of
     * paths to each xsd within the file. Leave it up to the entity resolver to
     * pass the actual file to the compiler.
     * 
     * @return A list of path's to the XSD's in the artifact jars. This doesn't
     *         include the jar paths.
     */
    private List getArtifactSchemas() throws XmlBeansException {
        getLog().debug("Artifact count: " + artifactMap.size());
        SchemaArtifactLookup lookup = new SchemaArtifactLookup(artifactMap, getLog());
        List artifactSchemas = new ArrayList();
        List xsdJars = getXsdJars();
        File prefix = getGeneratedSchemaDirectory();
        int count = xsdJars.size();

        // Collect the file paths to the actual jars
        Artifact nextArtifact = null;
        getLog().debug("looking for artifact schemas.");

        for (int i = 0; i < count; i++) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("resolving " + xsdJars.get(i) + "into a file path.");
            }
            nextArtifact = lookup.find((String) xsdJars.get(i));
            artifactSchemas.addAll(SchemaArtifact.getFilePaths(nextArtifact, getLog(), prefix));
        }

        return artifactSchemas;
    }

    /**
     * Returns the state of debuggin.
     * 
     * @return true if debug mode.
     */
    public final boolean isDebug() {
        return debug;
    }

    /**
     * Returns true if dependencies are to be downloaded by the schema compiler.
     * 
     * @return true if resources should be downloaded.
     */
    public final boolean isDownload() {
        return download;
    }

    /**
     * Returns true if jaxb is set.
     * 
     * @return true if the jaxb flag on the schema compiler should be set.
     */
    public final boolean isJaxb() {
        return jaxb;
    }

    /**
     * Returns True if generated source files are not to be compiled.
     * 
     * @return true if no compiling should occur.
     */
    public final boolean isNoJavac() {
        return noJavac;
    }

    /**
     * Do not enforce the particle valid (restriction) rule if true.
     * 
     * @return true if no enforcement should occur.
     */
    public final boolean isNoPvr() {
        return noPvr;
    }

    /**
     * If true, do not enforce the unique particle attribution rule.
     * 
     * @return particle attibution enforcement
     */
    public final boolean isNoUpa() {
        return noUpa;
    }

    /**
     * Returns true if the schema compiler should reduce verbosity.
     * 
     * @return true if message suppression is on.
     */
    public final boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns true if the schema compiler should increase verbosity.
     * 
     * @return true if verbose mode is on.
     */
    public final boolean isVerbose() {
        return verbose;
    }

    /**
     * No validation beyond those done by the maven plugin occur at this time.
     * 
     * @throws XmlBeansException Currently not used.
     */
    public final void validate() throws XmlBeansException {
    }

    public void setPluginArtifacts(List pluginArtifacts) {
        this.pluginArtifacts = pluginArtifacts;
    }
}
