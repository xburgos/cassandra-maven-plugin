package org.codehaus.mojo.patch;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.apache.maven.execution.MavenSession;

public class PatchContext
{

    //private static final String PATCH_CONTAINER_CONTEXT_KEY = "patch-container:contextKey";

    private static final String PATCH_ARTIFACT_WAS_RESOLVED_KEY = "patch-artifact-was-resolved";
    
    private static final String PATCH_DIRECTORY_KEY = "patch-directory";
    
    private static final String PATCH_ARTIFACT_KEY = "patch-artifact";

    private boolean patchArtifactResolved;
    private boolean patchArtifactResolvedStale;
    
    private File patchDirectory;
    private boolean patchDirectoryStale;

    private Artifact patchArtifact;
    private boolean patchArtifactStale;

    private MavenSession session;

    private BuildAdvisor ba;

    public PatchContext( MavenSession session, BuildAdvisor buildAdvisor )
    {
        this.session = session;
        ba = buildAdvisor;
        patchArtifactResolvedStale = patchDirectoryStale = patchArtifactStale = false;
        patchArtifactResolved = Boolean.TRUE.equals((Boolean) ba.retrieve( session, PATCH_ARTIFACT_WAS_RESOLVED_KEY ));
        patchDirectory = (File) ba.retrieve( session, PATCH_DIRECTORY_KEY );
        patchArtifact = (Artifact) ba.retrieve( session, PATCH_ARTIFACT_KEY );
    }
    
    public boolean hasPatchDirectory()
    {
        return patchDirectory != null;
    }
    
    public void setPatchDirectory( File patchDirectory )
    {
        this.patchDirectory = patchDirectory;
        this.patchDirectoryStale = true;
    }
    
    public File getPatchDirectory()
    {
        return patchDirectory;
    }

    public void setPatchArtifactResolved( boolean patchArtifactResolved )
    {
        this.patchArtifactResolved = patchArtifactResolved;
        this.patchArtifactResolvedStale = true;
    }

    public boolean isPatchArtifactResolved()
    {
        return patchArtifactResolved;
    }

    public void store()
    {
        // Try to write on the MavenSession lightly...
        if ( patchArtifactResolvedStale == true)
        {
            Boolean mybool = new Boolean(true);
            ba.store( this.session, PATCH_ARTIFACT_WAS_RESOLVED_KEY, mybool );
        }
        if ( patchDirectoryStale == true)
        {
            ba.store( this.session, PATCH_DIRECTORY_KEY, patchDirectory );
        }
        if ( patchArtifactStale == true)
        {
            ba.store( this.session, PATCH_ARTIFACT_KEY, patchArtifact );
        }
    }

    public void setPatchArtifact( Artifact patchArtifact )
    {
        this.patchArtifact = patchArtifact;
        this.patchArtifactStale = true;
    }

    public Artifact getPatchArtifact()
    {
        return patchArtifact;
    }
}
