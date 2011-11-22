package org.codehaus.mojo.tools.platform;

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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * @plexus.component role="org.codehaus.mojo.tools.platform.SystemDistributionDetector" role-hint="darwin-sysprop"
 * @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
 */
public class DarwinSyspropDistributionDetector implements SystemDistributionDetector, LogEnabled
{
    
    public static final String ROLE_HINT = "darwin-sysprop";
    
    // cached.
    private Logger logger;

    public String getDistributionInfo() throws PlatformDetectionException
    {
        try
        {
            return ( "OSX release " + System.getProperty( "os.version" ) );
        }
        catch ( SecurityException e )
        {
            throw new PlatformDetectionException( "Failed to get OS version property." );
        }
    }

    public String getDistributionInfoSource() throws PlatformDetectionException
    {
        return ( "os.name" );
    }

    public boolean isEnabled()
    {
        return ( "Mac OS X".equals( System.getProperty( "os.name" ) ) );
    }

    public void enableLogging( Logger log )
    {
        this.logger = log;
    }
    
    protected Logger getLogger()
    {
        return this.logger;
    }

}
