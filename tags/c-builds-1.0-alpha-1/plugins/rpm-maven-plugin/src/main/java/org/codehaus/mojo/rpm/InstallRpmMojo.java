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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInstallException;

/**
 * Used to install the RPM onto the OS. This is critical for multimodule
 * builds, since dependent compiles need RPMs installed.
 * 
 * @author jdcasey
 * 
 * @goal install
 * @requiresDependencyResolution runtime
 * @phase install
 */
public class InstallRpmMojo
    extends AbstractRpmInstallMojo
{

    /**
     * @parameter default-value="false" alias="rpm.install.skip"
     */
    private boolean skipInstall;

    /**
     * @parameter expression="${forceInstall}" default-value="false"
     */
    private boolean forceInstall;

    /**
     * MavenProject instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Build the RPM filesystem structure, setup the Rpm Ant task, and execute. Then, set the File for the
     * project's Artifact instance to the generated RPM for use in the install and deploy phases.
     * @throws MojoFailureException 
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skipInstall )
        {
            getLog().info( "Skipping RPM build (per configuration)." );
            return;
        }

        File rpmFile = project.getArtifact().getFile();
        
        if ( rpmFile == null )
        {
            throw new MojoFailureException( this, "RPM file does not exist.", "RPM file has not been set on project artifact for: " + project.getId() );
        }
        else if ( !rpmFile.exists() )
        {
            throw new MojoFailureException( this, "RPM file does not exist.", "Cannot install missing RPM: " + rpmFile );
        }
        
        try
        {
            install( project, forceInstall );
        }
        catch ( RpmInstallException e )
        {
            throw new MojoExecutionException( "Failed to install project RPM for: " + project, e );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Failed to install project RPM for: " + project, e );
        }
    }

}
