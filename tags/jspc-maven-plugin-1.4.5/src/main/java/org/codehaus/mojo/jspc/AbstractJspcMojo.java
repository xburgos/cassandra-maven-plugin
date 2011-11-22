/*
 * Copyright 2005 Jeff Genender.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Jeff Genender
 * @author Grzegorz Slowikowski
 */

package org.codehaus.mojo.jspc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.jasper.JspC;
import org.apache.jasper.xmlparser.ParserUtils;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

public abstract class AbstractJspcMojo extends AbstractMojo {

    private static final String EOL = System.getProperty("line.separator");

    private static final String PS = File.pathSeparator;

    private static final String WARNING_PREFIX = "warning: ";

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Character encoding
     * 
     * @parameter
     */
    protected String javaEncoding;

    /**
     * Provide source compatibility with specified release
     * 
     * @parameter
     */
    protected String source;

    /**
     * Generate class files for specific VM version
     * 
     * @parameter
     */
    protected String target;

    /**
     * The working directory to create the generated java source files.
     * 
     * @parameter expression="${project.build.directory}/jsp-source"
     * @required
     */
    protected String workingDirectory;

    /**
     * The path and location to the web fragment file.
     * 
     * @parameter expression="${project.build.directory}/web-fragment.xml"
     * @required
     */
    protected String webFragmentFile;

    /**
     * Source directory of the web source.
     * 
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    protected String warSourceDirectory;

    /**
     * The path and location of the original web.xml file
     * 
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     * @required
     */
    protected String inputWebXml;

    /**
     * The string to look for in the web.xml to replace with the web fragment
     * contents
     * 
     * @parameter expression="<!-- [INSERT FRAGMENT HERE] -->"
     * @required
     */
    protected String injectString;

    /**
     * The final path and file name of the web.xml.
     * 
     * @parameter expression="${project.build.directory}/jspweb.xml"
     * @required
     */
    protected String outputWebXml;

    /**
     * The package in which the jsp files will be contained.
     * 
     * @parameter default-value="jsp"
     */
    protected String packageName;

    /**
     * Verbose option for JcpC.
     * 
     * @parameter default-value="false"
     */
    protected boolean verbose;

    /**
     * Show Success option for JcpC.
     * 
     * @parameter default-value="true"
     */
    protected boolean showSuccess;

    /**
     * List Errors option for JcpC.
     * 
     * @parameter default-value="true"
     */
    protected boolean listErrors;

    /**
     * Validate XML option for JcpC.
     *
     * @parameter default-value="false"
     */
    protected boolean validateXml;

    protected abstract List getClasspathElements();


    public void execute() throws MojoExecutionException {
        if ( !"war".equalsIgnoreCase(project.getPackaging()) ) {
            if ( getLog().isDebugEnabled() )
                getLog().debug( "Packaging is not war. Plugin irrelevant" );
            return;
        }

        if ( getLog().isDebugEnabled() ) {
            getLog().debug( "Source directory: " + warSourceDirectory );
            getLog().debug( "Classpath: " + getClasspathElements().toString().replace( ',', '\n' ) );
            getLog().debug( "Output directory: " + workingDirectory/*getOutputDirectory()*/ );
        }

        try {
            // Create target directories if they don't already exist
            FileUtils.forceMkdir( new File(workingDirectory));
            FileUtils.forceMkdir( new File(webFragmentFile).getParentFile() );
            FileUtils.forceMkdir( new File(outputWebXml).getParentFile() );
            FileUtils.forceMkdir( new File(project.getBuild().getDirectory()) );
            FileUtils.forceMkdir( new File(project.getBuild().getOutputDirectory()) );

            // Find the tools.jar and add it to the classpath
            String toolsJar = null;
            if (System.getProperty("os.name").equals("Mac OS X")) {
                toolsJar = System.getProperty("java.home")
                    + "/../Classes/classes.jar";
            } else {
                toolsJar = System.getProperty("java.home") + File.separatorChar
                    + ".." + File.separatorChar + "lib"
                    + File.separatorChar + "tools.jar";
            }

            // Create the JspC arguments
            List classpathElements = getClasspathElements();
            String classPath = getPathString(classpathElements);
            ArrayList args = getJspcArgs(classPath);
            String strArgs[] = (String[]) args.toArray(new String[args.size()]);

            // JspC.main() doesn't provide an option for this, so set it a back-door way.
            ParserUtils.validating = validateXml;

            // JspC needs URLClassLoader
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            JspcMojoClassLoader cl = new JspcMojoClassLoader(parent);

            // Add the tools.jar
            cl.addURL(new File(toolsJar).getAbsoluteFile().toURL());

            Thread.currentThread().setContextClassLoader(cl);

            try {
                // Run JspC
                JspC jspc = new JspC();
                jspc.setArgs(strArgs);
                jspc.execute();

                // Now compile the JSP Java code
                
                String sourceFiles[] = getSourceFiles();
                if (sourceFiles.length == 0){
                    getLog().info("Nothing to compile.");
                } else {
                    getLog().info("Compiling new java files...");
                    Class c = cl.loadClass("com.sun.tools.javac.Main");
                    StringWriter out = new StringWriter();
                    String[] javacArgs = getJavacArgs(classPath.toString(), sourceFiles);
                    Method compile = c.getMethod("compile", new Class[] {
                        String[].class, PrintWriter.class });
                    Integer ok = (Integer) compile.invoke(null, new Object[] {
                        javacArgs, new PrintWriter(out) });
                    List messages = parseModernStream(new BufferedReader(
                                new StringReader(out.toString())));

                    for (Iterator iter = messages.iterator(); iter.hasNext();) {
                        Object element = (Object) iter.next();
                        if (element instanceof String)
                            getLog().info((String)element);   
                        else {
                            getLog().error(((CompilerError)element).toString());
                        }
                    }

                    if (ok.intValue() != 0)
                        throw new MojoExecutionException("Errors found during compilation.");
                }

            } finally {
                // Set back the old classloader
                Thread.currentThread().setContextClassLoader(cl);
            }

            writeWebXml();

            project.addCompileSourceRoot(workingDirectory);

        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Error", e);
        }
    }

    private String[] getJavacArgs(String classPath, String[] sourceFiles) {
        ArrayList args = new ArrayList();
        args.add("-d");
        args.add(new File(project.getBuild().getOutputDirectory())
                .getAbsolutePath());
        args.add("-sourcepath");
        args.add(new File(workingDirectory).getAbsolutePath());
        if (verbose) {
            args.add("-verbose");
        }
        if (source != null) {
            args.add("-source");
            args.add(source);
        }
        if (target != null) {
            args.add("-target");
            args.add(target);
        }

        if (javaEncoding != null) {
            args.add("-encoding");
            args.add(javaEncoding);
        }
        args.add("-classpath");
        args.add(classPath);

        for (int i = 0; i < sourceFiles.length; i++) {
            args.add(sourceFiles[i]);
        }

        this.getLog().debug("javac args: " + args);

        return (String[]) args.toArray(new String[args.size()]);
    }

    private ArrayList getJspcArgs(String classPath)
        throws MojoExecutionException {
        ArrayList args = new ArrayList();
        args.add("-uriroot");
        args.add(warSourceDirectory);
        args.add("-d");
        args.add(workingDirectory);
        if (javaEncoding != null) {
            args.add("-javaEncoding");
            args.add(javaEncoding);
        }
        if (source != null) {
            args.add("-source");
            args.add(source);
        }
        if (target != null) {
            args.add("-target");
            args.add(target);
        }

        if (showSuccess) {

            args.add("-s");
        }
        if (listErrors) {
            args.add("-l");
        }
        args.add("-webinc");
        args.add(webFragmentFile);
        args.add("-p");
        args.add(packageName);
        if (verbose) {
            args.add("-v");
        }
        args.add("-classpath");
        args.add(classPath);

        this.getLog().debug("jspc args: " + args);

        return args;
    }

    private void writeWebXml() throws Exception {
        String webXml = getFile(inputWebXml);
        String fragmentXml = getFile(webFragmentFile);

        int pos = webXml.indexOf(injectString);

        if (pos < 0) {
            throw new MojoExecutionException("injectString('" + injectString
                    + "') not found in webXml(" + inputWebXml + "')");
        }

        String output = webXml.substring(0, pos)
            + fragmentXml
            + webXml
            .substring(pos + injectString.length(), webXml.length());

        FileWriter fw = new FileWriter(outputWebXml);
        fw.write(output);
        fw.close();
    }

    private String getFile(String fName) throws Exception {
        File f = new File(fName);

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        fr.close();

        return sb.toString();
    }

    public String getPathString(List pathElements) {
        StringBuffer sb = new StringBuffer();

        for (Iterator it = pathElements.iterator(); it.hasNext();) {
            sb.append(it.next()).append(PS);
        }

        return sb.toString();
    }

    protected Set getSourceFilesForSourceRoot() {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(workingDirectory);

        scanner.setIncludes(new String[] { "**/*.java" });

        scanner.scan();

        String[] sourceDirectorySources = scanner.getIncludedFiles();

        Set sources = new HashSet();

        for (int j = 0; j < sourceDirectorySources.length; j++) {
            File f = new File(workingDirectory, sourceDirectorySources[j]);

            sources.add(f.getPath());
        }

        return sources;
    }

    protected String[] getSourceFiles() {
        Set sources = new HashSet();

        sources.addAll(getSourceFilesForSourceRoot());

        String[] result;

        if (sources.isEmpty()) {
            result = new String[0];
        } else {
            result = (String[]) sources.toArray(new String[sources.size()]);
        }

        return result;
    }

    protected List parseModernStream(BufferedReader input) throws IOException {
        List errors = new ArrayList();

        String line;

        StringBuffer buffer;

        while (true) {
            // cleanup the buffer
            buffer = new StringBuffer(); // this is quicker than clearing it

            // most errors terminate with the '^' char
            do {
                line = input.readLine();

                if (line == null) {
                    return errors;
                }

                // TODO: there should be a better way to parse these
                if (buffer.length() == 0 && line.startsWith("error: ")) {
                    errors.add(new CompilerError(line, true));
                } else if (buffer.length() == 0 && line.startsWith("Note: ")) {
                    // skip this one - it is JDK 1.5 telling us that the
                    // interface is deprecated.
                } else {
                    buffer.append(line);

                    buffer.append(EOL);
                }
            } while (!line.endsWith("^"));

            // add the error bean
            errors.add(parseModernError(buffer.toString()));
        }
    }

    public CompilerError parseModernError(String error) {
        StringTokenizer tokens = new StringTokenizer(error, ":");

        boolean isError = true;

        StringBuffer msgBuffer;

        try {
            String file = tokens.nextToken();

            // When will this happen?
            if (file.length() == 1) {
                file = new StringBuffer(file).append(":").append(
                        tokens.nextToken()).toString();
            }

            int line = Integer.parseInt(tokens.nextToken());

            msgBuffer = new StringBuffer();

            String msg = tokens.nextToken(EOL).substring(2);

            isError = !msg.startsWith(WARNING_PREFIX);

            // Remove the 'warning: ' prefix
            if (!isError) {
                msg = msg.substring(WARNING_PREFIX.length());
            }

            msgBuffer.append(msg);

            msgBuffer.append(EOL);

            String context = tokens.nextToken(EOL);

            String pointer = tokens.nextToken(EOL);

            if (tokens.hasMoreTokens()) {
                msgBuffer.append(context); // 'symbol' line

                msgBuffer.append(EOL);

                msgBuffer.append(pointer); // 'location' line

                msgBuffer.append(EOL);

                context = tokens.nextToken(EOL);

                try {
                    pointer = tokens.nextToken(EOL);
                } catch (NoSuchElementException e) {
                    pointer = context;

                    context = null;
                }

            }

            String message = msgBuffer.toString();

            int startcolumn = pointer.indexOf("^");

            int endcolumn = context == null ? startcolumn : context.indexOf(
                    " ", startcolumn);

            if (endcolumn == -1) {
                endcolumn = context.length();
            }

            return new CompilerError(file, isError, line, startcolumn, line,
                    endcolumn, message);
        } catch (NoSuchElementException e) {
            return new CompilerError(
                    "no more tokens - could not parse error message: " + error,
                    isError);
        } catch (NumberFormatException e) {
            return new CompilerError("could not parse error message: " + error,
                    isError);
        } catch (Exception e) {
            return new CompilerError("could not parse error message: " + error,
                    isError);
        }
    }
}
