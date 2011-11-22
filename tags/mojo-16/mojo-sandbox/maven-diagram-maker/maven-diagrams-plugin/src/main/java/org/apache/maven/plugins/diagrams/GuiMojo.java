package org.apache.maven.plugins.diagrams;

import java.awt.Toolkit;
import java.util.List;

import javax.swing.SwingUtilities;

//import org.apache.maven.artifact.factory.ArtifactFactory;
//import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
//import org.apache.maven.artifact.repository.ArtifactRepository;
//import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
//import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
//import org.apache.maven.artifact.resolver.ArtifactCollector;
//import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.diagrams.connector_api.context.GivenMavenConnectorContext;
import org.apache.maven.diagrams.gui.StartGui;
import org.apache.maven.diagrams.gui.controller.MainController;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import sun.swing.SwingUtilities2;

//import org.apache.maven.project.MavenProject;
//import org.apache.maven.project.MavenProjectBuilder;
//import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;

/**
 * <p>
 * Diagrams gui mojo
 * </p>
 * 
 * @goal gui
 * 
 * @author <a href="mailto:ptab@newitech.com">Piotr Tabor</a>
 */
public class GuiMojo extends AbstractMojo
{

    // /**
    // * @component
    // */
    // private ArtifactResolver artifactResolver;
    //
    // /**
    // * @component
    // */
    // private ArtifactFactory artifactFactory;
    //
    // /**
    // * @component
    // */
    // private ArtifactMetadataSource artifactMetadataSource;
    //
    // /**
    // * @component
    // */
    // private ArtifactRepositoryFactory artifactRepositoryFactory;
    //
    // /**
    // * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" roleHint="default"
    // */
    // private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;
    //
    // /**
    // * @parameter expression="${localRepository}"
    // * @required
    // */
    // private ArtifactRepository localRepository;

    /**
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * @parameter expression="${version}" default-value="1.0-SNAPSHOT"
     * @required
     */
    private String version;

    /**
     * @parameter expression="${connector}"
     */
    private String connectorClassName;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;
    
    // /**
    // * @component
    // */
    // private DependencyTreeBuilder dependencyTreeBuilder;
    // /**
    // * @component
    // */
    // private ArtifactCollector collector;
    //
    // /**
    // * @component
    // */
    // private MavenProjectBuilder mavenProjectBuilder;

    public void execute() throws MojoExecutionException
    {
        GivenMavenConnectorContext givenMavenConnectorContext=new GivenMavenConnectorContext( project );
        givenMavenConnectorContext.setLogger(new PluginLoggerToDiagramsLoggerAdapter(getLog()));
        MainController controller = new MainController(givenMavenConnectorContext);
        controller.run();
        controller.getView().waitUntilClosed();
    }
}
