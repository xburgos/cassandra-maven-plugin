package org.codehaus.mojo.resolver.bod.binary;

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

import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public abstract class AbstractOSBinaryDependencyManager
    implements BinaryDependencyManager, LogEnabled
{

    /**
     * @plexus.requirement
     */
    private ArtifactResolver artifactResolver;

    private Logger logger;

    protected AbstractOSBinaryDependencyManager( ArtifactResolver resolver )
    {
        this.artifactResolver = resolver;
    }

    protected AbstractOSBinaryDependencyManager()
    {
        // used for Plexus initialization.
    }

    // TODO: unit test all-clear code path
    public void findDependenciesWithMissingBinaries( List < MavenProject > dependencyProjects, ArtifactRepository localRepository )
        throws BuildOnDemandResolutionException
    {
        // TODO: unit test the case where no candidates are pending...all are cached somehow.
        if ( dependencyProjects == null || dependencyProjects.isEmpty() )
        {
            return;
        }

        MessageHolder errors = new DefaultMessageHolder();

        for ( Iterator < MavenProject > it = dependencyProjects.iterator(); it.hasNext(); )
        {
            MavenProject project = it.next();

            if ( isDependencyInstalledOnSystem( project ) )
            {
                it.remove();
                continue;
            }

            // TODO: is it possible that this artifact will ever be null?
            Artifact projectArtifact = project.getArtifact();
            try
            {
                artifactResolver.resolve( projectArtifact, project.getRemoteArtifactRepositories(), localRepository );

                // TODO: unit test the case where the artifact still isn't resolved
                // TODO: unit test the case where the artifact's file is null
                // TODO: unit test the case where the artifact is resolved with an existing file.
                if ( projectArtifact.isResolved() && projectArtifact.getFile() != null
                                && installDependencyOnSystem( project ) )
                {
                    it.remove();
                }
            }
            // TODO: unit test
            catch ( ArtifactResolutionException e )
            {
                // if this happens, we need to be more careful...if it's a transfer problem,
                // we should probably fail.
                Throwable cause = e.getCause();

                // TODO: unit test TransferFailedException
                if ( cause != null && ( cause instanceof TransferFailedException ) )
                {
                    errors.addMessage( "Transfer of artifact: " + project.getId() + " failed.", e );
                }
                // TODO: unit test non-TransferFailedException or null cause
                else
                {
                    getLogger().debug(
                                       "Failed to resolve artifact: " + project.getId() + "; it will need to be built.",
                                       e );
                }
            }
            // TODO: unit test
            catch ( ArtifactNotFoundException e )
            {
                // if this happens, we couldn't resolve it; we'll need to build it.
                getLogger().debug( "Failed to resolve artifact: " + project.getId()
                    + "; it will need to be built.", e );
            }
        }
    }

    /**
     * Extension point for this binary-dependency manager. This is to be used to install the dependency on the system,
     * once it's been resolved in the Maven repository.
     * 
     * An example would be a dependency whose RPM is installed in the RPM database.
     * 
     * @param project
     *            The dependency project
     * @return true if the dependency exists on the system, outside of the Maven repository; else false.
     */
    protected abstract boolean installDependencyOnSystem( MavenProject project )
        throws BuildOnDemandResolutionException;

    /**
     * Extension point for this binary-dependency manager. This is to be used to determine whether the dependency exists
     * on the system somewhere that is reachable for the build, regardless of whether it's in the Maven repository
     * system (local repository, remote repositories).
     * 
     * An example would be a dependency whose RPM is installed in the RPM database.
     * 
     * @param project
     *            The dependency project
     * @return true if the dependency exists on the system, outside of the Maven repository; else false.
     */
    protected abstract boolean isDependencyInstalledOnSystem( MavenProject project )
        throws BuildOnDemandResolutionException;

    protected ArtifactResolver getArtifactResolver()
    {
        return artifactResolver;
    }

    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "BinaryDependencyManager:internal" );
        }

        return logger;
    }

    public void enableLogging( Logger log )
    {
        this.logger = log;
    }
}
