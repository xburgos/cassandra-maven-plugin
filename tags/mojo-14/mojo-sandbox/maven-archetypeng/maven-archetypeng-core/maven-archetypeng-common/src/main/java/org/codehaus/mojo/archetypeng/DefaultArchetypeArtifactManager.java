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

package org.codehaus.mojo.archetypeng;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.GroupRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.shared.downloader.DownloadException;
import org.apache.maven.shared.downloader.DownloadNotFoundException;
import org.apache.maven.shared.downloader.Downloader;

import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.io.xpp3.ArchetypeDescriptorXpp3Reader;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;
import org.codehaus.mojo.archetypeng.exception.UnknownGroup;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @plexus.component
 */
public class DefaultArchetypeArtifactManager
extends AbstractLogEnabled
implements ArchetypeArtifactManager
{
    /**
     * @plexus.requirement
     */
    private Downloader downloader;

    /**
     * @plexus.requirement
     */
    private RepositoryMetadataManager repositoryMetadataManager;

    public ArchetypeDescriptor getArchetypeDescriptor (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            ClassLoader archetypeJarLoader =
                getArchetypeJarLoader (
                    groupId,
                    artifactId,
                    version,
                    localRepository,
                    repositories
                );

            return loadArchetypeDescriptor ( archetypeJarLoader );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( DownloadException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( DownloadNotFoundException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public ClassLoader getArchetypeJarLoader (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            File archetypeFile =
                downloader.download ( groupId, artifactId, version, localRepository, repositories );
            URL[] urls = new URL[1];

            urls[0] = archetypeFile.toURI ().toURL ();

            return new URLClassLoader ( urls );
        }
        catch ( MalformedURLException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( DownloadException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( DownloadNotFoundException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public List getArchetypes (
        String groupId,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownGroup
    {
        try
        {
            List archetypes = new ArrayList ();

            RepositoryMetadata metadata = new GroupRepositoryMetadata ( groupId );

            repositoryMetadataManager.resolve ( metadata, repositories, localRepository );

            for (
                Iterator iter = metadata.getMetadata ().getPlugins ().iterator ();
                iter.hasNext ();
            )
            {
                Plugin plugin = (Plugin) iter.next ();

                Archetype archetype = new Archetype ();

                archetype.setGroupId ( groupId );
                archetype.setArtifactId ( plugin.getArtifactId () );
                archetype.setName ( plugin.getName () );
                archetype.setPrefix ( plugin.getPrefix () );

                if ( getLogger ().isDebugEnabled () )
                {
                    getLogger ().debug ( "plugin=" + groupId + ":" + plugin.getArtifactId () );
                }

                if ( !archetypes.contains ( archetype ) )
                {
                    archetypes.add ( archetype );
                }
            } // end for

            return archetypes;
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownGroup ( e );
        }
    }

    public String getReleaseVersion (
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            RepositoryMetadata metadata =
                new GroupRepositoryMetadata ( groupId + "." + artifactId );

            repositoryMetadataManager.resolve ( metadata, repositories, localRepository );

            return metadata.getMetadata ().getVersioning ().getRelease ();
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public List getVersions (
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            RepositoryMetadata metadata =
                new GroupRepositoryMetadata ( groupId + "." + artifactId );

            repositoryMetadataManager.resolve ( metadata, repositories, localRepository );

            return metadata.getMetadata ().getVersioning ().getVersions ();
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    private Reader getArchetypeDescriptorReader ( ClassLoader archetypeJarLoader )
    throws IOException
    {
        InputStream is = getStream ( Constants.ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

        if ( is == null )
        {
            throw new IOException (
                "The " + Constants.ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        return new InputStreamReader ( is );
    }

    private ArchetypeDescriptor loadArchetypeDescriptor ( ClassLoader archetypeJarLoader )
    throws XmlPullParserException, IOException, DownloadException, DownloadNotFoundException
    {
        Reader reader = getArchetypeDescriptorReader ( archetypeJarLoader );

        ArchetypeDescriptorXpp3Reader archetypeReader = new ArchetypeDescriptorXpp3Reader ();

        try
        {
            return archetypeReader.read ( reader );
        }
        finally
        {
            reader.close ();
        }
    }

    private InputStream getStream ( String name, ClassLoader loader )
    {
        return
            ( loader == null )
            ? Thread.currentThread ().getContextClassLoader ().getResourceAsStream ( name )
            : loader.getResourceAsStream ( name );
    }
}
