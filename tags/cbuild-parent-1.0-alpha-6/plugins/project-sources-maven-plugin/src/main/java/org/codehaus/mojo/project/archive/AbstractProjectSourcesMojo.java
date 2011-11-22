package org.codehaus.mojo.project.archive;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.fs.archive.manager.ArchiverManager;
import org.codehaus.mojo.tools.project.extras.DerivedArtifact;
import org.codehaus.mojo.tools.project.extras.ProjectReleaseInfoUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

public abstract class AbstractProjectSourcesMojo
    extends AbstractMojo
    implements Contextualizable
{

    /**
     * @parameter default-value="project-sources"
     * @required
     */
    private String sourceArtifactClassifier;

    /**
     * @parameter default-value="zip"
     * @required
     */
    private String sourceArtifactType;
    
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
    
    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.artifact}"
     * @required
     * @readonly
     */
    private Artifact projectArtifact;
    
    /**
     * @component role-hint="local-overrides"
     */
    private ArchiverManager archiverManager;
    
    // contextualized.
    private PlexusContainer container;
    
    protected String getSourceArtifactType()
    {
        return sourceArtifactType;
    }
    
    protected String getSourceArtifactClassifier()
    {
        return sourceArtifactClassifier;
    }
    
    protected ProjectSourceContext loadContext() throws ComponentLookupException
    {
        ProjectSourceContext pc = (ProjectSourceContext) container.lookup( ProjectSourceContext.ROLE,
            ProjectSourceContext.ROLE_HINT );
        pc.read( session );
        return pc;
    }
    
    protected void storeContext( ProjectSourceContext context ) throws ComponentLookupException
    {
        context.store( session );
    }
    
    protected Artifact getProjectSourcesArtifact()
        throws MojoExecutionException
    {
        return getProjectSourcesArtifact( sourceArtifactType );
    }
    
    protected Artifact getProjectSourcesArtifact( String type )
    {

        ArtifactHandler handler;
        try
        {
            handler = (ArtifactHandler) getContainer().lookup( ArtifactHandler.ROLE, type );
        }
        catch ( ComponentLookupException e )
        {
            getLog().debug( "Cannot lookup ArtifactHandler for archive type: " + type + "; constructing stub artifact handler." );
            
            // use the defaults...it should be enough for our uses.
            handler = new DefaultArtifactHandler( type );
        }
        
        String baseVersion = ProjectReleaseInfoUtils.getBaseVersion( getProjectArtifact().getVersion() );

        return new DerivedArtifact( getProjectArtifact(), baseVersion, sourceArtifactClassifier, sourceArtifactType, handler );
    }
    
    protected Artifact getProjectArtifact()
    {
        return projectArtifact;
    }
    
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }
    
    protected ArchiverManager getArchiverManager()
    {
        return archiverManager;
    }
    
    protected PlexusContainer getContainer()
    {
        return container;
    }
    
    protected MavenSession getSession()
    {
        return session;
    }
    
    public MavenProject getProject()
    {
        return project;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    protected void setArchiverManager( ArchiverManager archiverManager )
    {
        this.archiverManager = archiverManager;
    }

    protected void setContainer( PlexusContainer container )
    {
        this.container = container;
    }

    protected void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    protected void setProject( MavenProject project )
    {
        this.project = project;
    }

    protected void setProjectArtifact( Artifact projectArtifact )
    {
        this.projectArtifact = projectArtifact;
    }

    protected void setSession( MavenSession session )
    {
        this.session = session;
    }

    protected void setSourceArtifactClassifier( String sourceArtifactClassifier )
    {
        this.sourceArtifactClassifier = sourceArtifactClassifier;
    }

    protected void setSourceArtifactType( String sourceArtifactType )
    {
        this.sourceArtifactType = sourceArtifactType;
    }
}
