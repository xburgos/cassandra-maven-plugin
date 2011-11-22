package org.codehaus.mojo.remotesrc;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Retrieve project sources from some external URL, and unpack them (into target, by default).
 * Dependency resolution is required here to keep from downloading a potentially huge source tarball
 * if the dependencies cannot be satisfied.
 * 
 * @goal resolve
 * @phase initialize
 */
public class GetSourceArtifactMojo
    extends AbstractRemoteSourceMojo
{

    /**
     * Whether to skip source retrieval.
     * 
     * @parameter default-value="false" alias="remote.source.skip"
     */
    private boolean skipGet;

    /**
     * The groupId of this project, so we can retrieve the source artifact for it. 
     * 
     * @parameter default-value="${project.groupId}"
     * @required
     * @readonly
     */
    private String groupId;

    /**
     * The artifactId of this project, so we can retrieve the source artifact for it. 
     * 
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;

    /**
     * The version of this project, so we can retrieve the source artifact for it. 
     * 
     * @parameter default-value="${project.version}"
     * @required
     * @readonly
     */
    private String version;
    
    /**
     * The classifier for the source artifact, defaults to "sources".
     * 
     * @parameter default-value="sources"
     */
    private String classifier;
    
    /**
     * The type of artifact we're trying to download here. 
     * Defaults to "tar.gz" for compressed tarballs.
     * 
     * @parameter default-value="tar.gz"
     */
    private String type;
    
    /**
     * Component used to resolve artifacts.
     * 
     * @component
     * @required
     * @readonly
     */
    private ArtifactResolver resolver;
    
    /**
     * Component used to create an Artifact instance for the source artifact we need to resolve.
     * 
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory factory;
    
    /**
     * The list of remote repositories to check for this source archive.
     * 
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;
    
    /**
     * The local repository to use for caching this source artifact.
     * 
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * An optional File destination to copy the resolved artifact into, for 
     * handling in a shell script or something.
     * 
     * @parameter
     */
    private File copyTo;
    
    /**
     * Perform the source archive retrieval, and unpack it.
     * 
     * This involves:
     * 
     * o 
     * o Setting up the transfer monitor (displays progress for the download)
     * o Download the archive to a temp file
     * o 
     * @throws MojoFailureException 
     */
    public void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Resolving source archive for project: " + getProjectId() + " from: {repository}." );
        
        if ( StringUtils.isEmpty( classifier ) || StringUtils.isEmpty( type ) )
        {
            throw new MojoExecutionException( "You must specify both \'classifier\' and \'type\' parameters.\nSource artifact resolution cannot proceed." );
        }
        
//      API (we have too damn many string params in this!):
//        
//      Artifact createArtifactWithClassifier( String groupId, String artifactId, String version, String type,
//                                             String classifier );
        Artifact artifact = factory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        
        try
        {
            resolver.resolve( artifact, remoteRepositories, localRepository );
            
            setSourceArchiveFile( getProjectId(), artifact.getFile() );
        }
        catch ( ArtifactResolutionException e )
        {
            getLog().info( "Failed to resolve source artifact for: " + getProjectId() );
            getLog().debug( "Error resolving artifact: " + artifact.getId() + ". Reason: " + e.getMessage(), e );
            artifact = null;
        }
        catch ( ArtifactNotFoundException e )
        {
            getLog().info( "Failed to resolve source artifact for: " + getProjectId() );
            getLog().debug( "Cannot resolve source artifact: " + artifact.getId(), e );
            artifact = null;
        }
        
        if ( getSourceArchiveFile( getProjectId() ) == null )
        {
            throw new MojoFailureException( this, "Failed to retrieve project source archive.", "Failed to retrieve project source archive. Use debug mode (-X) to see stack traces." );
        }
        
        if ( copyTo != null )
        {
            try
            {
                copyTo = copyTo.getCanonicalFile();
                
                getLog().debug( "Copying downloaded source archive to: " + copyTo );
                
                copyTo.getParentFile().mkdirs();
                
                FileUtils.copyFile( getSourceArchiveFile( getProjectId() ), copyTo );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy source archive artifact to: " + copyTo + ". Reason: " + e.getMessage(), e );
            }
        }
    }
    
    protected boolean isSkip()
    {
        return skipGet;
    }

    protected CharSequence getSkipMessage()
    {
        return "Skipping remote source retrieval (per configuration).";
    }
    
}
