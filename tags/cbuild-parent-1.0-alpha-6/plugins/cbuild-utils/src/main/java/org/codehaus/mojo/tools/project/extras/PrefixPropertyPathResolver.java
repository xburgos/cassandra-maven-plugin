package org.codehaus.mojo.tools.project.extras;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
//import org.codehaus.plexus.interpolation.InterpolationException;

public class PrefixPropertyPathResolver
    implements ArtifactPathResolver
{

    private final MavenProjectBuilder projectBuilder;

    private final List remoteRepositories;

    private final ArtifactRepository localRepository;

    private final Log log;

    private final ArtifactFactory artifactFactory;

    public PrefixPropertyPathResolver( MavenProjectBuilder projectBuilder, List remoteRepositories,
                                       ArtifactRepository localRepository, ArtifactFactory artifactFactory, 
                                       Log log )
    {
        this.projectBuilder = projectBuilder;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
        this.log = log;
    }

    public File resolve( Artifact artifact )
        throws PathResolutionException
    {
        MavenProject project;
        try
        {
            Artifact dummy = artifactFactory.createProjectArtifact( artifact.getGroupId(),
                artifact.getArtifactId(), artifact.getVersion() );
            
            project = projectBuilder.buildFromRepository( dummy,
                remoteRepositories, localRepository );
        }
        catch ( ProjectBuildingException e )
        {
            throw new PathResolutionException( "Cannot build project for artifact: "
                + artifact + ". Reason: " + e.getMessage(), e );
        }
        
        String prefix = project.getProperties().getProperty( "prefix" );
        
        List valueSources = new ArrayList();
        valueSources.add( new ObjectBasedValueSource( project ) );
        valueSources.add( new MapBasedValueSource( project.getProperties() ) );
        valueSources.add( new MapBasedValueSource( System.getProperties() ) );

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator( valueSources );

        try
        {
            prefix = interpolator.interpolate( prefix, "project|pom" );
        }
        catch ( org.codehaus.plexus.interpolation.InterpolationException e )
        {
            throw new PathResolutionException( "Cannot build project for artifact: "
                + artifact + ". Reason: " + e.getMessage(), e );
        }
        
        log.debug( "Prefix for project: " + project.getId() + " is: " + prefix );
        
        return new File( prefix );
    }

}
