package org.codehaus.mojo.project.archive;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildContextUtils;
import org.codehaus.plexus.context.Context;

public class ProjectSourceContext
{

    private static final String SOURCE_CONTAINER_CONTEXT_KEY = "project-source-container:contextKey";

    private static final String SOURCE_ARTIFACT_WAS_RESOLVED_KEY = "source-artifact-was-resolved";
    
    private static final String SOURCE_DIRECTORY_KEY = "source-directory";
    
    private static final String SOURCE_ORIGINAL_LOCATION_KEY = "original-source-location";
    
    private static final String SOURCE_ARTIFACT_KEY = "source-artifact";

    private boolean sourceArtifactResolved;
    
    private File sourceDirectory;

    private Artifact sourceArtifact;

    private File originalSourceLocation;

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

    public void store( Context context, MavenProject project )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( SOURCE_CONTAINER_CONTEXT_KEY, project, context, true );

        containerMap.put( SOURCE_ARTIFACT_WAS_RESOLVED_KEY, Boolean.valueOf( sourceArtifactResolved ) );
        containerMap.put( SOURCE_DIRECTORY_KEY, sourceDirectory );
        containerMap.put( SOURCE_ORIGINAL_LOCATION_KEY, originalSourceLocation );
        containerMap.put( SOURCE_ARTIFACT_KEY, sourceArtifact );
    }

    public static ProjectSourceContext read( Context context, MavenProject project )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( SOURCE_CONTAINER_CONTEXT_KEY, project, context, false );

        ProjectSourceContext ctx = null;

        if ( containerMap != null )
        {
            ctx = new ProjectSourceContext();

            Boolean isProjectSourceArtifactResolved = (Boolean) containerMap.get( SOURCE_ARTIFACT_WAS_RESOLVED_KEY );

            if ( isProjectSourceArtifactResolved != null )
            {
                ctx.sourceArtifactResolved = isProjectSourceArtifactResolved.booleanValue();
            }
            
            ctx.sourceDirectory = (File) containerMap.get( SOURCE_DIRECTORY_KEY );
            ctx.originalSourceLocation = (File) containerMap.get( SOURCE_ORIGINAL_LOCATION_KEY );
            ctx.sourceArtifact = (Artifact) containerMap.get( SOURCE_ARTIFACT_KEY );
        }

        return ctx;
    }

    public static void delete( Context context, MavenProject project )
    {
        BuildContextUtils.clearContextContainerMapForProject( SOURCE_CONTAINER_CONTEXT_KEY, project, context );
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
}
