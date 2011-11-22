package org.codehaus.mojo.rpm;

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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.ActiveProjectArtifact;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInstallException;

/**
 * Used to install the RPM onto the OS. This is critical for multimodule
 * builds, since dependent compiles need RPMs installed.
 * 
 * @author jdcasey
 * 
 * @goal install-dependencies
 * @requiresDependencyResolution test
 * @phase initialize
 */
public class InstallDependencyRpmsMojo
    extends AbstractRpmInstallMojo
{

    /**
     * @parameter default-value="false" alias="rpm.dependencies.skip"
     */
    private boolean skipDependencies;

    /**
     * These are the dependency RPMs for the current project.
     * 
     * @parameter default-value="${project.artifacts}"
     * @required
     * @readonly
     */
    private Set artifacts;

    /**
     * @parameter expression="${rpm.force.install}" default-value="false"
     */
    private boolean forceDependencyInstalls;

    /**
     * @parameter default-value="${project.packaging}"
     * @readonly
     * @required
     */
    private String projectPackaging;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * Build the RPM filesystem structure, setup the Rpm Ant task, and execute. Then, set the File for the
     * project's Artifact instance to the generated RPM for use in the install and deploy phases.
     * @throws MojoFailureException 
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( "pom".equals( projectPackaging ) )
        {
            getLog().info( "Skipping RPM-dependency installation for POM project." );
            return;
        }

        if ( skipDependencies )
        {
            getLog().info( "Skipping RPM dependency install (per configuration)." );
            return;
        }
        
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();
            
            getLog().debug( "Processing dependency: " + artifact.getId() );

            try
            {
                Artifact dummy = artifactFactory.createProjectArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
                
                MavenProject project = projectBuilder.buildFromRepository( dummy, remoteRepositories,
                                                                           localRepository );

                // avoid creating a recursive loop for things like Artifact.getFile(..).
                if ( !( artifact instanceof ActiveProjectArtifact ) )
                {
                    project.setArtifact( artifact );
                }

                install( project, forceDependencyInstalls );
            }
            catch ( RpmInstallException e )
            {
                throw new MojoExecutionException( "Failed to install dependency RPM: " + artifact.getId(), e );
            }
            catch ( RpmFormattingException e )
            {
                throw new MojoExecutionException( "Failed to install dependency RPM: " + artifact.getId(), e );
            }
            catch ( ProjectBuildingException e )
            {
                throw new MojoExecutionException( "Failed to retrieve POM information for dependency RPM: "
                    + artifact.getId(), e );
            }
        }

    }

}
