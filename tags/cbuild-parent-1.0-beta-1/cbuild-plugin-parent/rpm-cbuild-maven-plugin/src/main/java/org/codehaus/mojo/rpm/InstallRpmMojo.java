package org.codehaus.mojo.rpm;

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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
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
     * Use this flag to skip the install phase of an RPM.  Useful if you want to create
     * an RPM but not install it on your build machine.
     * 
     * @parameter default-value="false" alias="rpm.install.skip"
     * @since 1.0-alpha-1
     */
    private boolean skipInstall;

    /**
     * This will attempt to override an error which will usually abort an RPM install
     * 
     * @parameter expression="${forceInstall}" default-value="false"
     * @since 1.0-alpha-2
     */
    private boolean forceInstall;

    /**
     * The RPM version, typically set in a Dynamic Maven Property in the platform-detect
     * goal during the validate phase.
     * 
     * @parameter expression="${DYNAMIC.CBUILDPROP.RPM.VERSION}"
     * @since 1.0-beta-1
     */
    private String rpmVersion;

    /**
     * The build number of the RPM, so you get versions like 1.2-4 which
     * would be the fourth build of the 1.2 tarball.
     *
     * @parameter expression="${DYNAMIC.CBUILDPROP.RPM.RELEASE}"
     * @required
     * @since 1.0-alpha-2
     */
    private String release;

    /**
     * MavenProject used to furnish information required to construct the RPM name.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0-beta-1
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

        // RPM is an attatched artifact as of CBUILDS 1.0-beta-1
        File rpmFile = null;
        List < Artifact > attachedList = project.getAttachedArtifacts();
        for ( Iterator < Artifact > it = attachedList.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();
            getLog().debug( "rpm install: Found Artifact " + artifact.getId() + " type " + artifact.getType() );
            if ( artifact.getType() == "rpm" )
            {
                // if two RPM artifacts are attach, this is an obvious bug
                rpmFile = artifact.getFile();
            }
        }
        
        if ( rpmFile == null )
        {
            throw new MojoFailureException( this, "RPM file does not exist.",
                "RPM file has not been set on project artifact for: " + project.getId() );
        }
        else if ( !rpmFile.exists() )
        {
            throw new MojoFailureException( this, "RPM file does not exist.",
                "Cannot install missing RPM: " + rpmFile );
        }
        
        try
        {
            install( project, rpmVersion, release, forceInstall );
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
