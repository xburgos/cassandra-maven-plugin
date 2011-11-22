package org.apache.maven.plugin.deb;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @description A Maven 2 mojo which creates a Debian package from a Maven2 project.
 *
 * @goal attach-deb
 * @phase package
 * @requiresProject
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AttachDeb
    extends AbstractDebMojo
{
    /**
     * @parameter
     * @required
     */
    private File assemblyDirectory;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            File debFileName = new File( outputDirectory, getControlFileGenerator().getDebFileName() );

            projectHelper.attachArtifact( project, "deb", "deb", debFileName );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error while attaching artifact.", e );
        }
    }
}
