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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * If you have an artifact of package type like <code>pom</code> or <code>jar</code>, DerivedArtifact can derive
 * an artifact of another packaging type like <code>rpm</code> or classifier like <code>patches</code>.
 *
 */
public class DerivedArtifact extends DefaultArtifact
{

    /**
     * If you have an artifact of package type like <code>pom</code> or <code>jar</code>, DerivedArtifact can derive
     * an artifact of another packaging type like <code>rpm</code> or classifier like <code>patches</code>.
     * 
     * @param parentArtifact This is the artifact which will be used to derive another artifact
     * @param classifier The desired classifier for the derived artifact like <code>patches</code> or 
     *        <code>osx5.i386</code>.
     * @param type The derived packaging type for the derived artifact like <code>rpm</code> or <code>jar</code>
     * @param handler The ArtifactHandler for the derived artifact
     */
    public DerivedArtifact( Artifact parentArtifact, String classifier, String type, ArtifactHandler handler )
    {
        super( parentArtifact.getGroupId(), parentArtifact.getArtifactId(),
            parentArtifact.getVersionRange(),
            parentArtifact.getScope(), type, classifier, handler, false );
        
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

    /**
     * If you have an artifact of package type like <code>pom</code> or <code>jar</code>, DerivedArtifact can derive
     * an artifact of another packaging type like <code>rpm</code> or classifier like <code>patches</code>.
     * 
     * @param parentArtifact This is the artifact which will be used to derive another artifact
     * @param version The version of the parent and derived artifact
     * @param classifier The desired classifier for the derived artifact like <code>patches</code> or 
     *        <code>osx5.i386</code>.
     * @param type The derived packaging type for the derived artifact like <code>rpm</code> or <code>jar</code>
     * @param handler The ArtifactHandler for the derived artifact
     */
    public DerivedArtifact( Artifact parentArtifact, String version,
        String classifier, String type, ArtifactHandler handler )
    {
        super( parentArtifact.getGroupId(), parentArtifact.getArtifactId(),
               VersionRange.createFromVersion( version ),
               parentArtifact.getScope(), type, classifier, handler, false );
        
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
