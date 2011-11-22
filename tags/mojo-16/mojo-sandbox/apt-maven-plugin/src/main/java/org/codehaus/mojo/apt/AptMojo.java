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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.List;

/**
 * process annotations
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @version $Id:$
 * 
 * @goal execute
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @description generates and/or compiles application sources
 */
public class AptMojo extends AbstractAPTMojo
{
    /**
     * The directory for compiled classes.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    protected File outputDirectory;

    /**
     * The source directories containing the sources to be compiled.
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    protected List compileSourceRoots;

    /**
     * The source directory containing the generated sources.
     * 
     * @parameter default-value="main/generated"
     */
    protected String generated;

    /**
     * Project classpath.
     * 
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    protected List classpathElements;

    /**
     * A list of inclusion filters. When none specified all *.java in the
     * project source directories are included.
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
    protected String[] options;

    protected String getGenerated()
    {
        return generated;
    }

    protected List getCompileSourceRoots()
    {
        return compileSourceRoots;
    }

    protected List getClasspathElements()
    {
        return classpathElements;
    }

    protected File getOutputDirectory()
    {
        return outputDirectory;
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
        return options;
    }

    public void execute() throws MojoExecutionException
    {
        super.execute();

        String genDir = getGeneratedFinalDir();
        project.addCompileSourceRoot( genDir );
        Resource resource = new Resource();
        resource.setDirectory( genDir );
        resource.addExclude( "**/*.java" );
        resource.setFiltering( isResourceFiltering() );
        if ( getResourceTargetPath() != null )
        {
            resource.setTargetPath( getResourceTargetPath() );
        }
        project.addResource( resource );
    }


}
