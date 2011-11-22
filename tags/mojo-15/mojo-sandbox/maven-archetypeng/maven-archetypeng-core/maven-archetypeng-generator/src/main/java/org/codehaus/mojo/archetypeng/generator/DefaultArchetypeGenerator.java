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

import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeDescriptorException;
import org.apache.maven.archetype.ArchetypeNotFoundException;
import org.apache.maven.archetype.ArchetypeTemplateProcessingException;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypeFilesResolver;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.filesets.AbstractArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.FileSet;
import org.codehaus.mojo.archetypeng.archetype.filesets.ModuleDescriptor;
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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    private ArchetypeFilesResolver archetypeFilesResolver;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;
    /**
     * @plexus.requirement
     */
    private Archetype oldArchetype;

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

        if ( !archetypeArtifactManager.exists (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            throw new UnknownArchetype (
                "The desired archetype does not exist (" + archetypeDefinition.getGroupId () + ":"
                + archetypeDefinition.getArtifactId () + ":" + archetypeDefinition.getVersion ()
                + ")"
            );
        }

        if ( archetypeArtifactManager.isFileSetArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            processFileSetArchetype (
                properties,
                localRepository,
                basedir,
                repositories,
                archetypeDefinition
            );
        }
        else if (
            archetypeArtifactManager.isOldArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            processOldArchetype (
                localRepository,
                properties,
                basedir,
                archetypeDefinition,
                repositories
            );
        }
        else
        {
            throw new ArchetypeGenerationFailure ( "The defined artifact is not an archetype" );
        }
    }

    /**Common*/
    public String getPackageAsDirectory ( String packageName )
    {
        return StringUtils.replace ( packageName, ".", "/" );
    }

    /**FileSetArchetype*/
    private void copyFile (
        final File outFile,
        final String template,
        final boolean failIfExists,
        final ZipFile archetypeZipFile
    )
    throws FileNotFoundException, OutputFileExists, IOException
    {
        getLogger().debug("Copying file "+template);
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
            ZipEntry input =
                archetypeZipFile.getEntry ( Constants.ARCHETYPE_RESOURCES + "/" + template );

            InputStream inputStream = archetypeZipFile.getInputStream ( input );

            outFile.getParentFile ().mkdirs ();

            IOUtil.copy ( inputStream, new FileOutputStream ( outFile ) );
        }
    }

    /**FileSetArchetype*/
    private void copyFiles (
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName,
        File outputDirectoryFile,
        ZipFile archetypeZipFile,
        boolean failIfExists
    )
    throws OutputFileExists, FileNotFoundException, IOException
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String template = (String) iterator.next ();

            String templateName = StringUtils.replace ( template, directory + "/", "" );

            templateName = templateName.replace ( File.separator, "/" );

            File outFile =
                new File (
                    outputDirectoryFile,
                    directory + "/" + ( packaged ? getPackageAsDirectory ( packageName ) : "" )
                    + "/" + templateName
                );
            copyFile ( outFile, template, failIfExists, archetypeZipFile );
        } // end while
    }

    /**Common*/
    private String getEncoding ( String archetypeEncoding )
    {
        return
            ( ( null == archetypeEncoding ) || "".equals ( archetypeEncoding ) )
            ? "UTF-8"
            : archetypeEncoding;
    }

    /**Common*/
    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    /**Common*/
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

    /**FileSetArchetype*/
    private void processArchetypeTemplates (
        AbstractArchetypeDescriptor archetypeDescriptor,
        List archetypeResources,
        ZipFile archetypeZipFile,
        Context context,
        String packageName,
        File outputDirectoryFile
    )
    throws OutputFileExists, ArchetypeGenerationFailure, FileNotFoundException, IOException
    {
        processTemplates (
            packageName,
            outputDirectoryFile,
            context,
            archetypeDescriptor,
            archetypeResources,
            archetypeZipFile,
            false
        );
    }

    /**FileSetArchetype*/
    private void processArchetypeTemplatesWithWarning (
        org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor archetypeDescriptor,
        List archetypeResources,
        ZipFile archetypeZipFile,
        Context context,
        String packageName,
        File outputDirectoryFile
    )
    throws OutputFileExists, ArchetypeGenerationFailure, FileNotFoundException, IOException
    {
        processTemplates (
            packageName,
            outputDirectoryFile,
            context,
            archetypeDescriptor,
            archetypeResources,
            archetypeZipFile,
            true
        );
    }

    /**FileSetArchetype*/
    private void processFileSet (
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName,
        Context context,
        File outputDirectoryFile,
        String archetypeEncoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String template = (String) iterator.next ();

            String templateName = StringUtils.replace ( template, directory + "/", "" );

            processTemplate (
                new File (
                    outputDirectoryFile,
                    directory + "/" + ( packaged ? getPackageAsDirectory ( packageName ) : "" )
                    + "/" + templateName
                ),
                context,
                Constants.ARCHETYPE_RESOURCES + "/" + template,
                archetypeEncoding,
                failIfExists
            );
        } // end while
    }

    /**FileSetArchetype*/
    private void processFileSetArchetype (
        final Properties properties,
        final ArtifactRepository localRepository,
        final String basedir,
        final List repositories,
        final ArchetypeDefinition archetypeDefinition
    )
    throws InvalidPackaging,
        UnknownArchetype,
        DocumentException,
        PomFileExists,
        FileNotFoundException,
        IOException,
        ProjectDirectoryExists,
        OutputFileExists,
        ArchetypeGenerationFailure,
        XmlPullParserException,
        ArchetypeNotConfigured
    {
        ArchetypeConfiguration archetypeConfiguration;
        org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor archetypeDescriptor =
            archetypeArtifactManager.getFileSetArchetypeDescriptor (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );
        archetypeConfiguration =
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

        List archetypeResources =
            archetypeArtifactManager.getFilesetArchetypeResources (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );

        ZipFile archetypeZipFile =
            archetypeArtifactManager.getArchetypeZipFile (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );

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
                getLogger().debug("Procesing partial archetype "+archetypeDescriptor.getId());
                if ( outputDirectoryFile.exists () )
                {
                    if ( !pom.exists () )
                    {
                        throw new PomFileExists ( "The pom file already exists" );
                    }
                    else
                    {
                        processPomWithMerge ( context, pom );
                        processArchetypeTemplatesWithWarning (
                            archetypeDescriptor,
                            archetypeResources,
                            archetypeZipFile,
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
                        processPomWithMerge ( context, basedirPom );
                        processArchetypeTemplatesWithWarning (
                            archetypeDescriptor,
                            archetypeResources,
                            archetypeZipFile,
                            context,
                            packageName,
                            new File ( basedir )
                        );
                    }
                    else
                    {
                        processPom ( context, pom );
                        processArchetypeTemplates (
                            archetypeDescriptor,
                            archetypeResources,
                            archetypeZipFile,
                            context,
                            packageName,
                            outputDirectoryFile
                        );
                    }
                }

                if ( archetypeDescriptor.getModules ().size () > 0 )
                {
                    getLogger ().info ( "Modules ignored in partial mode" );
                }
            }
            else
            {
                getLogger().debug("Procesing complete archetype "+archetypeDescriptor.getId());
                if ( outputDirectoryFile.exists () )
                {
                    throw new ProjectDirectoryExists ( "The project directory already exists" );
                }
                else
                {
                    processFilesetModule (
                        artifactId,
                        archetypeResources,
                        pom,
                        archetypeZipFile,
                        basedirPom,
                        outputDirectoryFile,
                        packageName,
                        archetypeDescriptor,
                        context
                    );
                }
            } // end if
        }
        finally
        {
            Thread.currentThread ().setContextClassLoader ( old );
        }
    }

    /**FileSetArchetype*/
    private void processFilesetModule (
        String artifactId,
        final List archetypeResources,
        File pom,
        final ZipFile archetypeZipFile,
        File basedirPom,
        File outputDirectoryFile,
        final String packageName,
        final AbstractArchetypeDescriptor archetypeDescriptor,
        final Context context
    )
    throws DocumentException,
        XmlPullParserException,
        ArchetypeGenerationFailure,
        InvalidPackaging,
        IOException,
        OutputFileExists
    {
        outputDirectoryFile.mkdirs ();
            getLogger().debug("Processing "+artifactId);

        processFilesetProject (
            archetypeDescriptor,
            artifactId,
            archetypeResources,
            pom,
            archetypeZipFile,
            context,
            packageName,
            outputDirectoryFile,
            basedirPom
        );

        basedirPom = pom;

        Iterator subprojects = archetypeDescriptor.getModules ().iterator ();
        if ( subprojects.hasNext () )
        {
            getLogger().debug(artifactId+" has modules (" +archetypeDescriptor.getModules()+
                ")");
        }
        while ( subprojects.hasNext () )
        {
            ModuleDescriptor project = (ModuleDescriptor) subprojects.next ();

            artifactId = project.getId ();

            File basedirLocal = outputDirectoryFile;
            outputDirectoryFile = new File ( basedirLocal, artifactId );
            pom = new File ( outputDirectoryFile, Constants.ARCHETYPE_POM );
            context.put ( Constants.ARTIFACT_ID, artifactId );
            processFilesetModule (
                artifactId,
                archetypeResources,
                pom,
                archetypeZipFile,
                basedirPom,
                outputDirectoryFile,
                packageName,
                project,
                context
            );
        }
            getLogger().debug("Processed "+artifactId);
    }

    /**FileSetArchetype*/
    private void processFilesetProject (
        final AbstractArchetypeDescriptor archetypeDescriptor,
        final String artifactId,
        final List archetypeResources,
        final File pom,
        final ZipFile archetypeZipFile,
        final Context context,
        final String packageName,
        final File outputDirectoryFile,
        final File basedirPom
    )
    throws DocumentException,
        XmlPullParserException,
        ArchetypeGenerationFailure,
        InvalidPackaging,
        IOException,
        FileNotFoundException,
        OutputFileExists
    {
        if ( basedirPom.exists () )
        {
            processPomWithParent (

                context,
                pom,
                basedirPom,
                artifactId
            );
        }
        else
        {
            processPom ( context, pom );
        }

        processArchetypeTemplates (
            archetypeDescriptor,
            archetypeResources,
            archetypeZipFile,
            context,
            packageName,
            outputDirectoryFile
        );
    }

    /**OldArchetype*/
    private void processOldArchetype (
        final ArtifactRepository localRepository,
        final Properties properties,
        final String basedir,
        final ArchetypeDefinition archetypeDefinition,
        final List repositories
    )
    throws UnknownArchetype, ArchetypeGenerationFailure
    {
        ArchetypeConfiguration archetypeConfiguration;

        org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor =
            archetypeArtifactManager.getOldArchetypeDescriptor (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );
        archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );

        Map map = new HashMap ();

        map.put ( "basedir", basedir );

        map.put (
            "package",
            archetypeConfiguration.getProperties ().getProperty ( Constants.PACKAGE )
        );

        map.put (
            "packageName",
            archetypeConfiguration.getProperties ().getProperty ( Constants.PACKAGE )
        );

        map.put (
            "groupId",
            archetypeConfiguration.getProperties ().getProperty ( Constants.GROUP_ID )
        );

        map.put (
            "artifactId",
            archetypeConfiguration.getProperties ().getProperty ( Constants.ARTIFACT_ID )
        );

        map.put (
            "version",
            archetypeConfiguration.getProperties ().getProperty ( Constants.VERSION )
        );
        try
        {
            oldArchetype.createArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories,
                map
            );
        }
        catch ( ArchetypeDescriptorException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
        catch ( ArchetypeTemplateProcessingException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
        catch ( ArchetypeNotFoundException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
    }

    /**FileSetArchetype*/
    private void processPom ( Context context, File pom )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        getLogger().debug("Processing pom");
        processTemplate ( pom, context, Constants.POM_PATH, getEncoding ( null ), true );
    }

    /**FileSetArchetype*/
    private void processPomWithMerge ( Context context, File pom )
    throws OutputFileExists, IOException, XmlPullParserException, ArchetypeGenerationFailure
    {
        getLogger().debug("Processing pom with merge");
        File temporaryPom = getTemporaryFile ( pom );

        processTemplate ( temporaryPom, context, Constants.POM_PATH, getEncoding ( null ), true );

        pomManager.mergePoms ( pom, temporaryPom );

        FileUtils.forceDelete ( temporaryPom );
    }

    /**FileSetArchetype*/
    private void processPomWithParent (
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
        getLogger().debug("Processing pom with parent");
        processTemplate ( pom, context, Constants.POM_PATH, getEncoding ( null ), true );

        pomManager.addModule ( basedirPom, artifactId );
        pomManager.addParent ( pom, basedirPom );
    }

    /**Common*/
    private void processTemplate (
        File outFile,
        Context context,
        String templateFileName,
        String encoding,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        templateFileName = templateFileName.replace ( File.separator, "/" );
        
        getLogger().debug("Prosessing template "+templateFileName);

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

    /**FileSetArchetype*/
    private void processTemplates (
        String packageName,
        File outputDirectoryFile,
        Context context,
        AbstractArchetypeDescriptor archetypeDescriptor,
        List archetypeResources,
        ZipFile archetypeZipFile,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure, FileNotFoundException, IOException
    {
        Iterator iterator = archetypeDescriptor.getFileSets ().iterator ();
        if (iterator.hasNext())
        {
            getLogger().debug("Processing filesets");
        }
        while ( iterator.hasNext () )
        {
            FileSet fileSet = (FileSet) iterator.next ();

            List fileSetResources =
                archetypeFilesResolver.filterFiles ( fileSet, archetypeResources );

            if ( fileSet.isFiltered () )
            {
            getLogger().debug("Processing fileset "+fileSet);
                processFileSet (
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName,
                    context,
                    outputDirectoryFile,
                    getEncoding ( fileSet.getEncoding () ),
                    failIfExists
                );
            getLogger().debug("Processed " +fileSetResources.size()+
                " files");
            }
            else
            {
            getLogger().debug("Copying fileset "+fileSet);
                copyFiles (
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName,
                    outputDirectoryFile,
                    archetypeZipFile,
                    failIfExists
                );
            getLogger().debug("Copied " +fileSetResources.size()+
                " files");
            }
        } // end while
    }

    /**Common*/
    private File getTemporaryFile ( File file )
    {
        return FileUtils.getFile ( file.getAbsolutePath () + Constants.TMP );
    }
}
