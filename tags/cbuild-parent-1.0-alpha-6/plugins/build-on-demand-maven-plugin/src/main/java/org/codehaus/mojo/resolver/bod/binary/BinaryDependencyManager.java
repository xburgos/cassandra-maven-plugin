package org.codehaus.mojo.resolver.bod.binary;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;

public interface BinaryDependencyManager
{

    void findDependenciesWithMissingBinaries( List dependencyProjects, ArtifactRepository localRepository )
        throws BuildOnDemandResolutionException;

}
