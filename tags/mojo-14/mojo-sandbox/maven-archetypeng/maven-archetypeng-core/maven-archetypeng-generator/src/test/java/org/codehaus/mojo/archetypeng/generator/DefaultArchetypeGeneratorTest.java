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
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;

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
import java.util.Properties;

public class DefaultArchetypeGeneratorTest
extends AbstractMojoTestCase
{
    ArtifactRepository localRepository;
    List repositories;

    public void testArchetypeNotDefined ()
    throws Exception
    {
        System.out.println ( "testArchetypeNotDefined" );

        String project = "generate-2";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        try
        {
            instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

            fail ( "Exception must be thrown" );
        }
        catch ( ArchetypeNotDefined e )
        {
            assertEquals (
                "Exception not correct",
                "The archetype is not defined",
                e.getMessage ()
            );
        }
    }

    public void testGenerateArchetypeCompleteWithoutParent ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypeCompleteWithoutParent" );

        String project = "generate-4";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        File projectDirectory = new File ( basedir, "file-value" );
        if ( projectDirectory.exists () )
        {
            FileUtils.deleteDirectory ( projectDirectory );
        }

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        String template;
        template = "src/main/java/file/value/package/App.java";
        assertTemplateContent ( projectDirectory, template );

        template = "src/main/java/file/value/package/inner/package/App2.java";
        assertTemplateContent ( projectDirectory, template );

        template = "src/main/c/file/value/package/App.c";
        assertTemplateContent ( projectDirectory, template );

        template = "src/test/java/file/value/package/AppTest.java";
        assertTemplateContent ( projectDirectory, template );

        template = "src/test/c/file/value/package/AppTest.c";
        assertTemplateContent ( projectDirectory, template );

        template = "src/main/resources/App.properties";
        assertTemplateContent ( projectDirectory, template );

        template = "src/main/resources/inner/dir/App2.properties";
        assertTemplateContent ( projectDirectory, template );

        template = "src/main/mdo/App.mdo";
        assertTemplateContent ( projectDirectory, template );

        template = "src/test/resources/AppTest.properties";
        assertTemplateContent ( projectDirectory, template );

        template = "src/test/mdo/AppTest.mdo";
        assertTemplateContent ( projectDirectory, template );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNull ( model.getParent () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );
    }

    public void testGenerateArchetypeCompleteWithParent ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypeCompleteWithParent" );

        String project = "generate-5";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        File projectFile = getProjectFile ( project );
        File projectFileSample = getProjectSampleFile ( project );
        copy ( projectFileSample, projectFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        FileUtils.deleteDirectory ( basedir + File.separator + "file-value" );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertEquals ( "org.codehaus.mojo.archetypeng", model.getParent ().getGroupId () );
        assertEquals ( "test-generate-5-parent", model.getParent ().getArtifactId () );
        assertEquals ( "1.0-SNAPSHOT", model.getParent ().getVersion () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );

        Model parentModel = readPom ( projectFile );
        assertTrue ( parentModel.getModules ().contains ( "file-value" ) );
    }

    public void testGenerateArchetypePartialOnChild ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypePartialOnChild" );

        String project = "generate-8";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        File parentProjectFile = getProjectFile ( project );
        File parentProjectFileSample = getProjectSampleFile ( project );
        copy ( parentProjectFileSample, parentProjectFile );

        File projectFile = getProjectFile ( project + File.separator + "file-value" );
        File projectFileSample = getProjectSampleFile ( project + File.separator + "file-value" );
        copy ( projectFileSample, projectFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        FileUtils.deleteDirectory (
            basedir + File.separator + "file-value" + File.separator + "src"
        );
        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNotNull ( model.getParent () );
        assertEquals ( "org.codehaus.mojo.archetypeng", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "1.0-SNAPSHOT", model.getVersion () );
        assertTrue ( model.getModules ().isEmpty () );
        assertFalse ( model.getDependencies ().isEmpty () );
        assertFalse ( model.getBuild ().getPlugins ().isEmpty () );
        assertFalse ( model.getReporting ().getPlugins ().isEmpty () );
    }

    public void testGenerateArchetypePartialOnChildDontOverride ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypePartialOnChildDontOverride" );

        String project = "generate-9";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        File parentProjectFile = getProjectFile ( project );
        File parentProjectFileSample = getProjectSampleFile ( project );
        copy ( parentProjectFileSample, parentProjectFile );

        File projectFile = getProjectFile ( project + File.separator + "file-value" );
        File projectFileSample = getProjectSampleFile ( project + File.separator + "file-value" );
        copy ( projectFileSample, projectFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        FileUtils.deleteDirectory (
            basedir + File.separator + "file-value" + File.separator + "src"
        );
        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNotNull ( model.getParent () );
        assertEquals ( "org.codehaus.mojo.archetypeng", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "1.0-SNAPSHOT", model.getVersion () );
        assertTrue ( model.getModules ().isEmpty () );
        assertFalse ( model.getDependencies ().isEmpty () );
        assertEquals ( "1.0", ( (Dependency) model.getDependencies ().get ( 0 ) ).getVersion () );
        assertFalse ( model.getBuild ().getPlugins ().isEmpty () );
        assertEquals (
            "1.0",
            ( (Plugin) model.getBuild ().getPlugins ().get ( 0 ) ).getVersion ()
        );
        assertFalse ( model.getReporting ().getPlugins ().isEmpty () );
        assertEquals (
            "1.0",
            ( (ReportPlugin) model.getReporting ().getPlugins ().get ( 0 ) ).getVersion ()
        );
    }

    public void testGenerateArchetypePartialOnParent ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypePartialOnParent" );

        String project = "generate-7";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        File projectFile = getProjectFile ( project );
        File projectFileSample = getProjectSampleFile ( project );
        copy ( projectFileSample, projectFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        FileUtils.deleteDirectory ( basedir + File.separator + "src" );
        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project ) );
        assertNull ( model.getParent () );
        assertEquals ( "org.codehaus.mojo.archetypeng", model.getGroupId () );
        assertEquals ( "test-generate-7", model.getArtifactId () );
        assertEquals ( "1.0-SNAPSHOT", model.getVersion () );
        assertTrue ( model.getModules ().isEmpty () );
        assertFalse ( model.getBuild ().getPlugins ().isEmpty () );
    }

    public void testGenerateArchetypePartialWithoutPoms ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypePartialWithoutPoms" );

        String project = "generate-6";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        File projectDirectory = new File ( basedir, "file-value" );
        if ( projectDirectory.exists () )
        {
            FileUtils.deleteDirectory ( projectDirectory );
        }

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNull ( model.getParent () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );
    }

    public void testGenerateArchetypeSite ()
    throws Exception
    {
        System.out.println ( "testGenerateArchetypeSite" );

        String project = "generate-10";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        File projectDirectory = new File ( basedir, "file-value" );
        if ( projectDirectory.exists () )
        {
            FileUtils.deleteDirectory ( projectDirectory );
        }

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        String template;
        template = "src/site/site.xml";
        assertTemplateContent ( projectDirectory, template );

        template = "src/site/apt/test.apt";
        assertTemplateContent ( projectDirectory, template );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNull ( model.getParent () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );
    }

    public void testNoPropertyFile ()
    throws Exception
    {
        System.out.println ( "testNoPropertyFile" );

        String project = "generate-1";
        File propertyFile = getPropertiesFile ( project );
        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        try
        {
            instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

            fail ( "Exception must be thrown" );
        }
        catch ( FileNotFoundException e )
        {
            assertTrue (
                "Exception not correct",
                e.getMessage ().contains ( "No such file or directory" )
            );
        }
    }

    public void testPropertiesNotDefined ()
    throws Exception
    {
        System.out.println ( "testPropertiesNotDefined" );

        String project = "generate-3";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        try
        {
            instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

            fail ( "Exception must be thrown" );
        }
        catch ( ArchetypeNotConfigured e )
        {
            assertEquals (
                "Exception not correct",
                "The archetype is not configured",
                e.getMessage ()
            );
        }
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

    private void assertTemplateContent ( final File projectDirectory, final String template )
    throws IOException
    {
        File templateFile = new File ( projectDirectory, template );
        Properties properties = loadProperties ( templateFile );
        assertEquals ( "file-value", properties.getProperty ( "groupId" ) );
        assertEquals ( "file-value", properties.getProperty ( "artifactId" ) );
        assertEquals ( "file-value", properties.getProperty ( "version" ) );
        assertEquals ( "file.value.package", properties.getProperty ( "package" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-with-default-1" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-with-default-2" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-with-default-3" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-with-default-4" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-without-default-1" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-without-default-2" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-without-default-3" ) );
        assertEquals ( "file-value", properties.getProperty ( "property-without-default-4" ) );
    }

    private void copy ( final File in, final File out )
    throws IOException, FileNotFoundException
    {
        assertTrue ( !out.exists () || out.delete () );
        assertFalse ( out.exists () );
        IOUtil.copy ( new FileReader ( in ), new FileWriter ( out ) );
        assertTrue ( out.exists () );
        assertTrue ( in.exists () );
    }

    private void instanceDefined ( DefaultArchetypeGenerator instance )
    throws IllegalAccessException
    {
        assertNotNull ( instance );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypeArtifactManager" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypeFactory" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypePathResolver" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "archetypePropertiesManager" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "pomManager" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "velocity" ) );
    }

    private Properties loadProperties ( File propertyFile )
    throws IOException, FileNotFoundException
    {
        Properties properties = new Properties ();
        properties.load ( new FileReader ( propertyFile ) );
        return properties;
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
        return
            new File (
            getBasedir (),
            "target/test-classes/projects/" + project + "/archetype.properties.sample"
            );
    }

    private Model readPom ( final File pomFile )
    throws IOException, XmlPullParserException
    {
        Model generatedModel;
        FileReader pomReader = null;
        try
        {
            pomReader = new FileReader ( pomFile );

            MavenXpp3Reader reader = new MavenXpp3Reader ();

            generatedModel = reader.read ( pomReader );
        }
        finally
        {
            IOUtil.close ( pomReader );
        }
        return generatedModel;
    }
}
