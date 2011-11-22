package org.codehaus.mojo.make;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
     * @parameter expression="${command}" default-value="make"
     */
    private String command;

    /**
     * The make target to execute.
     * 
     * @parameter expression="${target}" default-value="dist"
     */
    private String target;
    
    /**
     * Set this parameter to skip this goal.
     * 
     * @parameter expression="${skip}" default-value="false"
     */
    private boolean skip;
    
    /**
     * Commandline options for the execution of make.
     * 
     * @parameter
     */
    private List options;
    
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
    
    public void execute()
        throws MojoExecutionException
    {
        try
        {
            setCommand( command );
            setTarget( target );
            setOptions( options );
            setSkipped( skip );
            
            getLog().info( "Creating source archive for \'" + getProject().getId() + "\'..." );
            super.execute();
            
            File sourceArchiveFile = new File( sourceArchivePath, sourceArchive );
            
            if ( !sourceArchiveFile.exists() )
            {
                getLog().info( "\'" + sourceArchiveFile.getAbsolutePath() + "\' not found." );
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
