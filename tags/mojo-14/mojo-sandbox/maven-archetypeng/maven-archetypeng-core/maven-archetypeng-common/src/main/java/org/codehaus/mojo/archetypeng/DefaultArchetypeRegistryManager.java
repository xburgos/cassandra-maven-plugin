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

import org.codehaus.mojo.archetypeng.registry.ArchetypeRegistry;
import org.codehaus.mojo.archetypeng.registry.io.xpp3.ArchetypeRegistryXpp3Reader;
import org.codehaus.mojo.archetypeng.registry.io.xpp3.ArchetypeRegistryXpp3Writer;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

/**
 * @plexus.component
 */
public class DefaultArchetypeRegistryManager
extends AbstractLogEnabled
implements ArchetypeRegistryManager
{
    public List getArchetypeGroups ( File archetypeRegistryFile )
    {
        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry ( archetypeRegistryFile );
            return registry.getArchetypeGroups ();
        }
        catch ( IOException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList (
                new String[] { "org.apache.maven.archetypes", "org.codehaus.mojo.archetypes" }
                );
        }
        catch ( XmlPullParserException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList (
                new String[] { "org.apache.maven.archetypes", "org.codehaus.mojo.archetypes" }
                );
        }
    }

    public ArchetypeRegistry readArchetypeRegistry ( File archetypeRegistryFile )
    throws IOException, FileNotFoundException, XmlPullParserException
    {
        ArchetypeRegistryXpp3Reader reader = new ArchetypeRegistryXpp3Reader ();
        FileReader fileReader = new FileReader ( archetypeRegistryFile );

        try
        {
            return reader.read ( fileReader );
        }
        finally
        {
            IOUtil.close ( fileReader );
        }
    }

    public void writeArchetypeRegistry (
        File archetypeRegistryFile,
        ArchetypeRegistry archetypeRegistry
    )
    throws IOException
    {
        ArchetypeRegistryXpp3Writer writer = new ArchetypeRegistryXpp3Writer ();
        FileWriter fileWriter = new FileWriter ( archetypeRegistryFile );

        try
        {
            writer.write ( fileWriter, archetypeRegistry );
        }
        finally
        {
            IOUtil.close ( fileWriter );
        }
    }
}
