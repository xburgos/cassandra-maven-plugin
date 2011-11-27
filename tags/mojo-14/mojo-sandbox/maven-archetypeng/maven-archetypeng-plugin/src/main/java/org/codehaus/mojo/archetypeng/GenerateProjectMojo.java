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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.mojo.archetypeng.generator.ArchetypeGenerator;

import java.io.File;

import java.util.List;

/**
 * @author           rafale
 * @description      Generate sample project.
 * @requiresProject  false
 * @goal             generate-project
 */
public class GenerateProjectMojo
extends AbstractMojo
{
    /**
     * @parameter  default-value="${user.dir}"
     */
    private String basedir = System.getProperty ( "user.dir" );

    /**
     * @component
     */
    private ArchetypeGenerator generator;

    /**
     * Local maven repository.
     *
     * @parameter  expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter  default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * @parameter  expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List repositories;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            generator.generateArchetype ( propertyFile, localRepository, repositories, basedir );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }
}