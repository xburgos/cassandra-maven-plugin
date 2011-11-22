/*
 * Copyright 2006 Tim O'Brien.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.sysdeo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.mojo.sysdeo.ide.AbstractIdeSupportMojo;
import org.codehaus.mojo.sysdeo.ide.IdeDependency;
import org.codehaus.mojo.sysdeo.ide.IdeUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @goal generate
 * @execute phase="generate-resources"
 */
public class SysdeoMojo extends AbstractIdeSupportMojo {

	private static String TOMCAT_PLUGIN = ".tomcatplugin";
	
    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    private File warSourceDirectory;
    
    /**
     * Application context definition for Tomcat.
     *
     * @parameter expression="${basedir}/src/main/webapp/META-INF/context.xml"
     * @required
     */
    private File contextDefinition;    
    
    private List ignoreArtifact;
    
    public SysdeoMojo() {
    	ignoreArtifact = new ArrayList();
    	ignoreArtifact.add( "jsp-api" );
    	ignoreArtifact.add( "servlet-api" );
    }
    
	protected boolean getUseProjectReferences() {
		return true;
	}

	protected boolean setup() throws MojoExecutionException {
		return true;
	}

	protected void writeConfiguration(IdeDependency[] dependencies) throws MojoExecutionException {

		if( !getProject().getPackaging().equals("war") ) {
			getLog().info( "Not executing sysdeo-tomcat plugin, this is project is not a war package" );
			return;
		}
		
		File projectDir = getProject().getBasedir();
		List referencedProjects = new ArrayList();
		List jarDependencies = new ArrayList();
		
		IdeDependency thisDependency = new IdeDependency();
		thisDependency.setArtifactId( getProject().getArtifactId() );
		referencedProjects.add( thisDependency );
		
		for( int i = 0; i < dependencies.length; i++ ) {
            IdeDependency dependency = dependencies[i];
			if( dependency.isProvided() || dependency.isTestDependency() ) {
				// Skip this dependency
				continue;
			}
			if( dependency.isReferencedProject() ) {
				referencedProjects.add( dependency );
			} else if( dependency.getType().equalsIgnoreCase("jar") && !ignoreArtifact.contains( dependency.getArtifactId() ) ) {
				jarDependencies.add( dependency );
			}
		}
		
		String extraContext = "";
		try {
			String contextString = FileUtils.readFileToString( contextDefinition, "UTF-8" );
			int context = contextString.indexOf( "Context");
			int start = contextString.indexOf(">", context);
			int stop = contextString.indexOf("</Context>");
			extraContext = contextString.substring(start + 1, stop );
			extraContext = URLEncoder.encode( extraContext );
		} catch( Exception e ) {
			getLog().info( "No valid context file found at: " + contextDefinition );
		}
		
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading( SysdeoMojo.class, "");
		
		Map context = new HashMap();
		context.put( "referencedProjects", referencedProjects );
		context.put( "jarDependencies", jarDependencies );
		context.put( "finalName", getProject().getBuild().getFinalName());
		context.put( "warSourceDir", IdeUtils.toRelativeAndFixSeparator( getProject().getBasedir(), getWarSourceDirectory(), false ) );
		context.put( "extraContext", extraContext);
		
		File tomcatPluginFile = new File( projectDir, TOMCAT_PLUGIN );
		try {
			Writer configWriter = new FileWriter( tomcatPluginFile );
			Template template = cfg.getTemplate( "tomcatplugin.fm" );
			template.process(context, configWriter);
			configWriter.flush();
			configWriter.close();
			getLog().info("Write tomcat plugin file to: " + tomcatPluginFile.getAbsolutePath() );
		} catch( IOException ioe ) {
			throw new MojoExecutionException( "Unable to write tomcat plugin config file", ioe );
		} catch( TemplateException te ) {
			throw new MojoExecutionException( "Unable to merge freemarker template", te );
		}
	}

	public File getContextDefinition() {
		return contextDefinition;
	}

	public void setContextDefinition(File contextDefinition) {
		this.contextDefinition = contextDefinition;
	}

	public File getWarSourceDirectory() {
		return warSourceDirectory;
	}

	public void setWarSourceDirectory(File warSourceDirectory) {
		this.warSourceDirectory = warSourceDirectory;
	}
}
