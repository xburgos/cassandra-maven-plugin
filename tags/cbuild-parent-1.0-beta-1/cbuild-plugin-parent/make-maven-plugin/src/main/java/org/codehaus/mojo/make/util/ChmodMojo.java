package org.codehaus.mojo.make.util;

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

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Change the file/directory mode on a list of files.
 * 
 * @goal chmod
 * @author jdcasey
 *
 */
public class ChmodMojo
    extends AbstractMojo
{
    
    /**
     * The chmod mode to use.
     * 
     * @parameter default-value="+x"
     * @required
     */
    private String mode;
    
    /**
     * Whether to execute recursively, as in the case where directories are being targeted.
     * 
     * @parameter default-value="false"
     */
    private boolean recursive;
    
    /**
     * The list of paths to chmod.
     * 
     * @parameter
     * @required
     */
    private List < String > chmodPaths;
    
    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * Mojo will change the mode of a file on the system.  You should probably use maven-antrun-plugin
     * instead of this goal and this may be depreciated in the future
     * 
     * @throws MojoExecutionException Procedural or configuration failure
     * @throws MojoFailureException Typically happens with a bad configuration like a type-o in a parameter name
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Commandline cli = new Commandline();
        
        cli.setExecutable( "chmod" );

        if ( recursive )
        {
            cli.createArg().setLine( "-R" );
        }
        
        cli.createArg().setLine( mode );

        String aggregatedPaths = StringUtils.join( chmodPaths.iterator(), " " );
        
        cli.createArg().setLine( aggregatedPaths );

        try
        {
            StreamConsumer consumer = cliManager.newDebugStreamConsumer();
            
            int result = cliManager.execute( cli, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( 
                    "chmod command returned an exit value != 0. Aborting build; "
                    + "see debug output for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to execute chmod with mode: " + mode + " on files:\n"
                                              + chmodPaths + "\n\nReason: " + e.getMessage(), e );
        }
    }

}
