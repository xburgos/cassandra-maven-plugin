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

import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
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
     */
    private boolean useSudo;

    /**
     * @parameter expression="${rpmDbPath}"
     */
    private String rpmDbPath;

    /**
     * @parameter expression="${rpm.force.all.installs}" default-value="false"
     */
    private boolean forceAllInstalls;
    
    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * MavenSession instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * The build number of the RPM, so you get versions like 1.2-4 which
     * would be the fourth build of the 1.2 tarball.
     *
     * @parameter expression="${release}" default-value="1"
     * @required
     */
    private String release;

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
        install(project, null, force);
    }

    protected final void install( MavenProject project, String release, boolean force )
        throws RpmFormattingException, RpmInstallException, MojoExecutionException
    {
        rpmMediator.setUseSudo( useSudo );
        
        Artifact artifact = project.getArtifact();

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

        String rpmName = rpmInfoFormatter.formatRpmNameWithoutVersion( session, project );

        try
        {
            if ( force || forceAllInstalls || !rpmMediator.isRpmInstalled( rpmName,
                 project.getVersion(), release, dbPath ) )
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
