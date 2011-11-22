package org.codehaus.mojo.project.archive;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * @plexus.component role="org.codehaus.mojo.project.archive.ProjectSourceContext" role-hint="default"
 */ 

public class ProjectSourceContext implements Contextualizable
{

    public static final String ROLE = ProjectSourceContext.class.getName();

    public static final String ROLE_HINT = "default";

    //private static final String SOURCE_CONTAINER_CONTEXT_KEY = "project-source-container:contextKey";

    private static final String SOURCE_ARTIFACT_WAS_RESOLVED_KEY = "source-artifact-was-resolved";
    
    private static final String SOURCE_DIRECTORY_KEY = "source-directory";
    
    private static final String SOURCE_ORIGINAL_LOCATION_KEY = "original-source-location";
    
    private static final String SOURCE_ARTIFACT_GRP_KEY = "source-artifact-grp";

    private static final String SOURCE_ARTIFACT_ID_KEY = "source-artifact-id";

    private static final String SOURCE_ARTIFACT_TYPE_KEY = "source-artifact-type";

    private static final String SOURCE_ARTIFACT_VERSION_KEY = "source-artifact-version";
    private static final String SOURCE_ARTIFACT_CLASS_KEY = "source-artifact-classifier";

    private boolean sourceArtifactResolved;
    
    private File sourceDirectory;

    private Artifact sourceArtifact;

    private File originalSourceLocation;

    private Context context;

    private PlexusContainer container;

    public ProjectSourceContext()
    {
    }
    
    public boolean hasProjectSourceDirectory()
    {
        return sourceDirectory != null;
    }
    
    public void setProjectSourceDirectory( File sourceDirectory )
    {
        this.sourceDirectory = sourceDirectory;
    }
    
    public File getProjectSourceDirectory()
    {
        return sourceDirectory;
    }

    public void setSourceArtifactResolved( boolean sourceArtifactResolved )
    {
        this.sourceArtifactResolved = sourceArtifactResolved;
    }

    public boolean isSourceArtifactResolved()
    {
        return sourceArtifactResolved;
    }

    public void store( MavenSession session ) throws ComponentLookupException
    {
        BuildAdvisor ba = (BuildAdvisor) container.lookup( BuildAdvisor.ROLE, BuildAdvisor.ROLE_HINT );

        ba.store( session, SOURCE_ARTIFACT_WAS_RESOLVED_KEY, Boolean.valueOf( this.isSourceArtifactResolved() ) );
        ba.store( session, SOURCE_DIRECTORY_KEY, this.getProjectSourceDirectory() );
        ba.store( session, SOURCE_ORIGINAL_LOCATION_KEY, this.getOriginalProjectSourceLocation() );
        //System.out.println("DEBUG... srcArtifact : " + this.getProjectSourceArtifact() );
        // Stream out the artifact into a bunch of simple strings
        Artifact art = this.getProjectSourceArtifact();
        if ( ( art != null ) && ( art.getGroupId() != null )
            && ( art.getArtifactId() != null ) && ( art.getVersion() != null ) )
        {
            ba.store( session, SOURCE_ARTIFACT_GRP_KEY, art.getGroupId() );
            ba.store( session, SOURCE_ARTIFACT_ID_KEY, art.getArtifactId() );
            ba.store( session, SOURCE_ARTIFACT_TYPE_KEY, art.getType() );
            ba.store( session, SOURCE_ARTIFACT_VERSION_KEY, art.getVersion() );
            ba.store( session, SOURCE_ARTIFACT_CLASS_KEY, art.getClassifier() );
        }
        else
        {
            ba.store( session, SOURCE_ARTIFACT_GRP_KEY, null );
            ba.store( session, SOURCE_ARTIFACT_ID_KEY, null );
            ba.store( session, SOURCE_ARTIFACT_TYPE_KEY, null );
            ba.store( session, SOURCE_ARTIFACT_VERSION_KEY, null );
            ba.store( session, SOURCE_ARTIFACT_CLASS_KEY, null );
        }
    }

    public void read( MavenSession session ) throws ComponentLookupException
    {
        BuildAdvisor ba = (BuildAdvisor) container.lookup( BuildAdvisor.ROLE, BuildAdvisor.ROLE_HINT );

        Boolean isResolved = (Boolean) ba.retrieve( session, SOURCE_ARTIFACT_WAS_RESOLVED_KEY );

        if ( isResolved != null )
        {
            this.setSourceArtifactResolved( isResolved.booleanValue() );
        }
        else
        {
            this.setSourceArtifactResolved( false );
        }

        this.setProjectSourceDirectory( (File) ba.retrieve( session, SOURCE_DIRECTORY_KEY ) );
        this.setOriginalProjectSourceLocation( (File) ba.retrieve( session, SOURCE_ORIGINAL_LOCATION_KEY ) );
        // TODO: Maven 3.0 - revert to memory
        //this.setProjectSourceArtifact( (Artifact) ba.retrieve( session, SOURCE_ARTIFACT_KEY ) );
        String grp = (String) ba.retrieve( session, SOURCE_ARTIFACT_GRP_KEY );
        String id  = (String) ba.retrieve( session, SOURCE_ARTIFACT_ID_KEY );
        String typ = (String) ba.retrieve( session, SOURCE_ARTIFACT_TYPE_KEY );
        String ver = (String) ba.retrieve( session, SOURCE_ARTIFACT_VERSION_KEY );
        String cls = (String) ba.retrieve( session, SOURCE_ARTIFACT_CLASS_KEY );
        Artifact artifact;
        try
        {
            ArtifactFactory artifactFactory = (ArtifactFactory) container.lookup( ArtifactFactory.ROLE );
            artifact = artifactFactory.createArtifactWithClassifier( grp, id, ver, typ, cls );
        }
        catch ( InvalidArtifactRTException e )
        {
            this.setProjectSourceArtifact( null );
            return;
        }

        this.setProjectSourceArtifact( artifact );

    }

    public void setProjectSourceArtifact( Artifact sourceArtifact )
    {
        this.sourceArtifact = sourceArtifact;
    }

    public Artifact getProjectSourceArtifact()
    {
        return sourceArtifact;
    }

    public void setOriginalProjectSourceLocation( File originalSourceLocation )
    {
        this.originalSourceLocation = originalSourceLocation;
    }
    
    public File getOriginalProjectSourceLocation()
    {
        return originalSourceLocation;
    }

    public void contextualize( Context mycontext ) throws ContextException
    {
        this.context = mycontext;
        this.container = (PlexusContainer) mycontext.get( PlexusConstants.PLEXUS_KEY );
    }
}
