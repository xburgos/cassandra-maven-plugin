package org.codehaus.mojo.rspec;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;

/**
 * Mojo to run Ruby Spec test
 * 
 * @author Michael Ward
 * @author Mauro Talevi
 * @goal spec
 */
public class RspecRunnerMojo extends AbstractMojo {

    /**
     * The classpath elements of the project being tested.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> classpathElements;

    /**
     * The directory containing the RSpec source files
     * 
     * @parameter
     * @required
     */
    protected String sourceDirectory;

    /**
     * The directory where the RSpec report will be written to
     * 
     * @parameter
     * @required
     */
    protected String outputDirectory;

    /**
     * The name of the RSpec report (optional, defaults to "rspec_report.html")
     * 
     * @parameter expression="rspec_report.html"
     */
    protected String reportName;

    /**
     * The directory where JRuby is installed (optional, defaults to
     * "${user.home}/.jruby")
     * 
     * @parameter expression="${user.home}/.jruby"
     */
    protected String jrubyHome;

    /**
     * The flag to ignore failures (optional, defaults to "false")
     * 
     * @parameter expression="false"
     */
    protected boolean ignoreFailure;
    
    /**
     * The flag to skip tests (optional, defaults to "false")
     * 
     * @parameter expression="false"
     */
    protected boolean skipTests;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ( skipTests ){
            getLog().info("Skipping RSpec tests");
            return;            
        }
        getLog().info("Running RSpec tests from " + sourceDirectory);

        if (jrubyHome == null) {
            throw new MojoExecutionException("jrubyHome directory not specified");
        }
        Ruby runtime = Ruby.newInstance();
        runtime.setJRubyHome(jrubyHome);
        runtime.getLoadService().init(classpathElements);

        // Build ruby script to run RSpec's
        StringBuilder script = new StringBuilder();
        try {
            script.append(handleClasspathElements(runtime));
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        // Run all specs
        String reportPath = outputDirectory + "/" + reportName;
        
        script.append("require 'rubygems'\n")
              .append("require 'spec'\n")
              .append("spec_dir = '").append(sourceDirectory).append("'\n")
              .append("report_file = '").append(reportPath).append("'\n")
              .append("options = ::Spec::Runner::OptionParser.parse([spec_dir, '-f', \"html#{report_file}\"], STDERR, STDOUT)\n")
              .append("::Spec::Runner::CommandLine.run(options)\n");

        script.append("if File.new(report_file, 'r').read =~ /, 0 failures/ \n")
              .append(" false\n")
              .append("else\n")
              .append(" true\n")
              .append("end");

        RubyBoolean failure = (RubyBoolean) runtime.evalScriptlet(script.toString());

        if (failure.isTrue()) {
            String message = "RSpec tests failed. See '"+reportPath+"' for details.";
            getLog().warn(message);
            if ( !ignoreFailure ){
                throw new MojoFailureException(message);
            }
        } else {
            getLog().info("RSpec tests successful. See '"+reportPath+"' for details.");
        }
    }

    private String handleClasspathElements(Ruby runtime) throws MalformedURLException {
        StringBuilder script = new StringBuilder();
        for (String path : classpathElements) {
            if (path.endsWith(".jar")) {
                // handling jar files
                script.append("require '").append(path).append("'\n");
            } else {
                // handling directories
                runtime.getJRubyClassLoader().addURL(new URL("file:" + path + "/"));
            }
        }
        return script.toString();
    }

}
