package org.codehaus.mojo.cis;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyTree;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.mojo.cis.model.CisApplication;
import org.codehaus.plexus.archiver.ArchiveFileFilter;
import org.codehaus.plexus.archiver.ArchiveFilterException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipFile;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.SelectorUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractCisWarMojo extends AbstractMojo {
    private static final String CIS_GROUP_ID = "com.softwareag.cis";
    private static final String CIS_ARTIFACT_ID = "cis";
    /**
     * Default set of libraries, which may be excluded from the
     * CIS distribution, because they are added through the
     * Maven POM.
     */
    private static final String[] DEFAULT_LIB_EXCLUDES = new String[]{
        "activation*.jar",
        "avalon-framework*.jar",
        "avalon-logkit*.jar",
        "axis*.jar",
        "batik*.jar",
        "castor*.jar",
        "cis*.jar",
        "commons-discovery*.jar",
        "commons-httpclient*.jar",
        "commons-logging*.jar",
        "jdom*.jar",
        "fop*.jar",
        "hsqldb*.jar",
        "jaxrpc*.jar",
        "krysalis-barcode*.jar",
        "log4j*.jar",
        "mailapi*.jar",
        "rome*.jar",
        "saglic*.jar",
        "wsdl4j*.jar",
        "xalan*.jar",
        "xerces*.jar",
        "xml-aüpis*.jar"
    };

    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * Whether a JAR file will be created for the classes in the webapp. Using this optional configuration
     * parameter will make the generated classes to be archived into a jar file
     * and the classes directory will then be excluded from the webapp.
     *
     * @parameter expression="${cis.archiveClasses}" default-value="false"
     */
    private boolean archiveClasses;

    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${cis.webappDirectory}" default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webappDirectory;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter expression="${cis.warSourceDirectory}" default-value="${basedir}/src/main/webapp"
     * @required
     */
    private File warSourceDirectory;

    /**
     * The list of webResources we want to transfer.
     *
     * @parameter
     */
    private Resource[] webResources;

    /**
     * Filters (property files) to include during the interpolation of the pom.xml.
     *
     * @parameter expression="${project.build.filters}"
     */
    private List filters;

    /**
     * The path to the web.xml file to use.
     *
     * @parameter expression="${cis.webXml}" default-value="src/main/cis/web.xml"
     */
    private File webXml;

    /**
     * The path to the context.xml file to use.
     *
     * @parameter expression="${cis.containerConfigXML}"
     */
    private File containerConfigXML;

    /**
     * Directory to unpack dependent WARs into if needed
     *
     * @parameter expression="${cis.workDir}" default-value="${project.build.directory}/cis/work"
     * @required
     */
    private File workDirectory;

    /**
     * Directory for creating marker files.
     *
     * @parameter expression="${cis.markerDir}" default-value="${project.build.directory}/cis/markers"
     * @required
     */
    private File markerDirectory;

    /**
     * Specifies a list of CIS applications, which are being built into
     * the web application.
     *
     * @parameter
     */
    private CisApplication[] cisApplications; 

    /**
     * Whether to exclude the CIS editor from the war file. This
     * is typically the case for release builds. For development builds,
     * you usually want the editor.
     *
     * @parameter expression="${cis.enableEditor}" default-value="true"
     */
    private boolean enableEditor;

    /**
     * Specifies a set of library files, which are being excluded from the
     * CIS war file. If you do not set this parameter, then a default
     * set of library files is excluded, which matches those libraries,
     * that are added automatically as Maven dependencies.
     * 
     * @parameter
     */
    private String[] libExcludes = DEFAULT_LIB_EXCLUDES;

    /**
     * The license file to include into the web archive.
     *
     * @parameter expression="${cis.licenseFile}" default-value="src/main/cis/license.xml"
     */
    private File licenseFile;

    /**
     * The maven project. This is a component, which is set automatically by Maven and
     * must not be set by the user.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The archiver manager to use for looking up Archiver/UnArchiver implementations.
     * This is a component, which is set automatically by Maven and must not be set
     * by the user.
     * 
     * @component
     * @required
     * @readonly
     */
    private ArchiverManager archiverManager;

    /**
     * The Jar archiver needed for archiving classes directory into jar file under WEB-INF/lib.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     * @required
     * @readonly
     */
    private JarArchiver jarArchiver;

    /**
     * The artifact factory to use. This is a component,
     * which is set automatically by Maven and must not be set
     * by the user.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * The artifact resolver to use. This is a component,
     * which is set automatically by Maven and must not be set
     * by the user.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * The local repository to use.  This is a parameter,
     * which is set automatically by Maven and must not be set
     * by the user.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
    
    /**
     * The list of remote repositories to use.  This is a parameter,
     * which is set automatically by Maven and must not be set
     * by the user.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * The artifact metadata source to use. This is a component,
     * which is set automatically by Maven and must not be set
     * by the user.
     * 
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use. This is a component,
     * which is set automatically by Maven and must not be set
     * by the user.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * The dependency tree builder to use. This is a component,
     * which is set automatically by Maven and must not be set
     * by the user.
     * 
     * @component
     * @required
     * @readonly
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * Called for creating a new war mojo, which does the actual work.
     */
    protected abstract AbstractWarMojo newWarMojo();
    
    protected void setParameter( AbstractWarMojo warMojo, String parameterName,
                                 Object value ) throws MojoExecutionException
    {
        Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses( parameterName, warMojo.getClass() );
        field.setAccessible( true );
        try
        {
            field.set( warMojo, value );
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Initializes the war mojo.
     */
    protected void initWarMojo( AbstractWarMojo warMojo )
        throws MojoExecutionException
    {
        setParameter(warMojo, "archiveClasses", Boolean.valueOf( archiveClasses ) );
        setParameter(warMojo, "archiverManager", archiverManager);
        setParameter(warMojo, "classesDirectory", classesDirectory);
        setParameter(warMojo, "containerConfigXML", containerConfigXML);
        setParameter(warMojo, "filters", filters);
        setParameter(warMojo, "jarArchiver", jarArchiver);
        setParameter(warMojo, "project", project);
        setParameter(warMojo, "warSourceDirectory", warSourceDirectory);
        setParameter(warMojo, "webappDirectory", webappDirectory);
        setParameter(warMojo, "webResources", webResources);
        /* Initialized later in initCisApp():
         * setParameter(warMojo, "webXml", webXml);
         */ 
        setParameter(warMojo, "workDirectory", workDirectory);
    }

    private Artifact getCisJar()
        throws MojoFailureException, MojoExecutionException
    {
        // Search for cis-x.y.jar, first in the direct dependencies
        final Set artifacts = project.getDependencyArtifacts();
        for ( Iterator iter = artifacts.iterator();  iter.hasNext();  )
        {
            Artifact a = (Artifact) iter.next();
            if ( CIS_GROUP_ID.equals( a.getGroupId() )
                 &&  CIS_ARTIFACT_ID.equals( a.getArtifactId() )
                 &&  "jar".equals( a.getType() ) )
            {
                return a;
            }
        }
        // Not found, search for cis-x.y.jar in the transitive dependencies
        DependencyTree tree;
        try
        {
            tree = dependencyTreeBuilder.buildDependencyTree( project, localRepository, artifactFactory,
                                                              artifactMetadataSource, artifactCollector );
        }
        catch ( DependencyTreeBuilderException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        for ( Iterator iter = tree.getArtifacts().iterator();  iter.hasNext();  )
        {
            Artifact a = (Artifact) iter.next();
            if ( CIS_GROUP_ID.equals( a.getGroupId() )
                 &&  CIS_ARTIFACT_ID.equals( a.getArtifactId() )
                 &&  "jar".equals( a.getType() ) )
            {
                return a;
            }
        }
        throw new MojoFailureException( "The project doesn't have a dependency "
                                        + CIS_GROUP_ID + ":" + CIS_ARTIFACT_ID );
    }

    private Artifact getCisWebapp( Artifact cisJar )
        throws MojoExecutionException
    {
        Artifact artifact = artifactFactory.createArtifact( cisJar.getGroupId(),
                                                            cisJar.getArtifactId() + "-webapp",
                                                            cisJar.getVersion(),
                                                            Artifact.SCOPE_COMPILE,
                                                            "war" );
        /* Make sure, that the artifact is available in the local repository.
         * This ensures, that we can access it through
         * <code>localRepository.pathOf( artifact )</code> later on.
         */
        try
        {
            artifactResolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Failed to resolve artifact "
                                              + artifact.getGroupId() + ":"
                                              + artifact.getArtifactId() + ":"
                                              + artifact.getVersion() + ": "
                                              + e.getMessage(), e );
        }
        return artifact;
    }

    private boolean isUptodate( File source, long lastModified, File target )
    {
        if ( lastModified == 0 )
        {
            getLog().debug( "Modification date of target file " + target
                            + " = 0, uptodate check fails." );
            return false;
        }
        long l = source.lastModified();
        if ( l == 0 )
        {
            getLog().debug( "Modification date of source file " + source
                            + " of target file " + target
                            + " = 0, uptodate check fails." );
            return false;
        }
        boolean result = l < lastModified;
        if ( !result ) {
            getLog().debug( "Source file " + source
                            + " isn't uptodate against target file "
                            + target );
        }
        return result;
    }

    private String[] getCisWarExcludes() {
        final String[] otherExcludes = new String[]{
            "cis/licensekey/*",
            "cis/licensekey/**/*",
            "WEB-INF/web.xml",
            "cis/config/startapps.xml"
        };
        final String[] result = new String[otherExcludes.length + libExcludes.length];
        System.arraycopy(otherExcludes, 0, result, 0, otherExcludes.length);
        for (int i = 0;  i < libExcludes.length;  i++) {
            result[otherExcludes.length+i] = "WEB-INF/lib/" + libExcludes[i];
        }
        return result;
    }

    private void extractCisWebapp( Artifact cisWar, File target )
        throws MojoExecutionException
    {
        final File cisWebappFile = new File( localRepository.getBasedir(), localRepository.pathOf( cisWar ) );
        final File markerFile = new File( markerDirectory, cisWebappFile.getName() + ".marker" );
        long lastModified = markerFile.lastModified();
        if ( isUptodate( cisWebappFile, lastModified, markerFile )
             &&  isUptodate( project.getFile(), lastModified, markerFile ) )
        {
            return;
        }
        final String[] includes =
            new String[]{
                "cis/*",
                "cis/**/*",
                "WEB-INF/*",
                "WEB-INF/**/*",
                "META-INF/*",
                "META-INF/**/*",
                "HTMLBasedGUI*",
                "HTMLBasedGUI/**/*"
        };
        String[] excludes = getCisWarExcludes();
        if ( !enableEditor )
        {
            final String[] newExcludes = new String[ excludes.length + 1 ];
            System.arraycopy( excludes, 0, newExcludes, 0, excludes.length );
            newExcludes[ excludes.length ] = "HTMLBasedGUI/com.softwareag.cis.editor*";
            excludes = newExcludes;
        }
        
        mkdir( markerDirectory );
        extractZipFile( target, includes, excludes, cisWebappFile );
        try
        {
            markerFile.delete();
            OutputStream os = new FileOutputStream( markerFile, true );
            os.write( "Marker file, ignore me!".getBytes( "US-ASCII" ) );
            os.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to update marker file "
                                              + markerFile.getPath() + ": "
                                              + e.getMessage(), e );
        }
    }

    private void mkdir( File dir ) throws MojoExecutionException
    {
        if ( dir != null  &&  !dir.isDirectory()  &&  !dir.mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create directory "
                                              + dir.getPath() );
        }
    }

    private void extractZipFile( File target, final String[] includes,
                                 final String[] excludes,
                                 File cisWebappFile )
        throws MojoExecutionException
    {
        if ( !target.isDirectory()  &&  !target.mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create directory " + target );
        }
        ZipUnArchiver unarchiver = new ZipUnArchiver();
        unarchiver.enableLogging( new org.codehaus.mojo.cis.model.Logger( getLog() ) );
        unarchiver.setSourceFile( cisWebappFile );
        unarchiver.setDestDirectory( target );
        unarchiver.setArchiveFilters( Collections.singletonList( new ArchiveFileFilter(){
            private boolean isMatching( String[] patterns, String name )
            {
                for ( int i = 0;  i < patterns.length;  i++ )
                {
                    if ( SelectorUtils.matchPath( patterns[ i ], name ) )
                    {
                        return true;
                    }
                }
                return false;
            }
            public boolean include( InputStream pDataStream, String pEntryName ) throws ArchiveFilterException
            {
                return isMatching( includes, pEntryName )  &&  !isMatching( excludes, pEntryName ); 
            }
        }));
        unarchiver.setOverwrite( true );
        try 
        {
            unarchiver.extract();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        /* Extract web.xml to a different location, because we need it for
         * merging in additional web.xml files.
         */
        extractCisWebFile( cisWebappFile, "WEB-INF/web.xml", "web.xml" );

        /* Extract config/startapps.xml to a different location, because we need
         * to modify it.
         */
        extractCisWebFile( cisWebappFile, "cis/config/startapps.xml", "startapps.xml" );
    }

    
    private void extractCisWebFile( File cisWebappFile,
                                    String pEntry, String pTarget ) throws MojoExecutionException
    {
        ZipFile zipFile;
        try {
            zipFile = new ZipFile( cisWebappFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to open zip file "
                                              + cisWebappFile.getPath()
                                              + ": " + e.getMessage(), e );
        }
        ZipEntry entry = zipFile.getEntry( pEntry );
        if ( entry == null )
        {
            throw new MojoExecutionException( "The zip file "
                                              + cisWebappFile.getPath()
                                              + " contains no entry named "
                                              + pEntry + "." );
        }
        InputStream stream = null;
        OutputStream os = null;
        try
        {
            stream = zipFile.getInputStream( entry );
            os = new FileOutputStream( new File( markerDirectory, pTarget ) );
            byte[] buffer = new byte[8192];
            for (;;)
            {
                int res = stream.read( buffer );
                if ( res == -1 )
                {
                    break;
                }
                if ( res > 0 )
                {
                    os.write( buffer, 0, res );
                }
            }
            os.close();
            os = null;
            stream.close();
            stream = null;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            if ( os != null )
            {
                try
                {
                    os.close();
                }
                catch ( Throwable ignore )
                {
                    // Ignore me
                }
            }
            if ( stream != null )
            {
                try
                {
                    stream.close();
                }
                catch ( Throwable ignore )
                {
                    // Ignore me
                }
            }
        }
    }

    private void createHtmlFiles( File target, CisApplication cisApplication, Artifact cisJar )
        throws MojoFailureException, MojoExecutionException
    {
        final String name = cisApplication.getName();
        if ( name == null  ||  name.trim().length() == 0 )
        {
            throw new MojoFailureException( "A CIS application must have its name set." );
        }
        File xmlDir = cisApplication.getXmlDir();
        if ( xmlDir == null )
        {
            xmlDir = new File( project.getBasedir(), "src/main/cis/" + name + "/xml" );
        }
        final File projectDir = new File( target, name );
        final File xmlToDir = new File( projectDir, "xml" );
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( xmlDir );
        ds.setIncludes( new String[]{ "**/*.xml" } );
        ds.scan();
        final String[] xmlFiles = ds.getIncludedFiles();
        boolean isUptodate = true;
        for ( int i = 0;  i < xmlFiles.length;  i++ )
        {
            File from = new File( xmlDir, xmlFiles[ i ] );
            File to = new File( xmlToDir, xmlFiles[ i ] );
            mkdir( to.getParentFile() );
            if ( isUptodate( from, to.lastModified(), to ) )
            {
                continue;
            }
            try
            {
                FileUtils.copyFile( from, to );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy " + from.getPath()
                                                  + " to " + to.getPath() + ": "
                                                  + e.getMessage(), e );
            }
            isUptodate = false;
        }
        if ( isUptodate )
        {
            return;
        }
        final File logDir = new File( projectDir, "log" );
        mkdir( logDir );
        final File accessPathDir = new File( projectDir, "accesspath" );
        mkdir( accessPathDir );
        runHTMLGenerator( target, xmlDir, projectDir, logDir, accessPathDir, cisJar );
    }

    private void runHTMLGenerator( File targetDir,
                                   File xmlDir, File projectDir,
                                   File logDir, File accessPathDir,
                                   Artifact cisJar )
        throws MojoExecutionException
    {
        final String cisJarPath = localRepository.pathOf( cisJar );
        if ( cisJarPath == null )
        {
            throw new MojoExecutionException( "Failed to resolve path of " + cisJar );
        }
        final File cisJarFile = new File( localRepository.getBasedir(), cisJarPath );
        if ( !cisJarFile.isFile() )
        {
            throw new MojoExecutionException( "The jar file " + cisJarFile.getAbsolutePath() + " doesn't exist." );
        }
        final URL url;
        try {
            url = cisJarFile.toURI().toURL();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        final URL[] urls = new URL[]{ url };
        URLClassLoader urlClassLoader = new URLClassLoader( urls, Thread.currentThread().getContextClassLoader() );
        
        Class c;
        try
        {
            c = urlClassLoader.loadClass( "com.softwareag.cis.gui.generate.HTMLGeneratorWholeDirectory" );
        }
        catch ( ClassNotFoundException e )
        {
            getLog().error( e );
            throw new MojoExecutionException( e.getMessage(), e );
        }
        Method m;
        try
        {
            m = c.getMethod( "main", new Class[]{ String[].class } );
        }
        catch ( NoSuchMethodException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        final String[] args = new String[]{
            xmlDir.getPath(), projectDir.getPath(),
            logDir.getPath(), accessPathDir.getPath()
        };
        final String oldCisHome = System.getProperty( "cis.home" );
        try
        {
            System.setProperty( "cis.home", targetDir.toString() );
            m.invoke( null, new Object[]{ args } );
        }
        catch ( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new MojoExecutionException( t.getMessage(), t );
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        } 
        finally
        {
            if ( oldCisHome == null )
            {
                System.getProperties().remove( "cis.home" );
            }
            else
            {
                System.setProperty( "cis.home", oldCisHome );
            }
        }
    }
    
    private void createHtmlFiles( Artifact cisJar )
        throws MojoFailureException, MojoExecutionException
    {
        if ( cisApplications != null )
        {
            for ( int i = 0;  i < cisApplications.length;  i++ )
            {
                CisApplication cisApplication = cisApplications[ i ];
                createHtmlFiles( webappDirectory, cisApplication, cisJar );
            }
        }
    }

    private String getLicenseKeyName( String jarVersion )
        throws MojoExecutionException
    {
        Pattern p = Pattern.compile( "^(\\d+)\\.(\\d+)" );
        Matcher m = p.matcher( jarVersion );
        if ( !m.find() )
        {
            throw new MojoExecutionException( "Failed to parse jar file version: " + jarVersion );
        }
        return "cit" + m.group( 1 ) + m.group(2) + ".xml";
    }

    private File[] findWebXmlFiles()
    {
        final List webXmlFiles = new ArrayList();
        if ( webXml != null  &&  webXml.isFile() )
        {
            webXmlFiles.add( webXml );
        }
        if ( cisApplications != null )
        {
            for ( int i = 0;  i < cisApplications.length;  i++ )
            {
                File f = cisApplications[i].getAppWebXml();
                if ( f == null ) {
                    f = new File( new File( "src/main/cis", cisApplications[i].getName() ), "web.xml" );
                } else if ( !f.isFile() ) {
                    getLog().warn( "The configured file " + f.getPath() + " does not exist." );
                }
                if ( f.isFile() )
                {
                    webXmlFiles.add( f );
                }
            }
        }
        File f = new File( markerDirectory, "web.xml" );
        if ( !f.isFile() )
        {
            throw new IllegalStateException("CIS web.xml does not exist.");
        }
        webXmlFiles.add( f );
        return (File[]) webXmlFiles.toArray( new File[ webXmlFiles.size() ] );
    }

    protected File createStartappsXmlFile()
    throws MojoExecutionException
    {
        File sourceStartappsXmlFile = new File( markerDirectory, "startapps.xml" );
        File targetStartappsXmlFile = new File( webappDirectory, "cis/config/startapps.xml" );
        long lTarget = targetStartappsXmlFile.lastModified();
        boolean uptodate = isUptodate( project.getFile(), lTarget, targetStartappsXmlFile )
            &&  isUptodate( sourceStartappsXmlFile, lTarget, targetStartappsXmlFile );
        if ( !uptodate )
        {
            mergeStartappsXmlFile( sourceStartappsXmlFile, targetStartappsXmlFile );
        }
        return targetStartappsXmlFile;
    }

    protected File createWebXmlFile()
        throws MojoFailureException, MojoExecutionException
    {
        File[] webXmlFiles = findWebXmlFiles();
        if ( webXmlFiles.length == 1 )
        {
            return webXmlFiles[0];
        }
        File targetWebXml = new File( markerDirectory, "generated-web.xml" );
        long lTarget = targetWebXml.lastModified();
        boolean uptodate = isUptodate( project.getFile(), lTarget, targetWebXml );
        for ( int i = 0;  uptodate && i < webXmlFiles.length;  i++ )
        {
            long lSource = webXmlFiles[i].lastModified();
            uptodate = lSource != 0  &&  lSource < lTarget;
        }
        if ( !uptodate )
        {
            mergeWebXmlFiles( webXmlFiles, targetWebXml );
        }
        return targetWebXml;
    }

    private void mergeStartappsXmlFile( File sourceStartappsXmlFile,
                                        File targetStartappsXmlFile )
        throws MojoExecutionException
    {
        try
        {
            DocumentBuilder db = newDocumentBuilderFactory().newDocumentBuilder();
            Document doc = db.parse(  sourceStartappsXmlFile );
            if ( cisApplications != null )
            {
                Element startAppsElement = doc.getDocumentElement();
                for ( int i = 0;  i < cisApplications.length;  i++ )
                {
                    CisApplication cisApp = cisApplications[i];
                    String[] startAppClasses = cisApp.getStartAppClasses();
                    if ( startAppClasses != null )
                    {
                        for ( int j = 0;  j < startAppClasses.length;  j++ )
                        {
                            final String startAppClass = startAppClasses[i];
                            boolean found = false;
                            for ( Node node = startAppsElement.getFirstChild();  node != null;  node = node.getNextSibling() )
                            {
                                if ( node.getNodeType() == Node.ELEMENT_NODE
                                     &&  (node.getNamespaceURI() == null  ||  node.getNamespaceURI().length() == 0)
                                     &&  "start".equals(node.getLocalName())) {
                                    if (startAppClass.equals(((Element) node).getAttribute( "class" )))
                                    {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if ( !found )
                            {
                                Element e = doc.createElementNS( null, "start" );
                                e.setAttribute( "class", startAppClass );
                                startAppsElement.appendChild( e );
                            }
                        }
                    }
                }
            }
            Transformer t = TransformerFactory.newInstance().newTransformer();
            mkdir( targetStartappsXmlFile.getParentFile() );
            t.transform( new DOMSource( doc), new StreamResult( targetStartappsXmlFile ) );
        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( SAXException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( ParserConfigurationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    private void mergeWebXmlFiles( File[] webXmlFiles, File targetWebXml )
        throws MojoExecutionException, MojoFailureException, TransformerFactoryConfigurationError
    {
        try
        {
            Document[] documents = new Document[webXmlFiles.length];
            DocumentBuilderFactory dbf = newDocumentBuilderFactory();
            for ( int i = 0;  i < documents.length;  i++ )
            {
                File f = webXmlFiles[i];
                try
                {
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    db.setEntityResolver( new EntityResolver(){
                        public InputSource resolveEntity( String pPublicId, String pSystemId )
                            throws SAXException, IOException
                        {
                            getLog().debug( "Resolving entity publicId=" + pPublicId
                                            + ", systemId=" + pSystemId );
                            final String res;
                            if ( "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals( pPublicId ) )
                            {
                                res = "resources/web-app_2_3.dtd";
                            }
                            else
                            {
                                return null;
                            }
                            URL url = getClass().getResource( res );
                            if ( url == null )
                            {
                                throw new SAXException( "Failed to locate resource: " + res );
                            }
                            InputSource isource = new InputSource( url.openStream() );
                            isource.setSystemId( url.toExternalForm() );
                            return isource;
                        }
                    });
                    documents[i] = db.parse( webXmlFiles[i] );
                }
                catch ( SAXParseException e )
                {
                    throw new MojoExecutionException( "Faile to parse file "
                                                      + f.getPath() + " at line " + e.getLineNumber()
                                                      + ", column " + e.getColumnNumber() + ": "
                                                      + e.getMessage(), e );
                }
                catch ( SAXException e )
                {
                    throw new MojoExecutionException( "Failed to parse file "
                                                      + f.getPath() + ": " + e.getMessage(),
                                                      e );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Failed to read file "
                                                      + f.getPath() + ": " + e.getMessage(),
                                                      e );
                }
            }
            Document doc = new WebXmlMerger().merge( documents );
            TransformerFactory.newInstance().newTransformer().transform( new DOMSource( doc), new StreamResult( targetWebXml ) );
        }
        catch ( ParserConfigurationException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "Failed to create file "
                                              + targetWebXml.getPath() + ": "
                                              + e.getMessage(), e );
        }
    }

    private DocumentBuilderFactory newDocumentBuilderFactory()
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating( false );
        dbf.setNamespaceAware( true );
        return dbf;
    }

    protected void initCisApp( AbstractWarMojo warMojo )
        throws MojoExecutionException, MojoFailureException
    {
        Artifact cisJar = getCisJar();
        Artifact cisWar = getCisWebapp( cisJar );
        extractCisWebapp( cisWar, webappDirectory );
        createHtmlFiles( cisJar );
        setParameter( warMojo, "webXml", createWebXmlFile() );
        createStartappsXmlFile();
        File f = new File( webappDirectory, "cis/licensekey/" + getLicenseKeyName( cisJar.getVersion() ) );
        long l = f.lastModified();
        if ( !isUptodate( licenseFile, l, f )  ||  !isUptodate( project.getFile(), l, f ) )
        {
            try
            {
                FileUtils.copyFile( licenseFile, f );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        AbstractWarMojo warMojo = newWarMojo();
        initWarMojo( warMojo );
        initCisApp( warMojo );
        warMojo.execute();
    }
}
