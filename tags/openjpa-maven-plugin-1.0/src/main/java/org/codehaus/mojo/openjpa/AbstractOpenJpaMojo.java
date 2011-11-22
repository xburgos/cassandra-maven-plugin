package org.codehaus.mojo.openjpa;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.plexus.util.FileUtils;

import org.apache.openjpa.lib.util.Options;

/**
 * Base class for  OpenJPA maven tasks.
 * 
 * @author <a href='mailto:struberg@yahoo.de'>Mark Struberg</a>
 * @version $Id$
 */
public abstract class AbstractOpenJpaMojo extends AbstractMojo 
{

    /**
     * Location where <code>persistence-enabled</code> classes are located.
     * 
     * @parameter expression="${openjpa.classes}"
     *            default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File classes;
    
    
    /**
     * Comma separated list of includes to scan searchDir to pass to the jobs.
     * This may be used to restrict the OpenJPA tasks to e.g. a single package which
     * contains all the entities.
     *   
     * @parameter default-value="**\/*.class"
     */
    protected String includes;

    /**
     * Comma separated list of excludes to scan searchDir to pass to the jobs.
     * This option may be used to stop OpenJPA tasks from scanning non-JPA classes
     * (which usually leads to warnings such as "Type xxx has no metadata")
     * 
     * @parameter default-value="";
     */
    protected String excludes;

    /**
     * Additional properties passed to the OpenJPA tools.
     * 
     * @parameter
     */
    protected Properties toolProperties;

    /**
     * List of all class path elements that will be searched for the
     * <code>persistence-enabled</code> classes and resources expected by
     * PCEnhancer.
     * 
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    protected List classpathElements;

    /**
     * Default directory containing the generated resources.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * The Maven Project Object
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;


    /**
     * default ct
     */
    public AbstractOpenJpaMojo() 
    {
        super();
    }

    /**
     * Get the options for the various OpenJPA tools.
     * @return populated Options
     */
    protected abstract Options getOptions();
    
    /**
     * This will prepare the current ClassLoader and add all jars and local
     * classpaths (e.g. target/classes) needed by the OpenJPA task.
     * 
     * @throws MojoExecutionException on any error inside the mojo
     */
    protected void extendRealmClasspath() 
        throws MojoExecutionException 
    { 
        ClassWorld world = new ClassWorld();
        ClassRealm jpaRealm;
        
        try 
        {
            jpaRealm = world.newRealm( "maven.plugin.openjpa"
                                     , Thread.currentThread().getContextClassLoader() );
        } 
        catch ( DuplicateRealmException e ) 
        {
            throw new MojoExecutionException( "Problem while creating new ClassRealm", e );
        }

        Iterator itor = classpathElements.iterator();
        
        while ( itor.hasNext() ) 
        {
            File pathElem = new File( (String) itor.next() );
            getLog().debug( "Adding classpathElement " + pathElem.getPath() );
            try 
            {
                // we need to use 3 slashes to prevent windoof from interpreting 'file://D:/path' as server 'D'
                // we also have to add a trailing slash after directory paths
                URL url = new URL( "file:///" + pathElem.getPath() + ( pathElem.isDirectory() ? "/" : "" ) );
                jpaRealm.addConstituent( url );
            } 
            catch ( MalformedURLException e ) 
            {
                throw new MojoExecutionException( "Error in adding the classpath " + pathElem, e );
            }
        }

        // set the new ClassLoader as default for this Thread
        Thread.currentThread().setContextClassLoader( jpaRealm.getClassLoader() );
    }
    
    /**
     * Locates and returns a list of class files found under specified class
     * directory.
     * 
     * @return list of class files.
     * @throws MojoExecutionException if there was an error scanning class file
     *             resources.
     */
    protected List findEntityClassFiles() throws MojoExecutionException 
    {
        List files = new ArrayList();
    
        try
        {
            files = FileUtils.getFiles( classes, includes, excludes );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while scanning for '" + includes + "' in " + "'"
                + classes.getAbsolutePath() + "'.", e );
        }

        return files;
    }

    /**
     * @param files List of files
     * @return the paths of the given files as String[]
     */
    protected String[] getFilePaths( List files ) 
    {
        String[] args = new String[ files.size() ];
        for ( int i = 0; i < files.size(); i++ )
        {
            File file = (File) files.get( i );
    
            args[ i ] = file.getAbsolutePath();
        }
        return args;
    }


}