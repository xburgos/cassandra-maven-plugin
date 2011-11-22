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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * Retrieve various system-specific parameters to aid in RPM building.
 * 
 * @plexus.component role="org.codehaus.mojo.tools.rpm.RpmInfoFormatter" role-hint="default"
 */
public class RpmInfoFormatter
    implements LogEnabled
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private PlatformDetective platformDetective;

    /**
     * @plexus.requirement role-hint="default"
     */
    private VersionRangeFormatter versionRangeFormatter;
    
    private String rpmPlatformName;

    private Logger logger;

    public RpmInfoFormatter()
    {
        // used for plexus init
    }
    
    public RpmInfoFormatter( PlatformDetective detective, VersionRangeFormatter versionRangeFormatter )
    {
        this.platformDetective = detective;
        this.versionRangeFormatter = versionRangeFormatter;
    }

    public String formatRpmNameWithoutVersion( MavenProject project ) throws RpmFormattingException
    {
        return formatRpmName( project, null, null, false, null, false );
    }

    /**
     * Formats a string, from a dependent mavenproject, for a RPM spec file.
     * 
     * @param project Project containing one dependency.
     * @return RPM spec formatted string for the dependency.
     * @author dapplegate
     * @author jdcasey
     */
    public String formatRpmDependency( MavenProject project )
        throws InvalidVersionSpecificationException
    {
        StringBuffer dependency = new StringBuffer();
        String versionString = project.getVersion();
        
        List < String > restrictions = versionRangeFormatter.getRpmVersionRestrictions( versionString );
        
        if ( restrictions != null && !restrictions.isEmpty() )
        {
            for ( Iterator < String > it = restrictions.iterator(); it.hasNext(); )
            {
                String restriction = it.next();
                
                dependency.append( project.getGroupId().replace( '.', '_' ) );
                dependency.append( '_' );
                dependency.append( project.getArtifactId().replace( '.', '_' ) );
                dependency.append( restriction );
                
                if ( it.hasNext() )
                {
                    dependency.append( ' ' );
                }
            }
        }
        else
        {
            dependency.append( project.getGroupId().replace( '.', '_' ) );
            dependency.append( '_' );
            dependency.append( project.getArtifactId().replace( '.', '_' ) );
        }
        
        return dependency.toString();
    }

    /**
     * Format the RPM name for the specified project for use in the Rpm task and the spec file.
     */
    public String formatRpmName( MavenProject project, String rpmVersion, String release,
        boolean withVersionInfo, String platformPostfix, boolean skipPlatformPostfix )
        throws RpmFormattingException
    {
        StringBuffer rpmName = new StringBuffer();
        Properties properties = project.getProperties();

        // FIXME: Find a more elegant way to inject this!
        String rpmNamePrefix = properties.getProperty( "applicationName" );

        getLogger().debug( "Prefixing RPM name with: \'" + rpmNamePrefix + "\'" );

        if ( rpmNamePrefix != null )
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
                String releaseInfo = formatProjectRelease( release, platformPostfix, skipPlatformPostfix );
                rpmName.append( '-' ).append( rpmVersion ).append( '-' ).append( releaseInfo );
            }
        }

        getLogger().info( "RPM Name: \'" + rpmName.toString() + "\'" );

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
     * have POM specify build release version in a mandatory fashion and pass
     * down to this routine.  Reason is tarball will be rrdtool-1.2.22.tar.gz 
     * and rpm will be rrdtool-1.2.22-3.rhel3.i386.rpm on the third modification
     * of the POM The upstream version will not change while the release will
     * change off the same upstream tarball.  platformPostfix is appended to 
     * release number so you know what platform the rpm was compiled on.
     * 
     * @throws RpmFormattingException
     */
    public String formatProjectRelease( String release, String platformPostfix, boolean skipPlatformPostfix )
    throws RpmFormattingException
    {
        String rpmRelease = new String( release );
        if ( !skipPlatformPostfix && ( platformPostfix != null ) && ( platformPostfix.trim().length() > 0 ) )
        {
            rpmRelease +=  "." +  platformPostfix;
        }

        return rpmRelease;
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
            catch ( ComponentLookupException e )
            {
                throw new RpmFormattingException(
                    "Error reading platform distribution information. Component Lookup problem", e );
            }
            catch ( PlatformDetectionException e )
            {
                throw new RpmFormattingException(
                    "Error reading platform distribution information.", e );
            }

            rpmPlatformName = operatingSystemToken.replace( '.', '_' );
        }

        return rpmPlatformName;
    }

    public String formatPlatformArchitecture() throws RpmFormattingException
    {
        try
        {
            return platformDetective.getArchitectureToken();
        }
        catch ( PlatformDetectionException e )
        {
            throw new RpmFormattingException( "Error reading platform distribution information.", e );
        }
    }

    public static boolean getUseRpmFinalName( MavenProject project )
    {
        String rpmUseFinalName = project.getProperties().getProperty( "rpm.useFinalName" );

        boolean useFinalName = false;

        if ( rpmUseFinalName != null )
        {
            useFinalName = Boolean.valueOf( rpmUseFinalName ).booleanValue();
        }

        return useFinalName;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
    
    protected final Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_INFO, "RpmInfoFormatter-lazyLoggerInstance" );
        }

        return logger;
    }
}
