package org.codehaus.mojo.make;

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

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;

/**
 * @goal make-dist
 * @author nramirez
 */
public class MakeDistExecMojo
    extends AbstractMakeExecMojo
{
    
    /**
     * The command to execute the 'make' program.
     * 
     * @parameter expression="${distCommand}" default-value="make"
     * @since 1.0-beta-1
     */
    private String distCommand;

    /**
     * The make target to execute.
     * 
     * @parameter expression="${distTarget}" default-value="dist"
     * @since 1.0-beta-1
     */
    private String distTarget;
    
    /**
     * Set this parameter to skip calling "make dist" which will create a release tarball
     * 
     * @parameter expression="${skipDist}" default-value="true"
     * @since 1.0-beta-1
     */
    private boolean skipDist;
    
    /**
     * Commandline options for the execution of make.
     * 
     * @parameter
     * @since 1.0-beta-1
     */
    private List < String > distOptions;
    
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;
    
    /**
     * @parameter expression="${sourceArchivePath}" default-value="${basedir}/src/main/project"
     */
    private String sourceArchivePath;
    
    /**
     * @parameter expression="${sourceArchive}" default-value="${project.artifactId}-${project.version}.tar.gz"
     */
    private String sourceArchive;

    /**
     * Creates a distribution tarball
     * 
     * @throws MojoExecutionException thown when distribution creation fails
     */
    public void execute() throws MojoExecutionException
    {
        try
        {
            setCommand( distCommand );
            setTarget( distTarget );
            setOptions( distOptions );
            setSkipped( skipDist );
            
            getLog().info( "Creating source archive for \'" + getProject().getId() + "\'..." );
            super.execute();
            
            File sourceArchiveFile = new File( sourceArchivePath, sourceArchive );
            
            if ( !sourceArchiveFile.exists() )
            {
                if ( !skipDist )
                {
                    getLog().info( "\'" + sourceArchiveFile.getAbsolutePath() + "\' not found." );
                }
            }
            else
            {
                getLog().info( "\'" + sourceArchiveFile.getAbsolutePath() + "\' found." );
                projectHelper.attachArtifact( getProject(), "tar.gz", "sources", sourceArchiveFile );
            }
        }
        catch ( MojoExecutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
}
