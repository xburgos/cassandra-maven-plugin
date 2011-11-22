package org.codehaus.mojo.emma;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Makes EMMA Coverage tests.
 * 
 * @author <a href="anna.nieslony@sdm.de">Anna Nieslony</a>
 * @goal instr
 * @phase process-classes
 * @description emma coverage test plugin
 */
public class EmmaInstrMojo extends EmmaMojo
{

    String commandName = "instr";

    /**
     * The directory containing generated classes of the project being tested.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * Determines whether code coverage results will be merged with any existing results.
     * 
     * @parameter expression="${emma.merge}" default-value="false"
     */
    private boolean mergeResults;

    /**
     * The project whose project files to create.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException
    {
        getLog().info( "[emma-plugin:instr]" );
        File buildDir = new File( project.getBuild().getDirectory() );
        File emmaClassesDir = new File( buildDir, "emma-classes" );
        File propsFile = new File( emmaClassesDir, "emma.properties" );
        File ecFile = new File( emmaClassesDir, "coverage.ec" );
        File emFile = new File( emmaClassesDir, "coverage.em" );

        List commandArgs = new ArrayList();

        commandArgs.add( "-instrpath" );
        commandArgs.add( classesDirectory.getAbsolutePath() );
        commandArgs.add( "-outdir" );
        commandArgs.add( emmaClassesDir.getAbsolutePath() );
        commandArgs.add( "-outfile" );
        commandArgs.add( emFile.getAbsolutePath() );

        getLog().info( "Emma instrument path: " + classesDirectory.getAbsolutePath() );
        getLog().info( "Emma output Directory: " + emmaClassesDir.getAbsolutePath() );
        runEmma( commandName, commandArgs );

        // Create file "emma.properties" with instruction for coverage run
        try
        {
            PrintWriter propWriter = new PrintWriter( new FileWriter( propsFile ) );
            propWriter.println( "coverage.out.file=" + propertyFormat( ecFile.getAbsolutePath() ) );
            propWriter.println( "coverage.out.merge=" + Boolean.toString( mergeResults ) );
            propWriter.close();
            if ( propWriter.checkError() )
                throw new MojoExecutionException( "Can't write emma.properties" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Can't write emma.properties", e );
        }
    }

}
