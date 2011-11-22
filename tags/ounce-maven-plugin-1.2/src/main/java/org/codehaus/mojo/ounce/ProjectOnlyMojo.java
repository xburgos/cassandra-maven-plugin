/*
 * Copyright (c) 2007, Ounce Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY OUNCE LABS, INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OUNCE LABS, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.codehaus.mojo.ounce;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * This mojo generates an Ounce project file. It does not fork the build like the "project" mojo and is instead intended
 * to be bound in a pom for automatic execution. If you would rather have the project generated on demand via the
 * command line, use the project goal instead.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @requiresDependencyResolution test
 * @goal project-only
 * @phase package
 */
public class ProjectOnlyMojo
    extends AbstractOunceMojo
{

    public static final String PATH_SEPARATOR = ";";

    public static final String M2_REPO = "M2_REPO";

    /**
     * The scope of the classpath used to analyze this project. <br/> Valid choices are: compile, test, runtime, or
     * system. If includeTestSources is true, then the classpathScope reverts to test. Otherwise, the default is
     * compile.
     * 
     * @parameter default-value="compile" expression="${ounce.classpathScope}";
     */
    private String classpathScope;

    /**
     * JDK configuration known to Ounce Core.
     * 
     * @parameter expression="${ounce.jdkName}"
     */
    private String jdkName;
    
    /**
     * Options to pass to the javac compiler.
     * 
     * @parameter expression="${ounce.javaCompilerOptions}"
     */
    private String javaCompilerOptions;

    /**
     * If TestSources should be included in the compilable sources. If set, adds project.getTestSourceRoot() to the path
     * and defaults the classpathScope to test.
     * 
     * @parameter expression="${ounce.includeTestSources}" default-value="false"
     */
    protected boolean includeTestSources;

    /**
     * Whether the plugin should use the Ounce Automation Server to create any necessary variables (such as M2_REPO).
     * Requires that the Ounce Automation Server be installed.
     * 
     * @parameter expression="${ounce.createVariables}" default-value="true"
     */
    protected boolean createVariables;

    /**
     * The location of the Ounce client installation directory. Required if ounceauto is not on the path.
     * 
     * @parameter expression="${ounce.installDir}"
     */
    String installDir;

    /**
     * The location of the web context root, if needed.
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private String webappDirectory;
    
    /**
     * Whether to analyze the framework for a Struts application
     * 
     * @parameter expression="${ounce.analyzeStrutsFramework}" default-value="false"
     */
    private boolean analyzeStrutsFramework;
    
    /**
     * Whether to import Struts validation routines
     * 
     * @parameter expression="${ounce.importStrutsValidation}" default-value="false"
     */
    private boolean importStrutsValidation;

    /**
     * Location of the local repository.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( project.getPackaging() != "pom" || !skipPoms )
        {
            try
            {
            	
                String classPath = buildClasspath();
                List sourceRoots = getSourceRoots();

                String projectRoot = getProjectRoot();

                // make all paths relative

                webappDirectory = Utils.convertToRelativePath( webappDirectory, projectRoot, "" );

                // remove project root from the
                // classpath
                classPath = Utils.convertToRelativePath( classPath, projectRoot, "" );

                // remove repo from the classpath
                classPath = Utils.convertToRelativePath( classPath, local.getBasedir(), ProjectOnlyMojo.M2_REPO );

                classPath = Utils.convertToVariablePath( classPath, pathVariableMap );

                sourceRoots = Utils.convertToRelativePaths( sourceRoots, projectRoot, "" );

                projectRoot = ".";

                OunceCore core = getCore();

                core.createProject( getProjectRoot(), name, projectRoot, sourceRoots, webappDirectory, classPath,
                                    jdkName, javaCompilerOptions, project.getPackaging(), this.options, 
                                    analyzeStrutsFramework, importStrutsValidation, this.getLog() );

                if ( createVariables )
                {
                    if ( pathVariableMap == null )
                    {
                        pathVariableMap = new HashMap();
                    }
                    if ( pathVariableMap.get( ProjectOnlyMojo.M2_REPO ) == null )
                    {
                        pathVariableMap.put( ProjectOnlyMojo.M2_REPO, local.getBasedir() );
                    }
                    core.createPathVariables( pathVariableMap, installDir, this.getLog() );
                }
            }
            catch ( ComponentLookupException e )
            {
                throw new MojoExecutionException( "Unable to lookup the core interface for hint: " + coreHint, e );
            }
            catch ( OunceCoreException e )
            {
                throw new MojoExecutionException( "Nested Ouncecore exception: " + e.getLocalizedMessage(), e );
            }
        }
        else
        {
            getLog().info( "Skipping Pom project." );
        }
    }

    /**
     * This method gets the source roots from the project. Overrides the ProjectOnly:getSourceRoots() method because we
     * don't have an executed project because the build wasn't forked.
     * 
     * @return List of source roots.
     */
    protected List getSourceRoots()
    {
        List sourceRoots = project.getCompileSourceRoots();

        if ( this.includeTestSources )
        {
            sourceRoots.addAll( project.getTestCompileSourceRoots() );
        }
        return sourceRoots;
    }

    /**
     * Gets the classpath elements and returns a properly formatted classpath.
     * 
     * @return
     * @throws MojoExecutionException
     */
    protected String buildClasspath()
        throws MojoExecutionException
    {

        List classpathElements = getClasspathElements();

        StringBuffer sb = new StringBuffer();
        Iterator i = classpathElements.iterator();

        if ( i.hasNext() )
        {
            // first one, no separator needed
            sb.append( Utils.convertToUnixStylePath( (String) i.next() ) );

            // separate the rest of them with pathSeparator
            while ( i.hasNext() )
            {
                sb.append( ProjectOnlyMojo.PATH_SEPARATOR );
                sb.append( Utils.convertToUnixStylePath( (String) i.next() ) );
            }
        }

        return sb.toString();

    }

    /**
     * Gets the properly scoped classpathElements from the Maven Project
     * 
     * @return List of classpath strings.
     * @throws MojoExecutionException
     */
    protected List getClasspathElements()
        throws MojoExecutionException
    {
        List classpathElements = null;

        try
        {
            // checking if test sources are included. If so,
            // then we want the classpath to be test
            // (includes everything)
            if ( Artifact.SCOPE_TEST.equalsIgnoreCase( this.classpathScope ) || this.includeTestSources )
            {
                classpathElements = project.getTestClasspathElements();
            }
            else if ( Artifact.SCOPE_COMPILE.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getCompileClasspathElements();
            }
            else if ( Artifact.SCOPE_RUNTIME.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getRuntimeClasspathElements();
            }
            else if ( Artifact.SCOPE_SYSTEM.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getSystemClasspathElements();
            }
            else
            {
                throw new MojoExecutionException( "Invalid classpathScope: " + this.classpathScope +
                    " valid values are: compile, test, runtime, system." );
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new MojoExecutionException( e.getLocalizedMessage(), e );
        }

        return classpathElements;
    }

    /**
     * @return the classpathScope
     */
    protected String getClasspathScope()
    {
        return this.classpathScope;
    }

    /**
     * @param theClasspathScope the classpathScope to set
     */
    protected void setClasspathScope( String theClasspathScope )
    {
        this.classpathScope = theClasspathScope;
    }

    /**
     * @return the jdkName
     */
    protected String getJdkName()
    {
        return this.jdkName;
    }

    /**
     * @param theJdkName the jdkName to set
     */
    protected void setJdkName( String theJdkName )
    {
        this.jdkName = theJdkName;
    }
    
    /**
     * @return the includeTestSources
     */
    protected boolean isIncludeTestSources()
    {
        return this.includeTestSources;
    }

    /**
     * @param theIncludeTestSources the includeTestSources to set
     */
    protected void setIncludeTestSources( boolean theIncludeTestSources )
    {
        this.includeTestSources = theIncludeTestSources;
    }

    /**
     * @return the java compiler options
     */
    protected String getJavaCompilerOptions()
    {
        return javaCompilerOptions;
    }

    /**
     * @param theJavaCompilerOptions the java compiler options
     */
    protected void setJavaCompilerOptions( String theJavaCompilerOptions )
    {
        this.javaCompilerOptions = theJavaCompilerOptions;
    }

    /**
     * @return the webappDirectory
     */
    protected String getWebappDirectory()
    {
        return this.webappDirectory;
    }

    /**
     * @param theWebappDirectory the webappDirectory to set
     */
    protected void setWebappDirectory( String theWebappDirectory )
    {
        this.webappDirectory = theWebappDirectory;
    }
    
    /**
     * @return whether to analyze Struts framework
     */
    protected boolean getAnalyzeStrutsFramework() {
    	return this.analyzeStrutsFramework;
    }
    
    /**
     * @param toAnalyzeStrutsFramework whether to analyze Struts Framework
     */
    protected void setAnalyzeStrutsFramework(boolean analyzeStrutsFramework) {
    	this.analyzeStrutsFramework = analyzeStrutsFramework;
    }
    
    /**
     * @return whether to import Struts validation
     */
    protected boolean getImportStrutsValidation() {
    	return this.importStrutsValidation;
    }
    
    /**
     * @param importStrutsValidation whether to import Struts validation 
     */ 
    protected void setImportStrutsValidation(boolean importStrutsValidation) {
    	this.importStrutsValidation = importStrutsValidation;
    }
    
    /**
     * @return the local
     */
    public ArtifactRepository getLocal()
    {
        return this.local;
    }

    /**
     * @param theLocal the local to set
     */
    public void setLocal( ArtifactRepository theLocal )
    {
        this.local = theLocal;
    }

}
