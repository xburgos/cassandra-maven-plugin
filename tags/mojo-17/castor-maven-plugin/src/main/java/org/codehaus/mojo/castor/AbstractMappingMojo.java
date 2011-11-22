package org.codehaus.mojo.castor;

/*
 * Copyright 2005 The Codehaus.
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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.exolab.castor.tools.MappingTool;
 
/**
 * A mojo that uses Castor MappingTool to generate mapping files from a Class.
 * <a href="http://castor.codehaus.org/javadoc/org/exolab/castor/tools/MappingTool.html">
 * MappingTool</a>.
 * 
 * @author nicolas <nicolas@apache.org>
 */
public abstract class AbstractMappingMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * @parameter default-value="false"
     */
    private boolean force;

    /**
     * The output directory.
     * 
     * @parameter default-value="${project.build.outputDirectory}/"
     */
    private File outputDirectory;
	
	/**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            outputDirectory.mkdirs();
            getLog().info( "Generate mapping " + getMappingName() + " for class " + getClassName() );

            ClassLoader cl = getProjectClassLoader();

            MappingTool tool = new MappingTool();

            // As of Castor 1.2, an InternalContext needs to be set; using reflection
            // to set it or not
            Class internalContextClass;
            try {
                internalContextClass = Class.forName( "org.castor.xml.InternalContext" );
                Class backwardsCompatibilityClass = 
                    Class.forName( "org.castor.xml.BackwardCompatibilityContext" );
                Method setter = MappingTool.class.getMethod( "setInternalContext", new Class[] { internalContextClass } );
                if ( setter != null ) {
                    getLog().info( "About to invoke 'setInternalContext()' on org.exolab.castor.tools.MappingTool" );
                    setter.invoke( tool, new Object[] { backwardsCompatibilityClass.newInstance() } );
                }
            } catch ( ClassNotFoundException e ) {
                // nothing to do as we check whether the class(es) exist or not
            }

            Class clazz = cl.loadClass( getClassName() );
            tool.addClass( clazz );

            File file = new File( outputDirectory, getMappingName().trim() );
            if ( file.exists() && ( !force ) )
            {
                getLog().info( getMappingName() + " allready generated" );
                return;
            }

            Writer writer = new FileWriter( file );
            tool.write( writer );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to generate mapping for "
                + getClassName(), e );
        }
    }

    private ClassLoader projectClassLoader;

    protected ClassLoader getProjectClassLoader()
        throws DependencyResolutionRequiredException, MalformedURLException
    {
        if ( projectClassLoader != null )
        {
            return projectClassLoader;
        }

        List compile = project.getCompileClasspathElements();
        URL[] urls = new URL[compile.size()];
        int i = 0;
        for ( Iterator iterator = compile.iterator(); iterator.hasNext(); )
        {
            Object object = (Object) iterator.next();
            if ( object instanceof Artifact )
            {
                urls[i] = ( (Artifact) object ).getFile().toURL();
            }
            else
            {
                urls[i] = new File( (String) object ).toURL();
            }
            i++;
        }
        projectClassLoader =
            new URLClassLoader( urls, getClass().getClassLoader().getSystemClassLoader() );
        return projectClassLoader;
    }

	/**
     * @return the classname
     */
    protected abstract String getClassName();

    /**
     * @return the mappingName
     */
    protected abstract String getMappingName();
}