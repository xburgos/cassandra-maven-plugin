package org.codehaus.mojo.tools.platform.detective;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.mojo.tools.context.BuildContextUtils;
import org.codehaus.plexus.context.Context;

public class PlatformPropertyPatterns
{
    public static final String SYSTEM_PROPERTY_PREFIX = "sysprop:";

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

    public static final String REDHAT_ENTERPRISE_PATTERN = "Red Hat Enterprise Linux [AW]S release ([0-9]+)";
    public static final String REDHAT_ENTERPRISE_SOURCE = "redhat-release";
    public static final String REDHAT_ENTERPRISE_REF = "redhat-enterprise";

    public static final String REDHAT_PATTERN = "Red Hat Linux release ([0-9]+)";
    public static final String REDHAT_SOURCE = "redhat-release";
    public static final String REDHAT_REF = "redhat";

    public static final String GENTOO_PATTERN = "version ([-.0-9]+)";
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

    // CentOS release 4.4 (Final)

    private static final Map COMMON_PATTERNS = new HashMap()
    {
        {
            put( FEDORA_REF, FEDORA_PATTERN );
            put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_PATTERN );
            put( REDHAT_REF, REDHAT_PATTERN );
            put( CENTOS_REF, CENTOS_PATTERN );
            put( GENTOO_REF, GENTOO_PATTERN );
            put( LSB_REF, LSB_PATTERN );
        }
    };

    private static final Map COMMON_SOURCES = new HashMap()
    {
        {
            put( FEDORA_REF, FEDORA_SOURCE );
            put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_SOURCE );
            put( REDHAT_REF, REDHAT_SOURCE );
            put( GENTOO_REF, GENTOO_SOURCE );
            put( LSB_REF, LSB_SOURCE );
        }
    };

    public static final PlatformPropertyPatterns DEFAULT_PATTERNS = new PlatformPropertyPatterns()
    {
        {
            addOsPattern( FEDORA_SOURCE, FEDORA_PATTERN, "fc$1" );
            addOsPattern( REDHAT_ENTERPRISE_SOURCE, REDHAT_ENTERPRISE_PATTERN, "rhel$1" );
            addOsPattern( REDHAT_SOURCE, REDHAT_PATTERN, "rh$1" );
            addOsPattern( GENTOO_SOURCE, GENTOO_PATTERN, "gentoo$1" );
            addOsPattern( LSB_SOURCE, LSB_PATTERN, "lsb$1" );
            addOsPattern( DEBIAN_SOURCE, DEBIAN_PATTERN, "debian$1" );
            addOsPattern( CENTOS_SOURCE, CENTOS_PATTERN, "centos$1" );
            addOsPattern( SYSTEM_PROPERTY_PREFIX + "os.name", "Windows", "win32" );
        }
    };

    private Map osPatternsBySource;

    public String getOperatingSystemToken( String source,
                                           String operatingSystem )
    {
        // cwb >>>
        operatingSystem = operatingSystem.trim();
        source = source.trim();
        System.out.println(
            "Retrieving OS token for source: '" + source + "' and OS-raw-value: '" + operatingSystem + "'." );

        // cwb >>>

        Properties props = (Properties) osPatternsBySource.get( source );
        System.out.println( "++++++++++++props= " + props );

        String prop = extractProperty( operatingSystem, props );
        System.out.println( "++++++++++++prop= " + prop );

        return prop == null ? null : prop.trim();
        //return extractProperty( operatingSystem, (Properties) osPatternsBySource.get( source ) );
    }

    public void setOsPatterns( List osPatterns )
    {
        System.out.println( "PlatformPropertyPatterns.setOsPatterns (osPatterns= " + osPatterns );
        //Thread.currentThread().dumpStack();

        // cwb >>>
        /*
        if ( osPatternsBySource != null )
        {
            System.out.println( "^^^^^^^^^^^^^^^^^^^^^^^^^^^^WARNING:: CLEARING osPatternsBySource" );
            osPatternsBySource.clear();
        }
        */
        osPatternsBySource = DEFAULT_PATTERNS.osPatternsBySource;
        //<<<<<<

        for ( Iterator it = osPatterns.iterator(); it.hasNext(); )
        {
            OsPattern osPattern = (OsPattern) it.next();
            System.out.println( "@@@@@@@@@ adding " + osPattern.getSource() );
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
            osPatternsBySource = new HashMap();
        }

        Properties osPatterns = (Properties) osPatternsBySource.get( source );
        if ( osPatterns == null )
        {
            osPatterns = new Properties();
            osPatternsBySource.put( source, osPatterns );
        }

        String pattern = get( COMMON_PATTERNS, originalPattern );

        System.out.println( "[PlatformPropertyPatterns] Adding OS pattern: {source: \'" + source + "\', pattern: \'" +
            pattern + "\', token: \'" + extractor + "\'} to: " + this );

        osPatterns.setProperty( pattern, extractor );
    }

    private static String get( Map map,
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
        // cwb
        realValue = realValue.trim();
        System.out.println( "extractProperty:: \'" + realValue + "\' " + patterns );

        // cwb
        if ( patterns == null || patterns.keySet() == null || patterns.keySet().iterator() == null )
        {
            //getLogger().error( "NO Patterns are available !!" );
            System.out.println( "NO Patterns are available !!" );
            return null;
        }

        for ( Iterator it = patterns.keySet().iterator(); it.hasNext(); )
        {
            String pattern = (String) it.next();
            System.out.println( "Checking pattern match for: '" + pattern + "'........" );

            Matcher matcher =
                Pattern.compile( pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE ).matcher( realValue );

            if ( matcher.matches() || matcher.find( 0 ) )
            {
                System.out.println( "MATCH." );
                return replaceReferences( patterns.getProperty( pattern ), matcher );
            }
//            else
//            {
//                getLogger().debug( "no match." );
//            }
        }

        return null;
    }

    //private String replaceReferences( String extraction, Matcher valueSource )
    String replaceReferences( String extraction,
                              Matcher valueSource )
    {
        System.out.println( "replaceReferences::extraction= " + extraction );
        int groupCount = valueSource.groupCount();
        String result = extraction;

        for ( int i = 0; i <= groupCount; i++ )
        {
            String group = valueSource.group( i );
            result = result.replaceAll( "\\$" + i, group );
        }
        System.out.println( "replaceReferences::result= " + result );
        return result;
    }

    public void saveToContext( Context context )
    {
        Map containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        containerMap.put( OS_PATTERNS_KEY, osPatternsBySource );
    }

    public static PlatformPropertyPatterns readFromContext( Context context )
    {
        Map containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        PlatformPropertyPatterns patterns = null;

        if ( containerMap != null )
        {
            Map osPatterns = (Map) containerMap.get( OS_PATTERNS_KEY );
            if ( osPatterns != null )
            {
                patterns = new PlatformPropertyPatterns();
                patterns.osPatternsBySource = osPatterns;
            }
        }
        return patterns;
    }

    public static void deleteFromContext( Context context )
    {
        Map containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        if ( containerMap != null )
        {
            containerMap.clear();
        }
    }
}
