package org.codehaus.mojo.minijar;

/*
 * Copyright 2005 The Apache Software Foundation.
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
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.vafer.dependency.Clazz;
import org.vafer.dependency.Clazzpath;
import org.vafer.dependency.ClazzpathUnit;
import org.vafer.dependency.utils.ResourceMatcher;

/**
 * Common class for the minijar mojos 
 */
public abstract class AbstractPluginMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${executedProject}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    protected File buildDirectory;

    /**
     * @parameter expression="${createUeberJar}" default-value="true" 
     * @readonly
     */
    protected boolean createUeberJar = true;

    /**
     * Classpath for dependency calculations
     */
    protected Clazzpath clazzpath;

    /**
     * Get the dependencies from the project
     * @return Set of project dependency Artifacts
     */
    protected Set getDependencies()
    {
        return project.getArtifacts();
    }

    /**
     * Calculates the transitive hull of classes that are required to execute the main build artifact
     * @return Set of classes that are NOT required to execute
     * @throws IOException on error
     * @throws MojoExecutionException on error
     */
    protected Set getNotRequiredClazzes() throws IOException, MojoExecutionException
    {
        clazzpath = new Clazzpath();
        ClazzpathUnit jar = new ClazzpathUnit( clazzpath, project.getArtifact().getFile().getAbsolutePath() );
    
        for ( Iterator i = getDependencies().iterator(); i.hasNext(); )
        {
            Artifact dependency = (Artifact) i.next();
            new ClazzpathUnit( clazzpath, dependency.getFile().getAbsolutePath() );
        }
    
        Set removable = new HashSet();
        removable.addAll( clazzpath.getClazzes() );
        
        int total = removable.size();
        
        removable.removeAll( jar.getClazzes() );
        removable.removeAll( jar.getTransitiveDependencies() );
    
        getLog().info( "Can remove " + removable.size() + " of " + total + " classes (" + (int) ( 100 * removable.size() / total ) + "%)." );                                
        
        return removable;
    }

    /**
     * Creates a matcher deciding whether a resource/class is meant to be kept or not
     * @param remove Set of classes that are meant to be removed
     * @return ResourceMatcher
     */
    protected ResourceMatcher createMatcher( final Set remove )
    {
        final ResourceMatcher matcher = new ResourceMatcher()
        {
            private final int extension = ".class".length();
                
            public boolean keepResourceWithName( String pResourceName )
            {
                // TODO: make it configurable what to do with resources
                if ( !pResourceName.endsWith( ".class" ) )
                {
                    // keep all resources
                    return true;
                }
    
                final Clazz clazz = new Clazz( pResourceName.replace( '/', '.' ).substring( 0, pResourceName.length() - extension ) );
                
                // keep only classes that are in the transitive hull
                return !remove.contains( clazz );
            }                
        };
        
        return matcher;     
    }

}
