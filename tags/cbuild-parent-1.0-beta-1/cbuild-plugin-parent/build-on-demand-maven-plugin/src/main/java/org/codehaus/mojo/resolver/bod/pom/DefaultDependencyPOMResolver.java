package org.codehaus.mojo.resolver.bod.pom;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.tools.project.extras.ProjectReleaseInfoUtils;
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

    public List < MavenProject > resolveDependencyPoms( MavenProject project,
        List < MavenProject > reactorProjects, ArtifactRepository localRepository,
        Set < String > previousBuilds ) throws BuildOnDemandResolutionException
    {
        Set < String > completedBuilds = previousBuilds;

        if ( completedBuilds == null )
        {
            completedBuilds = new HashSet < String > ();
        }

        MessageHolder errors = new DefaultMessageHolder();

        getLogger().debug( "Resolving build candidates from master project: " + project.getId() );
        
        Map < String, MavenProject > buildCandidates = resolveBuildCandidateMap( project, localRepository, errors );

        // TODO: test build exception when candidate resolution fails.
        if ( !errors.isEmpty() )
        {
            throw new BuildOnDemandResolutionException( "While collecting build candidates:\n\n" + errors.render() );
        }

        if ( buildCandidates == null || buildCandidates.isEmpty() )
        {
            return Collections.emptyList();
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
            throw new BuildOnDemandResolutionException(
                "While removing build candidates with existing binary artifacts:\n\n"
                + errors.render() );
        }

        if ( buildCandidates.isEmpty() )
        {
            getLogger().info( "Nothing to build for project: " + project.getId() );

            return Collections.emptyList();
        }
        else
        {
            getLogger().info( "Found " + buildCandidates.size() + " build candidates for project: " + project.getId() );

            return new ArrayList < MavenProject > ( buildCandidates.values() );
        }
    }

    protected void removeCompletedBuilds( Map < String, MavenProject > buildCandidates, Set < String > completedBuilds )
    {
        for ( Iterator < String > it = completedBuilds.iterator(); it.hasNext(); )
        {
            String id = it.next();

            buildCandidates.remove( id );
        }
    }

    protected Map < String, MavenProject > resolveBuildCandidateMap( MavenProject project,
        ArtifactRepository localRepository, MessageHolder errors )
        throws BuildOnDemandResolutionException
    {
        // copied from DefaultArtifactResolver (just the listener config)
        List < ResolutionListener > listeners = new ArrayList < ResolutionListener > ();

        if ( getLogger().isDebugEnabled() )
        {
            listeners.add( new DebugResolutionListener( getLogger() ) );
        }

        listeners.add( new WarningResolutionListener( getLogger() ) );

        ArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_TEST );

        Map < String, MavenProject > dependencyPoms = new HashMap < String, MavenProject > ();

        Artifact projectArtifact = project.getArtifact();

        if ( projectArtifact == null )
        {
            projectArtifact = artifactFactory.createBuildArtifact( project.getGroupId(), project.getArtifactId(),
                                                                   project.getVersion(), "pom" );
        }
        
        getLogger().debug( "Project artifact is: " + projectArtifact );

        Set < Artifact > dependencyArtifacts = project.getDependencyArtifacts();

        if ( dependencyArtifacts == null )
        {
            try
            {
                dependencyArtifacts = project.createArtifacts( artifactFactory, null, filter );
            }
            catch ( InvalidDependencyVersionException e )
            {
                errors.addMessage( "Failed to create dependency artifacts for: " + project.getId(), e );

                return Collections.emptyMap();
            }
        }
        
        int preResolveErrorSize = errors.size();
        
        ArtifactResolutionResult result = null;
        try
        {
            // FIXME: Re-enable this!
//            dependencyArtifacts = injectImpliedVersionRanges( dependencyArtifacts );
//            project.setDependencyArtifacts( dependencyArtifacts );
            
            getLogger().debug( "Direct dependencies: " + dependencyArtifacts );

            result = artifactCollector.collect( dependencyArtifacts,
                                                projectArtifact,
                                                //Collections.EMPTY_MAP,
                                                localRepository,
                                                project.getRemoteArtifactRepositories(),
                                                metadataSource,
                                                filter,
                                                listeners
                                                //, null
                                                );
        }
        catch ( ArtifactResolutionException e )
        {
            getLogger().info( "Failed to resolve dependency POMs for: " + project.getId() );
            errors.addMessage( "Failed to resolve dependency POMs for: " + project.getId(), e );
        }
//        catch ( InvalidVersionSpecificationException e )
//        {
//            getLogger().info( "Failed to format implied version ranges for: " + project.getId() );
//            errors.addMessage( "Failed to format implied version ranges for: " + project.getId(), e );
//        }
        
        if ( errors.size() != preResolveErrorSize )
        {
            throw new BuildOnDemandResolutionException(
                "Failed to resolve one or more dependency POMs.\n\n" + errors.render() );
        }

        if ( result != null )
        {
            dependencyPoms.putAll( createProjectInstances( result.getArtifactResolutionNodes(),
                localRepository, errors ) );
        }
        
        getLogger().debug( "Resolved " + dependencyPoms.size() + " dependency POMs: " + dependencyPoms );

        return dependencyPoms;
    }

    private Set < Artifact > injectImpliedVersionRanges( Set < Artifact > dependencyArtifacts )
        throws InvalidVersionSpecificationException
    {
        Set < Artifact > results = new LinkedHashSet < Artifact > ();
        
        if ( dependencyArtifacts != null )
        {
            for ( Iterator < Artifact > it = dependencyArtifacts.iterator(); it.hasNext(); )
            {
                Artifact dependencyArtifact = it.next();
                String version = dependencyArtifact.getVersion();
                
                version = ProjectReleaseInfoUtils.formatImpliedReleaseNumberVersionRanges( version );
                
                //Artifact createDependencyArtifact( String groupId, String artifactId, 
                //                                   VersionRange versionRange, String type, 
                //                                   String classifier, String scope, boolean optional );

                Artifact result =
                    artifactFactory.createDependencyArtifact( dependencyArtifact.getGroupId(),
                                                              dependencyArtifact.getArtifactId(),
                                                              VersionRange.createFromVersionSpec( version ),
                                                              dependencyArtifact.getType(),
                                                              dependencyArtifact.getClassifier(),
                                                              dependencyArtifact.getScope(),
                                                              null,
                                                              dependencyArtifact.isOptional() );
                
                result.setArtifactHandler( dependencyArtifact.getArtifactHandler() );
                
                results.add( result );
            }
        }
        
        return results;
    }

    protected Map < String, MavenProject > createProjectInstances(
        Set < ResolutionNode > artifactResolutionNodes,
        ArtifactRepository localRepository, MessageHolder errors )
    {
        if ( artifactResolutionNodes == null || artifactResolutionNodes.isEmpty() )
        {
            return Collections.emptyMap();
        }

        Map < String, MavenProject > projectsByVersionlessId = new HashMap
            < String, MavenProject > ( artifactResolutionNodes.size() );

        for ( Iterator < ResolutionNode > it = artifactResolutionNodes.iterator(); it.hasNext(); )
        {
            ResolutionNode node = (ResolutionNode) it.next();
            
            Artifact projectArtifact = node.getArtifact();
            List < ArtifactRepository > remoteRepositories = node.getRemoteRepositories();
            
            String id = ArtifactUtils.versionlessKey( projectArtifact );

            try
            {
                MavenProject project = projectBuilder.buildFromRepository( projectArtifact,
                    remoteRepositories, localRepository );

                projectsByVersionlessId.put( id, project );
            }
            catch ( ProjectBuildingException e )
            {
                errors.addMessage( "Failed to build project: " + id, e );
            }
        }

        return projectsByVersionlessId;
    }

    protected void removeBuildsInProgress( Map < String, MavenProject > buildCandidatesByVersionlessId,
        List < MavenProject > currentProjects )
    {
        if ( currentProjects == null || currentProjects.isEmpty() )
        {
            return;
        }

        for ( Iterator < MavenProject > it = currentProjects.iterator(); it.hasNext(); )
        {
            MavenProject project = it.next();

            buildCandidatesByVersionlessId.remove( ArtifactUtils.versionlessKey( project.getGroupId(), project
                .getArtifactId() ) );
        }
    }


}
