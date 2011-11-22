package org.codehaus.mojo.project.archive;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Deploy the project-sources artifact to the remote repository. The project-sources artifact should
 * already have been created by the package-project-sources mojo.
 * 
 * @goal deploy-project-sources
 * @phase deploy
 * @author jdcasey
 */
public class DeployProjectSourcesMojo
    extends AbstractProjectSourcesMojo
{

    /**
     * Location to which the project-sources should be deployed. This parameter is derived from the
     * repository or sourceRepository subsection within the distributionManagement section of the POM.
     * 
     * @parameter expression="${project.distributionManagementArtifactRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository deploymentRepository;

    /**
     * Component used to deploy the project-sources artifact.
     * 
     * @component
     */
    private ArtifactDeployer artifactDeployer;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectSourceContext context;
        try
        {
            context = loadContext();
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Unable to read build cookies.");
        }

        if ( !context.isSourceArtifactResolved() )
        {
            Artifact sourceArtifact = context.getProjectSourceArtifact();
            
            if ( sourceArtifact == null )
            {
                throw new MojoExecutionException( "Project-sources artifact not found. " +
                        "\nPlease ensure the package-project-sources mojo is bound to the current lifecycle." );
            }

            String protocol = deploymentRepository.getProtocol();

            if ( protocol.equals( "scp" ) )
            {
                File sshDir = new File( System.getProperty( "user.home" ), ".ssh" );

                if ( !sshDir.exists() )
                {
                    sshDir.mkdirs();
                }
            }

            File sourceArtifactFile = sourceArtifact.getFile();
            
            if ( sourceArtifactFile == null )
            {
                throw new MojoExecutionException(
                                                  "The packaging for this project did not assign a file to the build artifact" );
            }

            try
            {
                artifactDeployer.deploy( sourceArtifactFile, sourceArtifact, deploymentRepository, getLocalRepository() );
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( "Failed to deploy project-sources artifact: " + sourceArtifact
                                + " to repository with id: " + deploymentRepository.getId() );
            }
        }
    }

}
