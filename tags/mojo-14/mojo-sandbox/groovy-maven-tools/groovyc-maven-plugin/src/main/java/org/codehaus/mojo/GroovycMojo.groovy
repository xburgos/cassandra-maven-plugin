package org.codehaus.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.List;
import java.lang.String;

/**
 * A plugin that reuse the groovyc Ant task to compile .groovy files
 *
 * @goal compile
 * @phase compile
 * @description Compile the project's .groovy files
 * @author <a href="mailto:eburghard@free.fr">Éric BURGHARD</a>
 * @todo add .groovy support to plexus compiler (compile phase) instead of using a separate goal with a plugin
 * @version $Id$
 */
public class GroovycMojo extends AbstractMojo {

	/**
     * Reference to the maven project
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    /**
     * Source directories
     *
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     */
    private List srcDirs;
    
    /**
     * Build directory
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private String buildDir;
    
    public void execute() {
		def ant = new AntBuilder()
		ant.taskdef(name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc")

		ant.mkdir(dir: buildDir)

		for (srcDir in srcDirs) {
			ant.groovyc(srcdir: srcDir, destdir: buildDir)
		}
	}
}