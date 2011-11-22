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

import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.SystemArchitectureDetector;

/**
 * @plexus.component role="org.codehaus.mojo.tools.platform.SystemArchitectureDetector" role-hint="rpm"
 * 
 * @author jdcasey
 *
 */
public class RpmSystemArchitectureDetector implements SystemArchitectureDetector
{
    
    public static final String ROLE_HINT = "rpm";
    
    /**
     * @plexus.requirement role-hint="default"
     */
    private RpmMediator rpmMediator;

    public String getSystemArchitecture() throws PlatformDetectionException
    {
        try
        {
            return rpmMediator.eval( "_build_arch" );
        }
        catch ( RpmEvalException e )
        {
            throw new PlatformDetectionException( "Error retrieving RPM build architecture.", e );
        }
    }

    public boolean isEnabled()
    {
        boolean enabled = true;
        

        enabled = enabled
            && (  "Linux".equalsIgnoreCase( System.getProperty( "os.name" ) )
            || "Mac OS X".equals( System.getProperty( "os.name" ) )
            );

        // MacPorts installs rpm in /opt/local/bin
        enabled = enabled && ( new File( "/bin/rpm" ).exists() 
                  || new File( "/usr/bin/rpm" ).exists() 
                  || new File( "/usr/local/bin/rpm" ).exists() 
                  || new File( "/opt/local/bin/rpm" ).exists() );

        //System.out.println("RpmSystemArchitectureDetector isEnabled returning '" + enabled + "'");

        return enabled;
    }

}
