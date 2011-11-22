package org.codehaus.mojo.make.util;

/*
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
 *
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
 * Change the ownership on a set of paths. Assumes you have sudo installed and configured.
 * 
 * @goal chown
 * 
 * @author jdcasey
 *
 */
public class ChownMojo
    extends AbstractMojo
{
    
    /**
     * The user to use.
     * 
     * @parameter default-value="${user.name}"
     * @required
     */
    private String user;
    
    /**
     * The group to use.
     * 
     * @parameter
     */
    private String group;
    
    /**
     * Whether to execute recursively, as in the case where directories are being targeted.
     * 
     * @parameter default-value="false"
     */
    private boolean recursive;
    
    /**
     * The list of paths to chown.
     * 
     * @parameter
     * @required
     */
    private List chownPaths;
    
    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Commandline chown = new Commandline();
        
        chown.setExecutable( "sudo" );
        
        chown.createArgument().setLine( "chown" );
        
        if ( recursive )
        {
            chown.createArgument().setLine( "-R" );
        }

        String ownership = user;
        
        if ( group != null )
        {
            ownership += ":" + group;
        }
        
        chown.createArgument().setLine( ownership );

        String aggregatedPaths = StringUtils.join( chownPaths.iterator(), " " );
        
        chown.createArgument().setLine( aggregatedPaths );

        try
        {
            StreamConsumer consumer = cliManager.newDebugStreamConsumer();
            
            int result = cliManager.execute( chown, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( "chown command returned an exit value != 0. Aborting build; see command output above for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to execute chown with ownership: " + ownership + " on files:\n" + chownPaths + "\n\nReason: " + e.getMessage(), e );
        }
    }

}
