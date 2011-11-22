package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
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
import java.util.Set;

/**
 * Executes apt on project test sources.
 * 
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 * @goal test-process
 * @phase generate-test-resources
 * @requiresDependencyResolution test
 */
public class TestProcessMojo extends AbstractAptMojo
{
    // read-only parameters ---------------------------------------------------

    /**
     * The source directories containing the test sources to be processed.
     * 
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    private List testCompileSourceRoots;

    /**
     * The project's test resources.
     * 
     * @parameter expression="${project.testResources}"
     * @required
     * @readonly
     */
    private List testResources;
    
    /**
     * The project's test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List testClasspathElements;

    // configurable parameters ------------------------------------------------

    /**
     * A list of inclusion filters for apt. Default value is <code>**&#047;*.java</code>.
     * 
     * @parameter
     */
    private Set includes;

    /**
     * A list of exclusion filters for apt.
     * 
     * @parameter
     */
    private Set excludes;

    /**
     * The directory root under which processor-generated test source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     * 
     * @parameter default-value="${project.build.directory}/generated-test-sources/apt"
     */
    private File testOutputDirectory;

    // AbstractAptMojo methods ------------------------------------------------

    /**
     * {@inheritDoc}
     */
    protected List getCompileSourceRoots()
    {
        return testCompileSourceRoots;
    }

    /**
     * {@inheritDoc}
     */
    protected List getResources()
    {
        return testResources;
    }

    /**
     * {@inheritDoc}
     */
    protected List getClasspathElements()
    {
        return testClasspathElements;
    }

    /**
     * {@inheritDoc}
     */
    protected Set getIncludes()
    {
        return includes;
    }

    /**
     * {@inheritDoc}
     */
    protected Set getExcludes()
    {
        return excludes;
    }

    /**
     * {@inheritDoc}
     */
    protected File getOutputDirectory()
    {
        return testOutputDirectory;
    }
}
