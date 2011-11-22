package org.codehaus.mojo.tools.project.extras;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

public class DerivedArtifact extends DefaultArtifact
{

    public DerivedArtifact( Artifact parentArtifact, String classifier, String type, ArtifactHandler handler )
    {
        super( parentArtifact.getGroupId(), parentArtifact.getArtifactId(), parentArtifact.getVersionRange(),
               parentArtifact.getScope(), type, classifier, handler, false );
        
        setAvailableVersions( parentArtifact.getAvailableVersions() );
        setBaseVersion( parentArtifact.getBaseVersion() );
        setDependencyFilter( parentArtifact.getDependencyFilter() );
        setDependencyTrail( parentArtifact.getDependencyTrail() );
        setRelease( parentArtifact.isRelease() );
        setRepository( parentArtifact.getRepository() );
        
        if ( parentArtifact.isResolved() )
        {
            setResolvedVersion( parentArtifact.getVersion() );
        }
    }

    public DerivedArtifact( Artifact parentArtifact, String version, String classifier, String type, ArtifactHandler handler )
    {
        super( parentArtifact.getGroupId(), parentArtifact.getArtifactId(), VersionRange.createFromVersion( version ),
               parentArtifact.getScope(), type, classifier, handler, false );
        
        setAvailableVersions( parentArtifact.getAvailableVersions() );
        setBaseVersion( parentArtifact.getBaseVersion() );
        setDependencyFilter( parentArtifact.getDependencyFilter() );
        setDependencyTrail( parentArtifact.getDependencyTrail() );
        setRelease( parentArtifact.isRelease() );
        setRepository( parentArtifact.getRepository() );
        
        if ( parentArtifact.isResolved() )
        {
            setResolvedVersion( parentArtifact.getVersion() );
        }
    }

}
