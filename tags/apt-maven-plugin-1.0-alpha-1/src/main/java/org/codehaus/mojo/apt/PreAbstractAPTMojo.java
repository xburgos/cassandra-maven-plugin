package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @version $Id:$
 */
public abstract class PreAbstractAPTMojo extends AbstractMojo
{
    protected static final String PATH_SEPARATOR = System.getProperty( "path.separator" );

    protected static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

    /**
     * Name of AnnotationProcessorFactory to use; bypasses default discovery process
     * 
     * @parameter
     */
    protected String factory;

    /**
     * The directory to run the compiler from if fork is true.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    protected File builddir;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Wether to generate to build dir or not
     * @parameter expression="true"
     * @required
     */
    protected boolean generateToBuildDir;

    /**
     * 
     * @return list of classpath elements
     */
    protected abstract List getClasspathElements();

    /**
     * 
     * @return relative path of generated sources
     */
    protected abstract String getGenerated();

    /**
     * @return A list of inclusion filters. When none specified all *.java in the project source directories are
     *         included.
     */
    protected abstract String[] getIncludes();

    /**
     * @return A list of exclusion filters.
     */
    protected abstract String[] getExcludes();

    /**
     * @parameter Set of apt options. Coresponds with -A switch
     */
    protected abstract String[] getOptions();

    protected String getGeneratedFinalDir()
    {
        String ret = "";
        if ( generateToBuildDir )
        {
            ret = builddir.getAbsolutePath();
        }
        else
        {
            ret = project.getBasedir().getAbsolutePath();
        }
        ret += FILE_SEPARATOR + getGenerated();
        return new File( ret ).getAbsolutePath();
    }
}
