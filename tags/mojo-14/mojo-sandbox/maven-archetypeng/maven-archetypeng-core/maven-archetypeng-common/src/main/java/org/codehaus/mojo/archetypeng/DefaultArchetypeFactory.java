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

import org.apache.maven.project.MavenProject;

import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.RequiredProperty;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Iterator;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeFactory
extends AbstractLogEnabled
implements ArchetypeFactory
{
    public ArchetypeConfiguration createArchetypeConfiguration (
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();

        configuration.setGroupId ( properties.getProperty ( Constants.ARCHETYPE_GROUP_ID, null ) );
        configuration.setArtifactId (
            properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID, null )
        );
        configuration.setVersion ( properties.getProperty ( Constants.ARCHETYPE_VERSION, null ) );

        configuration.setName ( archetypeDescriptor.getName () );

        Iterator requiredProperties = archetypeDescriptor.getRequiredProperties ().iterator ();
        while ( requiredProperties.hasNext () )
        {
            RequiredProperty requiredProperty = (RequiredProperty) requiredProperties.next ();

            configuration.addRequiredProperty ( requiredProperty.getKey () );

            if ( null
                != properties.getProperty (
                    requiredProperty.getKey (),
                    requiredProperty.getDefaultValue ()
                )
            )
            {
                configuration.setProperty (
                    requiredProperty.getKey (),
                    properties.getProperty (
                        requiredProperty.getKey (),
                        requiredProperty.getDefaultValue ()
                    )
                );
            }
            if ( null != requiredProperty.getDefaultValue () )
            {
                configuration.setDefaultProperty (
                    requiredProperty.getKey (),
                    requiredProperty.getDefaultValue ()
                );
            }
        } // end while

        configuration.addRequiredProperty ( Constants.GROUP_ID );
        if ( null != properties.getProperty ( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty (
                Constants.GROUP_ID,
                properties.getProperty ( Constants.GROUP_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.ARTIFACT_ID );
        if ( null != properties.getProperty ( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty (
                Constants.ARTIFACT_ID,
                properties.getProperty ( Constants.ARTIFACT_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.VERSION );
        if ( null != properties.getProperty ( Constants.VERSION, null ) )
        {
            configuration.setProperty (
                Constants.VERSION,
                properties.getProperty ( Constants.VERSION )
            );
        }
        configuration.addRequiredProperty ( Constants.PACKAGE );
        if ( null != properties.getProperty ( Constants.PACKAGE, null ) )
        {
            configuration.setProperty (
                Constants.PACKAGE,
                properties.getProperty ( Constants.PACKAGE )
            );
        }

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration (
        ArchetypeDefinition archetypeDefinition,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();

        configuration.setGroupId ( archetypeDefinition.getGroupId () );
        configuration.setArtifactId ( archetypeDefinition.getArtifactId () );
        configuration.setVersion ( archetypeDefinition.getVersion () );

        Iterator propertiesIterator = properties.keySet ().iterator ();
        while ( propertiesIterator.hasNext () )
        {
            String property = (String) propertiesIterator.next ();
            if ( !Constants.ARCHETYPE_GROUP_ID.equals ( property )
                && !Constants.ARCHETYPE_ARTIFACT_ID.equals ( property )
                && !Constants.ARCHETYPE_VERSION.equals ( property )
            )
            {
                configuration.addRequiredProperty ( property );
                configuration.setProperty ( property, properties.getProperty ( property ) );
            }
        }

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration (
        MavenProject project,
        ArchetypeDefinition archetypeDefinition,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();

        configuration.setGroupId ( archetypeDefinition.getGroupId () );
        configuration.setArtifactId ( archetypeDefinition.getArtifactId () );
        configuration.setVersion ( archetypeDefinition.getVersion () );

        configuration.addRequiredProperty ( Constants.GROUP_ID );
        configuration.setDefaultProperty ( Constants.GROUP_ID, project.getGroupId () );
        if ( null != properties.getProperty ( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty (
                Constants.GROUP_ID,
                properties.getProperty ( Constants.GROUP_ID )
            );
        }

        configuration.addRequiredProperty ( Constants.ARTIFACT_ID );
        configuration.setDefaultProperty ( Constants.ARTIFACT_ID, project.getArtifactId () );
        if ( null != properties.getProperty ( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty (
                Constants.ARTIFACT_ID,
                properties.getProperty ( Constants.ARTIFACT_ID )
            );
        }

        configuration.addRequiredProperty ( Constants.VERSION );
        configuration.setDefaultProperty ( Constants.VERSION, project.getVersion () );
        if ( null != properties.getProperty ( Constants.VERSION, null ) )
        {
            configuration.setProperty (
                Constants.VERSION,
                properties.getProperty ( Constants.VERSION )
            );
        }

        configuration.addRequiredProperty ( Constants.PACKAGE );
        if ( null != properties.getProperty ( Constants.PACKAGE, null ) )
        {
            configuration.setProperty (
                Constants.PACKAGE,
                properties.getProperty ( Constants.PACKAGE )
            );
        }

        return configuration;
    }

    public ArchetypeDefinition createArchetypeDefinition ( Properties properties )
    {
        ArchetypeDefinition definition = new ArchetypeDefinition ();

        definition.setGroupId ( properties.getProperty ( Constants.ARCHETYPE_GROUP_ID, null ) );
        definition.setArtifactId (
            properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID, null )
        );
        definition.setVersion ( properties.getProperty ( Constants.ARCHETYPE_VERSION, null ) );

        return definition;
    }

    public void updateArchetypeConfiguration (
        ArchetypeConfiguration archetypeConfiguration,
        ArchetypeDefinition archetypeDefinition
    )
    {
        archetypeConfiguration.setGroupId ( archetypeDefinition.getGroupId () );
        archetypeConfiguration.setArtifactId ( archetypeDefinition.getArtifactId () );
        archetypeConfiguration.setVersion ( archetypeDefinition.getVersion () );
    }
}
