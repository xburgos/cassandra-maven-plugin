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
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.tools.fs.archive.ArchiverManagerUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * @goal package-patches
 * @phase package
 * @author jdcasey
 *
 */
public class PackagePatchesMojo extends AbstractPatchMojo
{
    
    /**
     * Classifier is a suffix in the filename, but it is before the filename externsion.
     *
     * @parameter default-value="patches"
     * @required
     */
    private String patchArtifactClassifier;

    /**
     * The filename extension, typically ".zip"
     *
     * @parameter default-value="zip"
     * @required
     */
    private String patchArtifactType;

    /**
     * Handling mode for long file paths.
     * 
     * @parameter default-value="gnu"
     */
    private String tarLongFileMode;
    
    /**
     * The name of the patch.  Usually the same name as the project.
     *
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;
    
    /**
     * The patch version after it is created, usually the same as the project version
     *
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     */
    private String version;
    
    /**
     * The directory where to write the bundle of patches.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File patchArchiveDestDir;
    
    /**
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;
    
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        PatchContext ctx = new PatchContext( this.getSession(), buildAdvisor );
        
        if ( ctx.isPatchArtifactResolved() )
        {
            getLog().debug( "Skipping patch-package step, since project patches were resolved at the beginning of the build." );
            return;
        }
        else
        {
            File patchDir = ctx.getPatchDirectory();
            
            File patchArchive = archivePatchArtifact( patchDir );
            
            projectHelper.attachArtifact( getProject(), patchArtifactType, patchArtifactClassifier, patchArchive );
            
            // find the attachment we just made...
            List attachments = getProject().getAttachedArtifacts();
            for ( Iterator it = attachments.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();
                
                if ( patchArtifactClassifier.equals( artifact.getClassifier() ) && patchArtifactType.equals( artifact.getType() ) )
                {
                    ctx.setPatchArtifact( artifact );
                }
            }
        }
        
        ctx.store();
    }

    private File archivePatchArtifact( File patchDir ) throws MojoExecutionException
    {
        File destFile = new File( patchArchiveDestDir, artifactId + "-" + version + "-" + patchArtifactClassifier + "." + patchArtifactType );
        
        Archiver archiver;
        
        try
        {
            Map options = Collections.singletonMap( ArchiverManagerUtils.TAR_LONG_FILE_MODE_OPTION, tarLongFileMode );
            
            archiver = ArchiverManagerUtils.getArchiver( archiverManager, patchArtifactType, options );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Cannot find archiver for artifact type: " + patchArtifactType, e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Cannot configure archiver for artifact type: " + patchArtifactType, e );
        }
        
        archiver.setDestFile( destFile );
        
        try
        {
            String[] excludes = new String[0];
            if ( useDefaultIgnores() )
            {
                excludes = (String[]) DEFAULT_IGNORED_PATCH_PATTERNS.toArray( new String[ DEFAULT_IGNORED_PATCH_PATTERNS.size() ] );
            }
            
            archiver.addDirectory( patchDir, new String[]{ "**" }, excludes );
            archiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to archive patch-source directory.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to archive patch-source directory.", e );
        }
        
        return destFile;
    }

}
