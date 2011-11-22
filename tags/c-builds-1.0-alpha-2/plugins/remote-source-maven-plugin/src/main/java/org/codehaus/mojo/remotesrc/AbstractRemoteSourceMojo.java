package org.codehaus.mojo.remotesrc;

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

import java.io.File;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;

public abstract class AbstractRemoteSourceMojo
    extends AbstractMojo
{
    
    private static final String SOURCE_ARCHIVE_FILE = ":sourceArchiveFile";
    
    /**
     * The projectId, so we can register the source archive file in the plugin context, even in a 
     * mulimodule build environment.
     * 
     * @parameter default-value="${project.id}"
     * @required
     * @readonly
     */
    private String projectId;
    
    /**
     * MavenProject instance used to power any property retrieval for the Ant task executions.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    protected abstract boolean isSkip();

    protected String getProjectId()
    {
        return projectId;
    }
    
    protected MavenProject getProject()
    {
        return project;
    }
    
    protected MavenSession getSession()
    {
        return session;
    }
    
    protected void setSourceArchiveFile( String projectId, File sourceArchive )
    {
        Map context = getPluginContext();
        
        getLog().info( "[ON SET] Plugin context hashcode is: " + context.hashCode() );
        getLog().info( "[SET] Source archive file for project: " + projectId + " is: " + sourceArchive );
        
        context.put( projectId + SOURCE_ARCHIVE_FILE, sourceArchive );
    }

    protected File getSourceArchiveFile( String projectId )
    {
        Map context = getPluginContext();
        
        getLog().info( "[ON GET] Plugin context hashcode is: " + context.hashCode() );
        
        File sourceArchive = (File) context.get( projectId + SOURCE_ARCHIVE_FILE );
        getLog().info( "[GET] Source archive file for project: " + projectId + " is: " + sourceArchive );
        
        return sourceArchive;
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( "pom".equals( project.getPackaging() ) )
        {
            getLog().info( "Skipping POM project." );
            return;
        }

        if ( isSkip() )
        {
            getLog().info( getSkipMessage() );
            return;
        }
        
        if ( BuildAdvisor.isProjectBuildSkipped( project, session.getContainer().getContext() ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        doExecute();
    }
    
    protected abstract CharSequence getSkipMessage();

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
