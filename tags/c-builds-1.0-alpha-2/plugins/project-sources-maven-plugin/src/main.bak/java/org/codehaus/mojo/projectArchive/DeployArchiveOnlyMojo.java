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

import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Deploys only the project archive and corresponding POM to the remote repository.
 * 
 * @author jdcasey
 * 
 * @phase deploy
 * @goal deploy
 * @requiresDirectInvocation
 */
public class DeployArchiveOnlyMojo
    extends AbstractProjectArchiveMojo
{

    /**
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.distributionManagementArtifactRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository remoteRepository;

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
            artifactDeployer.deploy( getOutputFile(), getProjectArchiveArtifact(), remoteRepository,
                                     localRepository );
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( "Failed to deploy project archive.", e );
        }
    }

}
