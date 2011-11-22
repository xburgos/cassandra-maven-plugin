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
import org.codehaus.mojo.archetypeng.ArchetypeFilesResolver;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.ListScanner;
import org.codehaus.mojo.archetypeng.PathUtils;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.FileSet;
import org.codehaus.mojo.archetypeng.archetype.filesets.ModuleDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.RequiredProperty;
import org.codehaus.mojo.archetypeng.archetype.filesets.io.xpp3.ArchetypeDescriptorXpp3Writer;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.TemplateCreationException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @plexus.component  role-hint="fileset"
 */
public class FilesetArchetypeCreator
extends AbstractLogEnabled
implements ArchetypeCreator
{
//    public static String LANGUAGES = "java/**,aspectj/**,csharp/**,"
//        + "groovy/**";
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

//    private String FILTERED =
//        "**/*.java,**/*.xml,**/*.txt,"
//        + "**/*.groovy,**/*.cs,**/*.mdo,"
//        + "**/*.aj,**/*.jsp,**/*.gsp,"
//        + "**/*.vm,**/*.html,**/*.xhtml,"
//        + "**/*.properties,**/.classpath,**/.project";

    /**
     * @plexus.requirement
     */
    private PomManager pomManager;

    public void createArchetype ( MavenProject project, File propertyFile, List languages, List filtereds )
    throws IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        TemplateCreationException,
        XmlPullParserException
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
        getLogger().debug("Creating archetype in " + generatedSourcesDirectory);

        Model model = new Model ();
        model.setGroupId ( archetypeDefinition.getGroupId () );
        model.setArtifactId ( archetypeDefinition.getArtifactId () );
        model.setVersion ( archetypeDefinition.getVersion () );
        model.setPackaging ( "maven-plugin" );
        getLogger().debug("Creating archetype's pom");        

        File archetypePomFile = FileUtils.resolveFile ( basedir, getArchetypePom () );
        archetypePomFile.getParentFile ().mkdirs ();
        pomManager.writePom ( model, archetypePomFile );

        File archetypeResourcesDirectory =
            FileUtils.resolveFile ( generatedSourcesDirectory, getTemplateOutputDirectory () );
        archetypeResourcesDirectory.mkdirs ();

        File archetypeFilesDirectory =
            FileUtils.resolveFile ( archetypeResourcesDirectory, Constants.ARCHETYPE_RESOURCES );
        archetypeFilesDirectory.mkdirs ();
        getLogger().debug("Archetype's files output directory " + archetypeFilesDirectory);

        File archetypeDescriptorFile =
            FileUtils.resolveFile ( archetypeResourcesDirectory, Constants.ARCHETYPE_DESCRIPTOR );
        archetypeDescriptorFile.getParentFile ().mkdirs ();

        ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor ();
        archetypeDescriptor.setId ( archetypeDefinition.getArtifactId () );
        getLogger().debug("Starting archetype's descriptor "+ archetypeDefinition.getArtifactId ());
        archetypeDescriptor.setPartial ( false );

        addRequiredProperties ( archetypeDescriptor, properties );

        // TODO ensure reversedproperties contains NO dotted properties
        Properties reverseProperties = getRequiredProperties ( archetypeDescriptor, properties );

        // TODO ensure pomReversedProperties contains NO dotted properties
        Properties pomReversedProperties = new Properties ();
        pomReversedProperties.putAll ( reverseProperties );
        pomReversedProperties.remove ( Constants.PACKAGE );

        String packageName = archetypeConfiguration.getProperty ( Constants.PACKAGE );

        Model pom =
            pomManager.readPom ( FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM ) );

        List fileNames = resolveFileNames ( pom, basedir );

        List filesets = resolveFileSets ( packageName, fileNames, languages, filtereds );
        getLogger().debug("Resolved filesets for "+archetypeDescriptor.getId());

        archetypeDescriptor.setFileSets ( filesets );

        createArchetypeFiles (
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory
        );
        getLogger().debug("Created files for "+archetypeDescriptor.getId());

        Iterator modules = pom.getModules ().iterator ();
        while ( modules.hasNext () )
        {
            String moduleId = (String) modules.next ();

            getLogger().debug("Creating module "+moduleId);
            ModuleDescriptor moduleDescriptor =
                createModule (
                    reverseProperties,
                    pomReversedProperties,
                    moduleId,
                    packageName,
                    FileUtils.resolveFile ( basedir, moduleId ),
                    FileUtils.resolveFile ( archetypeFilesDirectory, moduleId ), languages, filtereds
                );

            archetypeDescriptor.addModule ( moduleDescriptor );
            getLogger().debug("Added module "+moduleDescriptor.getId()+" in "+archetypeDescriptor.getId());
        }

        createArchetypePom ( pom, archetypeFilesDirectory, pomReversedProperties );
        getLogger().debug("Created Archetype "+archetypeDescriptor.getId()+" pom");

        ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer ();
        writer.write ( new FileWriter ( archetypeDescriptorFile ), archetypeDescriptor );
        getLogger().debug("Archetype "+archetypeDescriptor.getId()+" descriptor written");
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
            
            getLogger().debug("Adding requiredProperty "+ propertyKey + "=" + requiredProperties.getProperty ( propertyKey ) + "to archetype's descriptor");
        }
    }

    private String getArchetypePom ()
    {
        return getGeneratedSourcesDirectory () + File.separator + Constants.ARCHETYPE_POM;
    }

    private List concatenateToList ( List toConcatenate, String with )
    {
        List result = new ArrayList ( toConcatenate.size () );
        Iterator iterator = toConcatenate.iterator ();
        while ( iterator.hasNext () )
        {
            String concatenate = (String) iterator.next ();
            result.add ( ( ( with.length () > 0 ) ? ( with + "/" + concatenate ) : concatenate ) );
        }
        return result;
    }

    private void copyFiles (
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName
    )
    throws IOException
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String inputFileName = (String) iterator.next ();

            String packageAsDirectory = StringUtils.replace ( packageName, ".", "/" );

            String outputFileName =
                StringUtils.replace ( inputFileName, packageAsDirectory + "/", "" );

            File outputFile = new File ( archetypeFilesDirectory, outputFileName );

            File inputFile = new File ( basedir, inputFileName );

            outputFile.getParentFile ().mkdirs ();

            FileUtils.copyFile ( inputFile, outputFile );
        } // end while
    }

    private void createArchetypeFiles (
        Properties reverseProperties,
        List fileSets,
        String packageName,
        File basedir,
        File archetypeFilesDirectory
    )
    throws IOException
    {
        getLogger().debug("Creating Archetype/Module files from " +basedir+
            " to "+ archetypeFilesDirectory);
        Iterator iterator = fileSets.iterator ();

        while ( iterator.hasNext () )
        {
            FileSet fileSet = (FileSet) iterator.next ();

            DirectoryScanner scanner = new DirectoryScanner ();
            scanner.setBasedir ( basedir );
            scanner.setIncludes (
                (String[]) concatenateToList ( fileSet.getIncludes (), fileSet.getDirectory () )
                .toArray ( new String[fileSet.getIncludes ().size ()] )
            );
            scanner.setExcludes (
                (String[]) fileSet.getExcludes ().toArray (
                    new String[fileSet.getExcludes ().size ()]
                )
            );
            getLogger().debug("Using fileset "+fileSet);
            scanner.scan ();
            
            List fileSetResources = Arrays.asList ( scanner.getIncludedFiles () );
//            getLogger().debug("Scanned "+fileSetResources);

            if ( fileSet.isFiltered () )
            {
                processFileSet (
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName,
                    reverseProperties
                );
                getLogger().debug("Processed "+fileSet.getDirectory()+" files");
            }
            else
            {
                copyFiles (
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName
                );
                getLogger().debug("Copied "+fileSet.getDirectory()+" files");
            }
        } // end while
    }

    private void createArchetypePom (
        Model pom,
        File archetypeFilesDirectory,
        Properties pomReversedProperties
    )
    throws IOException
    {
        pom.setParent ( null );
        pom.setModules ( null );
        pom.setGroupId ( "${" + Constants.GROUP_ID + "}" );
        pom.setArtifactId ( "${" + Constants.ARTIFACT_ID + "}" );
        pom.setVersion ( "${" + Constants.VERSION + "}" );

        pomManager.writePom (
            pom,
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" )
        );

        File outputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        File inputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

        String initialcontent = FileUtils.fileRead ( inputFile );

        String content = getReversedContent ( initialcontent, pomReversedProperties );

        outputFile.getParentFile ().mkdirs ();

        FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );

        inputFile.delete ();
    }

    private FileSet createFileSet (
        final List excludes,
        final boolean packaged,
        final boolean filtered,
        final String group,
        final List includes
    )
    {
        FileSet fileSet = new FileSet ();

        fileSet.setDirectory ( group );
        fileSet.setPackaged ( packaged );
        fileSet.setFiltered ( filtered );
        fileSet.setIncludes ( includes );
        fileSet.setExcludes ( excludes );
        
        getLogger().debug("Created Fileset "+fileSet);
        
        return fileSet;
    }

    private List createFileSets (
        List files,
        int level,
        boolean packaged,
        String packageName,
        boolean filtered
    )
    {
        List fileSets = new ArrayList ();

        if ( !files.isEmpty () )
        {
            getLogger().debug("Creating filesets" + (packaged?" packaged ("+packageName+")":"")+
                (filtered?" filtered":"")+
                " at level "+level);
            if ( level == 0 )
            {
                List includes = new ArrayList ();
                List excludes = new ArrayList ();

                Iterator filesIterator = files.iterator ();
                while ( filesIterator.hasNext () )
                {
                    String file = (String) filesIterator.next ();

                    includes.add ( file );
                }

                if ( !includes.isEmpty () )
                {
                    fileSets.add ( createFileSet ( excludes, packaged, filtered, "", includes ) );
                }
            }
            else
            {
                Map groups = getGroupsMap ( files, level );

                Iterator groupIterator = groups.keySet ().iterator ();
                while ( groupIterator.hasNext () )
                {
                    String group = (String) groupIterator.next ();
                    
                    getLogger().debug("Creating filesets for group "+group);

                    if ( !packaged )
                    {
                        fileSets.add (
                            getUnpackagedFileSet (
                                packaged,
                                filtered,
                                group,
                                (List) groups.get ( group )
                            )
                        );
                    }
                    else
                    {
                        fileSets.addAll (
                            getPackagedFileSets (
                                packaged,
                                filtered,
                                group,
                                (List) groups.get ( group ),
                                packageName
                            )
                        );
                    }
                }
            } // end if
            
            getLogger().debug("Resolved "+fileSets.size()+" filesets");
        } // end if
        return fileSets;
    }

    private ModuleDescriptor createModule (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String moduleId,
        String packageName,
        File basedir,
        File archetypeFilesDirectory, List languages, List filtereds
    )
    throws IOException, XmlPullParserException
    {
        ModuleDescriptor archetypeDescriptor = new ModuleDescriptor ();
        archetypeDescriptor.setId ( moduleId );
        getLogger().debug("Starting module's descriptor "+ moduleId);

        archetypeFilesDirectory.mkdirs ();
        getLogger().debug("Module's files output directory " + archetypeFilesDirectory);

        Model pom =
            pomManager.readPom ( FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM ) );

        List fileNames = resolveFileNames ( pom, basedir );

        List filesets = resolveFileSets ( packageName, fileNames, languages, filtereds );
        getLogger().debug("Resolved filesets for module "+archetypeDescriptor.getId());

        archetypeDescriptor.setFileSets ( filesets );

        createArchetypeFiles (
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory
        );
        getLogger().debug("Created files for module "+archetypeDescriptor.getId());

        Iterator modules = pom.getModules ().iterator ();
        while ( modules.hasNext () )
        {
            String subModuleId = (String) modules.next ();

            getLogger().debug("Creating module "+subModuleId);
            ModuleDescriptor moduleDescriptor =
                createModule (
                    reverseProperties,
                    pomReversedProperties,
                    subModuleId,
                    packageName,
                    FileUtils.resolveFile ( basedir, subModuleId ),
                    FileUtils.resolveFile ( archetypeFilesDirectory, subModuleId ), languages, filtereds
                );

            archetypeDescriptor.addModule ( moduleDescriptor );
            getLogger().debug("Added module "+moduleDescriptor.getId()+" in "+archetypeDescriptor.getId());
        }

        createModulePom ( pom, archetypeFilesDirectory, pomReversedProperties );
        getLogger().debug("Created Module "+archetypeDescriptor.getId()+" pom");

        return archetypeDescriptor;
    }

    private void createModulePom (
        Model pom,
        File archetypeFilesDirectory,
        Properties pomReversedProperties
    )
    throws IOException
    {
        pom.setParent ( null );
        pom.setModules ( null );
        pom.setGroupId ( "${" + Constants.GROUP_ID + "}" );
        pom.setArtifactId ( "${" + Constants.ARTIFACT_ID + "}" );
        pom.setVersion ( "${" + Constants.VERSION + "}" );

        pomManager.writePom (
            pom,
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" )
        );

        File outputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        File inputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

        String initialcontent = FileUtils.fileRead ( inputFile );

        String content = getReversedContent ( initialcontent, pomReversedProperties );

        outputFile.getParentFile ().mkdirs ();

        FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );

        inputFile.delete ();
    }

    private Set getExtensions ( List files )
    {
        Set extensions = new HashSet ();
        Iterator filesIterator = files.iterator ();
        while ( filesIterator.hasNext () )
        {
            String file = (String) filesIterator.next ();

            extensions.add ( FileUtils.extension ( file ) );
        }

        return extensions;
    }

    private String getGeneratedSourcesDirectory ()
    {
        return "target" + File.separator + "generated-sources" + File.separator + "archetypeng";
    }

    private Map getGroupsMap ( final List files, final int level )
    {
        Map groups = new HashMap ();
        Iterator fileIterator = files.iterator ();
        while ( fileIterator.hasNext () )
        {
            String file = (String) fileIterator.next ();

            String directory = PathUtils.getDirectory ( file, level );

            if ( !groups.containsKey ( directory ) )
            {
                groups.put ( directory, new ArrayList () );
            }

            List group = (List) groups.get ( directory );

            String innerPath = file.substring ( directory.length () + 1 );

            group.add ( innerPath );
        }
        getLogger().debug("Sorted "+groups.size()+" groups in "+files.size()+" files");
        return groups;
    }

    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    private FileSet getPackagedFileSet (
        final boolean packaged,
        final boolean filtered,
        final Set packagedExtensions,
        final String group,
        final Set unpackagedExtensions,
        final List unpackagedFiles
    )
    {
        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = packagedExtensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();

            includes.add ( "**/*." + extension );

            if ( unpackagedExtensions.contains ( extension ) )
            {
                excludes.addAll (
                    archetypeFilesResolver.getFilesWithExtension ( unpackagedFiles, extension )
                );
            }
        }

        FileSet fileset = createFileSet ( excludes, packaged, filtered, group, includes );
        return fileset;
    }

    private List getPackagedFileSets (
        final boolean packaged,
        final boolean filtered,
        final String group,
        final List groupFiles,
        final String packageName
    )
    {
        String packageAsDir = StringUtils.replace ( packageName, ".", "/" );
        List packagedFileSets = new ArrayList ();
        List packagedFiles = archetypeFilesResolver.getPackagedFiles ( groupFiles, packageAsDir );

        List unpackagedFiles =
            archetypeFilesResolver.getUnpackagedFiles ( groupFiles, packageAsDir );

        Set packagedExtensions = getExtensions ( packagedFiles );
        getLogger().debug("Found packaged extensions "+packagedExtensions);

        Set unpackagedExtensions = getExtensions ( unpackagedFiles );

        packagedFileSets.add (
            getPackagedFileSet (
                packaged,
                filtered,
                packagedExtensions,
                group,
                unpackagedExtensions,
                unpackagedFiles
            )
        );

        if ( !unpackagedExtensions.isEmpty () )
        {
        getLogger().debug("Found unpackaged extensions "+unpackagedExtensions);
            packagedFileSets.add (
                getUnpackagedFileSet (
                    packaged,
                    filtered,
                    unpackagedExtensions,
                    unpackagedFiles,
                    group,
                    packagedExtensions
                )
            );
        }
        return packagedFileSets;
    }

    private void processFileSet (
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName,
        Properties reverseProperties
    )
    throws IOException
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String inputFileName = (String) iterator.next ();

            String packageAsDirectory = StringUtils.replace ( packageName, ".", "/" );

            String outputFileName =
                StringUtils.replace ( inputFileName, packageAsDirectory + "/", "" );

            File outputFile = new File ( archetypeFilesDirectory, outputFileName );
            File inputFile = new File ( basedir, inputFileName );

            String initialcontent = FileUtils.fileRead ( inputFile );
            String content = getReversedContent ( initialcontent, reverseProperties );
            outputFile.getParentFile ().mkdirs ();
            FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );
        } // end while
    }

    private Properties getRequiredProperties (
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        Properties reversedProperties = new Properties ();

        reversedProperties.putAll ( properties );
        reversedProperties.remove ( Constants.ARCHETYPE_GROUP_ID );
        reversedProperties.remove ( Constants.ARCHETYPE_ARTIFACT_ID );
        reversedProperties.remove ( Constants.ARCHETYPE_VERSION );
        reversedProperties.remove ( Constants.GROUP_ID );
        reversedProperties.remove ( Constants.ARTIFACT_ID );
        reversedProperties.remove ( Constants.VERSION );

        return reversedProperties;
    }

    private List resolveFileNames ( final Model pom, final File basedir )
    throws IOException
    {
        getLogger().debug("Resolving files for "+ pom.getId()+" in "+basedir);
        
        Iterator modules = pom.getModules ().iterator ();
        String excludes = "pom.xml*,archetype.properties*,target/**," + ListScanner.DEFAULTEXCLUDES;
        while ( modules.hasNext () )
        {
            excludes += "," + (String) modules.next () + "/**";
        }

        List fileNames = FileUtils.getFileNames ( basedir, "**", excludes, false );

        getLogger().debug("Resolved "+fileNames.size()+" files");
        
        return fileNames;
    }
 
    private List resolveFileSets ( String packageName, List fileNames, List languages, List filtereds )
    {
        List resolvedFileSets = new ArrayList ();
        getLogger().debug("Resolving filesets with package="+packageName +", languages="+languages+ " and extentions="+filtereds);

        List files = new ArrayList ( fileNames );
        
        String languageIncludes = "";
        Iterator languagesIterator=languages.iterator();
        while(languagesIterator.hasNext())
        {
            String language = (String) languagesIterator.next();
            
            languageIncludes+=
                (languageIncludes.length()==0?"":",")
                +language+"/**";
        }
        
        String filteredIncludes = "";
        Iterator filteredsIterator=filtereds.iterator();
        while(filteredsIterator.hasNext())
        {
            String filtered = (String) filteredsIterator.next();
            
            filteredIncludes+=
                (filteredIncludes.length()==0?"":",")
                +"**/" +(filtered.startsWith(".")?"":
                "*.")+filtered;
        }
        

        /*sourcesMainFiles*/
        List sourcesMainFiles = archetypeFilesResolver.findSourcesMainFiles ( files, languageIncludes );
        if ( !sourcesMainFiles.isEmpty () )
        {
//            getLogger().debug("Resolved sources "+sourcesMainFiles);
            files.removeAll ( sourcesMainFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( sourcesMainFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( sourcesMainFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    false
                )
            );
        }

        /*resourcesMainFiles*/
        List resourcesMainFiles =
            archetypeFilesResolver.findResourcesMainFiles ( files, languageIncludes );
        if ( !resourcesMainFiles.isEmpty () )
        {
//            getLogger().debug("Resolved resources "+resourcesMainFiles);
            files.removeAll ( resourcesMainFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( resourcesMainFiles, filteredIncludes ),
                    3,
                    false,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( resourcesMainFiles, filteredIncludes ),
                    3,
                    false,
                    packageName,
                    false
                )
            );
        }

        /*sourcesTestFiles*/
        List sourcesTestFiles = archetypeFilesResolver.findSourcesTestFiles ( files, languageIncludes );
        if ( !sourcesTestFiles.isEmpty () )
        {
//            getLogger().debug("Resolved test sources "+sourcesTestFiles);
            files.removeAll ( sourcesTestFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( sourcesTestFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( sourcesTestFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    false
                )
            );
        }

        /*ressourcesTestFiles*/
        List resourcesTestFiles =
            archetypeFilesResolver.findResourcesTestFiles ( files, languageIncludes );
        if ( !resourcesTestFiles.isEmpty () )
        {
//            getLogger().debug("Resolved test resources "+resourcesTestFiles);
            files.removeAll ( resourcesTestFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( resourcesTestFiles, filteredIncludes ),
                    3,
                    false,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( resourcesTestFiles, filteredIncludes ),
                    3,
                    false,
                    packageName,
                    false
                )
            );
        }

        /*siteFiles*/
        List siteFiles = archetypeFilesResolver.findSiteFiles ( files, languageIncludes );
        if ( !siteFiles.isEmpty () )
        {
//            getLogger().debug("Resolved site resources "+siteFiles);
            files.removeAll ( siteFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( siteFiles, filteredIncludes ),
                    2,
                    false,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( siteFiles, filteredIncludes ),
                    2,
                    false,
                    packageName,
                    false
                )
            );
        }

        /*thirdLevelSourcesfiles*/
        List thirdLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources ( 3, files, languageIncludes );
        if ( !thirdLevelSourcesfiles.isEmpty () )
        {
//            getLogger().debug("Resolved other sources "+thirdLevelSourcesfiles);
            files.removeAll ( thirdLevelSourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( thirdLevelSourcesfiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( thirdLevelSourcesfiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    false
                )
            );

            /*thirdLevelResourcesfiles*/
            List thirdLevelResourcesfiles =
                archetypeFilesResolver.findOtherResources (
                    3,
                    files,
                    thirdLevelSourcesfiles,
                    languageIncludes
                );
            if ( !thirdLevelResourcesfiles.isEmpty () )
            {
//            getLogger().debug("Resolved other resources "+thirdLevelResourcesfiles);
                files.removeAll ( thirdLevelResourcesfiles );
                resolvedFileSets.addAll (
                    createFileSets (
                        archetypeFilesResolver.getFilteredFiles (
                            thirdLevelResourcesfiles,
                            filteredIncludes
                        ),
                        3,
                        false,
                        packageName,
                        true
                    )
                );
                resolvedFileSets.addAll (
                    createFileSets (
                        archetypeFilesResolver.getUnfilteredFiles (
                            thirdLevelResourcesfiles,
                            filteredIncludes
                        ),
                        3,
                        false,
                        packageName,
                        false
                    )
                );
            }
        } // end if

        /*secondLevelSourcesfiles*/
        List secondLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources ( 2, files, languageIncludes );
        if ( !secondLevelSourcesfiles.isEmpty () )
        {
//            getLogger().debug("Resolved other sources "+secondLevelSourcesfiles);
            files.removeAll ( secondLevelSourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( secondLevelSourcesfiles, filteredIncludes ),
                    2,
                    true,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( secondLevelSourcesfiles, filteredIncludes ),
                    2,
                    true,
                    packageName,
                    false
                )
            );
        }

        /*secondLevelResourcesfiles*/
        List secondLevelResourcesfiles =
            archetypeFilesResolver.findOtherResources ( 2, files, languageIncludes );
        if ( !secondLevelResourcesfiles.isEmpty () )
        {
//            getLogger().debug("Resolved other resources "+secondLevelResourcesfiles);
            files.removeAll ( secondLevelResourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( secondLevelResourcesfiles, filteredIncludes ),
                    2,
                    false,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        secondLevelResourcesfiles,
                        filteredIncludes
                    ),
                    2,
                    false,
                    packageName,
                    false
                )
            );
        }

        /*rootResourcesfiles*/
        List rootResourcesfiles = archetypeFilesResolver.findOtherResources ( 0, files, languageIncludes );
        if ( !rootResourcesfiles.isEmpty () )
        {
//            getLogger().debug("Resolved other resources "+rootResourcesfiles);
            files.removeAll ( rootResourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( rootResourcesfiles, filteredIncludes ),
                    0,
                    false,
                    packageName,
                    true
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( rootResourcesfiles, filteredIncludes ),
                    0,
                    false,
                    packageName,
                    false
                )
            );
        }

        /**/
        if ( !files.isEmpty () )
        {
            getLogger ().info ( "Ignored files: " + files );
        }

        return resolvedFileSets;
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

    private FileSet getUnpackagedFileSet (
        final boolean packaged,
        final boolean filtered,
        final String group,
        final List groupFiles
    )
    {
        Set extensions = getExtensions ( groupFiles );

        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = extensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();

            includes.add ( "**/*." + extension );
        }

        return createFileSet ( excludes, packaged, filtered, group, includes );
    }

    private FileSet getUnpackagedFileSet (
        final boolean packaged,
        final boolean filtered,
        final Set unpackagedExtensions,
        final List unpackagedFiles,
        final String group,
        final Set packagedExtensions
    )
    {
        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = unpackagedExtensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();
            if ( packagedExtensions.contains ( extension ) )
            {
                includes.addAll (
                    archetypeFilesResolver.getFilesWithExtension ( unpackagedFiles, extension )
                );
            }
            else
            {
                includes.add ( "**/*." + extension );
            }
        }

        return createFileSet ( excludes, packaged, filtered, group, includes );
    }
}
