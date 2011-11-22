package org.codehaus.mojo.rpm;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * @plexus.component role="org.codehaus.mojo.rpm.ProjectRpmFileManager" role-hint="default"
 * @author jdcasey
 */
public class ProjectRpmFileManager
    implements LogEnabled
{
    
    /**
     * @plexus.requirement role-hint="default"
     */
    private RpmInfoFormatter rpmInfoFormatter;
    
    // injected.
    private Logger logger;

    public void formatAndSetProjectArtifactFile( MavenSession session, File topDir, String rpmBaseName, boolean skipPlatformPostfix )
        throws MojoExecutionException
    {
        File rpmsDir;
        String myArch;
        try
        {
            myArch = skipPlatformPostfix ?  "noarch" :
                rpmInfoFormatter.formatPlatformArchitecture( session );
            rpmsDir = new File( topDir, "RPMS/" + myArch);

        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot format OS architecture name for RPM directory structure.", e );
        }

        File artifactFile = new File( rpmsDir, rpmBaseName + "." +
            myArch + ".rpm" );

        setProjectArtifactFile( session.getCurrentProject(), artifactFile );
    }

    public void setProjectArtifactFile( MavenProject project, File artifactFile )
    {
        Artifact projectArtifact = project.getArtifact();

        projectArtifact.setFile( artifactFile );

        getLogger().info( "Project artifact set to file: " + artifactFile );
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
    
    protected Logger getLogger()
    {
        return logger;
    }
}
