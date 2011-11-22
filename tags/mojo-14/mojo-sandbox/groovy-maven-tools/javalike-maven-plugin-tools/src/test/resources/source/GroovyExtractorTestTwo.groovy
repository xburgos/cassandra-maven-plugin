package source;

import org.apache.maven.plugin.AbstractMojo;

/**
 * Create an IDEA project file from a Maven project.
 * @goal ideaTwo
 * @requiresDependencyResolution compile
 */
public class GroovyExtractorTestTwo
    extends AbstractMojo
{

    /**
     * Maven project used to generate IDEA project files.
     * @parameter
     * @required
     */
    private String[] project;

    public GroovyExtractorTestTwo()
    {        
    }

    public void execute()
    {
        if ( getLog() != null )
        {
            getLog().info( "projects: " + project );
        }
    }
}
