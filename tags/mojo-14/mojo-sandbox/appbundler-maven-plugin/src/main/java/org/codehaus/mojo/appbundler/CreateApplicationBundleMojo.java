package org.codehaus.mojo.appbundler;

/*
 * Copyright 2001-2006 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.velocity.VelocityComponent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Package dependencies as an Application Bundle for Mac OS X.
 *
 * @goal bundle
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class CreateApplicationBundleMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * The directory where the application bundle will be created
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}.app";
     */
    private File buildDirectory;

    /**
     * The location of the generated disk image file
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}.dmg"
     */
    private File diskImageFile;


    /**
     * The location of the Java Application Stub
     *
     * @parameter expression="/System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/JavaApplicationStub";
     */
    private File javaApplicationStub;

    /**
     * The main class to execute when double-clicking the Application Bundle
     *
     * @parameter expression="${mainClass}"
     * @required
     */
    private String mainClass;

    /**
     * The name of the Bundle. This is what will show up in the application menu, dock etc.
     *
     * @parameter default-value="${project.name}"
     * @required
     */
    private String bundleName;

    /**
     * The icon file for the bundle
     *
     * @parameter expression="${iconFile}"
     */
    private File iconFile;

    /**
     * The version of the project. Will be used as the value of the CFBundleVersion key.
     * @parameter expression="${project.version}"
     */
    private String version;

    /**
     * A value for the JVMVersion key.
     * @parameter default-value="1.4+"
     */
    private String jvmVersion;

    /**
     * The location of the produced Zip file containing the bundle.
     * @parameter expression="${project.build.directory}/${project.build.finalName}-app.zip"
     */
    private File zipFile;


    /**
     * Velocity Component.
     *
     * @component role="org.codehaus.plexus.velocity.VelocityComponent"
     * @readonly
     */
    private VelocityComponent velocity;


    /**
     * The location of the template for Info.plist.
     * Classpath is checked before the file system.
     *
     * @parameter default-value="org/codehaus/mojo/appbundler/Info.plist.template"
     */
    private String dictionaryFile;

    /**
     * The Zip archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
     * @required
     */
    private ZipArchiver zipArchiver;

    /**
     * Bundle project as a Mac OS X application bundle.
     *
     * @throws MojoExecutionException If an unexpected error occurs during packaging of the bundle.
     */
    public void execute()
        throws MojoExecutionException
    {

        // Set up and create directories
        buildDirectory.mkdirs();

        File contentsDir = new File( buildDirectory, "Contents" );
        contentsDir.mkdirs();

        File resourcesDir = new File( contentsDir, "Resources" );
        resourcesDir.mkdirs();

        File javaDirectory = new File( resourcesDir, "Java" );
        javaDirectory.mkdirs();

        File macOSDirectory = new File( contentsDir, "MacOS" );
        macOSDirectory.mkdirs();

        // Copy in the native java application stub
        File stub = new File( macOSDirectory, "JavaApplicationStub" );
        try
        {
            FileUtils.copyFile( javaApplicationStub, stub );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "Could not copy file " + javaApplicationStub + " to directory " + macOSDirectory, e );
        }

        // Copy icon file to the bundle if specified
        if ( iconFile != null )
        {
            try
            {
                FileUtils.copyFileToDirectory( iconFile, resourcesDir );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error copying file " + iconFile + " to " + resourcesDir, e );
            }
        }

        // Resolve and copy in all dependecies from the pom
        List files = copyDependencies( javaDirectory );

        // Create and write the Info.plist file
        File infoPlist = new File( buildDirectory, "Contents/Info.plist" );
        writeInfoPlist( infoPlist, files );


        if(isOsX()){
            // Make the stub executable
            Commandline chmod = new Commandline();
            try
            {
                chmod.setExecutable( "chmod" );
                chmod.createArgument().setValue( "755" );
                chmod.createArgument().setValue( stub.getAbsolutePath() );

                chmod.execute();
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException( "Error executing " + chmod + " ", e );
            }


            // This makes sure that the .app dir is actually registered as an application bundle
            Commandline setFile = new Commandline();
            try
            {
                setFile.setExecutable( "/Developer/Tools/SetFile" );
                setFile.createArgument().setValue( "-a B" );
                setFile.createArgument().setValue( buildDirectory.getAbsolutePath() );

                setFile.execute();
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException( "Error executing " + setFile, e );
            }

            // Create a .dmg file of the app
            Commandline dmg = new Commandline();
            try
            {
                dmg.setExecutable( "hdiutil" );
                dmg.createArgument().setValue( "create" );
                dmg.createArgument().setValue( "-srcfolder" );
                dmg.createArgument().setValue( buildDirectory.getAbsolutePath() );
                dmg.createArgument().setValue( diskImageFile.getAbsolutePath() );
                dmg.execute();
            }
            catch ( CommandLineException e )
            {
                throw new MojoExecutionException( "Error creating disk image " + diskImageFile, e );
            }
        }

        zipArchiver.setDestFile( zipFile);
        try
        {
            zipArchiver.addDirectory( buildDirectory.getParentFile(), new String[] {buildDirectory.getName() +"/**"}, new String[] {"**/JavaApplicationStub"});

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( buildDirectory.getParentFile());
            scanner.setIncludes( new String[] {buildDirectory.getName() +"/**/JavaApplicationStub"});
            scanner.scan();

            String[] stubs = scanner.getIncludedFiles();
            for ( int i = 0; i < stubs.length; i++ )
            {
                String s = stubs[i];
                zipArchiver.addFile( new File(buildDirectory.getParentFile(), s), s, 0755);
            }

            zipArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Could not create zip archive of application bundle in " +zipFile, e);
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "IOException creating zip archive of application bundle in " +zipFile, e);
        }


    }

    private boolean isOsX()
    {
        return System.getProperty( "mrj.version") != null;
    }

    /**
     * Copy all dependencies into the $JAVAROOT directory
     *
     * @param javaDirectory where to put jar files
     * @return A list of file names added
     * @throws MojoExecutionException
     */
    private List copyDependencies( File javaDirectory )
        throws MojoExecutionException
    {

        List list = new ArrayList();

        // First, copy the project's own artifact
        File artifactFile = project.getArtifact().getFile();
        list.add( artifactFile.getName() );

        try
        {
            FileUtils.copyFileToDirectory( artifactFile, javaDirectory );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not copy artifact file " + artifactFile + " to " + javaDirectory );
        }

        Set artifacts = project.getArtifacts();

        Iterator i = artifacts.iterator();

        while ( i.hasNext() )
        {
            Artifact artifact = (Artifact) i.next();

            File file = artifact.getFile();

            getLog().debug( "Adding " + file );

            try
            {
                FileUtils.copyFileToDirectory( file, javaDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error copying file " + file + " into " + javaDirectory, e );
            }

            list.add( file.getName() );
        }



    return list;

    }

    /**
     * Writes an Info.plist file describing this bundle.
     *
     * @param infoPlist The file to write Info.plist contents to
     * @param files     A list of file names of the jar files to add in $JAVAROOT
     * @throws MojoExecutionException
     */
    private void writeInfoPlist( File infoPlist, List files )
        throws MojoExecutionException
    {

        VelocityContext velocityContext = new VelocityContext();

        velocityContext.put( "mainClass", mainClass );

        velocityContext.put( "bundleName", bundleName );

        velocityContext.put( "iconFile", iconFile == null ? "GenericJavaApp.icns" : iconFile.getName() );

        velocityContext.put( "version", version);

        velocityContext.put( "jvmVersion", jvmVersion);
        
        StringBuffer jarFilesBuffer = new StringBuffer();

        jarFilesBuffer.append( "<array>" );
        for ( int i = 0; i < files.size(); i++ )
        {
            String name = (String) files.get( i );
            jarFilesBuffer.append( "<string>" );
            jarFilesBuffer.append( "$JAVAROOT/" ).append( name );
            jarFilesBuffer.append( "</string>" );

        }
        jarFilesBuffer.append( "</array>" );

        velocityContext.put( "classpath", jarFilesBuffer.toString() );

        try
        {

            FileWriter writer = new FileWriter( infoPlist );
            velocity.getEngine().mergeTemplate( dictionaryFile, "utf-8", velocityContext, writer );
            writer.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not write Info.plist to file " + infoPlist, e );
        }
        catch ( ParseErrorException e )
        {
            throw new MojoExecutionException( "Error parsing " + dictionaryFile, e );
        }
        catch ( ResourceNotFoundException e )
        {
            throw new MojoExecutionException( "Could not find resource for template " + dictionaryFile, e );
        }
        catch ( MethodInvocationException e )
        {
            throw new MojoExecutionException(
                "MethodInvocationException occured merging Info.plist template " + dictionaryFile, e );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Exception occured merging Info.plist template " + dictionaryFile, e );
        }

    }
}
