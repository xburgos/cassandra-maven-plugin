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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * process annotations
 * 
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @version $Id:$
 * 
 * @goal test-execute
 * @phase generate-sources
 * @requiresDependencyResolution test
 * @description Generats and/or compiles test sources
 */
public class TestAptMojo extends AbstractAPTMojo
{

    /**
     * The source directory containing the generated sources to be compiled.
     * 
     * @parameter default-value="test/generated"
     */
    protected String testGenerated;

    /**
     * The source directories containing the test-source to be compiled.
     * 
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    protected List testCompileSourceRoots;

    /**
     * Project test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List testClasspathElements;

    /**
     * The directory where compiled test classes go.
     * 
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    protected File testOutputDirectory;

    /**
     * A list of inclusion filters. When none specified all *.java in the project test source directories are included.
     * 
     * @parameter
     */
    protected String[] includes;

    /**
     * A list of exclusion filters.
     * 
     * @parameter
     */
    protected String[] excludes;

    /**
     * Set of apt options. Coresponds with -A switch
     * 
     * @parameter
     */
    protected String[] testOptions;

    protected String getGenerated()
    {
        return testGenerated;
    }

    protected List getCompileSourceRoots()
    {
        return testCompileSourceRoots;
    }

    protected List getClasspathElements()
    {
        return testClasspathElements;
    }

    protected File getOutputDirectory()
    {
        return testOutputDirectory;
    }

    protected String[] getIncludes()
    {
        return includes;
    }

    protected String[] getExcludes()
    {
        return excludes;
    }

    protected String[] getOptions()
    {
        return testOptions;
    }

    public void execute() throws MojoExecutionException
    {
        super.execute();

        String genDir = getGeneratedFinalDir();
        project.addTestCompileSourceRoot( genDir );
        Resource resource = new Resource();
        resource.setDirectory( genDir );
        resource.addExclude( "**/*.java" );
        resource.setFiltering( isResourceFiltering() );
        if ( getResourceTargetPath() != null )
        {
            resource.setTargetPath( getResourceTargetPath() );
        }
        project.addTestResource( resource );
    }

}
