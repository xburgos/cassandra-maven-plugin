package org.apache.maven.plugins.diagrams;

import java.util.Map;

import org.apache.maven.diagrams.connector_api.DiagramConnector;
import org.apache.maven.diagrams.gui.controller.DiagramGuiException;
import org.apache.maven.diagrams.gui.controller.MainController;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * <p>
 * Diagrams gui mojo
 * </p>
 * 
 * @goal gui
 * 
 * @author <a href="mailto:ptab@newitech.com">Piotr Tabor</a>
 */
public class GuiMojo extends AbstractMojo {
	/**
	 * @parameter expression="${connector}" default-value="connector-classes"
	 */
	private String connectorName;

	/**
	 * @parameter expression="${project}"
	 */
	private MavenProject project;

	/**
	 * @component role="org.apache.maven.diagrams.connector_api.DiagramConnector"
	 */
	private Map/* <String, DiagramConnector> */connectors;

	public void execute() throws MojoExecutionException {
		DiagramConnector connectorClass = (DiagramConnector) connectors
				.get(connectorName);

		if (connectorClass != null) {

			MainController controller;
			try {
				connectorClass.setMavenProject(project);
				controller = new MainController(connectorClass);
			} catch (DiagramGuiException e) {
				throw new MojoExecutionException("Cannot initiate gui: ", e);
			}
			controller.run();
			controller.getView().waitUntilClosed();
		} else
			throw new MojoExecutionException("Connector: '" + connectorName
					+ "' has not been found");
	}
}
