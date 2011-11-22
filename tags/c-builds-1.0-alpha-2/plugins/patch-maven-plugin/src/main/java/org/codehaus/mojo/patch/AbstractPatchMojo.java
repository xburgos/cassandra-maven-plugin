package org.codehaus.mojo.patch;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.context.Context;

public abstract class AbstractPatchMojo extends AbstractMojo
{
    
    /**
     * If set to true, the patch mojos will execute; otherwise, they will remain dormant. This is
     * a flag for disabling all patch-related activity in the build.
     * 
     * @parameter expression="${patchingEnabled}" default-value="true"
     */
    private boolean patchingEnabled;
    
    /**
     * MavenProject instance used to resolve Ant property expressions.
     * 
     * @parameter expression="${project}"
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

    protected final MavenProject getProject()
    {
        return project;
    }
    
    protected final Context getSessionContext()
    {
        return session.getContainer().getContext();
    }

    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( !patchingEnabled )
        {
            getLog().info( "Patching is disabled for this project." );
            return;
        }
        
        Context context = session.getContainer().getContext();
        
        if ( BuildAdvisor.isProjectBuildSkipped( project, context ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        doExecute();
    }
    
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
