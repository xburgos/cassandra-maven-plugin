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
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.optional.Rpm;
import org.apache.tools.ant.types.FileSet;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;

/**
 * Harvest a RPM from the project binaries and the generated spec file.
 * 
 * @goal build
 * @phase package
 */
public class BuildRpmMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="false" alias="rpm.build.skip"
     */
    private boolean skipBuild;

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
     * MavenProject instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Top directory of the RPM filesystem structure.
     * 
     * @parameter default-value="${project.build.directory}/rpm-topdir"
     * @required
     */
    private File topDir;

    /**
     * The Ant messageLevel to use.
     * 
     * @parameter expression="${messageLevel}" default-value="info"
     */
    private String messageLevel;

    /**
     * Directory, inside the RPM filesystem structure, where the RPM will be written. 
     */
    private File rpmsDir;

    /**
     * The RPM releaes level or build number
     * 
     * @parameter expression="${release}" default-value="1"
     */
    private String release;

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component
     */
    private ProjectRpmFileManager projectRpmFileManager;

    /**
     * Build the RPM filesystem structure, setup the Rpm Ant task, and execute. Then, set the File for the
     * project's Artifact instance to the generated RPM for use in the install and deploy phases.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipBuild )
        {
            getLog().info( "Skipping RPM build (per configuration)." );
            return;
        }

        if ( BuildAdvisor.isProjectBuildSkipped( project, session.getContainer().getContext() ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
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

        File tmpPath = new File( topDir, "tmp" );

        try
        {
            buildRpmDirectoryStructure();
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot read OS architecture from rpm command.", e );
        }

        Rpm rpm = new Rpm();

        rpm.setTaskName( "rpm" );
        rpm.setSpecFile( rpmBaseName + ".spec" );
        rpm.setCommand( "-bb --define \"_tmppath " + tmpPath.getAbsolutePath() + "\"" );
        rpm.setFailOnError( true );
        rpm.setTopDir( topDir );

        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );

        if ( messageLevel != null )
        {
            antCaller.setMessageLevel( messageLevel );
        }

        antCaller.addTask( rpm );
        
        Chmod chmod = new Chmod();
        chmod.setTaskName( "chmod" );
        chmod.setPerm( "g+w" );
        chmod.setFailonerror( true );
        
        FileSet fs = new FileSet();
        fs.setDir( topDir );
        fs.setIncludes( "**/*" );
        
        chmod.addFileset( fs );
        
        antCaller.addTask( chmod );

        try
        {
            antCaller.executeTasks( project );
        }
        catch ( AntExecutionException e )
        {
            Throwable cause = e.getCause();

            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error building RPM", cause );

                throw new MojoExecutionException( "Failed to build RPM." );
            }
            else
            {
                throw new MojoExecutionException( "Failed to build RPM.", e );
            }
        }

        if ( RpmInfoFormatter.getUseRpmFinalName( project ) )
        {
            rpmBaseName = rpmBaseName + "-" + project.getVersion() + "-" + release;
        }
        
        getLog().debug( "Just before setting the final RPM project-artifact, release is: " + release + "; rpmBaseName is: " + rpmBaseName );
        
        projectRpmFileManager.formatAndSetProjectArtifactFile( project, topDir, rpmBaseName );
        
        File projectFile = project.getArtifact().getFile();
        
        // if this doesn't exist, then we either had a problem building the RPM, or we're pointing
        // at the wrong location. Either way, it's time to take a time-out and look around.
        if ( !projectFile.exists() )
        {
            throw new MojoExecutionException( "RPM file: " + projectFile + " does not exist." );
        }
    }

    /**
     * Ensure that the RPM filesystem structure exists below the RPM top-directory.
     * @throws RpmFormattingException 
     */
    private void buildRpmDirectoryStructure()
        throws RpmFormattingException
    {
        topDir.mkdirs();

        // my @subdirs = ('BUILD', File::Spec->catfile('RPMS', SystemProperty->getArchitecture()), 'SOURCES', 
        // 'SPECS', 'SRPMS');
        File build = new File( topDir, "BUILD" );
        build.mkdirs();

        rpmsDir = new File( topDir, "RPMS/" + rpmInfoFormatter.formatPlatformArchitecture() );

        getLog().info( "RPMS Directory: \'" + rpmsDir.getAbsolutePath() + "\'" );

        rpmsDir.mkdirs();

        File sources = new File( topDir, "SOURCES" );
        sources.mkdirs();

        File specs = new File( topDir, "SPECS" );
        specs.mkdirs();

        File srpms = new File( topDir, "SRPMS" );
        srpms.mkdirs();
    }

}
