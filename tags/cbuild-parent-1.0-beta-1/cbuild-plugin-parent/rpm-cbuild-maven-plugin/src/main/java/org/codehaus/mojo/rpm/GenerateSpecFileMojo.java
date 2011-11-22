package org.codehaus.mojo.rpm;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;


/**
 * Build a RPM spec file and save it to disk for subsequent harvesting.
 * 
 * @goal generate-spec
 * @phase package
 */
public class GenerateSpecFileMojo
    extends AbstractMojo
{

    /**
     * Whether to skip RPM spec file generation.
     * 
     * @parameter expression="${skipSpecGeneration}" default-value="false" alias="rpm.genspec.skip"
     * @since 1.0-alpha-1
     */
    private boolean skip;

   /**
    * Override for platform postfix on RPM release number.
    *
    * @parameter expression="${DYNAMIC.CBUILDPROP.OS}" alias="rpm.genspec.platformPostfix"
    * @since 1.0-alpha-1
    */
    private String platformPostfix;

    /**
     * Whether to skip postfix on RPM release number.  When this is set, the RPM <code>BuildArch:</code>
     * will be set to <code>noarch</code> and the RPM <code>Release:</code> will only have the release
     * number established by the platform-detect goal.  When set to true, no <code>BuildArch:</code>
     * will be set in the RPM and the release will have a release number and a platform postfix in it
     * so you can tell what operating system distro was used when the RPM was created.
     *
     * @parameter expression="${skipPlatformPostfix}" default-value="false" alias="rpm.genspec.skipPlatformPostfix"
     * @since 1.0-alpha-1
     */
     private boolean skipPlatformPostfix;

    /**
     * The Artifact instance for the current project. This will be added to the list of 
     * functionality provided by this RPM.
     * 
     * @parameter expression="${project.artifact}"
     * @required
     * @readonly
     * @since 1.0-alpha-1
     */
    private Artifact projectArtifact;

    /**
     * The dependencies of this project, for use in defining the depends and provides sets for
     * the RPM.
     * 
     * @parameter expression="${project.dependencies}"
     * @required
     * @readonly
     * @since 1.0-alpha-1
     */
    private List < Dependency > dependencies;

    /**
     * List of dependencies in the form 'groupId:artifactId' which should be excluded from
     * the list of provided functionality.
     * 
     * @parameter
     * @since 1.0-alpha-1
     */
    private List < String > providesExclusions;

    /**
     * List of dependencies in the form 'groupId:artifactId' which should be excluded from
     * the list of dependency functionality.
     * 
     * @parameter
     * @since 1.0-alpha-1
     */
    private List < String > dependsExclusions;

    /**
     * The current project being built. This instance is used to furnish the information required to
     * construct the RPM spec file
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0-alpha-1
     */
    private MavenProject project;

    /**
     * Override parameter for the name of the RPM to be created.
     * 
     * @parameter
     * @since 1.0-alpha-1
     */
    private String rpmName;

    /**
     * The RPM version, typically set in a Dynamic Maven Property in the platform-detect
     * goal during the validate phase.
     * 
     * @parameter expression="${DYNAMIC.CBUILDPROP.RPM.VERSION}"
     * @since 1.0-beta-1
     */
    private String rpmVersion;

    /**
     * The top directory of the RPM harvesting structure.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     * @since 1.0-alpha-1
     */
    private File outputDirectory;

    /**
     * The top directory of the RPM harvesting structure.
     * 
     * @parameter default-value="${project.build.directory}/rpm-topdir"
     * @required
     * @since 1.0-alpha-1
     */
    private File topDir;

    /**
     * The Make DESTDIR, to let the RPM harvester know where the software staged
     * installation directory is located for packaging
     * 
     * @parameter default-value="${project.build.directory}/rpm-basedir"
     * @required
     * @since 1.0-alpha-1
     */
    private File destDir;

    /**
     * The configure prefix, to let the RPM harvester know how to build the dir structure.
     * 
     * @parameter
     * @required
     * @since 1.0-alpha-1
     */
    private String prefix;

    /**
     * The build number of the RPM, so you get versions like 1.2-4 which
     * would be the fourth build of the 1.2 tarball.  This is typically set during
     * the platform-detect goal in the validate phase.
     *
     * @parameter expression="${DYNAMIC.CBUILDPROP.RPM.RELEASE}"
     * @required
     * @since 1.0-alpha-1
     */
    private String release;

    /**
     * Turn off RPM compression and symbol stripping - this is very much
     * manditory for binary sources like Sybase, Oracle, and the SunJDK.
     * Usually you want this copression, so the default is <code>false</code>.
     *
     * @parameter expression="${rpmNoStrip}" default-value="false"
     * @since 1.0-alpha-2
     */
    private boolean rpmNoStrip;

    /**
     * Create a simple RPM pre install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${preInstallFile}"
     * @since 1.0-alpha-2
     */
    private File preInstallFile;

    /**
     * Create a simple RPM post un install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${postUninstallFile}"
     * @since 1.0-alpha-2
     */
    private File postUninstallFile;

    /**
     * Create a simple RPM pre un install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${preUninstallFile}"
     * @since 1.0-alpha-2
     */
    private File preUninstallFile;

    /**
     * Create a simple RPM post install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (some claim this is weak, but 
     * make the script a .sh.in file and let autoconf do some expanding)
     *
     * @parameter expression="${postInstallFile}"
     * @since 1.0-alpha-2
     */
    private File postInstallFile;

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * Project builder instance, so we can retrieve things like dependency RPM package names from
     * their POMs.
     * 
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * Used to create POM artifact instances from Dependency instances, so we can resolve non-standard
     * RPM package names.
     * 
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    /**
     * Generate a RPM spec file, and write it to disk.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping generation of RPM spec file (per configuration)." );
            return;
        }

        generateJar();
        
        Set < Dependency > depends = new HashSet < Dependency > ();
        Set < Artifact > provides = Collections.singleton( projectArtifact );

        // TODO: [JDCASEY] Fix Depends/Provides once we figure out relocatability...
        getLog().warn( "Assuming all dependencies are real RPM dependencies. "
            + "Provides statement is not currently being populated." );
        for ( Iterator < Dependency > it = dependencies.iterator(); it.hasNext(); )
        {
            Dependency dependency = it.next();

            depends.add( dependency );
        }

        // Marking this out for now...we have to engage in much more complex tactics to determine what is provided,
        // and what is a real RPM dependency...we'll use the above code instead in the interim.
        //        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        //        {
        //            Dependency dependency = (Dependency) it.next();
        //            
        //            if ( Artifact.SCOPE_SYSTEM.equals( dependency.getScope() ) )
        //            {
        //                depends.add( dependency );
        //            }
        //            else
        //            {
        //                provides.add( dependency );
        //            }
        //        }

        processExclusions( provides, depends );

        try
        {

        StringBuffer specBuffer = new StringBuffer();

        specBuffer.append( "Summary: " ).append( project.getName() );

        specBuffer.append( "\nName: " ).append( rpmInfoFormatter.formatRpmNameWithoutVersion( project ) );

        specBuffer.append( "\nVersion: " ).append( rpmVersion );
        specBuffer.append( "\nRelease: " ).append( rpmInfoFormatter.formatProjectRelease( release, 
               platformPostfix, skipPlatformPostfix ) );

        appendLicenses( project, specBuffer );

        if ( skipPlatformPostfix )
        {
            specBuffer.append( "\nBuildArch: noarch" );
        }
        // TODO: Replace with project categorization when available.
        specBuffer.append( "\nGroup: Maven 2.0" );
        specBuffer.append( "\nPackager: Maven 2.1" );
        
        // rhoover - Until we have a property to say otherwise,
        //           let's assume the package is not relocatable
        //specBuffer.append( "\nPrefix: " ).append( prefix );
        appendDependencyStatement( specBuffer, depends, project.getRemoteArtifactRepositories(),
            session.getLocalRepository() );
        // TODO: reference source rpm?
        specBuffer.append( "\nBuildRoot: " ).append( destDir.getAbsolutePath() );
        specBuffer.append( "\nAutoReqProv: no\n" );

        // lwt     - nostrip option needed for things like sybase, oracle, JDK
        if ( rpmNoStrip )
        {
            specBuffer.append( "%define __spec_install_post %{nil}\n" );
        }
        // lwt - (not really) Useful for pre-install and post-install scripts
        specBuffer.append( "%define MavenPrefix " ).append( prefix ).append( "\n" );

        //        %description
        String description = project.getDescription();
        if ( description == null )
        {
            description = "";
        }

        specBuffer.append( "\n%description\n" ).append( description ).append( "\n" );

        // lwt    %pre
        //        <preInstallFile>
        if ( preInstallFile != null )
        {
            try
            {
                FileReader preF = new FileReader( preInstallFile );
                specBuffer.append( "\n\n%pre\n" );
                int c;
                while ( ( c = preF.read() ) != -1 )
                {
                    specBuffer.append( (char) c );
                }
                preF.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error reading preInstallFile. Reason: " + e.getMessage(), e );
            }
        }

        // jjg    %post
        //        <postInstallFile>
        if ( postInstallFile != null )
        {
            try
            {
                FileReader postF = new FileReader( postInstallFile );
                specBuffer.append( "\n\n%post\n" );
                int c;
                while ( ( c = postF.read() ) != -1 )
                {
                    specBuffer.append( (char) c );
                }
                postF.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error reading postInstallFile. Reason: " + e.getMessage(), e );
            }
        }

        // lwt    %postun
        //        <postUninstallFile>
        if ( postUninstallFile != null )
        {
            try
            {
                FileReader postF = new FileReader( postUninstallFile );
                specBuffer.append( "\n\n%postun\n" );
                int c;
                while ( ( c = postF.read() ) != -1 )
                {
                    specBuffer.append( (char) c );
                }
                postF.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error reading postUninstallFile. Reason: " + e.getMessage(), e );
            }
        }

        // lwt    %preun
        //        <preUninstallFile>
        if ( preUninstallFile != null )
        {
            try
            {
                FileReader preF = new FileReader( preUninstallFile );
                specBuffer.append( "\n\n%preun\n" );
                int c;
                while ( ( c = preF.read() ) != -1 )
                {
                    specBuffer.append( (char) c );
                }
                preF.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error reading preUninstallFile. Reason: " + e.getMessage(), e );
            }
        }


        //        %files
        specBuffer.append( "\n%files" );
        //        %defattr(-, root, root, -)
        specBuffer.append( "\n%defattr(-, root, root, -)" );

        specBuffer.append( "\n" + prefix + "\n" );

        writeSpecFile( specBuffer.toString() );
        
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Failed to build RPM spec file. Reason: " + e.getMessage(), e );
        }
    }

    /**
     *  Generates a simple manifest that the maven-jar-plugin will jar up later in the maven lifecycle
     */
    private void generateJar() throws MojoExecutionException
    {
        Properties rpmProps = new Properties();
        String groupId = project.getGroupId(), artifactId = project.getArtifactId(), version = project.getVersion();

        try
        {
            File rpmJarDirectory = new File( outputDirectory, "classes/META-INF/rpm/"
                + groupId + "/" + artifactId );

            if ( !rpmJarDirectory.exists() ) 
            {
                rpmJarDirectory.mkdirs();
            }

            File rpmJarFile = new File( rpmJarDirectory, "rpm.properties" );

            rpmProps.store( new FileOutputStream( ( rpmJarFile ) ), "NAR Properties for "
                    + groupId + "." + artifactId + "-" + version + ", a dummy file for 1.0-beta" );

            getLog().debug( "Wrote JAR properties file for RPM project at " + rpmJarFile );
        }
        catch ( IOException ioe )
        {
            throw new MojoExecutionException( "Cannot write JAR properties file", ioe );
        }

    }

    /**
     * Persist the generated spec file to disk. This file is written into the RPM top-dir under
     * "/SPECS".
     */
    private void writeSpecFile( String spec )
        throws MojoExecutionException
    {
        String specFileName;

        if ( rpmName != null && rpmName.trim().length() > 0 )
        {
            specFileName = rpmName + ".spec";
        }
        else
        {
            try
            {
                specFileName = rpmInfoFormatter.formatRpmName( project, rpmVersion,
                    release, true, platformPostfix, skipPlatformPostfix ) + ".spec";
            }
            catch ( RpmFormattingException e )
            {
                throw new MojoExecutionException( "Failed to format RPM name. Reason: " + e.getMessage(), e );
            }
        }

        File specDir = new File( topDir, "SPECS" );
        specDir.mkdirs();

        File specFile = new File( specDir, specFileName );

        Writer writer = null;
        try
        {
            writer = new FileWriter( specFile );

            writer.write( spec );

            writer.flush();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write spec file: " + specFile, e );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( IOException e )
                {
                    getLog().debug( "Failed to close spec-file writer.", e );
                }
            }
        }
    }

    /**
     * Knock out any explicit exclusions from provided and dependency sets.
     */
    private void processExclusions( Set < Artifact > provides, Set < Dependency > depends )
    {
        Map < String, Dependency > dependencyMap = getDependencyMap();

        if ( providesExclusions != null )
        {
            for ( Iterator < String > it = providesExclusions.iterator(); it.hasNext(); )
            {
                String key = it.next();

                Dependency excluded = dependencyMap.get( key );

                provides.remove( excluded );
            }
        }

        if ( dependsExclusions != null )
        {
            for ( Iterator < String > it = dependsExclusions.iterator(); it.hasNext(); )
            {
                String key = it.next();

                Dependency excluded = (Dependency) dependencyMap.get( key );

                depends.remove( excluded );
            }
        }
    }

    /**
     * Retrieve a map of groupId:artifactId -> Dependency to help with excluding dependencies.
     */
    private Map < String, Dependency > getDependencyMap()
    {
        Map < String, Dependency > deps = new HashMap < String, Dependency > ();

        for ( Iterator < Dependency > it = dependencies.iterator(); it.hasNext(); )
        {
            Dependency dep = it.next();

            String key = ArtifactUtils.versionlessKey( dep.getGroupId(), dep.getArtifactId() );
            deps.put( key, dep );
        }

        return deps;
    }


    /**
     * Convenience method used to extract organizational info from the project for use in the copyright, if it
     * exists. Otherwise, return "Unknown"
     */
    private void appendLicenses( MavenProject tmpProject, StringBuffer specBuffer )
    {
        List < License > licenses = tmpProject.getLicenses();
        
        specBuffer.append( "\nLicense: " );
        
        if ( licenses != null && !licenses.isEmpty() )
        {
            for ( Iterator < License > it = licenses.iterator(); it.hasNext(); )
            {
                License license = it.next();
                
                specBuffer.append( license.getName() );
                
                if ( it.hasNext() )
                {
                    specBuffer.append( ", " );
                }
            }
        }
        else
        {
            specBuffer.append( "Unknown" );
        }
    }


    /**
     * Construct the dependency statement for the RPM. Essentially, this is any dependency declared in the POM
     * with scope == 'system'.
     * @throws RpmFormattingException 
     */
    private void appendDependencyStatement( StringBuffer specBuffer, Set < Dependency > depends,
        List < ArtifactRepository > remoteRepositories, ArtifactRepository localRepository )
        throws RpmFormattingException
    {
        if ( !depends.isEmpty() )
        {
            specBuffer.append( "\nRequires: " );

            for ( Iterator < Dependency > it = depends.iterator(); it.hasNext(); )
            {
                Dependency dependency = it.next();

                Artifact pomArtifact = artifactFactory.createProjectArtifact( dependency.getGroupId(),
                        dependency.getArtifactId(), dependency.getVersion() );

                MavenProject tmpProject;
                try
                {
                    tmpProject = projectBuilder.buildFromRepository( pomArtifact, remoteRepositories, localRepository );
                }
                catch ( ProjectBuildingException e )
                {
                    throw new RpmFormattingException( "Cannot build POM for dependency: "
                        + dependency.getManagementKey(), e );
                }

                try
                {
                    specBuffer.append( rpmInfoFormatter.formatRpmDependency( tmpProject ) );
                }
                catch ( InvalidVersionSpecificationException e )
                {
                    throw new RpmFormattingException( "Failed to parse dependency version range.", e );
                }

                if ( it.hasNext() )
                {
                    specBuffer.append( ", " );
                }
            }
        }
    }

}
