package org.codehaus.mojo.rpm;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
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

    public void formatAndSetProjectArtifactFile( MavenProject project, File topDir, String rpmBaseName )
        throws MojoExecutionException
    {
        File rpmsDir;
        try
        {
            rpmsDir = new File( topDir, "RPMS/" + rpmInfoFormatter.formatPlatformArchitecture() );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot format OS architecture name for RPM directory structure.", e );
        }

        File artifactFile;
        try
        {
            artifactFile =
                new File( rpmsDir, rpmBaseName + "." + rpmInfoFormatter.formatPlatformArchitecture() + ".rpm" );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot read OS architecture from rpm command.", e );
        }

        setProjectArtifactFile( project, artifactFile );
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
