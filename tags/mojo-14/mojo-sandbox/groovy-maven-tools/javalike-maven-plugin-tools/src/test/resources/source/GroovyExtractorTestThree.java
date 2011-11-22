package source;

import org.apache.maven.plugin.AbstractMojo;

/**
 * Create an IDEA project file from a Maven project.
 *
 * @goal ideaThree
 * @requiresDependencyResolution compile
 *
 */
public class GroovyExtractorTestThree
    extends AbstractMojo
{
    /**
     * Maven project used to generate IDEA project files.
     * @parameter
     * @required
     */
    protected String[] project;

    public GroovyExtractorTestThree()
    {
    }

    public void execute()
    {
    }
}
