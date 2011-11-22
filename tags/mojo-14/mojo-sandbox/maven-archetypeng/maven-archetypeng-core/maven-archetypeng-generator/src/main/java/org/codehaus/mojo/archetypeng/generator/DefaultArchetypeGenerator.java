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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePathResolver;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.ResourcesGroup;
import org.codehaus.mojo.archetypeng.archetype.SiteGroup;
import org.codehaus.mojo.archetypeng.archetype.SourcesGroup;
import org.codehaus.mojo.archetypeng.exception.ArchetypeGenerationFailure;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.InvalidPackaging;
import org.codehaus.mojo.archetypeng.exception.OutputFileExists;
import org.codehaus.mojo.archetypeng.exception.PomFileExists;
import org.codehaus.mojo.archetypeng.exception.ProjectDirectoryExists;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeGenerator
extends AbstractLogEnabled
implements ArchetypeGenerator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

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
    private PomManager pomManager;

    /**
     * @plexus.requirement
     */
    private VelocityComponent velocity;

    public void generateArchetype (
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
        ArchetypeGenerationFailure
    {
        Properties properties = initialiseArchetypeProperties ( propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );

        if ( !archetypeDefinition.isDefined () )
        {
            throw new ArchetypeNotDefined ( "The archetype is not defined" );
        }

        ArchetypeDescriptor archetypeDescriptor =
            archetypeArtifactManager.getArchetypeDescriptor (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );
        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );

        if ( !archetypeConfiguration.isConfigured () )
        {
            throw new ArchetypeNotConfigured ( "The archetype is not configured" );
        }

        Context context = prepareVelocityContext ( archetypeConfiguration );
        String packageName =
            archetypeConfiguration.getProperties ().getProperty ( Constants.PACKAGE );

        String artifactId =
            archetypeConfiguration.getProperties ().getProperty ( Constants.ARTIFACT_ID );
        File outputDirectoryFile = new File ( basedir, artifactId );
        File basedirPom = new File ( basedir, Constants.ARCHETYPE_POM );
        File pom = new File ( outputDirectoryFile, Constants.ARCHETYPE_POM );

        ClassLoader archetypeJarLoader =
            archetypeArtifactManager.getArchetypeJarLoader (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );

        ClassLoader old = Thread.currentThread ().getContextClassLoader ();
        Thread.currentThread ().setContextClassLoader ( archetypeJarLoader );
        try
        {
            if ( archetypeDescriptor.isPartial () )
            {
                if ( outputDirectoryFile.exists () )
                {
                    if ( !pom.exists () )
                    {
                        throw new PomFileExists ( "The pom file already exists" );
                    }
                    else
                    {
                        processPomWithMerge ( archetypeDescriptor, context, pom );
                        processArchetypeTemplatesWithWarning (
                            archetypeDescriptor,
                            context,
                            packageName,
                            outputDirectoryFile
                        );
                    }
                }
                else
                {
                    if ( basedirPom.exists () )
                    {
                        processPomWithMerge ( archetypeDescriptor, context, basedirPom );
                        processArchetypeTemplatesWithWarning (
                            archetypeDescriptor,
                            context,
                            packageName,
                            new File ( basedir )
                        );
                    }
                    else
                    {
                        processPom ( archetypeDescriptor, context, pom );
                        processArchetypeTemplates (
                            archetypeDescriptor,
                            context,
                            packageName,
                            outputDirectoryFile
                        );
                    }
                }
            }
            else
            {
                if ( outputDirectoryFile.exists () )
                {
                    throw new ProjectDirectoryExists ( "The project directory already exists" );
                }
                else
                {
                    if ( basedirPom.exists () )
                    {
                        processPomWithParent (
                            archetypeDescriptor,
                            context,
                            pom,
                            basedirPom,
                            artifactId
                        );
                    }
                    else
                    {
                        processPom ( archetypeDescriptor, context, pom );
                    }

                    processArchetypeTemplates (
                        archetypeDescriptor,
                        context,
                        packageName,
                        outputDirectoryFile
                    );
                }
            }
        }
        finally
        {
            Thread.currentThread ().setContextClassLoader ( old );
        }
    }

    private String getEncoding ( String archetypeEncoding )
    {
        return
            ( ( null == archetypeEncoding ) || "".equals ( archetypeEncoding ) )
            ? "UTF-8"
            : archetypeEncoding;
    }

    private String getEncoding ( String templateGroupEncoding, String archetypeEncoding )
    {
        return
            ( ( null == templateGroupEncoding ) || "".equals ( templateGroupEncoding ) )
            ? getEncoding ( archetypeEncoding )
            : templateGroupEncoding;
    }

    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    private Context prepareVelocityContext ( ArchetypeConfiguration archetypeConfiguration )
    {
        Context context = new VelocityContext ();
        Iterator iterator = archetypeConfiguration.getProperties ().keySet ().iterator ();
        while ( iterator.hasNext () )
        {
            String key = (String) iterator.next ();

            Object value = archetypeConfiguration.getProperties ().getProperty ( key );

            context.put ( key, value );
        }
        return context;
    }

    private void processArchetypeTemplates (
        ArchetypeDescriptor archetypeDescriptor,
        Context context,
        String packageName,
        File outputDirectoryFile
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        processTemplates ( packageName, outputDirectoryFile, context, archetypeDescriptor, false );
    }

    private void processArchetypeTemplatesWithWarning (
        ArchetypeDescriptor archetypeDescriptor,
        Context context,
        String packageName,
        File outputDirectoryFile
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        processTemplates ( packageName, outputDirectoryFile, context, archetypeDescriptor, true );
    }

    private void processPom ( ArchetypeDescriptor archetypeDescriptor, Context context, File pom )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        processTemplate (
            pom,
            context,
            archetypePathResolver.getTemplatePomPath (),
            getEncoding ( archetypeDescriptor.getEncoding () ),
            true
        );
    }

    private void processPomWithMerge (
        ArchetypeDescriptor archetypeDescriptor,
        Context context,
        File pom
    )
    throws OutputFileExists, IOException, XmlPullParserException, ArchetypeGenerationFailure
    {
        File temporaryPom = getTemporaryFile ( pom );

        processTemplate (
            temporaryPom,
            context,
            archetypePathResolver.getTemplatePomPath (),
            getEncoding ( archetypeDescriptor.getEncoding () ),
            true
        );

        pomManager.mergePoms ( pom, temporaryPom );

        FileUtils.forceDelete ( temporaryPom );
    }

    private void processPomWithParent (
        ArchetypeDescriptor archetypeDescriptor,
        Context context,
        File pom,
        File basedirPom,
        String artifactId
    )
    throws OutputFileExists,
        FileNotFoundException,
        XmlPullParserException,
        DocumentException,
        IOException,
        InvalidPackaging,
        ArchetypeGenerationFailure
    {
        processTemplate (
            pom,
            context,
            archetypePathResolver.getTemplatePomPath (),
            getEncoding ( archetypeDescriptor.getEncoding () ),
            true
        );

        pomManager.addModule ( basedirPom, artifactId );
        pomManager.addParent ( pom, basedirPom );
    }

    private void processResourcesGroup (
        ResourcesGroup resourcesGroup,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean isTest,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = resourcesGroup.getTemplates ().iterator ();

        while ( iterator.hasNext () )
        {
            String template = (String) iterator.next ();
            processTemplate (
                new File (
                    outputDirectoryFile,
                    archetypePathResolver.getResourcePath (
                        template,
                        isTest,
                        getResourcesDirectory ( resourcesGroup.getDirectory () )
                    )
                ),
                context,
                archetypePathResolver.getTemplateResourcePath (
                    template,
                    isTest,
                    getResourcesDirectory ( resourcesGroup.getDirectory () )
                ),
                getEncoding ( resourcesGroup.getEncoding (), archetypeEncoding ),
                failIfExists
            );
        }
    }

    private void processResourcesGroups (
        List resourcesGroups,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = resourcesGroups.iterator ();

        while ( iterator.hasNext () )
        {
            ResourcesGroup resourcesGroup = (ResourcesGroup) iterator.next ();

            if ( ( resourcesGroup.getTemplates () != null )
                && !resourcesGroup.getTemplates ().isEmpty ()
            )
            {
                processResourcesGroup (
                    resourcesGroup,
                    context,
                    outputDirectoryFile,
                    archetypeEncoding,
                    false,
                    failIfExists
                );
            }
        }
    }

    private void processSiteGroup (
        SiteGroup siteGroup,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = siteGroup.getTemplates ().iterator ();

        while ( iterator.hasNext () )
        {
            String template = (String) iterator.next ();

            processTemplate (
                new File ( outputDirectoryFile, archetypePathResolver.getSitePath ( template ) ),
                context,
                archetypePathResolver.getTemplateSitePath ( template ),
                getEncoding ( siteGroup.getEncoding (), archetypeEncoding ),
                failIfExists
            );
        }
    }

    private void processSourcesGroup (
        String packageName,
        SourcesGroup sourcesGroup,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean isTest,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = sourcesGroup.getTemplates ().iterator ();

        while ( iterator.hasNext () )
        {
            String template = (String) iterator.next ();
            processTemplate (
                new File (
                    outputDirectoryFile,
                    archetypePathResolver.getSourcePath (
                        template,
                        isTest,
                        getSourcesLanguage ( sourcesGroup.getLanguage () ),
                        packageName
                    )
                ),
                context,
                archetypePathResolver.getTemplateSourcePath (
                    template,
                    isTest,
                    getSourcesLanguage ( sourcesGroup.getLanguage () ),
                    packageName
                ),
                getEncoding ( sourcesGroup.getEncoding (), archetypeEncoding ),

                failIfExists
            );
        } // end while
    }

    private void processSourcesGroups (
        String packageName,
        List sourcesGroups,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = sourcesGroups.iterator ();

        while ( iterator.hasNext () )
        {
            SourcesGroup sourcesGroup = (SourcesGroup) iterator.next ();

            if ( ( sourcesGroup.getTemplates () != null )
                && !sourcesGroup.getTemplates ().isEmpty ()
            )
            {
                processSourcesGroup (
                    packageName,
                    sourcesGroup,
                    context,
                    outputDirectoryFile,
                    archetypeEncoding,
                    false,
                    failIfExists
                );
            }
        }
    }

    private void processTemplate (
        File outFile,
        Context context,
        String templateFileName,
        String encoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        if ( failIfExists && outFile.exists () )
        {
            throw new OutputFileExists ( "Don't rewrite file " + outFile.getName () );
        }
        else if ( outFile.exists () )
        {
            getLogger ().warn ( "Don't override file " + outFile.getName () );
        }
        else
        {
            if ( !outFile.getParentFile ().exists () )
            {
                outFile.getParentFile ().mkdirs ();
            }

            Writer writer = null;

            try
            {
                writer = new OutputStreamWriter ( new FileOutputStream ( outFile ), encoding );

                velocity.getEngine ().mergeTemplate ( templateFileName, encoding, context, writer );

                writer.flush ();
            }
            catch ( Exception e )
            {
                throw new ArchetypeGenerationFailure (
                    "Error merging velocity templates: " + e.getMessage (),
                    e
                );
            }
            finally
            {
                IOUtil.close ( writer );
            }
        }
    }

    private void processTemplates (
        String packageName,
        File outputDirectoryFile,
        Context context,
        ArchetypeDescriptor archetypeDescriptor,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        if ( ( archetypeDescriptor.getSourcesGroups () != null )
            && !archetypeDescriptor.getSourcesGroups ().isEmpty ()
        )
        {
            processSourcesGroups (
                packageName,
                archetypeDescriptor.getSourcesGroups (),
                context,
                outputDirectoryFile,
                getEncoding ( archetypeDescriptor.getEncoding () ),
                failIfExists
            );
        }

        if ( ( archetypeDescriptor.getTestSourcesGroups () != null )
            && !archetypeDescriptor.getTestSourcesGroups ().isEmpty ()
        )
        {
            processTestSourcesGroups (
                packageName,
                archetypeDescriptor.getTestSourcesGroups (),
                context,
                outputDirectoryFile,
                getEncoding ( archetypeDescriptor.getEncoding () ),
                failIfExists
            );
        }

        if ( ( archetypeDescriptor.getResourcesGroups () != null )
            && !archetypeDescriptor.getResourcesGroups ().isEmpty ()
        )
        {
            processResourcesGroups (
                archetypeDescriptor.getResourcesGroups (),
                context,
                outputDirectoryFile,
                getEncoding ( archetypeDescriptor.getEncoding () ),
                failIfExists
            );
        }

        if ( ( archetypeDescriptor.getTestResourcesGroups () != null )
            && !archetypeDescriptor.getTestResourcesGroups ().isEmpty ()
        )
        {
            processTestResourcesGroups (
                archetypeDescriptor.getTestResourcesGroups (),
                context,
                outputDirectoryFile,
                getEncoding ( archetypeDescriptor.getEncoding () ),
                failIfExists
            );
        }

        if ( ( archetypeDescriptor.getSite () != null )
            && ( archetypeDescriptor.getSite ().getTemplates () != null )
            && !archetypeDescriptor.getSite ().getTemplates ().isEmpty ()
        )
        {
            processSiteGroup (
                archetypeDescriptor.getSite (),
                context,
                outputDirectoryFile,
                getEncoding ( archetypeDescriptor.getEncoding () ),
                failIfExists
            );
        }
    }

    private void processTestResourcesGroups (
        List testResourcesgroups,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = testResourcesgroups.iterator ();

        while ( iterator.hasNext () )
        {
            ResourcesGroup resourcesGroup = (ResourcesGroup) iterator.next ();

            if ( ( resourcesGroup.getTemplates () != null )
                && !resourcesGroup.getTemplates ().isEmpty ()
            )
            {
                processResourcesGroup (
                    resourcesGroup,
                    context,
                    outputDirectoryFile,
                    archetypeEncoding,
                    true,
                    failIfExists
                );
            }
        }
    }

    private void processTestSourcesGroups (
        String packageName,
        List testSourcesGroups,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = testSourcesGroups.iterator ();

        while ( iterator.hasNext () )
        {
            SourcesGroup sourcesGroup = (SourcesGroup) iterator.next ();

            if ( ( sourcesGroup.getTemplates () != null )
                && !sourcesGroup.getTemplates ().isEmpty ()
            )
            {
                processSourcesGroup (
                    packageName,
                    sourcesGroup,
                    context,
                    outputDirectoryFile,
                    archetypeEncoding,
                    true,
                    failIfExists
                );
            }
        }
    }

    private String getResourcesDirectory ( String configuratedResourceLanguage )
    {
        return
            ( ( null == configuratedResourceLanguage )
            || "".equals ( configuratedResourceLanguage ) ) ? "resources"
                                                            : configuratedResourceLanguage;
    }

    private String getSourcesLanguage ( String configuratedSourceLanguage )
    {
        return
            ( ( null == configuratedSourceLanguage ) || "".equals ( configuratedSourceLanguage ) )
            ? "java"
            : configuratedSourceLanguage;
    }

    private File getTemporaryFile ( File file )
    {
        return FileUtils.getFile ( file.getAbsolutePath () + Constants.TMP );
    }
}
