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

import org.apache.maven.archetype.registry.ArchetypeRegistry;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;

public interface ArchetypeRegistryManager
{
    String ROLE = ArchetypeRegistryManager.class.getName ();

    public void addGroup ( String group, File archetypeRegistryFile )
    throws IOException, XmlPullParserException;

    /**
     */
    List getArchetypeGroups ( File archetypeRegistryFile );

    /**
     */
    List getFilteredExtensions ( String archetypeFilteredExtentions, File archetypeRegistryFile )
    throws IOException;

    /**
     */
    List getLanguages ( String archetypeLanguages, File archetypeRegistryFile )
    throws IOException;

    /**
     */
    ArchetypeRegistry readArchetypeRegistry ( File archetypeRegistryFile )
    throws IOException, FileNotFoundException, XmlPullParserException;

    /**
     */
    List getRepositories (
        List pomRemoteRepositories,
        String remoteRepositories,
        File archetypeRegistryFile
    )
    throws IOException, XmlPullParserException;

    /**
     */
    void writeArchetypeRegistry ( File archetypeRegistryFile, ArchetypeRegistry archetypeRegistry )
    throws IOException;
}
