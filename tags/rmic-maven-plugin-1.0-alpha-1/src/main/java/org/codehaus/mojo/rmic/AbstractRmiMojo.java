package org.codehaus.mojo.rmic;

/*
 * Copyright (c) 2004, Codehaus.org
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

import org.apache.maven.plugin.AbstractMojo;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractRmiMojo
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Configurable parameters
    // ----------------------------------------------------------------------

    /**
     * A list of inclusions when searching for classes to compile.
     * 
     * @parameter
     */
    protected Set includes;

    /**
     * A list of exclusions when searching for classes to compile.
     * 
     * @parameter
     */
    protected Set excludes;

    /**
     * The id of the rmi compiler to use.
     * 
     * @parameter default-value="sun"
     * @required
     */
    protected String compilerId;

    /**
     * Specifies where to place rmic generated class files.
     * 
     * @parameter default-value="${project.build.directory}/rmi-classes"
     */
    private File outputDirectory;

    /**
     * The version of the rmi protocol to which the stubs should be compiled.
     * Valid values include 1.1, 1.2, compat.  See the rmic documentation for more
     * information.  The default is 1.2.
     *  
     * @parameter
     */
    private String version;

    /**
     * Create stubs for IIOP.
     *  
     * @parameter default-value="false"
     */
    private boolean iiop;

    /**
     * Do not create stubs optimized for same process.
     *  
     * @parameter 
     */
    private boolean noLocalStubs;

    /**
     * Create IDL.
     * 
     * @parameter default-value="false"
     */
    private boolean idl;

    /**
     * Do not generate methods for valuetypes.
     * 
     * @parameter
     */
    private boolean noValueMethods;

    /**
     * Do not delete intermediate generated source files.
     * 
     * @parameter default-value="false"
     */
    private boolean keep;

    /**
     * Turn off rmic warnings.
     * 
     * @parameter
     */
    private boolean nowarn;

    /**
     * Enable verbose rmic output.
     * 
     * @parameter
     */
    private boolean verbose;

    /**
     * Directory tree where the compiled Remote classes are located.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    // ----------------------------------------------------------------------
    // Constant parameters
    // ----------------------------------------------------------------------

    /**
     * Compile classpath of the maven project.
     * 
     * @parameter expression="${project.compileClasspathElements}"
     * @readonly
     */
    protected List projectCompileClasspathElements;

    // ----------------------------------------------------------------------
    // Methods
    // ----------------------------------------------------------------------

    public String getCompilerId()
    {
        return compilerId;
    }

    /**
     * Get the directory where rmic generated class files are written.
     * 
     * @return the directory
     */
    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public File getClassesDirectory()
    {
        return classesDirectory;
    }

    public boolean isIiop()
    {
        return iiop;
    }

    public boolean isIdl()
    {
        return idl;
    }

    public boolean isKeep()
    {
        return keep;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public boolean isNowarn()
    {
        return nowarn;
    }
    
    public boolean isVerbose()
    {
        return verbose;
    }

    public boolean isNoLocalStubs()
    {
        return noLocalStubs;
    }

    public boolean isNoValueMethods()
    {
        return noValueMethods;
    }
}
