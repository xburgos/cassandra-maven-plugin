package org.codehaus.mojo.resolver.bod.pom;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * @plexus.component role="org.codehaus.mojo.resolver.bod.pom.DependencyPOMResolver" 
 *            role-hint="default"
 *            
 * @author jdcasey
 *
 */
public class DefaultDependencyPOMResolver
    extends AbstractLogEnabled
    implements DependencyPOMResolver
{

    public static final String ROLE_HINT = "default";

    /**
     * @plexus.requirement
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * @plexus.requirement
     */
    private ArtifactCollector artifactCollector;

    public DefaultDependencyPOMResolver( ArtifactMetadataSource metadataSource, MavenProjectBuilder projectBuilder,
                                          ArtifactFactory artifactFactory, ArtifactCollector artifactCollector,
                                          int logLevel )
    {
        this.metadataSource = metadataSource;
        this.projectBuilder = projectBuilder;
        this.artifactFactory = artifactFactory;
        this.artifactCollector = artifactCollector;

        enableLogging( new ConsoleLogger( logLevel, "default" ) );
    }

    public DefaultDependencyPOMResolver()
    {
    }

    public List resolveDependencyPoms( MavenProject project, List reactorProjects, ArtifactRepository localRepository,
                                   Set previousBuilds )
        throws BuildOnDemandResolutionException
    {
        Set completedBuilds = previousBuilds;

        if ( completedBuilds == null )
        {
            completedBuilds = new HashSet();
        }

        MessageHolder errors = new DefaultMessageHolder();

        getLogger().debug( "Resolving build candidates from master project: " + project.getId() );
        
        Map buildCandidates = resolveBuildCandidateMap( project, localRepository, errors );

        // TODO: test build exception when candidate resolution fails.
        if ( !errors.isEmpty() )
        {
            throw new BuildOnDemandResolutionException( "While collecting build candidates:\n\n" + errors.render() );
        }

        if ( buildCandidates == null || buildCandidates.isEmpty() )
        {
            return Collections.EMPTY_LIST;
        }

        getLogger().debug( "Removing builds which are already in progress..." );
        
        removeBuildsInProgress( buildCandidates, reactorProjects );
        
        getLogger().debug( "...done. Remaining build candidates: " + buildCandidates );

        getLogger().debug( "Removing builds which have already completed elsewhere..." );
        
        removeCompletedBuilds( buildCandidates, completedBuilds );
        
        getLogger().debug( "...done. Remaining build candidates: " + buildCandidates );

        errors = new DefaultMessageHolder();

        // TODO: Low-priority...test when we're not running in force-source-builds mode.
        // [jdcasey; 1-17-07] commenting this out for consolidation with the rpm resolution process.
//        if ( !force )
//        {
//            getLogger().debug( "Removing builds with existing binaries..." );
//            
//            removeCandidatesWithExistingBinaries( buildCandidates, localRepository, errors );
//            
//            getLogger().debug( "...done. Remaining build candidates: " + buildCandidates );
//        }

        // TODO: test build exception when binaries resolution fails.
        if ( !errors.isEmpty() )
        {
            throw new BuildOnDemandResolutionException( "While removing build candidates with existing binary artifacts:\n\n"
                + errors.render() );
        }

        if ( buildCandidates.isEmpty() )
        {
            getLogger().info( "Nothing to build for project: " + project.getId() );

            return Collections.EMPTY_LIST;
        }
        else
        {
            getLogger().info( "Found " + buildCandidates.size() + " build candidates for project: " + project.getId() );

            return new ArrayList( buildCandidates.values() );
        }
    }

    protected void removeCompletedBuilds( Map buildCandidates, Set completedBuilds )
    {
        for ( Iterator it = completedBuilds.iterator(); it.hasNext(); )
        {
            String id = (String) it.next();

            buildCandidates.remove( id );
        }
    }

    protected Map resolveBuildCandidateMap( MavenProject project, ArtifactRepository localRepository,
                                            MessageHolder errors )
    {
        // copied from DefaultArtifactResolver (just the listener config)
        List listeners = new ArrayList();

        if ( getLogger().isDebugEnabled() )
        {
            listeners.add( new DebugResolutionListener( getLogger() ) );
        }

        listeners.add( new WarningResolutionListener( getLogger() ) );

        ArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_TEST );

        Map candidates = new HashMap();

        Artifact projectArtifact = project.getArtifact();

        if ( projectArtifact == null )
        {
            projectArtifact = artifactFactory.createBuildArtifact( project.getGroupId(), project.getArtifactId(),
                                                                   project.getVersion(), "pom" );
        }
        
        getLogger().debug( "Project artifact is: " + projectArtifact );

        Set dependencyArtifacts = project.getDependencyArtifacts();

        if ( dependencyArtifacts == null )
        {
            try
            {
                dependencyArtifacts = project.createArtifacts( artifactFactory, null, filter );
            }
            catch ( InvalidDependencyVersionException e )
            {
                errors.addMessage( "Failed to create dependency artifacts for: " + project.getId(), e );

                return Collections.EMPTY_MAP;
            }
        }
        
        getLogger().debug( "Direct dependencies: " + dependencyArtifacts );

        ArtifactResolutionResult result = null;
        try
        {
            result = artifactCollector.collect( dependencyArtifacts, projectArtifact, localRepository, project
                .getRemoteArtifactRepositories(), metadataSource, filter, listeners );
        }
        catch ( ArtifactResolutionException e )
        {
            getLogger().info( "Failed to resolve POM for: " + project.getId() );
            errors.addMessage( "Failed to resolve POM for: " + project.getId(), e );
        }

        if ( result != null )
        {
            candidates.putAll( createProjectInstances( result.getArtifactResolutionNodes(), localRepository, errors ) );
        }
        
        getLogger().debug( "Resolved " + candidates.size() + " candidates: " + candidates );

        return candidates;
    }

    protected Map createProjectInstances( Set artifactResolutionNodes, ArtifactRepository localRepository, MessageHolder errors )
    {
        if ( artifactResolutionNodes == null || artifactResolutionNodes.isEmpty() )
        {
            return Collections.EMPTY_MAP;
        }

        Map projectsByVersionlessId = new HashMap( artifactResolutionNodes.size() );

        for ( Iterator it = artifactResolutionNodes.iterator(); it.hasNext(); )
        {
            ResolutionNode node = (ResolutionNode) it.next();
            
            Artifact projectArtifact = node.getArtifact();
            List remoteRepositories = node.getRemoteRepositories();
            
            String id = ArtifactUtils.versionlessKey( projectArtifact );

            try
            {
                MavenProject project = projectBuilder.buildFromRepository( projectArtifact, remoteRepositories, localRepository );

                projectsByVersionlessId.put( id, project );
            }
            catch ( ProjectBuildingException e )
            {
                errors.addMessage( "Failed to build project: " + id, e );
            }
        }

        return projectsByVersionlessId;
    }

    protected void removeBuildsInProgress( Map buildCandidatesByVersionlessId, List currentProjects )
    {
        if ( currentProjects == null || currentProjects.isEmpty() )
        {
            return;
        }

        for ( Iterator it = currentProjects.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();

            buildCandidatesByVersionlessId.remove( ArtifactUtils.versionlessKey( project.getGroupId(), project
                .getArtifactId() ) );
        }
    }


}
