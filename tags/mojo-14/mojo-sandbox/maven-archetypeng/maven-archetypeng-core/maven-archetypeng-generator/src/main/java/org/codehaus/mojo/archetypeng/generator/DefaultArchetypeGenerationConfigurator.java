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

package org.codehaus.mojo.archetypeng.generator;

import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.exception.ArchetypeGenerationConfigurationFailure;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;

import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeGenerationConfigurator
extends AbstractLogEnabled
implements ArchetypeGenerationConfigurator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    public void configureArchetype (
        Boolean interactiveMode,
        File propertyFile,
        Properties commandLineProperties,
        ArtifactRepository localRepository,
        List repositories
    )
    throws ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        IOException,
        PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        Properties properties =
            initialiseArchetypeProperties ( commandLineProperties, propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );

        if ( !archetypeDefinition.isDefined () )
        {
            throw new ArchetypeNotDefined ( "The archetype is not defined" );
        }

        ArchetypeDescriptor archetypeDescriptor =
            archetypeArtifactManager.getArchetypeDescriptor (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );
        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );

        if ( interactiveMode.booleanValue () )
        {
            boolean confirmed = false;
            while ( !confirmed )
            {
                if ( !archetypeConfiguration.isConfigured () )
                {
                    Iterator requiredProperties =
                        archetypeConfiguration.getRequiredProperties ().iterator ();

                    while ( requiredProperties.hasNext () )
                    {
                        String requiredProperty = (String) requiredProperties.next ();

                        if ( !archetypeConfiguration.isConfigured ( requiredProperty ) )
                        {
                            archetypeConfiguration.setProperty (
                                requiredProperty,
                                archetypeGenerationQueryer.getPropertyValue (
                                    requiredProperty,
                                    archetypeConfiguration.getDefaultValue ( requiredProperty )
                                )
                            );
                        }
                    }
                }
                if ( !archetypeConfiguration.isConfigured () )
                {
                    throw new ArchetypeGenerationConfigurationFailure (
                        "The archetype generation must be configured here"
                    );
                }
                else if (
                    !archetypeGenerationQueryer.confirmConfiguration ( archetypeConfiguration )
                )
                {
                    getLogger ().debug ( "Archetype generation configuration not confirmed" );
                    archetypeConfiguration.reset ();
                }
                else
                {
                    getLogger ().debug ( "Archetype generation configuration confirmed" );
                    confirmed = true;
                }
            } // end while
        }
        else
        {
            if ( !archetypeConfiguration.isConfigured () )
            {
                throw new ArchetypeNotConfigured ( "The archetype is not configurated" );
            }
        }

        archetypePropertiesManager.writeProperties (
            archetypeConfiguration.toProperties (),
            propertyFile
        );
    }

    private Properties initialiseArchetypeProperties (
        Properties commandLineProperties,
        File propertyFile
    )
    throws FileNotFoundException, IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );

        Iterator commandLinePropertiesIterator =
            new ArrayList ( commandLineProperties.keySet () ).iterator ();
        while ( commandLinePropertiesIterator.hasNext () )
        {
            String propertyKey = (String) commandLinePropertiesIterator.next ();
            properties.setProperty (
                propertyKey,
                commandLineProperties.getProperty ( propertyKey )
            );
        }
        return properties;
    }
}
