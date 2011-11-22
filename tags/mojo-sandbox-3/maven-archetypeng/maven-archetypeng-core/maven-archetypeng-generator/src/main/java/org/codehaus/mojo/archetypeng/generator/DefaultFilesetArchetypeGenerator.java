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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypeFilesResolver;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.filesets.AbstractArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.FileSet;
import org.codehaus.mojo.archetypeng.archetype.filesets.ModuleDescriptor;
import org.codehaus.mojo.archetypeng.exception.ArchetypeGenerationFailure;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @plexus.component
 */
public class DefaultFilesetArchetypeGenerator
extends AbstractLogEnabled
implements FilesetArchetypeGenerator
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
    private PomManager pomManager;

    /**
     * @plexus.requirement
     */
    private VelocityComponent velocity;

    public void generateArchetype ( Properties properties, File archetypeFile, String basedir )
    throws UnknownArchetype,
        ArchetypeNotConfigured,
        ProjectDirectoryExists,
        PomFileExists,
        OutputFileExists,
        ArchetypeGenerationFailure
    {
        ClassLoader old = Thread.currentThread ().getContextClassLoader ();

        try
        {
            ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getFileSetArchetypeDescriptor ( archetypeFile );
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

            List archetypeResources =
                archetypeArtifactManager.getFilesetArchetypeResources ( archetypeFile );

            ZipFile archetypeZipFile =
                archetypeArtifactManager.getArchetypeZipFile ( archetypeFile );

            ClassLoader archetypeJarLoader =
                archetypeArtifactManager.getArchetypeJarLoader ( archetypeFile );

            Thread.currentThread ().setContextClassLoader ( archetypeJarLoader );

            if ( archetypeDescriptor.isPartial () )
            {
                getLogger ().debug (
                    "Procesing partial archetype " + archetypeDescriptor.getId ()
                );
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
                getLogger ().debug (
                    "Procesing complete archetype " + archetypeDescriptor.getId ()
                );
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
        catch ( FileNotFoundException ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        catch ( IOException ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        catch ( XmlPullParserException ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        catch ( DocumentException ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        catch ( ArchetypeGenerationFailure ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        catch ( InvalidPackaging ex )
        {
            throw new ArchetypeGenerationFailure ( ex );
        }
        finally
        {
            Thread.currentThread ().setContextClassLoader ( old );
        }
    }

    public String getPackageAsDirectory ( String packageName )
    {
        return StringUtils.replace ( packageName, ".", "/" );
    }

    private void copyFile (
        final File outFile,
        final String template,
        final boolean failIfExists,
        final ZipFile archetypeZipFile
    )
    throws FileNotFoundException, OutputFileExists, IOException
    {
        getLogger ().debug ( "Copying file " + template );
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

    private String getEncoding ( String archetypeEncoding )
    {
        return
            ( ( null == archetypeEncoding ) || "".equals ( archetypeEncoding ) )
            ? "UTF-8"
            : archetypeEncoding;
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
        getLogger ().debug ( "Processing " + artifactId );

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
            getLogger ().debug (
                artifactId + " has modules (" + archetypeDescriptor.getModules () + ")"
            );
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
        getLogger ().debug ( "Processed " + artifactId );
    }

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

    private void processPom ( Context context, File pom )
    throws OutputFileExists, ArchetypeGenerationFailure
    {
        getLogger ().debug ( "Processing pom" );
        processTemplate ( pom, context, Constants.POM_PATH, getEncoding ( null ), true );
    }

    private void processPomWithMerge ( Context context, File pom )
    throws OutputFileExists, IOException, XmlPullParserException, ArchetypeGenerationFailure
    {
        getLogger ().debug ( "Processing pom with merge" );

        File temporaryPom = getTemporaryFile ( pom );

        processTemplate ( temporaryPom, context, Constants.POM_PATH, getEncoding ( null ), true );

        pomManager.mergePoms ( pom, temporaryPom );

        FileUtils.forceDelete ( temporaryPom );
    }

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
        getLogger ().debug ( "Processing pom with parent" );
        processTemplate ( pom, context, Constants.POM_PATH, getEncoding ( null ), true );

        pomManager.addModule ( basedirPom, artifactId );
        pomManager.addParent ( pom, basedirPom );
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
        templateFileName = templateFileName.replace ( File.separator, "/" );

        getLogger ().debug ( "Prosessing template " + templateFileName );

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
        AbstractArchetypeDescriptor archetypeDescriptor,
        List archetypeResources,
        ZipFile archetypeZipFile,
        boolean failIfExists
    )
    throws OutputFileExists, ArchetypeGenerationFailure, FileNotFoundException, IOException
    {
        Iterator iterator = archetypeDescriptor.getFileSets ().iterator ();
        if ( iterator.hasNext () )
        {
            getLogger ().debug ( "Processing filesets" );
        }
        while ( iterator.hasNext () )
        {
            FileSet fileSet = (FileSet) iterator.next ();

            List fileSetResources =
                archetypeFilesResolver.filterFiles ( fileSet, archetypeResources );

            if ( fileSet.isFiltered () )
            {
                getLogger ().debug ( "Processing fileset " + fileSet );
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
                getLogger ().debug ( "Processed " + fileSetResources.size () + " files" );
            }
            else
            {
                getLogger ().debug ( "Copying fileset " + fileSet );
                copyFiles (
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName,
                    outputDirectoryFile,
                    archetypeZipFile,
                    failIfExists
                );
                getLogger ().debug ( "Copied " + fileSetResources.size () + " files" );
            }
        } // end while
    }

    private File getTemporaryFile ( File file )
    {
        return FileUtils.getFile ( file.getAbsolutePath () + Constants.TMP );
    }
}
