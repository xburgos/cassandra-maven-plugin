package org.codehaus.mojo.openjpa;

/**
 * Copyright 2007  Rahul Thakur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Processes Application model classes and enhances them by running Open JPA
 * Enhancer tool.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 * @goal enhance
 * @phase process-classes
 * @requiresDependencyResolution
 */
public class OpenJpaEnhancerMojo
    extends AbstractMojo
{

    /**
     * Open JPA class file enchancer.
     */
    private static final String TOOL_NAME_OPENJPA_ENHANCER = "org.apache.openjpa.enhance.PCEnhancer";

    /**
     * Location where <code>persistence-enabled</code> classes are located.
     * 
     * @parameter expression="${openjpa.classDir}"
     *            default-value="${project.build.outputDirectory}"
     * @required
     */
    private File classes;

    /**
     * List of all class path elements that will be searched for the
     * <code>persistence-enabled</code> classes and resources expected by
     * Enhancer.
     * 
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     */
    private List classpathElements;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     */
    private List pluginArtifacts;

    /**
     * Properties passed to the Enhancer tool.
     * 
     * @parameter
     */
    private Properties toolProperties;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !classes.exists() )
        {
            FileUtils.mkdir( classes.getAbsolutePath() );
        }

        List files = findClassFiles();

        try
        {
            enhance( pluginArtifacts, files );
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Error while executing the OpenJPA tool " + getToolName(), e );
        }
    }

    /**
     * Locates and returns a list of class files found under specified class
     * directory.
     * 
     * @return list of class files.
     * @throws MojoExecutionException if there was an error scanning class file
     *             resources.
     */
    private List findClassFiles()
        throws MojoExecutionException
    {
        List files = new ArrayList();

        try
        {
            files = FileUtils.getFiles( classes, "**/*.class", "" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while scanning for '**/*.class' in " + "'"
                + classes.getAbsolutePath() + "'.", e );
        }

        return files;
    }

    /**
     * Returns the OpenJPA tool being invoked by the plugin.
     * 
     * @return OpenJPA tool being invoked by the plugin.
     */
    private String getToolName()
    {
        return "OpenJPA Enhancer";
    }

    /**
     * Processes a list of class file resources that are to be enhanced.
     * 
     * @param artifacts resources that form the classpath for the OpenJPA
     *            Enhancer tool invocation.
     * @param files class file resources to enhance.
     * @throws CommandLineException if there was an error invoking the OpenJPA
     *             Enhancer.
     * @throws MojoExecutionException
     */
    private void enhance( List artifacts, List files )
        throws CommandLineException, MojoExecutionException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "java" );

        StringBuffer cpBuffer = new StringBuffer();

        for ( Iterator it = getUniqueClasspathElements().iterator(); it.hasNext(); )
        {
            cpBuffer.append( (String) it.next() );

            if ( it.hasNext() )
            {
                cpBuffer.append( File.pathSeparator );
            }
        }

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            try
            {
                cpBuffer.append( File.pathSeparator ).append( artifact.getFile().getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error while creating the canonical path for '" + artifact.getFile()
                    + "'.", e );
            }
        }

        cl.createArgument().setValue( "-cp" );

        cl.createArgument().setValue( cpBuffer.toString() );

        cl.createArgument().setValue( TOOL_NAME_OPENJPA_ENHANCER );

        // options
        Set keySet = toolProperties == null ? Collections.EMPTY_SET : toolProperties.keySet();
        for ( Iterator it = keySet.iterator(); it.hasNext(); )
        {
            String key = (String) it.next();
            cl.createArgument().setValue( "-" + key );
            String value = ( null != toolProperties.getProperty( key ) ? toolProperties.getProperty( key ) : "" );
            cl.createArgument().setValue( value );
        }

        // list of input files
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            File file = (File) it.next();

            cl.createArgument().setValue( file.getAbsolutePath() );
        }

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLog().debug( "Executing command line:" );

        getLog().debug( cl.toString() );

        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

        getLog().debug( "Exit code: " + exitCode );

        getLog().debug( "--------------------" );
        getLog().debug( " Standard output from the OpenJPA Enhancer tool:" );
        getLog().debug( "--------------------" );
        getLog().info( stdout.getOutput() );
        getLog().debug( "--------------------" );

        String stream = stderr.getOutput();

        if ( stream.trim().length() > 0 )
        {
            getLog().error( "--------------------" );
            getLog().error( " Standard error from the OpenJPA Enhancer tool:" );
            getLog().error( "--------------------" );
            getLog().error( stderr.getOutput() );
            getLog().error( "--------------------" );
        }

        if ( exitCode != 0 )
        {
            throw new MojoExecutionException( "The OpenJPA Enhancer tool exited with a non-null exit code." );
        }
    }

    /**
     * Return the set of classpath elements, ensuring that {@link #classes}
     * location comes first, and that no entry is duplicated in the classpath.
     * <p>
     * There is a provision to specify an alternate <code>classes</code>
     * location by passing the <code>openjpa.classesDir</code> property.
     * 
     * @return list of unique classpath elements.
     */
    private List getUniqueClasspathElements()
    {
        List ret = new ArrayList();
        ret.add( this.classes.getAbsolutePath() );
        Iterator it = classpathElements.iterator();
        while ( it.hasNext() )
        {
            String pathelem = (String) it.next();
            if ( !ret.contains( new File( pathelem ).getAbsolutePath() ) )
            {
                ret.add( pathelem );
            }
        }
        return ret;
    }
}
