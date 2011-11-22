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

public class DerivedArtifact extends DefaultArtifact
{

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
