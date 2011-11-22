package source;

import org.apache.maven.plugin.AbstractMojo;

/**
 * Create an IDEA project file from a Maven project.
 *
 * @goal ideaOne
 *
 * @requiresDependencyResolution
 *
 */
public class GroovyExtractorTestOne
    extends AbstractMojo
{
    /**
     * Maven project used to generate IDEA project files.
     * @parameter
     * @required
     */
    protected String[] project;

    public GroovyExtractorTestOne()
    {
    }

    public void execute()
    {
    }
}
