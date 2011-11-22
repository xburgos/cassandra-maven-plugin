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

package org.apache.maven.archetype.common;

import org.apache.maven.project.MavenProject;

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
        ArchetypeDefinition archetypeDefinition,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();
        getLogger ().debug (
            "Creating ArchetypeConfiguration from ArchetypeDefinition and Properties"
        );

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

                getLogger ().debug ( "Adding requiredProperty " + property );

                configuration.setProperty ( property, properties.getProperty ( property ) );

                getLogger ().debug (
                    "Adding property " + property + "=" + properties.getProperty ( property )
                );
            }
        }

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration (
        org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();
        getLogger ().debug (
            "Creating ArchetypeConfiguration from legacy descriptor and Properties"
        );

        configuration.setGroupId ( properties.getProperty ( Constants.ARCHETYPE_GROUP_ID, null ) );
        configuration.setArtifactId (
            properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID, null )
        );
        configuration.setVersion ( properties.getProperty ( Constants.ARCHETYPE_VERSION, null ) );

        configuration.setName ( archetypeDescriptor.getId () );

        configuration.addRequiredProperty ( Constants.GROUP_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.GROUP_ID );
        if ( null != properties.getProperty ( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty (
                Constants.GROUP_ID,
                properties.getProperty ( Constants.GROUP_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.GROUP_ID + "="
                + configuration.getProperty ( Constants.GROUP_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.ARTIFACT_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.ARTIFACT_ID );
        if ( null != properties.getProperty ( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty (
                Constants.ARTIFACT_ID,
                properties.getProperty ( Constants.ARTIFACT_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.ARTIFACT_ID + "="
                + configuration.getProperty ( Constants.ARTIFACT_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.VERSION );
        getLogger ().debug ( "Adding requiredProperty " + Constants.VERSION );
        if ( null != properties.getProperty ( Constants.VERSION, null ) )
        {
            configuration.setProperty (
                Constants.VERSION,
                properties.getProperty ( Constants.VERSION )
            );
            getLogger ().debug (
                "Setting property " + Constants.VERSION + "="
                + configuration.getProperty ( Constants.VERSION )
            );
        }
        configuration.addRequiredProperty ( Constants.PACKAGE );
        getLogger ().debug ( "Adding requiredProperty " + Constants.PACKAGE );
        if ( null
            != properties.getProperty (
                Constants.PACKAGE,
                properties.getProperty ( Constants.PACKAGE_NAME, null )
            )
        )
        {
            configuration.setProperty (
                Constants.PACKAGE,
                properties.getProperty (
                    Constants.PACKAGE,
                    properties.getProperty ( Constants.PACKAGE_NAME )
                )
            );
            getLogger ().debug (
                "Setting property " + Constants.PACKAGE_NAME + "="
                + configuration.getProperty ( Constants.PACKAGE_NAME )
            );
        }

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration (
        org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration ();
        getLogger ().debug (
            "Creating ArchetypeConfiguration from fileset descriptor and Properties"
        );

        configuration.setGroupId ( properties.getProperty ( Constants.ARCHETYPE_GROUP_ID, null ) );
        configuration.setArtifactId (
            properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID, null )
        );
        configuration.setVersion ( properties.getProperty ( Constants.ARCHETYPE_VERSION, null ) );

        configuration.setName ( archetypeDescriptor.getId () );

        Iterator requiredProperties = archetypeDescriptor.getRequiredProperties ().iterator ();
        while ( requiredProperties.hasNext () )
        {
            org.apache.maven.archetype.metadata.RequiredProperty requiredProperty =
                (org.apache.maven.archetype.metadata.RequiredProperty) requiredProperties.next ();

            configuration.addRequiredProperty ( requiredProperty.getKey () );
            getLogger ().debug ( "Adding requiredProperty " + requiredProperty.getKey () );

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
                getLogger ().debug (
                    "Setting property " + requiredProperty.getKey () + "="
                    + configuration.getProperty ( requiredProperty.getKey () )
                );
            }
            if ( null != requiredProperty.getDefaultValue () )
            {
                configuration.setDefaultProperty (
                    requiredProperty.getKey (),
                    requiredProperty.getDefaultValue ()
                );
                getLogger ().debug (
                    "Setting defaultProperty " + requiredProperty.getKey () + "="
                    + configuration.getDefaultValue ( requiredProperty.getKey () )
                );
            }
        } // end while

        configuration.addRequiredProperty ( Constants.GROUP_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.GROUP_ID );
        if ( null != properties.getProperty ( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty (
                Constants.GROUP_ID,
                properties.getProperty ( Constants.GROUP_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.GROUP_ID + "="
                + configuration.getProperty ( Constants.GROUP_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.ARTIFACT_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.ARTIFACT_ID );
        if ( null != properties.getProperty ( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty (
                Constants.ARTIFACT_ID,
                properties.getProperty ( Constants.ARTIFACT_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.ARTIFACT_ID + "="
                + configuration.getProperty ( Constants.ARTIFACT_ID )
            );
        }
        configuration.addRequiredProperty ( Constants.VERSION );
        getLogger ().debug ( "Adding requiredProperty " + Constants.VERSION );
        if ( null != properties.getProperty ( Constants.VERSION, null ) )
        {
            configuration.setProperty (
                Constants.VERSION,
                properties.getProperty ( Constants.VERSION )
            );
            getLogger ().debug (
                "Setting property " + Constants.VERSION + "="
                + configuration.getProperty ( Constants.VERSION )
            );
        }
        configuration.addRequiredProperty ( Constants.PACKAGE );
        getLogger ().debug ( "Adding requiredProperty " + Constants.PACKAGE );
        if ( null
            != properties.getProperty (
                Constants.PACKAGE,
                properties.getProperty ( Constants.PACKAGE_NAME, null )
            )
        )
        {
            configuration.setProperty (
                Constants.PACKAGE,
                properties.getProperty (
                    Constants.PACKAGE,
                    properties.getProperty ( Constants.PACKAGE_NAME )
                )
            );
            getLogger ().debug (
                "Setting property " + Constants.PACKAGE + "="
                + configuration.getProperty ( Constants.PACKAGE )
            );
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
        getLogger ().debug (
            "Creating ArchetypeConfiguration from ArchetypeDefinition, MavenProject and Properties"
        );

        configuration.setGroupId ( archetypeDefinition.getGroupId () );
        configuration.setArtifactId ( archetypeDefinition.getArtifactId () );
        configuration.setVersion ( archetypeDefinition.getVersion () );

        configuration.addRequiredProperty ( Constants.GROUP_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.GROUP_ID );
        configuration.setDefaultProperty ( Constants.GROUP_ID, project.getGroupId () );
        if ( null != properties.getProperty ( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty (
                Constants.GROUP_ID,
                properties.getProperty ( Constants.GROUP_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.GROUP_ID + "="
                + configuration.getProperty ( Constants.GROUP_ID )
            );
        }

        configuration.addRequiredProperty ( Constants.ARTIFACT_ID );
        getLogger ().debug ( "Adding requiredProperty " + Constants.ARTIFACT_ID );
        configuration.setDefaultProperty ( Constants.ARTIFACT_ID, project.getArtifactId () );
        if ( null != properties.getProperty ( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty (
                Constants.ARTIFACT_ID,
                properties.getProperty ( Constants.ARTIFACT_ID )
            );
            getLogger ().debug (
                "Setting property " + Constants.ARTIFACT_ID + "="
                + configuration.getProperty ( Constants.ARTIFACT_ID )
            );
        }

        configuration.addRequiredProperty ( Constants.VERSION );
        getLogger ().debug ( "Adding requiredProperty " + Constants.VERSION );
        configuration.setDefaultProperty ( Constants.VERSION, project.getVersion () );
        if ( null != properties.getProperty ( Constants.VERSION, null ) )
        {
            configuration.setProperty (
                Constants.VERSION,
                properties.getProperty ( Constants.VERSION )
            );
            getLogger ().debug (
                "Setting property " + Constants.VERSION + "="
                + configuration.getProperty ( Constants.VERSION )
            );
        }

        configuration.addRequiredProperty ( Constants.PACKAGE );
        getLogger ().debug ( "Adding requiredProperty " + Constants.PACKAGE );
        if ( null
            != properties.getProperty (
                Constants.PACKAGE,
                properties.getProperty ( Constants.PACKAGE_NAME, null )
            )
        )
        {
            configuration.setProperty (
                Constants.PACKAGE,
                properties.getProperty (
                    Constants.PACKAGE,
                    properties.getProperty ( Constants.PACKAGE_NAME )
                )
            );
            getLogger ().debug (
                "Setting property " + Constants.PACKAGE + "="
                + configuration.getProperty ( Constants.PACKAGE )
            );
        }

        return configuration;
    }

    public ArchetypeDefinition createArchetypeDefinition ( Properties properties )
    {
        ArchetypeDefinition definition = new ArchetypeDefinition ();
        getLogger ().debug (
            "Creating ArchetypeDefinition ("
            + properties.getProperty ( Constants.ARCHETYPE_GROUP_ID, null ) + ":"
            + properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID, null ) + ":"
            + properties.getProperty ( Constants.ARCHETYPE_VERSION, null ) + ")"
        );

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
