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
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;
import org.codehaus.plexus.util.StringUtils;

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
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );
        
        ExecTask chown = new ExecTask();

        chown.setTaskName( "exec:chown" );

        chown.setExecutable( "sudo" );
        
        chown.createArg().setLine( "chown" );
        
        if ( recursive )
        {
            chown.createArg().setLine( "-R" );
        }

        String ownership = user;
        
        if ( group != null )
        {
            ownership += ":" + group;
        }
        
        chown.createArg().setLine( ownership );

        String aggregatedPaths = StringUtils.join( chownPaths.iterator(), " " );
        
        chown.createArg().setLine( aggregatedPaths );

        antCaller.addTask( chown );
        
        try
        {
            antCaller.executeTasks( project );
        }
        catch ( AntExecutionException e )
        {
            throw new MojoExecutionException( "Failed to execute chown with ownership: " + ownership + " on files:\n" + chownPaths + "\n\nReason: " + e.getMessage(), e );
        }
    }

}
