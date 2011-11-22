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

package org.codehaus.mojo.archetypeng.creator;

import org.apache.maven.project.MavenProject;

import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.ArchetypeTemplateResolver;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.TemplateCreationException;

import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeCreationConfigurator
extends AbstractLogEnabled
implements ArchetypeCreationConfigurator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeCreationQueryer archetypeCreationQueryer;

    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeTemplateResolver archetypeTemplateResolver;

    public void configureArchetypeCreation (
        MavenProject project,
        Boolean interactiveMode,
        Properties commandLineProperties,
        File propertyFile
    )
    throws FileNotFoundException,
        IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        PrompterException,
        TemplateCreationException
    {
        Properties properties =
            initialiseArchetypeProperties ( commandLineProperties, propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );

        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration (
                project,
                archetypeDefinition,
                properties
            );

        String resolvedPackage = archetypeTemplateResolver.resolvePackage ( project.getBasedir () );
        if ( !archetypeConfiguration.isConfigured ( Constants.PACKAGE )
            && !StringUtils.isEmpty ( resolvedPackage )
        )
        {
            archetypeConfiguration.setProperty ( Constants.PACKAGE, resolvedPackage );
        }

        if ( interactiveMode.booleanValue () )
        {
            boolean confirmed = false;
            while ( !confirmed )
            {
                if ( !archetypeDefinition.isDefined () )
                {
                    if ( !archetypeDefinition.isGroupDefined () )
                    {
                        archetypeDefinition.setGroupId (
                            archetypeCreationQueryer.getArchetypeGroupId ( project.getGroupId () )
                        );
                    }
                    if ( !archetypeDefinition.isArtifactDefined () )
                    {
                        archetypeDefinition.setArtifactId (
                            archetypeCreationQueryer.getArchetypeArtifactId (
                                project.getArtifactId () + Constants.ARCHETYPE_SUFFIX
                            )
                        );
                    }
                    if ( !archetypeDefinition.isVersionDefined () )
                    {
                        archetypeDefinition.setVersion (
                            archetypeCreationQueryer.getArchetypeVersion ( project.getVersion () )
                        );
                    }

                    archetypeFactory.updateArchetypeConfiguration (
                        archetypeConfiguration,
                        archetypeDefinition
                    );
                }

                if ( !archetypeConfiguration.isConfigured () )
                {
                    if ( !archetypeConfiguration.isConfigured ( Constants.GROUP_ID ) )
                    {
                        archetypeConfiguration.setProperty (
                            Constants.GROUP_ID,
                            archetypeCreationQueryer.getGroupId (
                                archetypeConfiguration.getDefaultValue ( Constants.GROUP_ID )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured ( Constants.ARTIFACT_ID ) )
                    {
                        archetypeConfiguration.setProperty (
                            Constants.ARTIFACT_ID,
                            archetypeCreationQueryer.getArtifactId (
                                archetypeConfiguration.getDefaultValue ( Constants.ARTIFACT_ID )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured ( Constants.VERSION ) )
                    {
                        archetypeConfiguration.setProperty (
                            Constants.VERSION,
                            archetypeCreationQueryer.getVersion (
                                archetypeConfiguration.getDefaultValue ( Constants.VERSION )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured ( Constants.PACKAGE ) )
                    {
                        archetypeConfiguration.setProperty (
                            Constants.PACKAGE,
                            archetypeCreationQueryer.getPackage (
                                StringUtils.isEmpty ( resolvedPackage )
                                ? archetypeConfiguration.getDefaultValue ( Constants.PACKAGE )
                                : resolvedPackage
                            )
                        );
                    }
                } // end if

                boolean stopAddingProperties = false;
                while ( !stopAddingProperties )
                {
                    stopAddingProperties = !archetypeCreationQueryer.askAddAnotherProperty ();

                    if ( !stopAddingProperties )
                    {
                        String propertyKey = archetypeCreationQueryer.askNewPropertyKey ();
                        String replacementValue =
                            archetypeCreationQueryer.askReplacementValue (
                                propertyKey,
                                archetypeConfiguration.getDefaultValue ( propertyKey )
                            );
                        archetypeConfiguration.setDefaultProperty ( propertyKey, replacementValue );
                        archetypeConfiguration.setProperty ( propertyKey, replacementValue );
                    }
                }

                if ( archetypeCreationQueryer.confirmConfiguration ( archetypeConfiguration ) )
                {
                    confirmed = true;
                }
                else
                {
                    archetypeConfiguration.reset ();
                    archetypeDefinition.reset ();
                }
            } // end while
        }
        else
        {
            if ( !archetypeDefinition.isDefined () )
            {
                throw new ArchetypeNotDefined ( "The archetype is not defined" );
            }
            else if ( !archetypeConfiguration.isConfigured () )
            {
                throw new ArchetypeNotConfigured ( "The archetype is not configured" );
            }
        } // end if

        archetypePropertiesManager.writeProperties (
            archetypeConfiguration.toProperties (),
            propertyFile
        );
    }

    private Properties initialiseArchetypeProperties (
        Properties commandLineProperties,
        File propertyFile
    )
    throws IOException
    {
        Properties properties = new Properties ();
        try
        {
            archetypePropertiesManager.readProperties ( properties, propertyFile );
        }
        catch ( FileNotFoundException ex )
        {
            getLogger ().debug ( "archetype.properties does not exist" );
        }

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
