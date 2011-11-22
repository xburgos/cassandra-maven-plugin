package org.codehaus.mojo.tools.rpm;

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
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

/**
 * Utility class used to centralize the logic surrounding RPM spec file handling.
 * 
 * Basically, this class provides two pieces of functionality: building the default RPM name, and
 * creating a RPM spec file.
 * 
 * @plexus.component role="org.codehaus.mojo.tools.rpm.RpmSpecFormatter" role-hint="default"
 */
public class RpmSpecFormatter
{
    public static final String ROLE = RpmSpecFormatter.class.getName ();
    public static final String ROLE_HINT = "default";
    
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
     * Used to retrieve RPM-specific identity formats, such as version-range conversions, 
     * platform name tokens.
     * 
     * @plexus.requirement
     */
    private RpmInfoFormatter rpmInfoFormatter;
    
    public RpmSpecFormatter()
    {
        // used for plexus lookup.
    }

    // used by testers
    public RpmSpecFormatter( MavenProjectBuilder projectBuilder,
        ArtifactFactory artifactFactory, RpmInfoFormatter rpmInfoFormatter )
    {
        this.projectBuilder = projectBuilder;
        this.artifactFactory = artifactFactory;
        this.rpmInfoFormatter = rpmInfoFormatter;
    }
    
    /**
     * Create a RPM spec file based on the specified project, dependency information, set of provided
     * functionality, and temporary working directory (the Make DEST_DIR).
     * 
     * @todo Wire in the provides set.
     */
        //        Summary: Todo
        //        Name: $rpm_name
        //        Version: $rpm_version
        //        Release: $rpm_release
        //        Copyright: Todo
        //        Group: ezbuild
        //        $rpm_deps
        //        Source: Todo
        //        BuildRoot: $rpm_buildroot
        //        AutoReqProv: no
        //
    public String buildSpec( MavenSession session, Set depends, Set provides,
        File destDir, String prefix, String release, boolean rpmNoStrip,
        File preInstallFile, File postInstallFile, File postUninstallFile,
        File preUninstallFile, String platformPostfix, boolean skipPlatformPostfix )
        throws RpmFormattingException
    {
        ArtifactRepository localRepository = session.getLocalRepository();
        StringBuffer specBuffer = new StringBuffer();

        MavenProject project = session.getCurrentProject();
        specBuffer.append( "Summary: " ).append( project.getName() );

        specBuffer.append( "\nName: " ).append( rpmInfoFormatter.formatRpmNameWithoutVersion( session ) );

        String[] versionInfo = rpmInfoFormatter.formatProjectVersionAndRelease( session,
            release, platformPostfix, skipPlatformPostfix );

        specBuffer.append( "\nVersion: " ).append( versionInfo[0] );
        specBuffer.append( "\nRelease: " ).append( versionInfo[1] );

        appendLicenses( project, specBuffer );

        if ( skipPlatformPostfix )
        {
            specBuffer.append( "\nBuildArch: noarch" );
        }
        // TODO: Replace with project categorization when available.
        specBuffer.append( "\nGroup: " ).append( "Maven 2.0" );
        specBuffer.append( "\nPackager: " ).append( "Maven 2.1" );
        
        // rhoover - Until we have a property to say otherwise,
        //           let's assume the package is not relocatable
        //specBuffer.append( "\nPrefix: " ).append( prefix );
        appendDependencyStatement( specBuffer, depends, project.getRemoteArtifactRepositories(), localRepository );
        // TODO: reference source rpm?
        specBuffer.append( "\nBuildRoot: " ).append( destDir.getAbsolutePath() );
        specBuffer.append( "\nAutoReqProv: no\n" );

        // lwt     - nostrip option needed for things like sybase
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
                System.out.println( "Error reading preInstallFile" );
                e.printStackTrace();
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
                System.out.println( "Error reading postInstallFile" );
                e.printStackTrace();
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
                System.out.println( "Error reading postUninstallFile" );
                e.printStackTrace();
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
                System.out.println( "Error reading preUninstallFile" );
                e.printStackTrace();
            }
        }


        //        %files
        specBuffer.append( "\n%files" );
        //        %defattr(-, root, root, -)
        specBuffer.append( "\n%defattr(-, root, root, -)" );

        specBuffer.append( "\n" + prefix + "\n" );

        return specBuffer.toString();
    }

    /**
     * Convenience method used to extract organizational info from the project for use in the copyright, if it
     * exists. Otherwise, return "Unknown"
     */
    private void appendLicenses( MavenProject project, StringBuffer specBuffer )
    {
        List licenses = project.getLicenses();
        
        specBuffer.append( "\nLicense: " );
        
        if ( licenses != null && !licenses.isEmpty() )
        {
            for ( Iterator it = licenses.iterator(); it.hasNext(); )
            {
                License license = (License) it.next();
                
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
    private void appendDependencyStatement( StringBuffer specBuffer, Set depends, List remoteRepositories,
                                            ArtifactRepository localRepository )
        throws RpmFormattingException
    {
        if ( !depends.isEmpty() )
        {
            specBuffer.append( "\nRequires: " );

            for ( Iterator it = depends.iterator(); it.hasNext(); )
            {
                Dependency dependency = (Dependency) it.next();

                Artifact pomArtifact = artifactFactory.createProjectArtifact( dependency.getGroupId(),
                        dependency.getArtifactId(), dependency.getVersion() );

                MavenProject project;
                try
                {
                    project = projectBuilder.buildFromRepository( pomArtifact, remoteRepositories, localRepository );
                }
                catch ( ProjectBuildingException e )
                {
                    throw new RpmFormattingException( "Cannot build POM for dependency: "
                        + dependency.getManagementKey(), e );
                }

                try
                {
                    specBuffer.append( rpmInfoFormatter.formatRpmDependency( project ) );
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
