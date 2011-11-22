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
    private List chmodPaths;
    
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
        
        ExecTask chmod = new ExecTask();

        chmod.setTaskName( "exec:chmod" );

        chmod.setExecutable( "chmod" );

        if ( recursive )
        {
            chmod.createArg().setLine( "-R" );
        }
        
        chmod.createArg().setLine( mode );

        String aggregatedPaths = StringUtils.join( chmodPaths.iterator(), " " );
        
        chmod.createArg().setLine( aggregatedPaths );

        antCaller.addTask( chmod );
        
        try
        {
            antCaller.executeTasks( project );
        }
        catch ( AntExecutionException e )
        {
            throw new MojoExecutionException( "Failed to execute chmod with mode: " + mode + " on files:\n" + chmodPaths + "\n\nReason: " + e.getMessage(), e );
        }
    }

}
