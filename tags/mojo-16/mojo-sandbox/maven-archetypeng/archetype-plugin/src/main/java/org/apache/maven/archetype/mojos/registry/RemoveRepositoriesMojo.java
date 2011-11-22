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

package org.apache.maven.archetype.mojos.registry;

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.registry.ArchetypeRegistry;
import org.apache.maven.archetype.registry.ArchetypeRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author           rafale
 * @requiresProject  false
 * @goal             remove-repositories
 */
public class RemoveRepositoriesMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @parameter  expression="${repositories}"
     */
    String repositories;

    /**
     * @parameter  expression="${repositoryId}"
     */
    String repositoryId;
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        if ( StringUtils.isEmpty ( repositoryId ) && StringUtils.isEmpty ( repositories ) )
        {
            throw new MojoFailureException ( " -DrepositoryId or -Drepositories must be set" );
        }
        else if (
            StringUtils.isNotEmpty ( repositoryId )
            && StringUtils.isNotEmpty ( repositories )
        )
        {
            throw new MojoFailureException (
                "Only one of -DrepositoryId or -Drepositories can be set"
            );
        }

        try
        {
            List repositoriesToRemove = new ArrayList ();
            if ( StringUtils.isNotEmpty ( repositoryId ) )
            {
                ArchetypeRepository repository = new ArchetypeRepository ();

                repository.setId ( repositoryId );
                repository.setUrl ( "EMPTY" );

                repositoriesToRemove.add ( repository );
            }
            else
            {
                Iterator repositoriesDefinitions =
                    Arrays.asList ( StringUtils.split ( repositories, "," ) ).iterator ();
                while ( repositoriesDefinitions.hasNext () )
                {
                    String repositoryDefinition = (String) repositoriesDefinitions.next ();

                    ArchetypeRepository repository = new ArchetypeRepository ();

                    repository.setId ( repositoryDefinition );
                    repository.setUrl ( "EMPTY" );

                    repositoriesToRemove.add ( repository );
                }
            }

            ArchetypeRegistry registry =
                archetypeRegistryManager.readArchetypeRegistry ( archetypeRegistryFile );

            Iterator repositoriesToRemoveIterator = repositoriesToRemove.iterator ();
            while ( repositoriesToRemoveIterator.hasNext () )
            {
                ArchetypeRepository repositoryToRemove =
                    (ArchetypeRepository) repositoriesToRemoveIterator.next ();
                if ( registry.getArchetypeRepositories ().contains ( repositoryToRemove ) )
                {
                    registry.removeArchetypeRepository ( repositoryToRemove );
                    getLog ().debug ( "Repository " + repositoryToRemove.getId () + " removed" );
                }
                else
                {
                    getLog ().debug (
                        "Repository " + repositoryToRemove.getId () + " doesn't exist"
                    );
                }
            }
            archetypeRegistryManager.writeArchetypeRegistry ( archetypeRegistryFile, registry );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }
}
