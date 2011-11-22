package org.codehaus.mojo.projectArchive;

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

import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Installs only the project archive and corresponding POM to the local repository.
 * 
 * @author jdcasey
 * 
 * @phase install
 * @goal install
 * @requiresDirectInvocation
 */
public class InstallArchiveOnlyMojo
    extends AbstractProjectArchiveMojo
{
    
    /**
     * @component
     */
    private ArtifactInstaller artifactInstaller;
    
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * @parameter default-value="${project.packaging}"
     * @required
     * @readonly
     */
    private String packaging;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( "pom".equals( packaging ) )
        {
            getLog().info( "Skipping project-archive operations for POM project." );
            return;
        }
        
        createProjectArchive();
        
        try
        {
            artifactInstaller.install( getOutputFile(), getProjectArchiveArtifact(), localRepository );
        }
        catch ( ArtifactInstallationException e )
        {
            throw new MojoExecutionException( "Failed to install project archive to local repository.", e );
        }
    }

}
