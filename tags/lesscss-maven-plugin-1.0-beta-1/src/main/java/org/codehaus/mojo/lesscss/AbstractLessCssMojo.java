package org.codehaus.mojo.lesscss;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Abstract class which provides common configuration properties and methods.
 * 
 * @author Marcel Overdijk
 */
public abstract class AbstractLessCssMojo
    extends AbstractMojo
{
    /**
     * The source directory containing the LESS sources.
     * 
     * @parameter expression="${lesscss.sourceDirectory}" default-value="${project.basedir}/src/main/less"
     * @required
     */
    protected File sourceDirectory;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value
     * is: { "**\/*.less" }
     * 
     * @parameter
     */
    protected String[] includes = new String[] { "**/*.less" };

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     * 
     * @parameter
     */
    protected String[] excludes = new String[] {};

    /**
     * Scans for the LESS sources that should be compiled.
     * 
     * @return The list of LESS sources.
     */
    protected String[] getIncludedFiles()
    {
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir( sourceDirectory );
        directoryScanner.setIncludes( includes );
        directoryScanner.setExcludes( excludes );
        directoryScanner.scan();
        return directoryScanner.getIncludedFiles();
    }
}
