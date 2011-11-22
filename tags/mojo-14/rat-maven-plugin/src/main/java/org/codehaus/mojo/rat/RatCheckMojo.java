package org.codehaus.mojo.rat;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Run RAT to perform a violation check.
 * 
 * @goal check
 * @phase verify
 */
public class RatCheckMojo extends AbstractRatMojo
{
    /**
     * Where to store the report.
     * 
     * @parameter expression="${rat.outputFile}" default-value="${project.build.directory}/rat.txt"
     */
    private File reportFile;

    /**
     * Invoked by Maven to execute the Mojo.
     * @throws MojoFailureException An error in the plugin configuration was detected.
     * @throws MojoExecutionException Another error occurred while executing the plugin.
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File parent = reportFile.getParentFile();
        parent.mkdirs();

        FileWriter fw = null;
        try
        {
            fw = new FileWriter( reportFile );
            createReport( new PrintWriter( fw ) );
            fw.close();
            fw = null;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create file " + reportFile + ": " + e.getMessage(), e );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
        }
    }
}
