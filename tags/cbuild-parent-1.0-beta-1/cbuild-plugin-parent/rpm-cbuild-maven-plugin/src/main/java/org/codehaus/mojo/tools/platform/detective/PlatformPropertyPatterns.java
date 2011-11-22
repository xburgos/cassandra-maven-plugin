package org.codehaus.mojo.tools.platform.detective;

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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This component is knowing patterns used to detect the environment (OS) which you are running
 *
 * @plexus.component role="org.codehaus.mojo.tools.platform.detective.PlatformPropertyPatterns" role-hint="default"
 * @author jdcasey
 */

public class PlatformPropertyPatterns implements Contextualizable
{
    public static final String DEFAULT_DYNPROP_OS = "DYNAMIC.CBUILDPROP.OS";
    public static final String DEFAULT_DYNPROP_ARCH = "DYNAMIC.CBUILDPROP.ARCH";
    public static final String DEFAULT_DYNPROP_RPMVERSION = "DYNAMIC.CBUILDPROP.RPM.VERSION";
    public static final String DEFAULT_DYNPROP_RPMRELEASE = "DYNAMIC.CBUILDPROP.RPM.RELEASE";

    public static final String ROLE = PlatformPropertyPatterns.class.getName();

    public static final String ROLE_HINT = "default";

    public static final String SYSTEM_PROPERTY_PREFIX = "sysprop:";

    private Context containerContext;

    private PlexusContainer container;

    private static final String CONTAINER_KEY = "platformPropertyPatterns:context-container";

    private static final String OS_PATTERNS_KEY = "os-patterns";

    public static final String REF = "ref:";

    // Fedora Core release 4 (Stentz)
    // FIXME
    public static final String FEDORA_PATTERN = "Fedora Core release ([0-9]+) .*";
    //public static final String FEDORA_PATTERN = "Fedora Core release 4 (Stentz)";
    //public static final String FEDORA_PATTERN = "Fedora Core release ([0-9]+)";
    //public static final String FEDORA_PATTERN = "Fedora Core release ([0-9]+) .*$";
    //public static final String FEDORA_PATTERN = "Fedora Core release ([-.0-9]+)";
    public static final String FEDORA_SOURCE = "fedora-release";
    public static final String FEDORA_REF = "fedora";

    public static final String REDHAT_ENTERPRISE_PATTERN = "Red Hat Enterprise Linux .* release ([0-9]+)";
    public static final String REDHAT_ENTERPRISE_SOURCE = "redhat-release";
    public static final String REDHAT_ENTERPRISE_REF = "redhat-enterprise";

    public static final String REDHAT_PATTERN = "Red Hat Linux release ([0-9]+)";
    public static final String REDHAT_SOURCE = "redhat-release";
    public static final String REDHAT_REF = "redhat";

    public static final String GENTOO_PATTERN = "Gentoo Base System release ([0-9]+).([0-9]+)";
    public static final String GENTOO_SOURCE = "gentoo-release";
    public static final String GENTOO_REF = "gentoo";

    public static final String LSB_PATTERN = "DISTRIB_RELEASE=([-.0-9]+)";
    public static final String LSB_SOURCE = "lsb-release";
    public static final String LSB_REF = "lsb";

    public static final String DEBIAN_PATTERN = "([.0-9]+)";
    public static final String DEBIAN_SOURCE = "debian_version";
    public static final String DEBIAN_REF = "debian";

    public static final String CENTOS_PATTERN = "CentOS release ([.0-9]+)";
    public static final String CENTOS_SOURCE = "redhat-release";
    public static final String CENTOS_REF = "centos";

    public static final String DARWIN_PATTERN = "OSX release 10.([0-9]+)";
    public static final String DARWIN_SOURCE = "os.name";
    public static final String DARWIN_REF = "osx";

    public static final String WINDOWS_PATTERN = "Windows release ([0-9]+)";
    public static final String WINDOWS_SOURCE = "os.name";
    public static final String WINDOWS_REF = "win";


    private static final Map < String, String > COMMON_PATTERNS = new HashMap < String, String > ()
    {
        private static final long serialVersionUID = 2L;

        {
            put( FEDORA_REF, FEDORA_PATTERN );
            put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_PATTERN );
            put( REDHAT_REF, REDHAT_PATTERN );
            put( CENTOS_REF, CENTOS_PATTERN );
            put( GENTOO_REF, GENTOO_PATTERN );
            put( LSB_REF, LSB_PATTERN );
            put( DARWIN_REF, DARWIN_PATTERN );
            put( WINDOWS_REF, WINDOWS_PATTERN );
        }
    };

    private static final Map < String, String > COMMON_SOURCES = new HashMap < String, String > ()
    {
        private static final long serialVersionUID = 2L;

        {
            put( FEDORA_REF, FEDORA_SOURCE );
            put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_SOURCE );
            put( REDHAT_REF, REDHAT_SOURCE );
            put( CENTOS_REF, CENTOS_SOURCE );
            put( GENTOO_REF, GENTOO_SOURCE );
            put( LSB_REF, LSB_SOURCE );
            put( DARWIN_REF, DARWIN_SOURCE );
            put( WINDOWS_REF, WINDOWS_SOURCE );
        }
    };

    public static final PlatformPropertyPatterns DEFAULT_PATTERNS = new PlatformPropertyPatterns()
    {
        {
            addOsPattern( FEDORA_SOURCE, FEDORA_PATTERN, "fc$1" );
            addOsPattern( REDHAT_ENTERPRISE_SOURCE, REDHAT_ENTERPRISE_PATTERN, "rhel$1" );
            addOsPattern( REDHAT_SOURCE, REDHAT_PATTERN, "rh$1" );
            addOsPattern( GENTOO_SOURCE, GENTOO_PATTERN, "gentoo$1.$2" );
            addOsPattern( LSB_SOURCE, LSB_PATTERN, "lsb$1" );
            addOsPattern( DEBIAN_SOURCE, DEBIAN_PATTERN, "debian$1" );
            addOsPattern( CENTOS_SOURCE, CENTOS_PATTERN, "centos$1" );
            addOsPattern( WINDOWS_SOURCE, WINDOWS_PATTERN, "win$1" );
            addOsPattern( DARWIN_SOURCE, DARWIN_PATTERN, "osx$1" );
        }
    };

    private Map < String, Properties > osPatternsBySource;

    public String getOperatingSystemToken( String source,
                                           String operatingSystem )
    {
        operatingSystem = operatingSystem.trim();
        source = source.trim();

        Properties props = osPatternsBySource.get( source );

        String prop = extractProperty( operatingSystem, props );

        return prop == null ? null : prop.trim();
    }

    public void setOsPatterns( List < OsPattern > osPatterns )
    {
        osPatternsBySource = DEFAULT_PATTERNS.osPatternsBySource;

        for ( Iterator < OsPattern > it = osPatterns.iterator(); it.hasNext(); )
        {
            OsPattern osPattern = it.next();
            addOsPattern( osPattern.getSource(), osPattern.getExpression(), osPattern.getToken() );
        }
    }

    public void addOsPattern( OsPattern osPattern )
    {
        addOsPattern( osPattern.getSource(), osPattern.getExpression(), osPattern.getToken() );
    }

    public void addOsPattern( String originalSource,
                              String originalPattern,
                              String extractor )
    {
        String source = get( COMMON_SOURCES, originalSource );

        if ( osPatternsBySource == null )
        {
            osPatternsBySource = new HashMap < String, Properties > ();
        }

        Properties osPatterns = (Properties) osPatternsBySource.get( source );
        if ( osPatterns == null )
        {
            osPatterns = new Properties();
            osPatternsBySource.put( source, osPatterns );
        }

        String pattern = get( COMMON_PATTERNS, originalPattern );

        osPatterns.setProperty( pattern, extractor );
    }

    private static String get( Map < String, String > map,
                               String key )
    {
        if ( key.startsWith( REF ) )
        {
            return (String) map.get( key.substring( REF.length() ) );
        }
        else
        {
            return key;
        }
    }

    private String extractProperty( String realValue,
                                    Properties patterns )
    {
        realValue = realValue.trim();
        System.out.println( "extractProperty:: \'" + realValue + "\' " + patterns );

        if ( patterns == null || patterns.keySet() == null || patterns.keySet().iterator() == null )
        {
            System.out.println( "NO Patterns are available !!" );
            return null;
        }

        for ( Iterator it = patterns.keySet().iterator(); it.hasNext(); )
        {
            String pattern = (String) it.next();

            Matcher matcher =
                Pattern.compile( pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE ).matcher( realValue );

            if ( matcher.matches() || matcher.find( 0 ) )
            {
                return replaceReferences( patterns.getProperty( pattern ), matcher );
            }
        }

        return null;
    }

    String replaceReferences( String extraction,
                              Matcher valueSource )
    {
        int groupCount = valueSource.groupCount();
        String result = extraction;

        for ( int i = 0; i <= groupCount; i++ )
        {
            String group = valueSource.group( i );
            result = result.replaceAll( "\\$" + i, group );
        }
        return result;
    }

    public void contextualize( Context context ) throws ContextException
    {
        this.containerContext = context;
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
