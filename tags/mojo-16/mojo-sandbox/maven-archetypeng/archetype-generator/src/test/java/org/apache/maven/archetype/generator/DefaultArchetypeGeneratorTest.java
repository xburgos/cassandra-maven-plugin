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

import org.apache.maven.archetype.common.DefaultArchetypeFilesResolver;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.metadata.FileSet;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
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
        assertDeleted ( projectDirectory );

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
        assertDeleted ( new File ( basedir, "file-value" ) );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertEquals ( "org.apache.maven.archetype", model.getParent ().getGroupId () );
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
        assertDeleted ( new File ( basedir, "file-value" + File.separator + "src" ) );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNotNull ( model.getParent () );
        assertEquals ( "org.apache.maven.archetype", model.getGroupId () );
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
        assertDeleted ( new File ( basedir, "file-value" + File.separator + "src" ) );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNotNull ( model.getParent () );
        assertEquals ( "org.apache.maven.archetype", model.getGroupId () );
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
        assertDeleted ( new File ( basedir, "src" ) );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model = readPom ( getProjectFile ( project ) );
        assertNull ( model.getParent () );
        assertEquals ( "org.apache.maven.archetype", model.getGroupId () );
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
        assertDeleted ( projectDirectory );

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
        assertDeleted ( projectDirectory );

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

    public void testGenerateFileSetArchetype ()
    throws Exception
    {
        System.out.println ( "testGenerateFileSetArchetype" );

        String project = "generate-12";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        File projectDirectory = new File ( basedir, "file-value" );
        assertDeleted ( projectDirectory );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        Model model;
        String template;

        template = "src/main/java/file/value/package/App.java";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = "src/main/java/file/value/package/inner/package/App2.java";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = "src/main/java/file/value/package/App.ogg";
        assertTemplateCopiedWithFileSetArchetype ( projectDirectory, template );

        template = "src/main/resources/App.properties";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = "src/main/resources/some-dir/App.png";
        assertTemplateCopiedWithFileSetArchetype ( projectDirectory, template );

        template = "src/site/site.xml";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = "src/site/apt/usage.apt";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = ".classpath";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        template = "profiles.xml";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "file-value"
        );

        model = readPom ( getProjectFile ( project + File.separator + "file-value" ) );
        assertNull ( model.getParent () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "file-value", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );

        template = "subproject/src/main/java/file/value/package/App.java";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "subproject"
        );

        model =
            readPom ( getProjectFile ( project + File.separator + "file-value" + "/subproject/" ) );
        assertNotNull ( model.getParent () );
        assertEquals ( "file-value", model.getParent ().getGroupId () );
        assertEquals ( "file-value", model.getParent ().getArtifactId () );
        assertEquals ( "file-value", model.getParent ().getVersion () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "subproject", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );

        template = "subproject/subsubproject/src/main/java/file/value/package/App.java";
        assertTemplateContentGeneratedWithFileSetArchetype (
            projectDirectory,
            template,
            "subsubproject"
        );

        model =
            readPom (
                getProjectFile (
                    project + File.separator + "file-value" + "/subproject/subsubproject/"
                )
            );
        assertNotNull ( model.getParent () );
        assertEquals ( "file-value", model.getParent ().getGroupId () );
        assertEquals ( "subproject", model.getParent ().getArtifactId () );
        assertEquals ( "file-value", model.getParent ().getVersion () );
        assertEquals ( "file-value", model.getGroupId () );
        assertEquals ( "subsubproject", model.getArtifactId () );
        assertEquals ( "file-value", model.getVersion () );
    }

    public void testGenerateOldArchetype ()
    throws Exception
    {
        System.out.println ( "testGenerateOldArchetype" );

        String project = "generate-11";
        File propertyFile = getPropertiesFile ( project );
        File propertyFileSample = getPropertiesSampleFile ( project );
        copy ( propertyFileSample, propertyFile );

        String basedir = propertyFile.getParent ();

        File projectDirectory = new File ( basedir, "file-value" );
        assertDeleted ( projectDirectory );

        DefaultArchetypeGenerator instance =
            (DefaultArchetypeGenerator) lookup ( ArchetypeGenerator.ROLE );
        instanceDefined ( instance );

        instance.generateArchetype ( propertyFile, localRepository, repositories, basedir );

        String template;
        template = "src/main/java/file/value/package/App.java";
        assertTemplateContentGeneratedWithOldArchetype ( projectDirectory, template );

        template = "src/main/resources/App.properties";
        assertTemplateContentGeneratedWithOldArchetype ( projectDirectory, template );

        template = "src/site/site.xml";
        assertTemplateContentGeneratedWithOldArchetype ( projectDirectory, template );

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
            /* Commented out as error messages are localised by default, and I (rafale)
             * don't know how to have unlocalised messages from the exception object. String
             * errorMessage = e.getMessage (); assertTrue (  "Exception not correct",
             * errorMessage.contains ( "No such file or directory"  ) || errorMessage.contains (
             * "The system cannot find the file specified" ) );
             */
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

    public void testResourceFiltering ()
    throws Exception
    {
        FileSet fileSet = new FileSet ();

        fileSet.addInclude ( "**/*.java" );

        fileSet.setDirectory ( "src/main/java" );
        fileSet.setEncoding ( "UTF-8" );
        fileSet.setPackaged ( true );
        fileSet.setFiltered ( true );

        List archetypeResources = new ArrayList ();

        archetypeResources.add ( "pom.xml" );
        archetypeResources.add ( "App.java" );
        archetypeResources.add ( "src/main/c/App.c" );
        archetypeResources.add ( "src/main/java/App.java" );
        archetypeResources.add ( "src/main/java/inner/package/App2.java" );
        archetypeResources.add ( "src/main/mdo/App.mdo" );
        archetypeResources.add ( "src/main/resources/App.properties" );
        archetypeResources.add ( "src/main/resources/inner/dir/App2.properties" );
        archetypeResources.add ( "src/test/c/AppTest.c" );
        archetypeResources.add ( "src/test/java/AppTest.java" );
        archetypeResources.add ( "src/test/mdo/AppTest.mdo" );
        archetypeResources.add ( "src/test/resources/AppTest.properties" );

        System.out.println ( "FileSet:" + fileSet );
        System.out.println ( "Resources:" + archetypeResources );

        DefaultArchetypeFilesResolver resolver = new DefaultArchetypeFilesResolver ();

        List fileSetResources = resolver.filterFiles ( "", fileSet, archetypeResources );
        System.out.println ( "Result:" + fileSetResources );
        assertEquals ( 2, fileSetResources.size () );
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
                new ArtifactRepository[]
                {
                    new DefaultArtifactRepository (
                        "central",
                        new File ( getBasedir (), "target/test-classes/repositories/central" )
                        .toURI ().toString (),
                        new DefaultRepositoryLayout ()
                    )
                }
            );
    }

    /**
     * This method attempts to delete a directory or file if it exists. If the file exists after
     * deletion, it throws a failure.
     *
     * @param  file  to delete.
     */
    private void assertDeleted ( File file )
    {
        if ( file.exists () )
        {
            if ( file.isDirectory () )
            {
                try
                {
                    FileUtils.deleteDirectory ( file );
                }
                catch ( IOException e )
                {
                    fail ( "Unable to delete directory:" + file + ":" + e.getLocalizedMessage () );
                }
            }
        }
        else
        {
            try
            {
                FileUtils.forceDelete ( file );
            }
            catch ( IOException e )
            {
                fail ( "Unable to delete file:" + file + ":" + e.getLocalizedMessage () );
                e.printStackTrace ();
            }
        }

        if ( file.exists () )
        {
            fail ( "File not deleted:" + file );
        }
    }

    private void assertTemplateContent ( final File projectDirectory, final String template )
    throws IOException
    {
        Properties properties = loadProperties ( projectDirectory, template );
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

    private void assertTemplateContentGeneratedWithFileSetArchetype (
        File projectDirectory,
        String template,
        String artifactId
    )
    throws IOException
    {
        Properties properties = loadProperties ( projectDirectory, template );
        assertEquals ( "file-value", properties.getProperty ( "groupId" ) );
        assertEquals ( artifactId, properties.getProperty ( "artifactId" ) );
        assertEquals ( "file-value", properties.getProperty ( "version" ) );
        assertEquals ( "file.value.package", properties.getProperty ( "package" ) );
    }

    private void assertTemplateContentGeneratedWithOldArchetype (
        final File projectDirectory,
        final String template
    )
    throws IOException
    {
        Properties properties = loadProperties ( projectDirectory, template );
        assertEquals ( "file-value", properties.getProperty ( "groupId" ) );
        assertEquals ( "file-value", properties.getProperty ( "artifactId" ) );
        assertEquals ( "file-value", properties.getProperty ( "version" ) );
        assertEquals ( "file.value.package", properties.getProperty ( "package" ) );
    }

    private void assertTemplateCopiedWithFileSetArchetype ( File projectDirectory, String template )
    throws IOException
    {
        Properties properties = loadProperties ( projectDirectory, template );
        assertEquals ( "${groupId}", properties.getProperty ( "groupId" ) );
        assertEquals ( "${artifactId}", properties.getProperty ( "artifactId" ) );
        assertEquals ( "${version}", properties.getProperty ( "version" ) );
        assertEquals ( "${package}", properties.getProperty ( "package" ) );
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
        assertNotNull ( getVariableValueFromObject ( instance, "archetypePropertiesManager" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "oldArchetype" ) );
        assertNotNull ( getVariableValueFromObject ( instance, "filesetGenerator" ) );
    }

    private Properties loadProperties ( File propertyFile )
    throws IOException, FileNotFoundException
    {
        Properties properties = new Properties ();
        properties.load ( new FileInputStream ( propertyFile ) );
        return properties;
    }

    private Properties loadProperties ( final File projectDirectory, final String template )
    throws IOException
    {
        File templateFile = new File ( projectDirectory, template );
        if ( !templateFile.exists () )
        {
            fail ( "Missing File:" + templateFile );
        }

        Properties properties = loadProperties ( templateFile );
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
