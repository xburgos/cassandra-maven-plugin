package org.codehaus.mojo.bod.build;

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

import java.io.File;
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
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.BuildConfiguration;
import org.codehaus.mojo.bod.PomRewriteConfiguration;
import org.codehaus.mojo.bod.candidate.BuildCandidateResolver;
import org.codehaus.mojo.bod.rewrite.PomRewriter;
import org.codehaus.mojo.bod.source.ProjectSourceResolver;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;

/**
 * @plexus.component role="org.apache.maven.plugin.depbuild.build.DependencyBuilder"
 *            role-hint="default"
 *  
 * @author jdcasey
 *
 */
public class DefaultDependencyBuilder
    implements DependencyBuilder
{

    public static final String ROLE_HINT = "default";
    public static final String SNAPSHOT_PARENT_ARTIFACT_ID = "cpkg-projects";

    /**
     * @plexus.requirement
     */
    private BuildCandidateResolver candidateResolver;

    /**
     * @plexus.requirement
     */
    private ProjectSourceResolver sourceResolver;

    /**
     * @plexus.requirement
     */
    private Invoker invoker;

    /**
     * @plexus.requirement
     */
    private PomRewriter rewriter;

    public DefaultDependencyBuilder()
    {
        // used for Plexus initialization.
    }

    public DefaultDependencyBuilder( BuildCandidateResolver candidateResolver, ProjectSourceResolver sourceResolver,
                                     PomRewriter pomRewriter, Invoker mavenInvoker )
    {
        this.candidateResolver = candidateResolver;
        this.sourceResolver = sourceResolver;
        rewriter = pomRewriter;
        invoker = mavenInvoker;
    }

    // TODO: unit test clean code path.
    public void buildMissingDependencies( DependencyBuildRequest request )
        throws BuildException
    {
        processDependencies( request, true );
    }
    
    // TODO: unit test clean code path.
    public void removeDependencies( DependencyBuildRequest request )
    	throws BuildException
    {
        processDependencies( request, false );
    }
    
    private void processDependencies( DependencyBuildRequest request, boolean bottomFirst )
    	throws BuildException
    {
        Set completedBuilds = request.getCompletedBuilds();

        if ( completedBuilds == null )
        {
            completedBuilds = new HashSet();
        }

        ArtifactRepository localRepository = request.getLocalRepository();

        List candidates = candidateResolver.resolveCandidates( request.getProject(), request
            .getCurrentPendingProjects(), localRepository, completedBuilds, request.isForce() );

        candidates = orderCandidates( candidates );
        
        if( !bottomFirst )
        {
            Collections.reverse( candidates );
        }

        PomRewriteConfiguration rewriteConfig = request.getPomRewriteConfiguration();

        rewriteCandidates( candidates, rewriteConfig, localRepository );

        BuildConfiguration prototypeConfig = request.getBuildPrototype();

        // TODO: unit test when provided build prototype config is null.
        if ( prototypeConfig == null )
        {
            prototypeConfig = new BuildConfiguration();
        }

        File projectsDirectory = request.getProjectsDirectory();

        buildCandidates( candidates, completedBuilds, localRepository, rewriteConfig, prototypeConfig,
                         projectsDirectory, request );
    }

    // TODO: unit test clean code path
    private List rewriteCandidates( List candidates, PomRewriteConfiguration rewriteConfig,
                                    ArtifactRepository localRepository )
        throws BuildException
    {
        List result = candidates;

        MessageHolder errors = new DefaultMessageHolder();

        result = rewriter.rewrite( candidates, rewriteConfig, localRepository, errors );

        // TODO: unit test case where rewrite fails for > 0 POMs.
        if ( !errors.isEmpty() )
        {
            throw new BuildException( "While rewriting POMs:\n\n" + errors.render() );
        }

        return result;
    }

    protected void buildCandidates( List candidates, Set completedBuilds, ArtifactRepository localRepository,
                                    PomRewriteConfiguration rewriteConfig, BuildConfiguration buildPrototype,
                                    File projectsDirectory, DependencyBuildRequest request )
        throws BuildException
    {
        MessageHolder errors = new DefaultMessageHolder();

        if ( rewriteConfig == null )
        {
            rewriteConfig = new PomRewriteConfiguration();
        }

        for ( Iterator it = candidates.iterator(); it.hasNext(); )
        {
            MavenProject dependencyProject = (MavenProject) it.next();

            File projectDir = null;
            
            // LWT, 23JUL06, syntax that works with JDK's before 1.5
            if ( request != null && request.isUseLatestProjectSources() && dependencyProject.getVersion().indexOf( "SNAPSHOT" ) > 0 )
            {
                projectDir = sourceResolver.resolveLatestProjectSources( dependencyProject, errors, request );
                
                //LTE: this is just a hack to get snapshots to build using the latest sources rather than the archived ones
                if ( dependencyProject.getParent() == null || dependencyProject.getParent().getArtifactId().equals( "cpkg-archive") )
                {
                    rewriteConfig.setParentArtifactId( SNAPSHOT_PARENT_ARTIFACT_ID );
                }
            }
            else
            {
                projectDir = sourceResolver.resolveProjectSources( dependencyProject, projectsDirectory,
                                                                    localRepository, errors );
                
                if( dependencyProject.getParent() != null )
                {
                    rewriteConfig.setParentArtifactId( dependencyProject.getParent().getArtifactId() );
                }
                else
                {
                    rewriteConfig.setParentArtifactId( null );
                }
            }

            if ( projectDir == null )
            {
                errors.addMessage( "Failed to resolve project sources for: " + dependencyProject.getId() );

                continue;
            }

            BuildConfiguration config = buildPrototype.copy();
            config.setBaseDirectory( projectDir );

            rewriteSourceProjectPom( projectDir, config.getPomFileName(), rewriteConfig, errors );

            if ( buildProject( dependencyProject.getId(), projectDir, config, errors ) )
            {
                String key = ArtifactUtils.versionlessKey( dependencyProject.getGroupId(), dependencyProject
                    .getArtifactId() );

                completedBuilds.add( key );
            }
        }

        if ( !errors.isEmpty() )
        {
            throw new BuildException( "While building missing dependencies:\n\n" + errors.render() );
        }
    }

    private void rewriteSourceProjectPom( File projectDir, String pomFileName, PomRewriteConfiguration rewriteConfig,
                                          MessageHolder errors )
    {
        if ( rewriteConfig == null || rewriteConfig.isEmpty() )
        {
            return;
        }
        
        String pomName = pomFileName;

        if ( pomName == null || pomName.trim().length() < 1 )
        {
            pomName = "pom.xml";
        }

        File pom = new File( projectDir, pomName );

        rewriter.rewriteOnDisk( pom, rewriteConfig, errors );
    }

    protected List orderCandidates( List candidates )
        throws BuildException
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
        throws BuildException
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
                        throw new BuildException( "Cycle detected with dependency: " + dependencyId + " of project: "
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
                        throw new BuildException( "THIS SHOULD NEVER HAPPEN!\n" + "Cycle detected with parent: "
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
        throws BuildException
    {
        Map projectMap = new HashMap();

        for ( Iterator i = candidates.iterator(); i.hasNext(); )
        {
            MavenProject project = (MavenProject) i.next();

            String id = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            // this is currently important because of the possibility for version ranges
            if ( dag.getVertex( id ) != null )
            {
                throw new BuildException( "Project '" + id + "' is duplicated in build-candidate set." );
            }

            dag.addVertex( id );

            projectMap.put( id, project );
        }

        return projectMap;
    }

    protected boolean buildProject( String projectId, File projectDir, BuildConfiguration configuration,
                                    MessageHolder errors )
    {
        InvocationResult result;
        try
        {
            result = invoker.execute( configuration );
        }
        catch ( MavenInvocationException e )
        {
            errors.addMessage( "Failed to build project: " + projectId, e );

            return false;
        }

        CommandLineException executionException = result.getExecutionException();
        if ( executionException != null )
        {
            errors.addMessage( "Failed invoke Maven to build project: " + projectId, executionException );

            return false;
        }
        else if ( result.getExitCode() != 0 )
        {
            errors.addMessage( "Build for project: " + projectId + " failed; returned exit code: "
                + result.getExitCode() );
            return false;
        }
        else
        {
            return true;
        }
    }
}
