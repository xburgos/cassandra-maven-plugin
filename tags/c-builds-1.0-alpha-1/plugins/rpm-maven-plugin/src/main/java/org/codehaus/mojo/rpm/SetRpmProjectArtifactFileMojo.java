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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;

/**
 * Setup the RPM file reference for MavenProject.getArtifact().getFile(), so we can use this as a
 * basis for determining whether to run a build or not (based on the lastMod of this file vs. the
 * latest lastMod of the source files...this is handled in another plugin.)
 * 
 * @author jdcasey
 * 
 * @goal set-project-file
 */
public class SetRpmProjectArtifactFileMojo
    extends AbstractMojo
{

   /**
    *Override for platform postfix on RPM release number
    *
    * @parameter expression="${platformPostfix}" alias="rpm.genspec.platformPostfix"
    */
    private String platformPostfix;

    /**
     * Whether to skip postfix on RPM release number
     * rhoover - we can't use null on platformPostfix as an indication to skip the postfix
     *           until this bug is fixed (http://jira.codehaus.org/browse/MNG-1959;jsessionid=a9HqXpP8ZvX7DDXqNR?page=all)
     *           because currently specifying an empty string for a parameter in the POM yields null instead
     *           of an empty string.
     *
     * @parameter expression="${skipPlatformPostfix}" default-value="false" alias="rpm.genspec.skipPlatformPostfix"
     */
     private boolean skipPlatformPostfix;

    /**
     * Override parameter for the name of this RPM
     * 
     * @parameter
     */
    private String rpmName;
    
    /**
     * Override the entire rpm file, not just the basename (which doesn't include
     * architecture and .rpm extension).
     * 
     * @parameter
     */
    private File rpmFile;

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
     * The RPM releaes level or build number
     * 
     * @parameter expression="${release}" default-value="1"
     */
    private String release;

    /**
     * Top directory of the RPM filesystem structure.
     * 
     * @parameter default-value="${project.build.directory}/rpm-topdir"
     * @required
     */
    private File topDir;
    
    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @component role-hint="default"
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component role-hint="default"
     */
    private ProjectRpmFileManager projectRpmFileManager;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( BuildAdvisor.isProjectBuildSkipped( project, session.getContainer().getContext() ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        if ( rpmFile != null )
        {
            getLog().info(  "Using overridden RPM file location: " + rpmFile );
            projectRpmFileManager.setProjectArtifactFile( project, rpmFile );
        }
        else
        {
            String rpmBaseName;

            if ( rpmName != null && rpmName.trim().length() > 0 )
            {
                rpmBaseName = rpmName;
            }
            else
            {
                try
                {
                    rpmBaseName = rpmInfoFormatter.formatRpmName( project, release, platformPostfix, skipPlatformPostfix );
                }
                catch ( RpmFormattingException e )
                {
                    throw new MojoExecutionException( "Failed to format RPM name. Reason: " + e.getMessage(), e );
                }
            }
            
            projectRpmFileManager.formatAndSetProjectArtifactFile( project, topDir, rpmBaseName );
        }
    }

}
