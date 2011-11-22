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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.rpm.RpmConstants;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.mojo.tools.rpm.RpmInstallException;
import org.codehaus.mojo.tools.rpm.RpmMediator;

/**
 * Used to install the RPM onto the OS. This is critical for multimodule
 * builds, since dependent compiles need RPMs installed.
 * 
 * @author jdcasey
 * 
 * @goal remove
 * @phase clean
 * @aggregator
 */
public class RemoveRpmMojo
    extends AbstractMojo
{
    
    /**
     * Flag to determine when to use sudo to execute the rpm command.
     * 
     * @parameter default-value="true" expression="${rpm.install.useSudo}"
     */
    private boolean useSudo;

    /**
     * @parameter default-value="false" alias="rpm.remove.skip"
     */
    private boolean skipRemove;

    /**
     * MavenProject instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     * 
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List < MavenProject > projects;
    
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    private static boolean completed = false;
    
    /**
     * @parameter expression="${rpmDbPath}"
     */
    private String rpmDbPath;

    /**
     * @component role-hint="default"
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component role-hint="default"
     */
    private RpmMediator rpmMediator;

    /**
     * Build the RPM filesystem structure, setup the Rpm Ant task, and execute. Then, set the File for the
     * project's Artifact instance to the generated RPM for use in the install and deploy phases.
     * @throws MojoFailureException 
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        rpmMediator.setUseSudo( useSudo );
        
        if ( skipRemove )
        {
            getLog().info( "Skipping RPM removal (per configuration)." );
            return;
        }
        
        if ( completed )
        {
            getLog().info(  "RPM erase has completed. Skipping this invocation." );
            return;
        }
        
        if ( projects != null && !projects.isEmpty() )
        {
            getLog().info( "Removing " + projects.size() + " project RPMs." );
            
            List < MavenProject > projectsInReverseOrder = new ArrayList < MavenProject > ( projects );
            Collections.reverse( projectsInReverseOrder );
            for ( Iterator < MavenProject > it = projectsInReverseOrder.iterator(); it.hasNext(); )
            {
                MavenProject tmpProject = it.next();

                if ( RpmConstants.RPM_PACKAGINGS.contains( tmpProject.getPackaging() ) )
                {
                    removeRPM( tmpProject );
                }
            }
        }        
        else
        {
            getLog().info( "Removing single project RPM." );
            
            removeRPM( project );
        }
        
        completed = true;
    }

    private void removeRPM( MavenProject project ) throws MojoExecutionException
    {
        getLog().info( "Removing: " + project.getId() );
        
        String rpmBaseName;
        try
        {
            rpmBaseName = rpmInfoFormatter.formatRpmNameWithoutVersion( project );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Failed to format RPM name. Reason: " + e.getMessage(), e );
        }

        try
        {
            rpmMediator.remove( rpmBaseName, rpmDbPath );
        }
        catch ( RpmInstallException e )
        {
            throw new MojoExecutionException( "Failed to install project RPM for: " + project, e );
        }
    }

}
