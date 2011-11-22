package org.codehaus.mojo.resolver.bod.binary;

import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.tools.rpm.RpmConstants;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.mojo.tools.rpm.RpmInstallException;
import org.codehaus.mojo.tools.rpm.RpmMediator;
import org.codehaus.mojo.tools.rpm.RpmQueryException;

/**
 * @plexus.component role="org.codehaus.mojo.resolver.bod.binary.BinaryDependencyManager" role-hint="rpm"
 * @author jdcasey
 */
public class RpmBinaryDependencyManager
    extends AbstractOSBinaryDependencyManager
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RpmMediator rpmMediator;

    /**
     * @plexus.configuration default-value=""
     */
    private String rpmDbPath;

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
     * @plexus.configuration default-value="true"
     * 
     * @todo Should we change this to false?
     */
    private boolean forceAllInstalls = true;

    protected boolean installDependencyOnSystem( MavenProject project )
        throws BuildOnDemandResolutionException
    {
        String dbPath = getDbPath( project );

        String rpmName;
        try
        {
            rpmName = rpmInfoFormatter.formatRpmNameWithoutVersion( session, project );
        }
        catch ( RpmFormattingException e )
        {
            throw new BuildOnDemandResolutionException( "Failed to format RPM name from project: " + project.getId(), e );
        }

        try
        {
            if ( forceAllInstalls || !rpmMediator.isRpmInstalled( rpmName, project.getVersion(), dbPath ) )
            {
                Artifact artifact = project.getArtifact();

                rpmMediator.install( rpmName, artifact.getFile(), dbPath, forceAllInstalls );
            }
        }
        catch ( RpmQueryException e )
        {
            throw new BuildOnDemandResolutionException( "Error querying for the existence of: " + rpmName
                            + " in RPM database: " + dbPath, e );
        }
        catch ( RpmInstallException e )
        {
            throw new BuildOnDemandResolutionException( "Failed to install RPM dependency: " + rpmName
                            + " for project: " + project.getId() + " in RPM database: " + dbPath, e );
        }

        return true;
    }

    private String getDbPath( MavenProject project )
    {
        Properties properties = project.getProperties();

        String dbPath = rpmDbPath;

        if ( dbPath == "" && properties != null )
        {
            dbPath = properties.getProperty( RpmConstants.RPM_DB_PATH );

            getLogger().debug( "Using RPM database path from POM: \'" + dbPath + "\'." );
        }
        else
        {
            getLogger().debug( "Using RPM database path from plugin parameter: \'" + dbPath + "\'." );
        }

        return dbPath;
    }

    protected boolean isDependencyInstalledOnSystem( MavenProject project )
        throws BuildOnDemandResolutionException
    {
        String dbPath = getDbPath( project );

        String rpmName;
        try
        {
            rpmName = rpmInfoFormatter.formatRpmNameWithoutVersion( session, project );
        }
        catch ( RpmFormattingException e )
        {
            throw new BuildOnDemandResolutionException( "Failed to format RPM name from project: " + project.getId(), e );
        }

        // TODO: Incorporate version somehow here.
        try
        {
            return rpmMediator.isRpmInstalled( rpmName, dbPath );
        }
        catch ( RpmQueryException e )
        {
            throw new BuildOnDemandResolutionException( "Failed to query RPM database: " + dbPath + " for: " + rpmName,
                                                        e );
        }
    }

}
