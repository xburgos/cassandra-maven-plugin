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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.RequiredProperty;
import org.codehaus.mojo.archetypeng.archetype.ResourcesGroup;
import org.codehaus.mojo.archetypeng.archetype.SourcesGroup;
import org.codehaus.mojo.archetypeng.archetype.io.xpp3.ArchetypeDescriptorXpp3Reader;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

public class DefaultArchetypeCreatorTest
extends AbstractMojoTestCase
{
    private DefaultArtifactRepository localRepository;

    private List repositories;

    public void testCreateArchetype ()
    throws Exception
    {
        System.out.println ( "testCreateArchetype" );

        MavenProjectBuilder builder = (MavenProjectBuilder) lookup ( MavenProjectBuilder.ROLE );

        String project = "create-1";

        File projectFile = getProjectFile ( project );
        File projectFileSample = getProjectSampleFile ( project );
        copy ( projectFileSample, projectFile );

        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );

        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        MavenProject mavenProject =
            builder.buildWithDependencies ( projectFile, localRepository, null );
        DefaultArchetypeCreator instance =
            (DefaultArchetypeCreator) lookup ( ArchetypeCreator.class.getName () );
        instanceDefined ( instance );

        instance.createArchetype ( mavenProject, propertyFile );

        File template = getTemplateFile ( project, "pom.xml" );
        assertTrue ( template.exists () );
        assertContent ( template, "${groupId}" );
        assertContent ( template, "${artifactId}" );
        assertContent ( template, "${version}" );
        assertContent ( template, "Maven ArchetypeNG Test create-1" );
        assertContent ( template, "pom" );
        assertNotContent ( template, "<parent>" );

        template = getTemplateFile ( project, "src/main/java/subfolder1/App.java" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "package ${package}.subfolder1;" );
        assertNotContent ( template, "// ${someProperty}" );

        template = getTemplateFile ( project, "src/main/java/subfolder2/App.java" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "package ${package}.subfolder2;" );

        template = getTemplateFile ( project, "src/main/resources/log4j.properties" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "#${package}" );

        template = getTemplateFile ( project, "src/main/webapp/WEB-INF/web.xml" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "${package}" );

        template = getTemplateFile ( project, "src/site/site.xml" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "${package}" );

        template = getTemplateFile ( project, "src/test/java/test/AppTest.java" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "package ${package}.test;" );

        template = getTemplateFile ( project, "src/test/resources/log4j.properties" );
        assertTrue ( template.exists () );
        assertNotContent ( template, "#${package}" );

        ArchetypeDescriptor descriptor = getDescriptor ( project );
        assertEquals ( "maven-archetype-test", descriptor.getName () );
        assertEquals ( 1, descriptor.getSite ().getTemplates ().size () );
        assertEquals ( "site.xml", descriptor.getSite ().getTemplates ().get ( 0 ) );
        assertEquals ( 1, descriptor.getSourcesGroups ().size () );
        assertEquals (
            "java",
            ( (SourcesGroup) descriptor.getSourcesGroups ().get ( 0 ) ).getLanguage ()
        );
        assertEquals (
            2,
            ( (SourcesGroup) descriptor.getSourcesGroups ().get ( 0 ) ).getTemplates ().size ()
        );
        assertEquals (
            "subfolder1/App.java",
            ( (SourcesGroup) descriptor.getSourcesGroups ().get ( 0 ) ).getTemplates ().get ( 0 )
        );
        assertEquals (
            "subfolder2/App.java",
            ( (SourcesGroup) descriptor.getSourcesGroups ().get ( 0 ) ).getTemplates ().get ( 1 )
        );
        assertEquals ( 2, descriptor.getResourcesGroups ().size () );
        assertEquals (
            "resources",
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 0 ) ).getDirectory ()
        );
        assertEquals (
            1,
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 0 ) ).getTemplates ()
            .size ()
        );
        assertEquals (
            "log4j.properties",
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 0 ) ).getTemplates ().get (
                0
            )
        );
        assertEquals (
            "webapp",
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 1 ) ).getDirectory ()
        );
        assertEquals (
            1,
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 1 ) ).getTemplates ()
            .size ()
        );
        assertEquals (
            "WEB-INF/web.xml",
            ( (ResourcesGroup) descriptor.getResourcesGroups ().get ( 1 ) ).getTemplates ().get (
                0
            )
        );
        assertEquals ( 1, descriptor.getTestSourcesGroups ().size () );
        assertEquals (
            "java",
            ( (SourcesGroup) descriptor.getTestSourcesGroups ().get ( 0 ) ).getLanguage ()
        );
        assertEquals (
            1,
            ( (SourcesGroup) descriptor.getTestSourcesGroups ().get ( 0 ) ).getTemplates ()
            .size ()
        );
        assertEquals (
            "test/AppTest.java",
            ( (SourcesGroup) descriptor.getTestSourcesGroups ().get ( 0 ) ).getTemplates ().get (
                0
            )
        );
        assertEquals ( 1, descriptor.getTestResourcesGroups ().size () );
        assertEquals (
            "resources",
            ( (ResourcesGroup) descriptor.getTestResourcesGroups ().get ( 0 ) ).getDirectory ()
        );
        assertEquals (
            1,
            ( (ResourcesGroup) descriptor.getTestResourcesGroups ().get ( 0 ) ).getTemplates ()
            .size ()
        );
        assertEquals (
            "log4j.properties",
            ( (ResourcesGroup) descriptor.getTestResourcesGroups ().get ( 0 ) ).getTemplates ()
            .get ( 0 )
        );
        assertEquals ( 1, descriptor.getRequiredProperties ().size () );
        assertEquals (
            "someProperty",
            ( (RequiredProperty) descriptor.getRequiredProperties ().get ( 0 ) ).getKey ()
        );
        assertEquals (
            "A String to search for",
            ( (RequiredProperty) descriptor.getRequiredProperties ().get ( 0 ) )
            .getDefaultValue ()
        );

        File pom = getArchetypePom ( project );
        assertTrue ( pom.exists () );

        Model archetypeModel =
            ( (PomManager) getVariableValueFromObject ( instance, "pomManager" ) ).readPom ( pom );
        assertEquals ( "org.codehaus.mojo.archetypes", archetypeModel.getGroupId () );
        assertEquals ( "maven-archetype-test", archetypeModel.getArtifactId () );
        assertEquals ( "1.0", archetypeModel.getVersion () );
    }

    protected void tearDown ()
    throws Exception
    {
        super.tearDown ();
    }

    protected void setUp ()
    throws Exception
    {
        super.setUp ();

        localRepository =
            new DefaultArtifactRepository (
                "local",
                new File ( getBasedir (), "target/test-classes/repositories/local" ).toURI ()
                .toString (),
                new DefaultRepositoryLayout ()
            );

        repositories =
            Arrays.asList (
                new ArtifactRepository[] { new DefaultArtifactRepository (
                        "central",
                        new File ( getBasedir (), "target/test-classes/repositories/central" )
                        .toURI ().toString (),
                        new DefaultRepositoryLayout ()
                    ) }
            );
    }

    private File getArchetypePom ( String project )
    {
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/target/generated-sources/archetypeng/"
            + "pom.xml"
            );
    }

    private boolean assertContent ( File template, String content )
    throws FileNotFoundException, IOException
    {
        String templateContent = IOUtil.toString ( new FileReader ( template ) );
        return StringUtils.countMatches ( templateContent, content ) > 0;
    }

    private boolean assertNotContent ( File template, String content )
    throws FileNotFoundException, IOException
    {
        return !assertContent ( template, content );
    }

    private void copy ( File in, File out )
    throws IOException, FileNotFoundException
    {
        assertTrue ( !out.exists () || out.delete () );
        assertFalse ( out.exists () );
        IOUtil.copy ( new FileReader ( in ), new FileWriter ( out ) );
        assertTrue ( out.exists () );
        assertTrue ( in.exists () );
    }

    private ArchetypeDescriptor getDescriptor ( String project )
    throws FileNotFoundException, IOException, XmlPullParserException
    {
        ArchetypeDescriptorXpp3Reader reader = new ArchetypeDescriptorXpp3Reader ();
        return reader.read ( new FileReader ( getDescriptorFile ( project ) ) );
    }

    private File getDescriptorFile ( String project )
    {
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/target/generated-sources/archetypeng/"
            + "src/main/resources/"
            + "META-INF/maven/archetype.xml"
            );
    }

    private void instanceDefined ( DefaultArchetypeCreator instance )
    throws IllegalAccessException
    {
        assertNotNull ( instance );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypeFactory" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypePathResolver" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypePropertiesManager" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypeTemplateResolver" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "pomManager" ) );
    }

    private File getProjectFile ( String project )
    {
        return new File ( getBasedir (), "target/test-classes/projects/" + project + "/pom.xml" );
    }

    private File getProjectSampleFile ( String project )
    {
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/pom.xml.sample"
            );
    }

    private File getPropertiesFile ( String project )
    {
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/archetype.properties"
            );
    }

    private File getPropertiesSampleFile ( final String project )
    {
        File propertyFileSample =
            new File (
                getBasedir (),
                "target/test-classes/projects/" + project + "/archetype.properties.sample"
            );
        return propertyFileSample;
    }

    private File getTemplateFile ( String project, String template )
    {
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/target/generated-sources/archetypeng/"
            + "src/main/resources/"
            + "archetype-resources/" + template
            );
    }
}
