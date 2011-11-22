package org.codehaus.mojo.patch;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildContextUtils;
import org.codehaus.plexus.context.Context;

public class PatchContext
{

    private static final String PATCH_CONTAINER_CONTEXT_KEY = "patch-container:contextKey";

    private static final String PATCH_ARTIFACT_WAS_RESOLVED_KEY = "patch-artifact-was-resolved";
    
    private static final String PATCH_DIRECTORY_KEY = "patch-directory";
    
    private static final String PATCH_ARTIFACT_KEY = "patch-artifact";

    private boolean patchArtifactResolved;
    
    private File patchDirectory;

    private Artifact patchArtifact;

    public PatchContext()
    {
    }
    
    public boolean hasPatchDirectory()
    {
        return patchDirectory != null;
    }
    
    public void setPatchDirectory( File patchDirectory )
    {
        this.patchDirectory = patchDirectory;
    }
    
    public File getPatchDirectory()
    {
        return patchDirectory;
    }

    public void setPatchArtifactResolved( boolean patchArtifactResolved )
    {
        this.patchArtifactResolved = patchArtifactResolved;
    }

    public boolean isPatchArtifactResolved()
    {
        return patchArtifactResolved;
    }

    public void store( Context context, MavenProject project )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( PATCH_CONTAINER_CONTEXT_KEY, project, context, true );

        containerMap.put( PATCH_ARTIFACT_WAS_RESOLVED_KEY, Boolean.valueOf( patchArtifactResolved ) );
        containerMap.put( PATCH_DIRECTORY_KEY, patchDirectory );
        containerMap.put( PATCH_ARTIFACT_KEY, patchArtifact );
    }

    public static PatchContext read( Context context, MavenProject project )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( PATCH_CONTAINER_CONTEXT_KEY, project, context, false );

        PatchContext ctx = null;

        if ( containerMap != null )
        {
            ctx = new PatchContext();

            Boolean isPatchArtifactResolved = (Boolean) containerMap.get( PATCH_ARTIFACT_WAS_RESOLVED_KEY );

            if ( isPatchArtifactResolved != null )
            {
                ctx.patchArtifactResolved = isPatchArtifactResolved.booleanValue();
            }
            
            File patchDirectory = (File) containerMap.get( PATCH_DIRECTORY_KEY );
            
            if ( patchDirectory != null )
            {
                ctx.patchDirectory = patchDirectory;
            }
            
            Artifact patchArtifact = (Artifact) containerMap.get( PATCH_ARTIFACT_KEY );
            
            if ( patchArtifact != null )
            {
                ctx.patchArtifact = patchArtifact;
            }
        }

        return ctx;
    }

    public static void delete( Context context, MavenProject project )
    {
        BuildContextUtils.clearContextContainerMapForProject( PATCH_CONTAINER_CONTEXT_KEY, project, context );
    }

    public void setPatchArtifact( Artifact patchArtifact )
    {
        this.patchArtifact = patchArtifact;
    }

    public Artifact getPatchArtifact()
    {
        return patchArtifact;
    }
}
