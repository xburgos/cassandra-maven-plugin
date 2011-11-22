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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.rpm.RpmConstants;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.mojo.tools.rpm.RpmInstallException;
import org.codehaus.mojo.tools.rpm.RpmMediator;
import org.codehaus.mojo.tools.rpm.RpmQueryException;

public abstract class AbstractRpmInstallMojo
    extends AbstractMojo
{

    /**
     * Flag to determine when to use sudo to execute the rpm command.
     * 
     * @parameter default-value="true" expression="${rpm.install.useSudo}"
     * @since 1.0-alpha-1
     */
    private boolean useSudo;

    /**
     * Use this if your RPM database is not installed in the typical location, usually
     * /var/lib/rpm
     * 
     * @parameter expression="${rpmDbPath}"
     * @since 1.0-alpha-2
     */
    private String rpmDbPath;

    /**
     * @parameter expression="${rpm.force.all.installs}" default-value="false"
     */
    private boolean forceAllInstalls;
    
    /**
     * RPM 4.4.6 introduced a feature that the parent directories of a package must be
     * provided by another package as a dependency.  This usually isn't what you want so set
     * this to false if you are getting installation failures on a directory dependency.  You
     * can check your system for orphanced dependencies with <code>rpm -Va --orphandirs</code>.
     * 
     * @parameter  default-value="false"
     * @since 1.0-beta-1
     */
    private boolean noParentDirs;

    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component role-hint="default"
     */
    private RpmMediator rpmMediator;

    protected AbstractRpmInstallMojo()
    {
    }

    protected final void install( MavenProject project, boolean force )
        throws RpmFormattingException, RpmInstallException, MojoExecutionException
    {
        install( project, null, null, force );
    }

    protected final void install( MavenProject project, String rpmVersion, 
        String release, boolean force )
        throws RpmFormattingException, RpmInstallException, MojoExecutionException
    {
        rpmMediator.setUseSudo( useSudo );
        rpmMediator.setNoParentDirs( noParentDirs );
        
        // RPM is an attatched artifact as of CBUILDS 1.0-beta-1
        Artifact artifact = null;
        List < Artifact > attachedList = project.getAttachedArtifacts();
        for ( Iterator < Artifact > it = attachedList.iterator(); it.hasNext(); )
        {
            Artifact artifactTemp = it.next();
            if ( artifactTemp.getType() == "rpm" )
            {
                // if two RPM artifacts are attach, this is an obvious bug
                artifact = artifactTemp;
            }
        }
        
        Properties properties = project.getProperties();

        String dbPath = rpmDbPath;

        if ( dbPath == null && properties != null )
        {
            dbPath = properties.getProperty( RpmConstants.RPM_DB_PATH );

            getLog().info( "Using RPM database path from POM: \'" + dbPath + "\'." );
        }
        else
        {
            getLog().info( "Using RPM database path from plugin parameter: \'" + dbPath + "\'." );
        }

        String rpmName = rpmInfoFormatter.formatRpmNameWithoutVersion( project );

        try
        {
            if ( force || forceAllInstalls || !rpmMediator.isRpmInstalled( rpmName,
                 rpmVersion, release, dbPath ) )
            {
                rpmMediator.install( rpmName, artifact.getFile(), dbPath, force || forceAllInstalls );
            }
        }
        catch ( RpmQueryException e )
        {
            throw new MojoExecutionException( "Error querying for the existence of: " + rpmName + " in RPM database: "
                + dbPath );
        }

    }

}
