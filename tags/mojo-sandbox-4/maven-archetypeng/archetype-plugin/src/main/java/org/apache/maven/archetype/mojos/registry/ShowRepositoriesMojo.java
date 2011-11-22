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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

import java.util.Iterator;

/**
 * @author           rafale
 * @requiresProject  false
 * @goal             show-repositories
 */
public class ShowRepositoriesMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Iterator repositories =
                archetypeRegistryManager.readArchetypeRegistry ( archetypeRegistryFile )
                .getArchetypeRepositories ().iterator ();

            getLog ().info ( "Archetype repositories defined in " + archetypeRegistryFile );
            while ( repositories.hasNext () )
            {
                getLog ().info ( " - " + repositories.next () );
            }
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }
}
