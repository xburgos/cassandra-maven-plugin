package org.codehaus.mojo.jboss;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Delete file form <code>$JBOSS_HOME/server/[serverName]/deploy</code>
 * directory.
 * 
 * @author <a href="mailto:bjkuczynski@gmial.com">Bartek 'Koziolek' Kuczynski</a>
 * @goal hardundeploy
 */
public class HardUnDeployMojo extends AbstractJBossMojo {
	/**
	 * The name of the file or directory to undeploy.
	 * 
	 * @parameter expression=
	 *            "${project.build.directory}/${project.build.finalName}.${project.packaging}"
	 * @required
	 */
	protected String fileName;

	public void execute() throws MojoExecutionException, MojoFailureException {
		checkConfig();
		
		File tmp = new File(fileName); 
		
		File earFile = new File(jbossHome + "/server/" + serverName + "/deploy/" + tmp.getName());
		getLog().info("Undeploy file: " + earFile.getName());
		if(!earFile.exists()){
			getLog().info("File " + earFile.getAbsolutePath()+ " doesn't exist!");
			return;
		}
		if(earFile.delete())
			getLog().info("File " + earFile.getName() + " undeployed!\nhave a nice day!");
	}
}
