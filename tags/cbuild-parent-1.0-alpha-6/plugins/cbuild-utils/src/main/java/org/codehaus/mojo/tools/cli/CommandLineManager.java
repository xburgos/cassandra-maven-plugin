package org.codehaus.mojo.tools.cli;

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

import java.io.InputStream;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Plexus component which command line functions
 * @plexus.component role="org.codehaus.mojo.tools.cli.CommandLineManager" role-hint="default"
 * @author jdcasey
 */
public class CommandLineManager
    extends CommandLineUtils
    implements LogEnabled
{
	/**
	 * The plexus logger object
     */
    private Logger logger;

    public StreamConsumer newDebugStreamConsumer()
    {
        return new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( line );
                }
            }
        };
    }

    public StreamConsumer newInfoStreamConsumer()
    {
        return new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( line );
                }
            }
        };
    }
    /**
     * Command line execution when you don't need to pass in system input
     */
    public int execute( Commandline cli, StreamConsumer systemOut, StreamConsumer systemErr )
        throws CommandLineException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing: " + StringUtils.join( cli.getShellCommandline(), " " ) );
        }
        
        return CommandLineUtils.executeCommandLine( cli, systemOut, systemErr );
    }
    /**
     * Command line execution with system input
     */
    public int execute( Commandline cli, InputStream systemIn, StreamConsumer systemOut, StreamConsumer systemErr )
        throws CommandLineException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing: " + StringUtils.join( cli.getShellCommandline(), " " ) );
        }
        
        return CommandLineUtils.executeCommandLine( cli, systemIn, systemOut, systemErr );
    }

    /**
     * Standard Plexus log implementition retrieval
     */
    protected Logger getLogger()
    {
        if ( this.logger == null )
        {
            this.logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "CommandLineManager::internal" );
        }

        return this.logger;
    }

    /**
     * Plexus log initialization
     */
    public void enableLogging( Logger log )
    {
        this.logger = log;
    }

}
