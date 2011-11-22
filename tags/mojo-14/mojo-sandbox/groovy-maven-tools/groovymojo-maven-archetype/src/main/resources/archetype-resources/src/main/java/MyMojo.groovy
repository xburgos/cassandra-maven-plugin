package $package;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A plugin that achieve the 'test' goal.
 *
 * @goal test
 * @description Test native groovy Mojo
 * @author <a href="mailto:eburghard@free.fr">Éric BURGHARD</a>
 * @version $Id$
 */
public class MyMojo extends AbstractMojo {

	/**
     * Reference to the maven project
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    /**
     * A test value given to the script at runtime.
     *
     * @parameter expression="${test}" default-value="test is successfull !"
     * @required
     */
    private String test;

    public void execute() {
        getLog().info("plugin groupId    : " + project.groupId)
        getLog().info("plugin artifactId : " + project.artifactId)
        getLog().info(test)
	}
}