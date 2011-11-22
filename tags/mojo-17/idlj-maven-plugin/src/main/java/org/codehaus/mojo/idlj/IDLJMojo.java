package org.codehaus.mojo.idlj;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file 
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 */

import java.util.List;

/**
 * Process CORBA IDL files in IDLJ.
 * 
 * @author Alan D. Cabrera <adc@apache.org>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 */
public class IDLJMojo extends AbstractIDLJMojo
{
    /**
     * The source directory containing *.idl files.
     * 
     * @parameter default-value="${basedir}/src/main/idl"
     */
    private String sourceDirectory;

    /**
     * Additional include directories containing additional *.idl files required for
     * compilation.
     * 
     * @parameter
     */
    private List includeDirs;

    /**
     * The directory to output the generated sources to.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/idl"
     */
    private String outputDirectory;

    /**
     * @return the source directory that conatins the IDL files
     */
    protected String getSourceDirectory()
    {
        return sourceDirectory;
    }

    /**
     * @return the <code>List</code> of the directories to use as 
     * include directories for the compilation
     */
    protected List getIncludeDirs()
    {
        return includeDirs;
    }

    /**
     * @return the path of the directory that will contains the results of the compilation
     */
    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Set the source directory.
     * 
     * @param dir the path of directory that conatins the IDL files
     */
    protected void setSourceDirectory( String dir )
    {
        this.sourceDirectory = dir;
    }

    /**
     * //TODO ????
     */
    protected void addCompileSourceRoot()
    {
        getProject().addCompileSourceRoot( getOutputDirectory() );
    }
}
