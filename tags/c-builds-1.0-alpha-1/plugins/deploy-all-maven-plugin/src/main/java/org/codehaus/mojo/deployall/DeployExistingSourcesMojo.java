package org.codehaus.mojo.deployall;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;

/**
 * Deploys an artifact to remote repository.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:jdcasey@apache.org">John Casey (refactoring only)</a>
 * @version $Id$
 * 
 * @goal deploy-all
 */
public class DeployExistingSourcesMojo
    extends AbstractDeployMojo
{

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * @parameter expression="${srcURL}" default-value="${srcURL}"
     * @required
     */
    private File sourceFile;
    
    /**
     * @component
     */
    private ArtifactFactory artifactFactory;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ArtifactRepository deploymentRepository = project.getDistributionManagementArtifactRepository();
        
        if ( sourceFile == null || !sourceFile.exists() )
        {
            getLog().info( "Source-archive doesn't exist. Skipping project: " + project.getId() );
            return;
        }
        
        if ( deploymentRepository == null )
        {
            String msg = "Deployment failed: repository element was not specified in the pom inside" +
                " distributionManagement element. Skipping project: " + project.getId();
            
            getLog().info( msg );
            return;
        }
        
        String protocol = deploymentRepository.getProtocol();
        
        if( protocol.equals( "scp" ) )
        {
                File sshFile = new File( System.getProperty( "user.home" ), ".ssh" );

                if( !sshFile.exists() )
                {
                        sshFile.mkdirs();
                }	
        }
        
        String type = null;
        
        String sourceFileName = sourceFile.getName();
        
        if ( sourceFileName.endsWith( ".tgz" ) )
        {
            type = "tgz";
        }
        else if ( sourceFileName.endsWith( ".tar.gz" ) )
        {
            type = "tar.gz";
        }
        else if ( sourceFileName.endsWith( ".tar.bz2" ) )
        {
            type = "tar.bz2";
        }
        else if ( sourceFileName.endsWith( ".tar.Z" ) )
        {
            type = "tar.Z";
        }
        else if ( sourceFileName.endsWith( ".zip" ) )
        {
            type = "zip";
        }
        else if ( sourceFileName.endsWith( ".bin" ) )
        {
            type = "bin";
        }
        else
        {
            int dot = sourceFile.getName().indexOf( '.' );
            type = sourceFile.getName().substring( dot + 1 );
        }
        
        Artifact artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(), project.getArtifactId(), project.getVersion(), type, "sources" );
        
        artifact.setFile( sourceFile );
        
        File pomFile = project.getFile();
        
        // Deploy the POM
        boolean isPomArtifact = "pom".equals( project.getPackaging() );
        if ( !isPomArtifact )
        {
            ArtifactMetadata metadata = new ProjectArtifactMetadata( artifact, pomFile );
            artifact.addMetadata( metadata );
        }

        try
        {
            if ( isPomArtifact )
            {
                getDeployer().deploy( pomFile, artifact, deploymentRepository, getLocalRepository() );
            }
            else
            {
                getDeployer().deploy( sourceFile, artifact, deploymentRepository, getLocalRepository() );
            }
        }
        catch ( ArtifactDeploymentException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
}
