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

package org.apache.maven.archetype.generator;

import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.exception.OutputFileExists;
import org.apache.maven.archetype.exception.PomFileExists;
import org.apache.maven.archetype.exception.ProjectDirectoryExists;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;

public interface ArchetypeGenerator
{
    String ROLE = ArchetypeGenerator.class.getName ();

    void generateArchetype (
        File propertyFile,
        ArtifactRepository localRepository,
        List repositories,
        String basedir
    )
    throws IOException,
        ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        ProjectDirectoryExists,
        PomFileExists,
        OutputFileExists,
        FileNotFoundException,
        XmlPullParserException,
        DocumentException,
        InvalidPackaging,
        ArchetypeGenerationFailure;
}
