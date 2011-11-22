package org.codehaus.mojo.rspec;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;

public abstract class AbstractScriptFactory implements ScriptFactory {

	protected List<String> classpathElements;
	protected File outputDir;
	protected String baseDir;
	protected String sourceDir;
	protected String reportPath;
	protected Properties systemProperties;

	public void setClasspathElements(List<String> classpathElements) {
		this.classpathElements = classpathElements;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}
	
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}
	
	public void setSystemProperties(Properties systemProperties) {
		this.systemProperties = systemProperties;
	}
	
	protected abstract String getScriptName();
	
	public void emit() throws Exception {
		String script = getScript();
		
		File scriptFile = new File( outputDir, getScriptName() );
		
		FileWriter out = new FileWriter( scriptFile );
		
		try {
			out.write( script );
		} finally {
			if ( out != null ) {
				out.close();
			}
		}
		
		scriptFile.setExecutable(true);
	}

}
