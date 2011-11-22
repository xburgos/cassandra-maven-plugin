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

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePathResolver;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.ArchetypeTemplateResolver;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.Template;
import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.RequiredProperty;
import org.codehaus.mojo.archetypeng.archetype.ResourcesGroup;
import org.codehaus.mojo.archetypeng.archetype.SiteGroup;
import org.codehaus.mojo.archetypeng.archetype.SourcesGroup;
import org.codehaus.mojo.archetypeng.archetype.io.xpp3.ArchetypeDescriptorXpp3Writer;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.TemplateCreationException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeCreator
extends AbstractLogEnabled
implements ArchetypeCreator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypePathResolver archetypePathResolver;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeTemplateResolver archetypeTemplateResolver;

    /**
     * @plexus.requirement
     */
    private PomManager pomManager;

    public void createArchetype ( MavenProject project, File propertyFile )
    throws IOException, ArchetypeNotDefined, ArchetypeNotConfigured, TemplateCreationException
    {
        Properties properties = initialiseArchetypeProperties ( propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );
        if ( !archetypeDefinition.isDefined () )
        {
            throw new ArchetypeNotDefined ( "The archetype is not defined" );
        }

        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDefinition, properties );
        if ( !archetypeConfiguration.isConfigured () )
        {
            throw new ArchetypeNotConfigured ( "The archetype is not configured" );
        }

        File basedir = project.getBasedir ();
        File generatedSourcesDirectory =
            FileUtils.resolveFile ( basedir, getGeneratedSourcesDirectory () );
        generatedSourcesDirectory.mkdirs ();

        Model model = new Model ();
        model.setGroupId ( archetypeDefinition.getGroupId () );
        model.setArtifactId ( archetypeDefinition.getArtifactId () );
        model.setVersion ( archetypeDefinition.getVersion () );
        model.setPackaging ( "maven-plugin" );

        File archetypePomFile = FileUtils.resolveFile ( basedir, getArchetypePom () );
        archetypePomFile.getParentFile ().mkdirs ();
        pomManager.writePom ( model, archetypePomFile );

        File archetypeResourcesDirectory =
            FileUtils.resolveFile ( generatedSourcesDirectory, getTemplateOutputDirectory () );
        archetypeResourcesDirectory.mkdirs ();

        File archetypeDescriptorFile =
            FileUtils.resolveFile (
                archetypeResourcesDirectory,
                archetypePathResolver.getDescriptorPath ()
            );
        archetypeDescriptorFile.getParentFile ().mkdirs ();

        List templates = archetypeTemplateResolver.resolveTemplates ( basedir );

        Properties reverseProperties = new Properties ();
        reverseProperties.putAll ( properties );
        reverseProperties.remove ( Constants.GROUP_ID );

        Properties pomReversedProperties = new Properties ();
        pomReversedProperties.putAll ( properties );
        pomReversedProperties.remove ( Constants.PACKAGE );

        generateReversedTemplates (
            templates,
            basedir,
            archetypeResourcesDirectory,
            archetypeConfiguration.getProperty ( Constants.PACKAGE ),
            reverseProperties,
            pomReversedProperties
        );

        ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor ();
        archetypeDescriptor.setName ( archetypeDefinition.getArtifactId () );

        addTemplates (
            archetypeDescriptor,
            templates,
            archetypeConfiguration.getProperty ( Constants.PACKAGE )
        );
        addRequiredProperties ( archetypeDescriptor, properties );

        ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer ();
        writer.write ( new FileWriter ( archetypeDescriptorFile ), archetypeDescriptor );
    }

    private void addRequiredProperties (
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        Properties requiredProperties = new Properties ();
        requiredProperties.putAll ( properties );
        requiredProperties.remove ( Constants.ARCHETYPE_GROUP_ID );
        requiredProperties.remove ( Constants.ARCHETYPE_ARTIFACT_ID );
        requiredProperties.remove ( Constants.ARCHETYPE_VERSION );
        requiredProperties.remove ( Constants.GROUP_ID );
        requiredProperties.remove ( Constants.ARTIFACT_ID );
        requiredProperties.remove ( Constants.VERSION );
        requiredProperties.remove ( Constants.PACKAGE );

        Iterator propertiesIterator = requiredProperties.keySet ().iterator ();
        while ( propertiesIterator.hasNext () )
        {
            String propertyKey = (String) propertiesIterator.next ();
            RequiredProperty requiredProperty = new RequiredProperty ();
            requiredProperty.setKey ( propertyKey );
            requiredProperty.setDefaultValue ( requiredProperties.getProperty ( propertyKey ) );
            archetypeDescriptor.addRequiredProperty ( requiredProperty );
        }
    }

    private void addResourcesGroups (
        final List resourcesGroupsTemplates,
        final ArchetypeDescriptor archetypeDescriptor
    )
    {
        Iterator groupTemplatesIterator = resourcesGroupsTemplates.iterator ();
        while ( groupTemplatesIterator.hasNext () )
        {
            List archetypeTemplates = new ArrayList ();
            ResourcesGroup resourcesGroup = new ResourcesGroup ();
            resourcesGroup.setTemplates ( archetypeTemplates );

            Iterator templatesIterator = ( (List) groupTemplatesIterator.next () ).iterator ();
            while ( templatesIterator.hasNext () )
            {
                Template template = (Template) templatesIterator.next ();
                resourcesGroup.setDirectory ( template.getDirectory () );
                archetypeTemplates.add ( archetypePathResolver.getTemplatePath ( template ) );
            }
            archetypeDescriptor.addResourcesGroup ( resourcesGroup );
        }
    }

    private void addSiteGroup (
        final List siteResourceGroupTemplates,
        final ArchetypeDescriptor archetypeDescriptor
    )
    {
        Iterator groupTemplatesIterator = siteResourceGroupTemplates.iterator ();
        List archetypeTemplates = new ArrayList ();
        SiteGroup siteGroup = new SiteGroup ();
        siteGroup.setTemplates ( archetypeTemplates );
        archetypeDescriptor.setSite ( siteGroup );
        while ( groupTemplatesIterator.hasNext () )
        {
            Template template = (Template) groupTemplatesIterator.next ();
            siteGroup.addTemplate ( archetypePathResolver.getTemplatePath ( template ) );
        }
    }

    private void addSourcesGroups (
        final List sourcesGroupsTemplates,
        final ArchetypeDescriptor archetypeDescriptor,
        String packageName
    )
    {
        Iterator groupTemplatesIterator = sourcesGroupsTemplates.iterator ();
        while ( groupTemplatesIterator.hasNext () )
        {
            List archetypeTemplates = new ArrayList ();
            SourcesGroup sourcesGroup = new SourcesGroup ();
            sourcesGroup.setTemplates ( archetypeTemplates );

            Iterator templatesIterator = ( (List) groupTemplatesIterator.next () ).iterator ();
            while ( templatesIterator.hasNext () )
            {
                Template template = (Template) templatesIterator.next ();
                sourcesGroup.setLanguage ( template.getLanguage () );
                archetypeTemplates.add (
                    archetypePathResolver.getTemplatePathWithoutPackage ( template, packageName )
                );
            }
            archetypeDescriptor.addSourcesGroup ( sourcesGroup );
        }
    }

    private void addTemplates (
        ArchetypeDescriptor archetypeDescriptor,
        List templates,
        String packageName
    )
    {
        List sourcesGroupsTemplates =
            archetypeTemplateResolver.getSourcesGroupsTemplates ( templates, false );
        addSourcesGroups ( sourcesGroupsTemplates, archetypeDescriptor, packageName );

        List testSourcesGroupsTemplates =
            archetypeTemplateResolver.getSourcesGroupsTemplates ( templates, true );
        addTestSourcesGroups ( testSourcesGroupsTemplates, archetypeDescriptor, packageName );

        List resourcesGroupsTemplates =
            archetypeTemplateResolver.getResourcesGroupsTemplates ( templates, false );
        addResourcesGroups ( resourcesGroupsTemplates, archetypeDescriptor );

        List testResourcesGroupsTemplates =
            archetypeTemplateResolver.getResourcesGroupsTemplates ( templates, true );
        addTestResourcesGroup ( testResourcesGroupsTemplates, archetypeDescriptor );

        List siteResourceGroupTemplates =
            archetypeTemplateResolver.getSiteResourceGroupTemplates ( templates );
        addSiteGroup ( siteResourceGroupTemplates, archetypeDescriptor );
    }

    private void addTestResourcesGroup (
        final List testResourcesGroupsTemplates,
        final ArchetypeDescriptor archetypeDescriptor
    )
    {
        Iterator groupTemplatesIterator = testResourcesGroupsTemplates.iterator ();
        while ( groupTemplatesIterator.hasNext () )
        {
            List archetypeTemplates = new ArrayList ();
            ResourcesGroup resourcesGroup = new ResourcesGroup ();
            resourcesGroup.setTemplates ( archetypeTemplates );

            Iterator templatesIterator = ( (List) groupTemplatesIterator.next () ).iterator ();
            while ( templatesIterator.hasNext () )
            {
                Template template = (Template) templatesIterator.next ();
                resourcesGroup.setDirectory ( template.getDirectory () );
                archetypeTemplates.add ( archetypePathResolver.getTemplatePath ( template ) );
            }
            archetypeDescriptor.addTestResourcesGroup ( resourcesGroup );
        }
    }

    private void addTestSourcesGroups (
        final List testSourcesGroupsTemplates,
        final ArchetypeDescriptor archetypeDescriptor,
        String packageName
    )
    {
        Iterator groupTemplatesIterator = testSourcesGroupsTemplates.iterator ();
        while ( groupTemplatesIterator.hasNext () )
        {
            List archetypeTemplates = new ArrayList ();
            SourcesGroup sourcesGroup = new SourcesGroup ();
            sourcesGroup.setTemplates ( archetypeTemplates );

            Iterator templatesIterator = ( (List) groupTemplatesIterator.next () ).iterator ();
            while ( templatesIterator.hasNext () )
            {
                Template template = (Template) templatesIterator.next ();
                sourcesGroup.setLanguage ( template.getLanguage () );
                archetypeTemplates.add (
                    archetypePathResolver.getTemplatePathWithoutPackage ( template, packageName )
                );
            }
            archetypeDescriptor.addTestSourcesGroup ( sourcesGroup );
        }
    }

    private String getArchetypePom ()
    {
        return getGeneratedSourcesDirectory () + File.separator + Constants.ARCHETYPE_POM;
    }

    private String getGeneratedSourcesDirectory ()
    {
        return "target" + File.separator + "generated-sources" + File.separator + "archetypeng";
    }

    private void generateReversedTemplates (
        List templates,
        File sourcesDirectory,
        File templateDirectory,
        String packageName,
        Properties reverseProperties,
        Properties pomReversedProperties
    )
    throws IOException
    {
        Iterator templatesIterator = templates.iterator ();
        while ( templatesIterator.hasNext () )
        {
            Template template = (Template) templatesIterator.next ();

            File sourceFile =
                FileUtils.resolveFile ( sourcesDirectory, template.getTemplatePath () );

            Properties properties;
            if ( template.isPom () )
            {
                properties = pomReversedProperties;
            }
            else
            {
                properties = reverseProperties;
            }

            String initialcontent = FileUtils.fileRead ( sourceFile );
            String content = getReversedContent ( initialcontent, properties );

            File outputFile = null;
            if ( template.isPom () )
            {
                outputFile =
                    FileUtils.resolveFile (
                        templateDirectory,
                        archetypePathResolver.getTemplatePomPath ()
                    );
            }
            else if ( template.isSiteResource () )
            {
                outputFile =
                    FileUtils.resolveFile (
                        templateDirectory,
                        archetypePathResolver.getTemplateSitePath ( template )
                    );
            }
            else if ( template.isResource () )
            {
                outputFile =
                    FileUtils.resolveFile (
                        templateDirectory,
                        archetypePathResolver.getTemplateResourcePath ( template )
                    );
            }
            else
            { // template.isSource()
                outputFile =
                    FileUtils.resolveFile (
                        templateDirectory,
                        archetypePathResolver.getTemplateSourcePath ( template, packageName )
                    );
            }
            outputFile.getParentFile ().mkdirs ();
            FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );
        } // end while
    }

    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    private String getReversedContent ( String content, final Properties properties )
    {
        String result = content;
        Iterator propertyIterator = properties.keySet ().iterator ();
        while ( propertyIterator.hasNext () )
        {
            String propertyKey = (String) propertyIterator.next ();
            result =
                StringUtils.replace (
                    result,
                    properties.getProperty ( propertyKey ),
                    "${" + propertyKey + "}"
                );
        }
        return result;
    }

    private String getTemplateOutputDirectory ()
    {
        return
            Constants.SRC + File.separator + Constants.MAIN + File.separator + Constants.RESOURCES;
    }
}
