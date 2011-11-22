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
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
//import org.codehaus.plexus.interpolation.InterpolationException;

/**
 * This object is used to convert an expression like <code>@pathOf(org.codehaus.mojo:mojo-parent)@</code>
 * into the value of the property <code>prefix</code> for the project listed.
 */
public class PrefixPropertyPathResolver
    implements ArtifactPathResolver
{

    /**
     * Maven object used to help find the artifact's <code>prefix</code> property
     */
    private final MavenProjectBuilder projectBuilder;

    /**
     * List of maven remote repositories used to resolve the artifact to be queried
     */
    private final List < ArtifactRepository > remoteRepositories;

    /**
     * The maven users's local artifact repository
     */
    private final ArtifactRepository localRepository;

    /**
     * Plexus logger
     */
    private final Log log;

    /**
     * Maven object used to help find the artifact's <code>prefix</code> property
     */
    private final ArtifactFactory artifactFactory;

    /**
     * The object constructor.
     * This object is used to convert an expression like <code>@pathOf(org.codehaus.mojo:mojo-parent)@</code>
     * into the value of the property <code>prefix</code> for the project listed.
     * 
     * @param projectBuilder A MavenProjectBuilder plexus component
     * @param remoteRepositories The remote properties that can be used to find the project
     * @param localRepository The local repository which may have the project
     * @param artifactFactory A ArtifactFactory plexus component
     * @param log The logger so this object can log messages
     */
    public PrefixPropertyPathResolver( MavenProjectBuilder projectBuilder, 
                                       List < ArtifactRepository > remoteRepositories,
                                       ArtifactRepository localRepository, 
                                       ArtifactFactory artifactFactory, 
                                       Log log )
    {
        this.projectBuilder = projectBuilder;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
        this.log = log;
    }

    /**
     * This object is used to convert an expression like <code>@pathOf(org.codehaus.mojo:mojo-parent)@</code>
     * into the value of the property <code>prefix</code> for the project listed.
     *
     * @param artifact Artifact to look up the </code>prefix</code> property which is used to resolve
     *        <code>@pathOf()@</code>
     * @throws PathResolutionException thrown if method can not resolve artifact <code>prefix</code> property
     * @return File object returned which is the installation directory of the dependency, which is called
     *         <code>prefix</code> in GNU coding standards.
     */
    public File resolve( Artifact artifact ) throws PathResolutionException
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
        
        List < AbstractValueSource > valueSources = new ArrayList < AbstractValueSource > ();
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
