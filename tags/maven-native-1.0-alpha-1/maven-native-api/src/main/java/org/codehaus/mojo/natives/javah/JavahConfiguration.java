package org.codehaus.mojo.natives.javah;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
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

public class JavahConfiguration 
{
	/*
	 *  the fully-qualified name of a class/or classes separated by a ','
	 */
	private String [] classNames;
	
	/**
	 *  Sets the directory where javah saves the header files or the stub files
	 */
	private File destDir;
    
    /**
     * Support javah -o option
     */
    private String fileName;
	
	/**
	 * ClassPaths to locate classNames, separated by a ','
	 */
	private String [] classPaths;
	
	private boolean verbose = false;
	
	public void setDestDir( File destDir )
	{
		this.destDir = destDir;
	}
	
    public String getDestdir()
    {
    	return this.destDir.getPath();
    }
    
    public String [] getClassPaths()
    {
    	return this.classPaths;
    }
    
    public void setClassPaths(String [] paths ) 
    {
    	this.classPaths = paths;
    }

    public void setVerbose( boolean flag )
    {
    	this.verbose = flag;
    }
    
    public boolean getVerbose()
    {
    	return this.verbose;
    }
    
    public void setClassNames ( String [] names )
    {
    	this.classNames = names;
    }
    
    public String [] getClassNames() 
    {
    	return this.classNames;
    }
    
    public void setFileName( String name )
    {
        this.fileName = name;
    }

    public String getFileName()
    {
        return this.fileName;
    }
	
}
