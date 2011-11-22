package org.codehaus.mojo.autotools;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;

/**
 * Execute autoreconf with command line arguments.
 * 
 * @goal autoreconf
 * @phase process-resources
 * @requiresProject false
 */
public class AutoreconfExecMojo
    extends AbstractMojo
{
    
    /**
     * The actual shell command to run.
     */
    private String command = "autoreconf";

    /**
     * The executable to run
     */
    private File executable;
    
   /**
    * @parameter
    */
   private List < String > arguments;
   
   /**
    * @parameter
    */
   private Properties environment;
   
   /**
    * The temporary working directory where the project is actually built. By default, this is
    * within the '/target' directory.
    * 
    * @parameter expression="${workDir}" default-value="${project.build.directory}"
    */
   private File workDir;
   
   /**
    * @component role-hint="default"
    */
   private CommandLineManager cliManager;
   
    /**
     * Setup the command-line instruction and arguments, then execute it.
     * @throws MojoExecutionException thrown if autoreconf fails
     */
    public void execute() throws MojoExecutionException
    {
        workDir.mkdirs();
        
        Commandline exec = new Commandline();
        exec.setShell( new BourneShell( true ) );
        
        exec.setWorkingDirectory( workDir.getAbsolutePath() );
        
        if ( executable != null )
        {
            exec.setExecutable( executable.getPath() );
        }
        else
        {
            // assume the command is somewhere on the path...
            exec.setExecutable( command );
        }
        
        if ( arguments != null )
        {
            for ( Iterator < String > it = arguments.iterator(); it.hasNext(); )
            {
                String option = it.next();
                exec.createArg().setLine( option );
            }
        }
        
        if ( environment != null && !environment.isEmpty() )
        {
            for ( Iterator it = environment.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                
                exec.addEnvironment( key, val );
                
                getLog().info( "Setting envar: " + key + "=" + val );
            }                                           
        }
        
        try
        {
            StreamConsumer consumer = cliManager.newDebugStreamConsumer();
            
            int result = cliManager.execute( exec, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( command
                  + " command returned an exit value != 0. Aborting build; see debug output for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to execute autoreconf. Reason: " + e.getMessage(), e );
        }
    }
    
}
