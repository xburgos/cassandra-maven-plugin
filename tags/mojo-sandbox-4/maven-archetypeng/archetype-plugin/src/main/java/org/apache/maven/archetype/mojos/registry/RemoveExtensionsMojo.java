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
 * @goal             remove-extensions
 */
public class RemoveExtensionsMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @parameter  expression="${extension}"
     */
    String extension;

    /**
     * @parameter  expression="${extensions}"
     */
    String extensions;
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        if ( StringUtils.isEmpty ( extension ) && StringUtils.isEmpty ( extensions ) )
        {
            throw new MojoFailureException ( "-Dextension or -Dextensions must be set" );
        }
        else if ( StringUtils.isNotEmpty ( extension ) && StringUtils.isNotEmpty ( extensions ) )
        {
            throw new MojoFailureException ( "Only one of -Dextension or -Dextensions can be set" );
        }

        try
        {
            List extensionsToRemove = new ArrayList ();
            if ( StringUtils.isNotEmpty ( extension ) )
            {
                extensionsToRemove.add ( extension );
            }
            else
            {
                extensionsToRemove.addAll (
                    Arrays.asList ( StringUtils.split ( extensions, "," ) )
                );
            }

            ArchetypeRegistry registry =
                archetypeRegistryManager.readArchetypeRegistry ( archetypeRegistryFile );

            Iterator extensionsToRemoveIterator = extensionsToRemove.iterator ();
            while ( extensionsToRemoveIterator.hasNext () )
            {
                String extensionToRemove = (String) extensionsToRemoveIterator.next ();
                if ( registry.getFilteredExtensions ().contains ( extensionToRemove ) )
                {
                    registry.removeFilteredExtension ( extensionToRemove );
                    getLog ().debug ( "Extension " + extensionToRemove + " removed" );
                }
                else
                {
                    getLog ().debug ( "Extension " + extensionToRemove + " doesn't exist" );
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
