package org.codehaus.mojo.resolver.bod.build;

import java.util.List;
import java.util.Set;

import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionRequest;

public interface DependencyBuilder
{

    public void buildDependencies( List dependencyProjects, Set completedBuilds, BuildOnDemandResolutionRequest request )
        throws BuildOnDemandResolutionException;

}
