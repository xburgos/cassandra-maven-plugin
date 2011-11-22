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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jasper.JspC;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractJspcMojo extends AbstractMojo {

    private static final String PS = System.getProperty("path.separator");

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


    protected abstract List getClasspathElements();

    
    public void execute() throws MojoExecutionException {

        if ( getLog().isDebugEnabled() ) {
            getLog().debug( "Source directory: " + warSourceDirectory );
            getLog().debug( "Classpath: " + getClasspathElements().toString().replace( ',', '\n' ) );
            getLog().debug( "Output directory: " + workingDirectory/*getOutputDirectory()*/ );
        }

        try {
            // Create target directories if they don't already exist
            FileUtils.forceMkdir( new File(webFragmentFile).getParentFile() );
            FileUtils.forceMkdir( new File(outputWebXml).getParentFile() );

            // Create the JspC arguments
            List classpathElements = getClasspathElements();
            String classPath = getPathString(classpathElements);
            ArrayList args = getJspcArgs(classPath);
            String strArgs[] = (String[]) args.toArray(new String[args.size()]);

            // JspC needs URLClassLoader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URLClassLoader cl2 = new URLClassLoader(new URL[0], cl);
            Thread.currentThread().setContextClassLoader(cl2);
            try {
                // Run JspC
                JspC jspc = new JspC();
                jspc.setArgs(strArgs);
                jspc.execute();
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
}
