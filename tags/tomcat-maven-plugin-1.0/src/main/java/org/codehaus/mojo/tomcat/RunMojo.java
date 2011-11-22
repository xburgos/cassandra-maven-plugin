package org.codehaus.mojo.tomcat;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Runs the current project as a dynamic web application using an embedded Tomcat server.
 * 
 * @goal run
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 * @author Jurgen Lust
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class RunMojo
    extends AbstractRunMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The classes directory for the web application being run.
     * 
     * @parameter expression = "${project.build.outputDirectory}"
     */
    private File classesDir;

    /**
     * The set of dependencies for the web application being run.
     * 
     * @parameter default-value = "${project.artifacts}"
     * @required
     * @readonly
     */
    private Set<Artifact> dependencies;

    /**
     * The web resources directory for the web application being run.
     * 
     * @parameter expression="${basedir}/src/main/webapp"
     */
    private File warSourceDirectory;

    /**
     * The path of the Tomcat context XML file.
     * 
     * @parameter expression = "src/main/webapp/META-INF/context.xml"
     */
    private File contextFile;
    
    /**
     * Set the "follow standard delegation model" flag used to configure our ClassLoader.
     * @see http://tomcat.apache.org/tomcat-6.0-doc/api/org/apache/catalina/loader/WebappLoader.html#setDelegate(boolean)
     * @parameter expression = "${tomcat.delegate}" default-value="true"
     * @since 1.0
     */    
    private boolean delegate = true;

    // ----------------------------------------------------------------------
    // AbstractRunMojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @throws MojoExecutionException 
     */
    @Override
    protected Context createContext( Embedded container )
        throws IOException, MojoExecutionException
    {
        Context context = super.createContext( container );

        context.setReloadable( true );
        
        return context;
    }

    /**
     * {@inheritDoc}
     * @throws MojoExecutionException 
     */
    @Override
    protected WebappLoader createWebappLoader()
        throws IOException, MojoExecutionException
    {
        WebappLoader loader = super.createWebappLoader();
        //super.project.
        if ( useSeparateTomcatClassLoader )
        {
            loader.setDelegate( delegate );
        }
                
        // add classes directories to loader
        if ( classesDir != null )
        {
            try
            {
                List<String> classPathElements = project.getCompileClasspathElements();
                for (String classPathElement : classPathElements)
                {
                    File classPathElementFile = new File(classPathElement);
                    if (classPathElementFile.exists() && classPathElementFile.isDirectory())
                    {
                        getLog().debug( "adding classPathElementFile " + classPathElementFile.toURI().toString() );
                        loader.addRepository( classPathElementFile.toURI().toString() );
                    }
                }
            }
            catch ( DependencyResolutionRequiredException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            
            //loader.addRepository( classesDir.toURI().toString() );
        }

        // add artifacts to loader
        if ( dependencies != null )
        {
            for ( Artifact artifact : dependencies )
            {
                String scope = artifact.getScope();

                // skip provided and test scoped artifacts
                if ( !Artifact.SCOPE_PROVIDED.equals( scope ) && !Artifact.SCOPE_TEST.equals( scope ) )
                {
                    getLog().debug(
                                    "add dependency to webapploader " + artifact.getGroupId() + ":"
                                        + artifact.getArtifactId() + ":" + artifact.getVersion() + ":"
                                        + artifact.getScope() );
                    if ( !isInProjectReferences( artifact ) )
                    {
                        loader.addRepository( artifact.getFile().toURI().toString() );
                    }
                    else
                    {
                        getLog().debug( "skip adding artifact " + artifact.getArtifactId() + " as it's in reactors" );
                    }
                }
            }
        }

        return loader;
    }
    
    protected boolean isInProjectReferences(Artifact artifact)
    {
        if ( project.getProjectReferences() == null || project.getProjectReferences().isEmpty() )
        {
            return false;
        }
        Collection<MavenProject> mavenProjects = project.getProjectReferences().values();
        for ( MavenProject mavenProject : mavenProjects )
        {
            if (StringUtils.equals( mavenProject.getId(), artifact.getId() ))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getDocBase()
    {
        return warSourceDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getContextFile()
    {
        return contextFile;
    }
}
