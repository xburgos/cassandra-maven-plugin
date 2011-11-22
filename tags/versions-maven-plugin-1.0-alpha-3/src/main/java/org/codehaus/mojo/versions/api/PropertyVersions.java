package org.codehaus.mojo.versions.api;

/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Manages a property that is associated with one or more artifacts.
 *
 * @author <a href="mailto:stephen.alan.connolly@gmail.com">Stephen Connolly</a>
 * @since 1.0-alpha-3
 */
public class PropertyVersions
{
    private final String name;

    private final String profileId;

    private final Set/*<String*/ associations;

    private final VersionsHelper helper;

    /**
     * Constructs a new {@link org.codehaus.mojo.versions.api.PropertyVersions}.
     *
     * @param profileId
     * @param name      The property name.
     * @param helper    The {@link org.codehaus.mojo.versions.api.DefaultVersionsHelper}.
     */
    public PropertyVersions( String profileId, String name, VersionsHelper helper )
    {
        this.profileId = profileId;
        this.name = name;
        this.associations = new TreeSet();
        this.helper = helper;
    }

    public void addAssociation( Artifact artifact, boolean usePluginRepositories )
    {
        associations.add( new DefaultArtifactAssociation( artifact, usePluginRepositories ) );
    }

    public void removeAssociation( Artifact artifact, boolean usePluginRepositories )
    {
        associations.remove( new DefaultArtifactAssociation( artifact, usePluginRepositories ) );
    }

    public ArtifactAssocation[] getAssociations()
    {
        return (ArtifactAssocation[]) associations.toArray( new ArtifactAssocation[associations.size()] );
    }

    private Comparator[] lookupComparators()
    {
        Set result = new HashSet();
        Iterator i = associations.iterator();
        while ( i.hasNext() )
        {
            ArtifactAssocation association = (ArtifactAssocation) i.next();
            result.add( helper.getVersionComparator( association.getArtifact() ) );
        }
        return (Comparator[]) result.toArray( new Comparator[result.size()] );
    }

    public int compare( ArtifactVersion v1, ArtifactVersion v2 )
        throws MojoExecutionException
    {
        if (!isAssociated())
        {
            throw new IllegalStateException( "Cannot compare versions for a property with no associations");
        }
        Comparator[] comparators = lookupComparators();
        assert comparators.length >= 1 : "we have at least one association => at least one comparator";
        int result = comparators[0].compare( v1, v2 );
        for (int i = 1; i < comparators.length; i++) {
            int alt = comparators[i].compare( v1, v2 );
            if ( result != alt && ( result >= 0 && alt < 0 ) || ( result <= 0 && alt > 0 ) )
            {
                throw new MojoExecutionException( "Property " + name + " is associated with multiple artifacts"
                    + " and these artifacts use different version sorting rules and these rules are effectively"
                    + " incompatible for the two of versions being compared.\nFirst rule says compare(\"" + v1 
                    + "\", \"" + v2 + "\") = " + result + "\nSecond rule says compare(\"" + v1 
                    + "\", \"" + v2 + "\") = " + alt );
            }
        }
        return result;
    }

    /**
     * Uses the supplied {@link Collection} of {@link Artifact} instances to see if an ArtifactVersion can be provided.
     *
     * @param artifacts The {@link Collection} of {@link Artifact} instances .
     * @return The versions that can be resolved from the supplied Artifact instances or an empty array if no version
     *         can be resolved (i.e. the property is not associated with any of the supplied artifacts or the property is
     *         also associated to an artifact that has not been provided).
     * @since 1.0-alpha-3
     */
    public ArtifactVersion[] getVersions( Collection/*<Artifact>*/ artifacts )
        throws MojoExecutionException
    {
        List/*<ArtifactVersion>*/ result = new ArrayList();
        // go through all the associations
        // see if they are met from the collection
        // add the version if they are
        // go through all the versions
        // see if the version is available for all associations
        Iterator i = associations.iterator();
        while ( i.hasNext() )
        {
            ArtifactAssocation association = (ArtifactAssocation) i.next();
            Iterator j = artifacts.iterator();
            while ( j.hasNext() )
            {
                Artifact artifact = (Artifact) j.next();
                if ( association.getGroupId().equals( artifact.getGroupId() ) && association.getArtifactId().equals(
                    artifact.getArtifactId() ) )
                {
                    try
                    {
                        result.add( artifact.getSelectedVersion() );
                    }
                    catch ( OverConstrainedVersionException e )
                    {
                        // ignore this one as we cannot resolve a valid version
                    }
                }
            }
        }
        // we now have a list of all the versions that partially satisfy the association requirements
        Iterator k = result.iterator();
        while ( k.hasNext() )
        {
            ArtifactVersion candidate = (ArtifactVersion) k.next();
            i = associations.iterator();
            while ( i.hasNext() )
            {
                ArtifactAssocation association = (ArtifactAssocation) i.next();
                boolean haveMatch = false;
                Iterator j = artifacts.iterator();
                while ( j.hasNext() && !haveMatch )
                {
                    Artifact artifact = (Artifact) j.next();
                    if ( association.getGroupId().equals( artifact.getGroupId() ) && association.getArtifactId().equals(
                        artifact.getArtifactId() ) )
                    {
                        try
                        {
                            haveMatch = candidate.toString().equals( artifact.getSelectedVersion().toString() );
                        }
                        catch ( OverConstrainedVersionException e )
                        {
                            // ignore this one again
                        }
                    }
                }
                if ( !haveMatch )
                {
                    // candidate is not valid as at least one association cannot be met
                    k.remove();
                    break;
                }
            }
        }
        return asArtifactVersionArray( result );
    }

    /**
     * Uses the {@link DefaultVersionsHelper} to find all available versions that match all
     * the associations with this property.
     *
     * @param includeSnapshots Whether to include snapshot versions in our search.
     * @return The (possibly empty) array of versions.
     * @throws MojoExecutionException
     */
    public ArtifactVersion[] getVersions( boolean includeSnapshots )
        throws MojoExecutionException
    {
        List result = null;
        Iterator i = associations.iterator();
        while ( i.hasNext() )
        {
            ArtifactAssocation association = (ArtifactAssocation) i.next();
            final ArtifactVersions versions =
                helper.lookupArtifactVersions( association.getArtifact(), association.isUsePluginRepositories() );
            if ( result != null )
            {
                final ArtifactVersion[] artifactVersions = versions.getVersions( includeSnapshots );
                // since ArtifactVersion does not override equals, we have to do this the hard way
                // result.retainAll( Arrays.asList( artifactVersions ) );
                Iterator j = result.iterator();
                while ( j.hasNext() )
                {
                    boolean contains = false;
                    ArtifactVersion version = (ArtifactVersion) j.next();
                    for ( int k = 0; k < artifactVersions.length; k++ )
                    {
                        if ( version.compareTo( artifactVersions[k] ) == 0 )
                        {
                            contains = true;
                            break;
                        }
                    }
                    if ( !contains )
                    {
                        j.remove();
                    }
                }
            }
            else
            {
                result = new ArrayList( Arrays.asList( versions.getVersions( includeSnapshots ) ) );
            }
        }
        return asArtifactVersionArray( result );
    }

    private ArtifactVersion[] asArtifactVersionArray( List result )
        throws MojoExecutionException
    {
        if ( result == null || result.isEmpty() )
        {
            return new ArtifactVersion[0];
        }
        else
        {
            final ArtifactVersion[] answer = (ArtifactVersion[]) result.toArray( new ArtifactVersion[result.size()] );
            Comparator[] rules = lookupComparators();
            assert rules.length > 0;
            Arrays.sort( answer, rules[0] );
            if ( rules.length == 1 || answer.length == 1 )
            {
                // only one rule...
                return answer;
            }
            ArtifactVersion[] alt = (ArtifactVersion[]) answer.clone();
            for ( int j = 1; j < rules.length; j++ )
            {
                Arrays.sort( alt, rules[j] );
                if ( !Arrays.equals( alt, answer ) )
                {
                    throw new MojoExecutionException( "Property " + name + " is associated with multiple artifacts"
                        + " and these artifacts use different version sorting rules and these rules are effectively"
                        + " incompatible for the set of versions available to this property.\nFirst rule says: "
                        + Arrays.asList( answer ) + "\nSecond rule says: " + Arrays.asList( alt ) );
                }
            }
            return answer;
        }
    }

    public String getName()
    {
        return name;
    }

    public String getProfileId()
    {
        return profileId;
    }

    public boolean isAssociated()
    {
        return !associations.isEmpty();
    }

    public String toString()
    {
        return "PropertyVersions{" + ( profileId == null ? "" : "profileId='" + profileId + "', " ) + "name='" + name
            + '\'' + ", associations=" + associations + '}';
    }

    public void clearAssociations()
    {
        associations.clear();
    }

    private static final class DefaultArtifactAssociation
        implements Comparable, ArtifactAssocation
    {
        private final Artifact artifact;

        private final boolean usePluginRepositories;

        private DefaultArtifactAssociation( Artifact artifact, boolean usePluginRepositories )
        {
            artifact.getClass(); // throw NPE if null;
            this.artifact = artifact;
            this.usePluginRepositories = usePluginRepositories;
        }

        public String getGroupId()
        {
            return artifact.getGroupId();
        }

        public String getArtifactId()
        {
            return artifact.getArtifactId();
        }

        public Artifact getArtifact()
        {
            return artifact;
        }

        public boolean isUsePluginRepositories()
        {
            return usePluginRepositories;
        }

        public int compareTo( Object o )
        {
            if ( this == o )
            {
                return 0;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return 1;
            }
            DefaultArtifactAssociation that = (DefaultArtifactAssociation) o;

            int rv = getGroupId().compareTo( that.getGroupId() );
            if ( rv != 0 )
            {
                return rv;
            }
            rv = getArtifactId().compareTo( that.getArtifactId() );
            if ( rv != 0 )
            {
                return rv;
            }
            if ( usePluginRepositories != that.usePluginRepositories )
            {
                return usePluginRepositories ? 1 : -1;
            }
            return 0;
        }

        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            DefaultArtifactAssociation that = (DefaultArtifactAssociation) o;

            if ( usePluginRepositories != that.usePluginRepositories )
            {
                return false;
            }
            if ( !getArtifactId().equals( that.getArtifactId() ) )
            {
                return false;
            }
            if ( !getGroupId().equals( that.getGroupId() ) )
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            int result = getGroupId().hashCode();
            result = 31 * result + getArtifactId().hashCode();
            result = 31 * result + ( usePluginRepositories ? 1 : 0 );
            return result;
        }

        public String toString()
        {
            return ( usePluginRepositories ? "plugin:" : "artifact:" ) + ArtifactUtils.versionlessKey( artifact );
        }
    }

}
