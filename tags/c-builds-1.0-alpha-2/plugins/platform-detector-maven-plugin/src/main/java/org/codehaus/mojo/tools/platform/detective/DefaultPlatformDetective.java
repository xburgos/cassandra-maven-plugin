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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.SystemArchitectureDetector;
import org.codehaus.mojo.tools.platform.SystemDistributionDetector;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * This component is responsible for scanning the environment for platform-specific information, so RPM names and
 * artifact classifiers can be formulated correctly.
 *
 * @plexus.component role="org.codehaus.mojo.tools.platform.detective.PlatformDetective" role-hint="default"
 * @author jdcasey
 */
public class DefaultPlatformDetective implements Contextualizable, LogEnabled, PlatformDetective
{

    public static final String ROLE_HINT = "default";

    private String rawOSInfo;

    private String architecture;

    private String releaseSource;

    private Context containerContext;

    private PlexusContainer container;

    private Logger logger;

    /* (non-Javadoc)
     * @see org.codehaus.mojo.tools.platform.PlatformDetective#getArchitectureToken()
     */
    public String getArchitectureToken() throws PlatformDetectionException
    {
        scanArchitectureInfo();

        return architecture;
    }

    /* (non-Javadoc)
     * @see org.codehaus.mojo.tools.platform.PlatformDetective#getOperatingSystemToken()
     */
    public String getOperatingSystemToken() throws PlatformDetectionException
    {
        scanOperatingSystemInfo();

        PlatformPropertyPatterns platformPatterns = getPlatformPatterns();

        return platformPatterns.getOperatingSystemToken( releaseSource, rawOSInfo );
    }

    protected PlatformPropertyPatterns getPlatformPatterns()
    {
        PlatformPropertyPatterns platformPatterns = PlatformPropertyPatterns.readFromContext( containerContext );

        if ( platformPatterns == null )
        {
            getLogger().debug( "Can't get PlatformPropertyPatterns from context: " + containerContext + "; using defaults..." );

            platformPatterns = PlatformPropertyPatterns.DEFAULT_PATTERNS;
        }

        getLogger().debug( "[in detective] Got platform patterns: " + platformPatterns );

        return platformPatterns;
    }

    protected void scanOperatingSystemInfo() throws PlatformDetectionException
    {
        if ( rawOSInfo == null )
        {
            Map distroDetectors;

            try
            {
                distroDetectors = container.lookupMap( SystemDistributionDetector.ROLE );
            }
            catch ( ComponentLookupException e )
            {
                throw new PlatformDetectionException( "Cannot lookup system distribution detector components.", e );
            }

            for ( Iterator it = distroDetectors.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();

                String roleHint = (String) entry.getKey();
                SystemDistributionDetector detector = (SystemDistributionDetector) entry.getValue();

                getLogger().info( "Detector: " + detector.getClass().getName() + " enabled: " + detector.isEnabled() );

                if ( detector.isEnabled() )
                {
                    getLogger().debug( "Applying OS detector: " + roleHint );

                    releaseSource = detector.getDistributionInfoSource();

                    System.out.println( "releaseSource = " + releaseSource );

                    rawOSInfo = detector.getDistributionInfo();
                }
                else
                {
                    getLogger().debug( "Skipping OS detector: " + roleHint );
                }
            }

            if ( rawOSInfo == null )
            {
                throw new PlatformDetectionException( "Failed to detect distribution info for "
                                + System.getProperty( "os.name" ) );
            }
        }
    }

    protected void scanArchitectureInfo() throws PlatformDetectionException
    {
        if ( architecture == null )
        {
            List archDetectors;

            try
            {
                archDetectors = container.lookupList( SystemArchitectureDetector.ROLE );
            }
            catch ( ComponentLookupException e )
            {
                throw new PlatformDetectionException( "Cannot lookup system architecture detector components.", e );
            }

            for ( Iterator it = archDetectors.iterator(); it.hasNext(); )
            {
                SystemArchitectureDetector detector = (SystemArchitectureDetector) it.next();

                if ( detector.isEnabled() )
                {
                    architecture = detector.getSystemArchitecture();
                }
            }

            if ( architecture == null )
            {
                throw new PlatformDetectionException( "Failed to detect architecture info for "
                                + System.getProperty( "os.name" ) );
            }
        }
    }

    public void contextualize( Context context ) throws ContextException
    {
        this.containerContext = context;
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }

}
