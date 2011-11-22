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

import java.util.List;
import java.util.Properties;

import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;
import org.codehaus.mojo.tools.project.extras.ProjectReleaseInfoUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * This goal will do basic validation of the project for use with the rpm-cbuild-maven-plugin and
 * set Maven dynamic properties which will be needed in later rpm-cbuild goals
 * 
 * @goal platform-detect
 * @phase validate
 * @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
 */
public class ValidateRpmMojo
    extends AbstractMojo
{

    /**
     * Whether to skip RPM platform-detect file generation.
     * 
     * @parameter expression="${skipPlatformDetect}" default-value="false" alias="rpm.platformdetect.skip"
     * @since 1.0-beta-1
     */
    private boolean skip;

    /**
     * platform-detect will set a Maven property during runtime which the project's POM may
     * utilize after this plugin runs.  The system os name (rhel3, fc10, osx5, etc) will be the
     * value and the value of <code>osProperty</code> will be the name of the dynamic Maven property.
     * 
     * @parameter default-value="DYNAMIC.CBUILDPROP.OS"
     * @since 1.0-beta-1
     */
    private String osProperty;

    /**
     * platform-detect will set a Maven property during runtime which the project's POM may
     * utilize after this plugin runs.  The system architecture (i386, x86_64, etc) will be the
     * value and the value of <code>archProperty</code> will be the name of the dynamic Maven property.
     * 
     * @parameter default-value="DYNAMIC.CBUILDPROP.ARCH"
     * @since 1.0-beta-1
     */
    private String archProperty;

    /**
     * platform-detect will set a Maven property during runtime which the project's POM may
     * utilize after this plugin runs.  The declared maven project's version will be parsed to
     * derive an RPM version.  Many legal maven version will generate an error as they introduce
     * compatibility problems with RPM version numbers.  Legal maven versions are similar to 
     * <code>1.2.3-SNAPSHOT</code> and <code>1.2.3-4</code>.  The version will be the  
     * string to the left of the last dash.  Maven keywords like <code>ALPHA</code>, 
     * <code>BETA</code>, <code>RC</code>, and the like will generate an error.  Don't use 
     * maven milestone versioning either such as <code>2.1.0-M1-1</code> as this will not do 
     * what you expect when put into RPM packaging.  If you run into one of these Maven 
     * projects that you want to put in an RPM, best to version it as either 
     * <code>2.1.0-SNAPSHOT</code> or <code>2.0.999-1</code>.
     * 
     * @parameter default-value="DYNAMIC.CBUILDPROP.RPM.VERSION"
     * @since 1.0-beta-1
     */
    private String rpmVersionProperty;

    /**
     * platform-detect will set a Maven property during runtime which the project's POM may
     * utilize after this plugin runs.  The declared maven project's version will be parsed to
     * derive an RPM release number.  This will be concatenated in the rpm-cbuild-maven-plugin
     * with the platform distro (rhel3, osx5, centos5, etc) to get you a full rpm 
     * release named something like <code>4.rhel3</code>.  The release will be the string to
     * the right of the last dash and must be <code>SNAPSHOT</code> or a numeric value, 
     * otherwise an error will be generated.
     * 
     * @parameter default-value="DYNAMIC.CBUILDPROP.RPM.RELEASE"
     * @since 1.0-beta-1
     */
    private String rpmReleaseProperty;

    /**
     * Whether to skip postfix on RPM release number.  When this is set, the RPM <code>BuildArch:</code>
     * will be set to <code>noarch</code> and the RPM <code>Release:</code> will only have the release
     * number established by this platform-detect goal.  When set to true, no <code>BuildArch:</code>
     * will be set in the RPM and the release will have a release number and a platform postfix in it
     * so you can tell what operating system distro was used when the RPM was created.
     *
     * @parameter expression="${skipPlatformPostfix}" default-value="false" alias="rpm.genspec.skipPlatformPostfix"
     * @since 1.0-beta-1
     */
     private boolean skipPlatformPostfix;

    /**
     * The platform detective is a plexus component which may be overloaded if needed.
     * 
     * @component role-hint="default"
     */
    private PlatformDetective detective;

    /**
     * The current project being built. This instance is used to furnish the information required to
     * construct the RPM spec file
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0-beta-1
     */
    private MavenProject project;

    /**
     * Check the system and the project's pom and set appropriate dynamic maven properties
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping generation of RPM spec file (per configuration)." );
            return;
        }

        List < License > licenses = project.getLicenses();
        if ( licenses == null || licenses.isEmpty() )
        {
            throw new MojoExecutionException( "You need to specify the project license in your POM." );
        }

        String description = project.getDescription();
        if ( description == null || ( description.trim().length() < 1 ) )
        {
            throw new MojoExecutionException( "You need to write a description of the project in your POM." );
        }

        // set the system cpu architecture property
        final Properties projectProps = project.getProperties();
        String myprop = projectProps.getProperty( archProperty );
        if ( myprop == null )
        {
            try
            {
                String archToken = detective.getArchitectureToken();
                getLog().debug( "Injecting architecture token: {key: \'"
                    + archProperty + "\', value: \'" + archToken + "\'}." );
                projectProps.put( archProperty, archToken );
            }
            catch ( PlatformDetectionException e )
            {
                throw new MojoExecutionException( "Error scanning for platform architecture information.\n"
                    + "    This usually happens when you don't have the rpm binary in your path.", e );
            }
        }

        // set the OS property
        myprop = projectProps.getProperty( osProperty );
        if ( myprop == null )
        {
            if ( skipPlatformPostfix )
            {
                projectProps.put( osProperty, "noarch" );
            }
            else
            {
                try
                {
                    String osToken = detective.getOperatingSystemToken();

                    getLog().debug( "Setting Dynamic Property for OS token: {key: \'"
                        + osProperty + "\', value: \'" + osToken + "\'}." );

                    projectProps.put( osProperty, osToken );
                }
                catch ( PlatformDetectionException e )
                {
                    throw new MojoExecutionException( "Error scanning for platform distribution information.", e );
                }
                catch ( ComponentLookupException e )
                {
                    throw new MojoExecutionException( "Error scanning for platform distribution information.", e );
                }
            }
        }

        // Scan the version and release for errors
        String origVersion = project.getVersion();
        if ( origVersion.toLowerCase().contains( "-alpha" )
          || origVersion.toLowerCase().contains( "-beta" )
          || origVersion.toLowerCase().contains( "-rc-" ) )
        {
            throw new MojoExecutionException(
                "Illegal version encountered, see http://jira.codehaus.org/browse/CBUILDS-38." );
        }

        String[] splitVersion = ProjectReleaseInfoUtils.splitVersionBaseAndRelease( origVersion, false );
        if ( splitVersion[1] == null )
        {
            throw new MojoExecutionException(
                "No release in version encountered, see http://jira.codehaus.org/browse/CBUILDS-38." );
        }
        
        if ( splitVersion[1] != "SNAPSHOT" )
        {
            int releaseNum;
            try
            {
                    releaseNum = Integer.parseInt( splitVersion[1] );
            }
            catch ( Exception e )
            {
            throw new MojoExecutionException( "Illegal release encountered, "
                + "see http://jira.codehaus.org/browse/CBUILDS-38." );
            }
            if ( releaseNum < 1 )
            {
                throw new MojoExecutionException( 
                   "Release number must be greater than zero, see http://jira.codehaus.org/browse/CBUILDS-38." );
            }
        }


        // Set the release
        myprop = projectProps.getProperty( rpmReleaseProperty );
        if ( myprop == null )
        {
            if ( splitVersion[1] == "SNAPSHOT" )
            {
                projectProps.put( rpmReleaseProperty, "0.0.SNAPSHOT" );
            }
            else
            {
                projectProps.put( rpmReleaseProperty, splitVersion[1] );
            }
        }

        // Set the rpm version
        myprop = projectProps.getProperty( rpmVersionProperty );
        if ( myprop == null )
        {
            projectProps.put( rpmVersionProperty, splitVersion[0] );
        }
    }
}
