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

    public static final PlatformPropertyPatterns DEFAULT_PATTERNS;

    private static final String CONTAINER_KEY = "platformPropertyPatterns:context-container";

    private static final String OS_PATTERNS_KEY = "os-patterns";
    

    public static final String REF = "ref:";
    
    public static final String FEDORA_PATTERN = "Fedora Core release ([-.0-9]+)";
    
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
    
    
    private static final Map COMMON_PATTERNS;
    
    private static final Map COMMON_SOURCES;

    static
    {
        Map commonPatterns = new HashMap();
        
        commonPatterns.put( FEDORA_REF, FEDORA_PATTERN );
        commonPatterns.put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_PATTERN );
        commonPatterns.put( REDHAT_REF, REDHAT_PATTERN );
        commonPatterns.put( GENTOO_REF, GENTOO_PATTERN );
        commonPatterns.put( LSB_REF, LSB_PATTERN );
        
        COMMON_PATTERNS = commonPatterns;
        
        Map commonSources = new HashMap();
        
        commonSources.put( FEDORA_REF, FEDORA_SOURCE );
        commonSources.put( REDHAT_ENTERPRISE_REF, REDHAT_ENTERPRISE_SOURCE );
        commonSources.put( REDHAT_REF, REDHAT_SOURCE );
        commonSources.put( GENTOO_REF, GENTOO_SOURCE );
        commonSources.put( LSB_REF, LSB_SOURCE );
        
        COMMON_SOURCES = commonSources;
        
        PlatformPropertyPatterns ppp = new PlatformPropertyPatterns();

        ppp.addOsPattern( FEDORA_SOURCE, FEDORA_PATTERN, "fc$1" );
        ppp.addOsPattern( REDHAT_ENTERPRISE_SOURCE, REDHAT_ENTERPRISE_PATTERN, "rhel$1" );
        ppp.addOsPattern( REDHAT_SOURCE, REDHAT_PATTERN, "rh$1" );
        ppp.addOsPattern( GENTOO_SOURCE, GENTOO_PATTERN, "gentoo$1" );
        ppp.addOsPattern( LSB_SOURCE, LSB_PATTERN, "lsb$1" );
        ppp.addOsPattern( SYSTEM_PROPERTY_PREFIX + "os.name", "Windows", "win32" );

        DEFAULT_PATTERNS = ppp;
    }

    private Map osPatternsBySource;

    public String getOperatingSystemToken( String source, String operatingSystem )
    {
//        System.out.println( "Retrieving OS token for source: \'" + source + "\' and OS-raw-value: \'" + operatingSystem + "\'." );
        
        return extractProperty( operatingSystem, (Properties) osPatternsBySource.get( source ) );
    }

    public void setOsPatterns( List osPatterns )
    {
        if ( osPatternsBySource != null )
        {
            osPatternsBySource.clear();
        }

        for ( Iterator it = osPatterns.iterator(); it.hasNext(); )
        {
            OsPattern osPattern = (OsPattern) it.next();

            addOsPattern( osPattern.getSource(), osPattern.getExpression(), osPattern.getToken() );
        }
    }

    public void addOsPattern( OsPattern osPattern )
    {
        addOsPattern( osPattern.getSource(), osPattern.getExpression(), osPattern.getToken() );
    }

    public void addOsPattern( String originalSource, String originalPattern, String extractor )
    {
        String source = originalSource;
        
        if ( source.startsWith( REF ) )
        {
            source = (String) COMMON_SOURCES.get( source.substring( REF.length() ) );
        }

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
        
        String pattern = originalPattern;
        
        if ( pattern.startsWith( REF ) )
        {
            pattern = (String) COMMON_PATTERNS.get( originalPattern.substring( REF.length() ) );
        }

//        System.out.println( "Adding OS pattern: {source: \'" + source + "\', pattern: \'" + pattern + "\', token: \'"
//                        + extractor + "\'} to: " + this );
        
        osPatterns.setProperty( pattern, extractor );
    }

    private String extractProperty( String realValue, Properties patterns )
    {
        for ( Iterator it = patterns.keySet().iterator(); it.hasNext(); )
        {
            String pattern = (String) it.next();
            
//            System.out.println( "Checking pattern match for: \'" + pattern + "\'..." );

            Matcher matcher =
                Pattern.compile( pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE ).matcher( realValue );

            if ( matcher.matches() || matcher.find( 0 ) )
            {
//                System.out.println( "MATCH." );
                
                return replaceReferences( patterns.getProperty( pattern ), matcher );
            }
//            else
//            {
//                getLogger().debug( "no match." );
//            }
        }

        return null;
    }

    private String replaceReferences( String extraction, Matcher valueSource )
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
