package org.codehaus.mojo.tools.rpm;

/*
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
 *
 */

import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;

/**
 * Retrieve various system-specific parameters to aid in RPM building.
 * 
 * @plexus.component role="org.codehaus.mojo.tools.rpm.RpmInfoFormatter" role-hint="default"
 */
public class RpmInfoFormatter
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private PlatformDetective platformDetective;

    private String rpmPlatformName;
    
    public RpmInfoFormatter()
    {
        // used for plexus init
    }
    
    public RpmInfoFormatter( PlatformDetective detective )
    {
        platformDetective = detective;
    }

    public String formatRpmName( MavenProject project, String release, String platformPostfix,
                                 boolean skipPlatformPostfix ) throws RpmFormattingException
    {
        return formatRpmName( project, release, true, platformPostfix, skipPlatformPostfix );
    }

    public String formatRpmNameWithoutVersion( MavenProject project ) throws RpmFormattingException
    {
        return formatRpmName( project, null, false, null, false );
    }

    /**
     * Formats a string, from a dependent mavenproject, for a RPM spec file.
     * 
     * @param project
     *            Project containing one dependency.
     * @return RPM spec formatted string for the dependency.
     * @author dapplegate
     */
    public String formatRpmDependency( MavenProject project )
    {

        StringBuffer dependency = new StringBuffer();
        String versionString = project.getVersion();
        String[] versions = null;

        if ( versionString != null && !"".equals( versionString ) )
            versions = calcVersions( versionString );

        if ( versions != null )
        {
            for ( int i = 0; i < versions.length; i++ )
            {
                if ( i == 0 && versions.length == 2 )
                {// One of Two
                    generateRequires( project, dependency, ">=", versions[i] );
                }
                if ( i == 0 && versions.length == 1 )
                {// One of One
                    generateRequires( project, dependency, "=", versions[i] );
                }
                if ( i == 1 )
                { // Two of Two
                    dependency.append( "," );
                    generateRequires( project, dependency, "<", versions[i] );
                }
            }
        }
        else
        {
            generateRequires( project, dependency, "", "" );// no version info
        }
        return dependency.toString();
    }

    private static String[] calcVersions( String rawVersion )
    {
        String returnArray[] = null;
        if ( rawVersion.matches( "\\[+[\\w\\.\\,]*\\)+" ) )// e.g.[1.2.0,1.2.2)
        {
            returnArray = new String[2];
            returnArray[0] = rawVersion.substring( 1, rawVersion.indexOf( "," ) );
            returnArray[1] = rawVersion.substring( rawVersion.indexOf( "," ) + 1, rawVersion.length() - 1 );
        }
        else if ( rawVersion.matches( "\\[+[\\w\\.]*\\]+" ) )// e.g. [1.2.0]
        {
            returnArray = new String[1];
            returnArray[0] = rawVersion.substring( 1, rawVersion.length() - 1 );
        }
        return returnArray;

    }

    private static void generateRequires( MavenProject project, StringBuffer dependency, String relation, String version )
    {

        dependency.append( project.getGroupId().replace( '.', '_' ) );
        dependency.append( '_' );
        dependency.append( project.getArtifactId().replace( '.', '_' ) );
        dependency.append( " " );
        dependency.append( relation );
        dependency.append( " " );
        dependency.append( version );
    }

    /**
     * Format the RPM name for the specified project for use in the Rpm task and the spec file.
     */
    public String formatRpmName( MavenProject project, String release, boolean withVersionInfo, String platformPostfix,
                                 boolean skipPlatformPostfix ) throws RpmFormattingException
    {
        StringBuffer rpmName = new StringBuffer();

        Properties properties = project.getProperties();

        // FIXME: Find a more elegant way to inject this!
        String rpmNamePrefix = properties.getProperty( "applicationName" );

        System.out.println( "Prefixing RPM name with: \'" + rpmNamePrefix + "\'" );

        // TODO: Get rid of the Perl exception here!
        if ( rpmNamePrefix != null && !project.getArtifactId().equals( "perl" ) )
        {
            rpmName.append( rpmNamePrefix );
            rpmName.append( "_" );
        }

        if ( getUseRpmFinalName( project ) )
        {
            rpmName.append( project.getBuild().getFinalName() );
        }
        else
        {
            rpmName.append( project.getGroupId().replace( '.', '_' ) );
            rpmName.append( '_' );
            rpmName.append( project.getArtifactId().replace( '.', '_' ) );

            if ( withVersionInfo )
            {
                String[] versionInfo =
                    formatProjectVersionAndRelease( project, release, platformPostfix, skipPlatformPostfix );
                rpmName.append( '-' ).append( versionInfo[0] ).append( '-' ).append( versionInfo[1] );
            }
        }

        System.out.println( "RPM Name: \'" + rpmName.toString() + "\'" );

        return rpmName.toString();
    }

    /**
     * Construct the RPM name for a dependency.
     * 
     * @todo Add version range translation.
     */
    // static String formatArtifactReference( String groupId, String artifactId, String version )
    // {
    // StringBuffer name = new StringBuffer();
    //
    // // TODO: Factor in version range information.
    // name.append( groupId.replace( '.', '_' ) ).append( '_' ).append( artifactId.replace( '.', '_' ) );
    //
    // return name.toString();
    // }
    /**
     * Modified by LWT 07JAN06 - have POM specify build release version in a mandatory fashion and pass down to this
     * routine Reason is tarball will be rrdtool-1.2.22.tar.gz and rpm will be rrdtool-1.2.22-3.i386.rpm on the third
     * modification of the POM The upstream version will not change while the release will change off the same upstream
     * tarball.
     * 
     * @throws RpmFormattingException
     */
    public String[] formatProjectVersionAndRelease( MavenProject project, String release, String platformPostfix,
                                                    boolean skipPlatformPostfix ) throws RpmFormattingException
    {
        String[] versionAndRelease = new String[2];

        versionAndRelease[0] = project.getVersion();
        versionAndRelease[1] = release;
        if ( !skipPlatformPostfix )
        {
            versionAndRelease[1] += platformPostfix == null ? "." + formatRPMPlatformName() : platformPostfix;
        }

        return versionAndRelease;
    }

    /**
     * Retrieve a platform name suffix for use in RPM naming, etc.
     * 
     * @throws RpmFormattingException
     */
    public String formatRPMPlatformName() throws RpmFormattingException
    {
        if ( rpmPlatformName == null )
        {
            String operatingSystemToken;

            try
            {
                operatingSystemToken = platformDetective.getOperatingSystemToken();
            }
            catch ( PlatformDetectionException e )
            {
                throw new RpmFormattingException( "Error reading platform distribution information.", e );
            }

            rpmPlatformName = operatingSystemToken.replace( '.', '_' );
        }

        return rpmPlatformName;
    }

    public String formatPlatformArchitecture() throws RpmFormattingException
    {
        try
        {
            return platformDetective.getOperatingSystemToken();
        }
        catch ( PlatformDetectionException e )
        {
            throw new RpmFormattingException( "Error reading platform distribution information.", e );
        }
    }

    public static boolean getUseRpmFinalName( MavenProject project )
    {
        String rpm_useFinalName = project.getProperties().getProperty( "rpm.useFinalName" );

        boolean useFinalName = false;

        if ( rpm_useFinalName != null )
        {
            useFinalName = Boolean.valueOf( rpm_useFinalName ).booleanValue();
        }

        return useFinalName;
    }
}
