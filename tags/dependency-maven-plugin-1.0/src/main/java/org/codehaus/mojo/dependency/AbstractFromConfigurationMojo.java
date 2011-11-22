/*
 *  Copyright 2005-2006 Brian Fox (brianefox@gmail.com)
 *
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
 */
package org.codehaus.mojo.dependency;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * Abstract Parent class used by mojos that get Artifact information from the plugin configuration as an ArrayList of ArtifactItems
 * @see ArtifactItem
 * @author brianf
 *
 */
public abstract class AbstractFromConfigurationMojo
    extends AbstractMojo
{

    /**
     * Used to look up Artifacts in the remote repository.
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private org.apache.maven.artifact.factory.ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Location of the local repository.
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private org.apache.maven.artifact.repository.ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private java.util.List remoteRepos;

    /**
     * Default location used for mojo unless overridden in ArtifactItem
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/dependency"
     * @required
     */
    private File outputDirectory;

    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    protected ArchiverManager archiverManager;

    /**
     * Collection of ArtifactItems to work on. (ArtifactItem contains groupId, artifactId, version, type, location, destFile, markerFile and overwrite.)
     * See "How To Use" and "Javadoc" for details.
     * @parameter
     * @required
     */
    private ArrayList artifactItems;

    /**
     * POM
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * Preprocesses the list of ArtifactItems. This method defaults the outputDirectory if not set
     * and creates the output Directory if it doesn't exist.
 
     * @return An ArrayList of preprocessed ArtifactItems
     * 
     * @throws MojoExecutionException 
     *          with a message if an error occurs.
     * 
     * @see ArtifactItem
     */
    protected ArrayList getArtifactItems()
        throws MojoExecutionException
    {

        Iterator iter = artifactItems.iterator();
        while ( iter.hasNext() )
        {
            ArtifactItem artifactItem = (ArtifactItem) iter.next();
            this.getLog().info( "Configured Artifact: " + artifactItem.toString() );

            if ( artifactItem.getOutputDirectory() == null )
            {
                artifactItem.setOutputDirectory( this.outputDirectory );
            }
            artifactItem.getOutputDirectory().mkdirs();
        }
        return artifactItems;
    }

    /**
     * Resolves the Artifact from the remote repository if nessessary. If no version is specified, it will
     * be retrieved from the DependencyManagement section of the pom.
     *
     * @param artifactItem 
     *          containing information about artifact from plugin configuration.
     * @return Artifact 
     *          object representing the specified file.
     * 
     * @throws MojoExecutionException 
     *          with a message if the version can't be found in DependencyManagement.
     */
    protected Artifact getArtifact( ArtifactItem artifactItem )
        throws MojoExecutionException
    {
        Artifact artifact;

        if ( artifactItem.getVersion() == null )
        {
            fillArtifactVersionFromDependencyManagement( artifactItem );

            if ( artifactItem.getVersion() == null )
            {
                throw new MojoExecutionException( "Unable to find artifact version of " + artifactItem.getGroupId()
                    + ":" + artifactItem.getArtifactId() + " in project's dependency management." );
            }

        }

        //use classifer if set.
        String classifier = artifactItem.getClassifier();

        if ( classifier == null || classifier.equals( "" ) )
        {
            artifact = factory.createArtifact( artifactItem.getGroupId(), artifactItem.getArtifactId(), artifactItem
                .getVersion(), Artifact.SCOPE_PROVIDED, artifactItem.getType() );
        }
        else
        {
            artifact = factory.createArtifactWithClassifier( artifactItem.getGroupId(), artifactItem.getArtifactId(),
                                                             artifactItem.getVersion(), artifactItem.getType(),
                                                             artifactItem.getClassifier() );
        }

        try
        {
            resolver.resolve( artifact, remoteRepos, local );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Unable to resolve artifact.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Unable to find artifact.", e );
        }

        return artifact;
    }

    /**
     * Tries to find missing version from dependancy management. If found, the artifact is updated with
     * the correct version.
     * @param artifact representing configured file.
     */
    private void fillArtifactVersionFromDependencyManagement( ArtifactItem artifact )
    {
        this.getLog().debug( "Attempting to find missing version from dependency management." );

        List list = this.project.getDependencyManagement().getDependencies();

        for ( int i = 0; i < list.size(); ++i )
        {
            Dependency dependency = (Dependency) list.get( i );

            if ( dependency.getGroupId().equals( artifact.getGroupId() )
                && dependency.getArtifactId().equals( artifact.getArtifactId() )
                && dependency.getType().equals( artifact.getType() ) )
            {
                this.getLog().debug( "Found missing version: " + dependency.getVersion() );

                artifact.setVersion( dependency.getVersion() );
            }
        }
    }
}
