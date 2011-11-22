package org.codehaus.mojo.tools.project.extras;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

public class DerivedArtifact extends DefaultArtifact
{

    public DerivedArtifact( Artifact parentArtifact, String classifier, String type )
    {
        super( parentArtifact.getGroupId(), parentArtifact.getArtifactId(), parentArtifact.getVersionRange(),
               parentArtifact.getScope(), type, classifier, parentArtifact.getArtifactHandler(), false );
        
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
