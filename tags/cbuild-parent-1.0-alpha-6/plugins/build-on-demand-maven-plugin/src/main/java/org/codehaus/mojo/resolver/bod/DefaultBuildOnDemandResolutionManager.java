package org.codehaus.mojo.resolver.bod;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.resolver.bod.binary.BinaryDependencyManager;
import org.codehaus.mojo.resolver.bod.build.DependencyBuilder;
import org.codehaus.mojo.resolver.bod.pom.DependencyPOMResolver;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;

/**
 * @plexus.component role="org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionManager" role-hint="default"
 *  
 * @author jdcasey
 *
 */
public class DefaultBuildOnDemandResolutionManager
    implements BuildOnDemandResolutionManager
{

    public static final String ROLE_HINT = "default";
    public static final String SNAPSHOT_PARENT_ARTIFACT_ID = "cpkg-projects";

    /**
     * @plexus.requirement role-hint="default"
     */
    private DependencyPOMResolver pomResolver;

    /**
     * @plexus.requirement role-hint="default"
     */
    private DependencyBuilder dependencyBuilder;
    
    /**
     * @plexus.requirement role-hint="rpm"
     */
    private BinaryDependencyManager binaryDependencyManager;

    public DefaultBuildOnDemandResolutionManager()
    {
        // used for Plexus initialization.
    }

    public DefaultBuildOnDemandResolutionManager( DependencyPOMResolver pomResolver )
    {
        this.pomResolver = pomResolver;
    }

    // TODO: unit test clean code path.
    public void resolveDependencies( BuildOnDemandResolutionRequest request )
        throws BuildOnDemandResolutionException
    {
        Set completedBuilds = request.getCompletedBuilds();

        if ( completedBuilds == null )
        {
            completedBuilds = new HashSet();
        }

        ArtifactRepository localRepository = request.getLocalRepository();

        List missingDependencyProjects = pomResolver.resolveDependencyPoms( request.getProject(), request
            .getCurrentPendingProjects(), localRepository, completedBuilds );

        missingDependencyProjects = orderDependencyProjects( missingDependencyProjects );
        
        if ( request.useBinaryOnlyMode() || request.useBuildOnDemandMode() )
        {
            binaryDependencyManager.findDependenciesWithMissingBinaries( missingDependencyProjects, localRepository );
        }
        
        if ( request.useBuildOnDemandMode() || request.useSourceOnlyMode() )
        {
            dependencyBuilder.buildDependencies( missingDependencyProjects, completedBuilds, request );
        }
        
        if ( !missingDependencyProjects.isEmpty() )
        {
            throw new BuildOnDemandResolutionException( buildLeftoverDependencyMessage( missingDependencyProjects ) );
        }
    }

    private String buildLeftoverDependencyMessage( List missingDependencyProjects )
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( "Failed to resolve " + missingDependencyProjects.size() + " projects:" );
        
        for ( Iterator it = missingDependencyProjects.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();
            
            buffer.append( "\n- " ).append( project.getId() );
        }
        
        return buffer.toString();
    }

    protected List orderDependencyProjects( List candidates )
        throws BuildOnDemandResolutionException
    {
        DAG dag = new DAG();

        if ( candidates == null || candidates.isEmpty() )
        {
            return Collections.EMPTY_LIST;
        }

        Map projectMap = populateGraphVerticesAndProjectMap( dag, candidates );

        graphRelationships( dag, candidates );

        List sortedProjects = new ArrayList();

        for ( Iterator i = TopologicalSorter.sort( dag ).iterator(); i.hasNext(); )
        {
            String id = (String) i.next();

            sortedProjects.add( projectMap.get( id ) );
        }

        return sortedProjects;
    }

    protected void graphRelationships( DAG dag, List candidates )
        throws BuildOnDemandResolutionException
    {
        for ( Iterator i = candidates.iterator(); i.hasNext(); )
        {
            MavenProject project = (MavenProject) i.next();

            String id = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            for ( Iterator j = project.getDependencies().iterator(); j.hasNext(); )
            {
                Dependency dependency = (Dependency) j.next();

                String dependencyId = ArtifactUtils
                    .versionlessKey( dependency.getGroupId(), dependency.getArtifactId() );

                if ( dag.getVertex( dependencyId ) != null )
                {
                    try
                    {
                        dag.addEdge( id, dependencyId );
                    }
                    catch ( CycleDetectedException e )
                    {
                        throw new BuildOnDemandResolutionException( "Cycle detected with dependency: " + dependencyId + " of project: "
                            + id + "\n\nCycle: " + e.getCycle(), e );
                    }
                }
            }

            MavenProject parent = project.getParent();
            if ( parent != null )
            {
                String parentId = ArtifactUtils.versionlessKey( parent.getGroupId(), parent.getArtifactId() );

                if ( dag.getVertex( parentId ) != null )
                {
                    // Parent is added as an edge, but must not cause a cycle - so we remove any other edges it has in conflict
                    if ( dag.hasEdge( parentId, id ) )
                    {
                        dag.removeEdge( parentId, id );
                    }

                    try
                    {
                        dag.addEdge( id, parentId );
                    }
                    catch ( CycleDetectedException e )
                    {
                        throw new BuildOnDemandResolutionException( "THIS SHOULD NEVER HAPPEN!\n" + "Cycle detected with parent: "
                            + parentId + " of project: " + id + "\n\nCycle: " + e.getCycle(), e );
                    }
                }
            }

            // We're assuming that the plugins, reports, and build extensions
            // used in these project builds will already be present. This helps
            // avoid any strange problems we might have building those projects
            // alongside the projects they're meant to build.
        }
    }

    protected Map populateGraphVerticesAndProjectMap( DAG dag, List candidates )
        throws BuildOnDemandResolutionException
    {
        Map projectMap = new HashMap();

        for ( Iterator i = candidates.iterator(); i.hasNext(); )
        {
            MavenProject project = (MavenProject) i.next();

            String id = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            // this is currently important because of the possibility for version ranges
            if ( dag.getVertex( id ) != null )
            {
                throw new BuildOnDemandResolutionException( "Project '" + id + "' is duplicated in build-candidate set." );
            }

            dag.addVertex( id );

            projectMap.put( id, project );
        }

        return projectMap;
    }

}
