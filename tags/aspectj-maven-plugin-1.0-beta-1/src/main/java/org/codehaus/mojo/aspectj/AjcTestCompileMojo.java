package org.codehaus.mojo.aspectj;

/**
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Weaves all test classes.
 * 
 * @goal test-compile
 * @requiresDependencyResolution test
 * @phase process-test-sources
 * @description AspectJ Compiler Plugin.
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class AjcTestCompileMojo
    extends AbstractAjcCompiler
{   
    /**
     * 
     */
    protected List getOutputDirectories()
    {
        return Arrays.asList(new String[]{project.getBuild().getOutputDirectory(),project.getBuild().getTestOutputDirectory()});
    }

    /**
     * 
     */
    protected List getSourceDirectories()
    {
    	List sourceDirs = new ArrayList();
    	sourceDirs.addAll(project.getTestCompileSourceRoots());
    	sourceDirs.addAll(project.getCompileSourceRoots());
        return sourceDirs;
    }


}
