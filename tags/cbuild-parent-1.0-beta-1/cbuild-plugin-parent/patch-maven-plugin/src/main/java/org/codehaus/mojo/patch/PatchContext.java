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

/**
 * Rewrote object to determine state by inspecting the target directory instead of using
 * The cookie get/set mechanism of "BuildAdvisor".  BuildAdvisor was implemented with Maven 3.0
 * in mind using a MavenSession get/set mechanism.  CBUILDS is being re-written without 
 * Maven 3.0 required.
 * 
 * The Maven goals of patch-maven-plugin need to know if the patches were retrieved 
 * from a remote repository or found locally.  For instance, if found locally, the plugins
 * will bundle the patches used into an Attached Artifact for later release/deployment.
 * 
 * @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
 */
public class PatchContext
{
   /**
     * A helper class to determine if patch-maven-plugin is getting its patches from
     * a maven repository or a local filesystem directory associated with the pom file.
     */
    public PatchContext()
    {
    }

    /**
     * Take a look at the likely directories for patches and report if one of them looks
     * like a valid patch directory containing patches.
     * 
     * @param patchArtifactUnpackDirectory where a patch would be unpacked if downloaded from a repo
     * @param patchDirectory used when in development when the patch is not yet in a repo
     * @return If unpacked from repo or <code>patchDirectory</code> exists, method returns true
     */
    public boolean hasPatchDirectory( File patchArtifactUnpackDirectory, File patchDirectory )
    {
        return ( isPatchArtifactResolved( patchArtifactUnpackDirectory )
            || ( patchDirectory.exists() && patchDirectory.list().length > 0 ) );
    }

    /**
     * Determine which directory this project will retrieve its patches from.
     * 
     * @param patchArtifactUnpackDirectory where a patch would be unpacked if downloaded from a repo
     * @param patchDirectory used when in development when the patch is not yet in a repo
     * @return Directory to use for the patch
     */
    public File getPatchDirectory( File patchArtifactUnpackDirectory, File patchDirectory )
    {
        return isPatchArtifactResolved( patchArtifactUnpackDirectory )
            ? patchArtifactUnpackDirectory : patchDirectory;
    }

    /**
     * Resolved in this case means downloaded from a maven repository.  This is determined by
     * looking at the target directory of the project reporting if files exist in the patch
     * unpack directory.  Files there are indicative of a downloaded patch bundle.
     * 
     * @param patchArtifactUnpackDirectory where a patch would be unpacked if downloaded from a repo
     * @return will be true if the <code>patchArtifactUnpackDirectory</code> exists
     */
    public boolean isPatchArtifactResolved( File patchArtifactUnpackDirectory )
    {
        return ( patchArtifactUnpackDirectory.exists()
            && patchArtifactUnpackDirectory.list().length > 0 );
    }

}
