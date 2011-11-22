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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.tools.project.extras.DerivedArtifact;

/**
 * Goal will remove a patch artifact fro the local repository
 * 
 * @goal purge-local-patch-artifact
 * @phase install
 * @author jdcasey
 *
 */
public class PurgePatchArtifactFromLocalRepoMojo extends AbstractPatchMojo
{
    
    /**
     * Your local maven repository
     * 
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Classifier is a suffix in the filename, but it is before the filename extension.  A 
     * typical patch artifact will typically have the name similar to 
     * <code>ProjName-1.2.3-patches.tar.gz</code>.
     *
     * @parameter default-value="patches"
     * @since 1.0-beta-1
     * @required
     */
    private String patchArtifactClassifier;

    /**
     * The filename extension for your patch artifact, typically ".tar.gz"
     *
     * @parameter default-value="tar.gz"
     * @since 1.0-beta-1
     * @required
     */
    private String patchArtifactType;

    /**
     * The project artifact which will be used to derive the name of your patch artifact.
     * 
     * @parameter default-value="${project.artifact}"
     * @required
     * @since 1.0-beta-1
     * @readonly
     */
    private Artifact projectArtifact; 

    /**
     * Removes a archive of patch files from the local repo
     * 
     * @throws MojoExecutionException not thrown, inherited from abstract class
     * @throws MojoFailureException not thrown, inherited from abstract class
     */
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        ArtifactHandler handler = new DefaultArtifactHandler( patchArtifactType );
        
        Artifact patchArtifact =
            new DerivedArtifact( projectArtifact, patchArtifactClassifier, patchArtifactType, handler );
        
        if ( patchArtifact != null )
        {
            String relativePath = localRepository.pathOf( patchArtifact );
            
            File patchArtifactLocalRepoFile = new File( localRepository.getBasedir(), relativePath );
            
            getLog().debug( "trying to purge: " + patchArtifactLocalRepoFile.getAbsolutePath() );
            
            if ( patchArtifactLocalRepoFile.exists() )
            {
                getLog().debug( "Purging: " + patchArtifact + " from local repository at: "
                                + localRepository.getBasedir() );
                patchArtifactLocalRepoFile.delete();
            }
        }
    }

}
