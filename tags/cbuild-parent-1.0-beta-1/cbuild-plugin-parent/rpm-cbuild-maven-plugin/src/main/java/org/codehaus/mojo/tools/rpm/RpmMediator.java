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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.StringUtils;


/**
 * @plexus.component role="org.codehaus.mojo.tools.rpm.RpmMediator" role-hint="default"
 * @author jdcasey
 */
public class RpmMediator
    implements LogEnabled
{
    
    public static final String ROLE = RpmMediator.class.getName();
    
    /**
     * @plexus.requirement role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * @plexus.configuration default-value="true"
     */
    private boolean useSudo = true;
    
    /**
     * @plexus.configuration default-value="false"
     */
    private Boolean noParentDirs = false;

    // cache
    private Map < String, Boolean > rpmPresenceQueryResults = new HashMap < String, Boolean > ();

    private Logger logger;
    
    public RpmMediator()
    {
        // used for plexus init.
    }
    
    public RpmMediator( CommandLineManager cliManager )
    {
        // used for testing.
        this.cliManager = cliManager;
    }
    
    public void setUseSudo( boolean useSudo )
    {
        this.useSudo = useSudo;
    }

    public void setNoParentDirs( boolean noParentDirs )
    {
        this.noParentDirs = noParentDirs;
    }

    public void install( String rpmPackage, File rpmFile, boolean force ) throws RpmInstallException
    {
        install( rpmPackage, rpmFile, null, force );
    }
    
    public void install( String rpmPackage, File rpmFile, String rpmDbPath, boolean force ) throws RpmInstallException
    {
        getLogger().debug( "Installing RPM: " + rpmPackage );
        
        Commandline cmdLine = new Commandline();
        if ( useSudo )
        {
            cmdLine.setExecutable( "sudo" );
            cmdLine.createArg().setLine( "rpm" );
        }
        else
        {
            cmdLine.setExecutable( "rpm" );
        }
        
        cmdLine.createArg().setLine( "-Uh" );
        
        if ( force )
        {
            cmdLine.createArg().setLine( "--force" );
            cmdLine.createArg().setLine( "--nodeps" );
        }

        // TODO: figure better way to detect rpm > 4.4.6
        File gentooFile = new File( "/etc/gentoo-release" );
        if ( ( this.noParentDirs ) || ( gentooFile.exists() ) )
        {
            cmdLine.createArg().setLine( "--noparentdirs" );
        }

        cmdLine.createArg().setLine( StringUtils.quoteAndEscape( rpmFile.getAbsolutePath(), (char) 0 ) );
        
        if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
        {
            cmdLine.createArg().setLine( "--dbpath" );
            cmdLine.createArg().setLine( rpmDbPath );
            
            getLogger().debug( "RPM DB Path is set to: \'" + rpmDbPath + "\'" );
        }
        else
        {
            getLogger().debug( "RPM DB Path is not set." );
        }
        
        
        getLogger().debug( "Executing command:\n\t\t" + cmdLine );
        
        try
        {
            StreamConsumer consumer = cliManager.newInfoStreamConsumer();
            
            int exitValue = cliManager.execute( cmdLine, consumer, consumer );

            if ( exitValue == 0 )
            {
                rpmPresenceQueryResults.put( rpmPackage, Boolean.TRUE );
            }
            else
            {
                throw new RpmInstallException( "RPM installation process exited abnormally (returned: " + exitValue
                    + ")." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new RpmInstallException( "Error executing RPM installation.", e );
        }
        
//        boolean isInstalled;
//        
//        try
//        {
//            isInstalled = isRpmInstalled( rpmPackage, rpmDbPath );
//        }
//        catch ( RpmQueryException e )
//        {
//            throw new RpmInstallException( "Error checking for RPM presence. Error was: " + e.getMessage(), e );
//        }
//        
//        if ( !isInstalled )
//        {
//        }        
    }
    
    public void remove( String rpmPackage ) throws RpmInstallException
    {
        remove( rpmPackage, null );
    }
    
    public void remove( String rpmPackage, String rpmDbPath ) throws RpmInstallException
    {
        getLogger().debug( "Attempting to remove: " + rpmPackage );
        
        boolean isInstalled;
        
        try
        {
            isInstalled = isRpmInstalled( rpmPackage, rpmDbPath );
        }
        catch ( RpmQueryException e )
        {
            throw new RpmInstallException( "Error checking for RPM presence. Error was: " + e.getMessage(), e );
        }
        
        if ( isInstalled )
        {
            Commandline cmdLine = new Commandline();

            if ( useSudo )
            {
                cmdLine.setExecutable( "sudo" );
                cmdLine.createArg().setLine( "rpm" );
            }
            else
            {
                cmdLine.setExecutable( "rpm" );
            }

            cmdLine.createArg().setLine( "-e" );
            cmdLine.createArg().setLine( "--allmatches" );

            cmdLine.createArg().setLine( rpmPackage );
            
            if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
            {
                cmdLine.createArg().setLine( "--dbpath" );
                cmdLine.createArg().setLine( rpmDbPath );
                
                getLogger().debug( "RPM DB Path is set to: \'" + rpmDbPath + "\'" );
            }
            else
            {
                getLogger().debug( "RPM DB Path is not set." );
            }

            try
            {
                StreamConsumer consumer = cliManager.newDebugStreamConsumer();
                
                int exitValue = cliManager.execute( cmdLine, consumer, consumer );
                
                if ( exitValue == 0 )
                {
                    rpmPresenceQueryResults.put( rpmPackage, Boolean.FALSE );
                }
                else
                {
                    throw new RpmInstallException( "RPM removal process exited abnormally (returned: "
                        + exitValue + ")." );
                }
            }
            catch ( CommandLineException e )
            {
                throw new RpmInstallException( "Error executing RPM removal.", e );
            }
            
            getLogger().debug( "Removed RPM: " + rpmPackage );
        }
        else
        {
            getLogger().debug( "RPM: " + rpmPackage + " is not installed. Skipping removal." );
        }
    }
    
    public boolean isRpmInstalled( String rpmPackage ) throws RpmQueryException
    {
        return isRpmInstalled( rpmPackage, null );
    }
    
    public boolean isRpmInstalled( String rpmPackage, String rpmDbPath ) throws RpmQueryException
    {
        return isRpmInstalled( rpmPackage, null, rpmDbPath );
    }
    
    public boolean isRpmInstalled( String rpmPackage, String version, String rpmDbPath ) throws RpmQueryException
    {
        return isRpmInstalled( rpmPackage, version, null, rpmDbPath );
    }

    public boolean isRpmInstalled( String rpmPackage, String version, String release,
        String rpmDbPath ) throws RpmQueryException
    {
        boolean result = false;
        
        if ( version != null && version.trim().length() > 0 )
        {
            rpmPackage += "-" + version;
        }
        
        getLogger().debug( "rpmPackage='" + rpmPackage + "'" );
        
        Boolean cached = (Boolean) rpmPresenceQueryResults.get( rpmPackage );
        
        if ( cached != null )
        {
            result = cached.booleanValue();
        }
        else
        {
            Commandline cmdLine = new Commandline();

            // rpm query commands do not typically need sudo
            cmdLine.setExecutable( "rpm" );

            cmdLine.createArg().setLine( "-q" );
            cmdLine.createArg().setLine( rpmPackage );
            
            if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
            {
                cmdLine.createArg().setLine( "--dbpath" );
                cmdLine.createArg().setLine( rpmDbPath );
                
                getLogger().debug( "RPM DB Path is set to: \'" + rpmDbPath + "\'" );
            }
            else
            {
                getLogger().debug( "RPM DB Path is not set." );
            }

            try
            {
                StreamConsumer consumer = cliManager.newDebugStreamConsumer();
                
                int exitValue = cliManager.execute( cmdLine, consumer, consumer );
                
                result = exitValue == 0;

                if ( result && ( release != null ) )
                {
                    // Hold the phone, maybe we have a newer build of the same version to install
                    cmdLine.createArg().setLine( "--queryformat" );
                    cmdLine.createArg().setLine( "%{RELEASE}" );
                    StringStreamConsumer stdoutConsumer = new StringStreamConsumer();
                    exitValue = cliManager.execute( cmdLine, stdoutConsumer, consumer );
                    String myRel = stdoutConsumer.getOutput().trim();
                    if ( ( myRel.length() > 0 ) && ( Character.isDigit( myRel.charAt( 0 ) ) ) )
                    {
                        int rpmRelease;
                        if ( myRel.indexOf( '.' ) > 0 )
                        {
                            rpmRelease = Integer.parseInt( myRel.substring( 0, myRel.indexOf( '.' ) ) );
                        }
                        else
                        {
                            rpmRelease = Integer.parseInt( myRel );
                        }
                        if ( ( exitValue == 0 ) && ( Integer.parseInt( release ) > rpmRelease ) )
                        {
                            getLogger().info( "RPM: " + rpmPackage + " can upgrade from release "
                                + rpmRelease + " to " + release );
                            result = false;
                            return result; // Don't want to record that the package isn't installed
                        }
                        else
                        {
                            getLogger().debug( "RPM: " + rpmPackage + " found release " + rpmRelease
                                + " and will not install release " + release );
                        }
                    }
                }
                
                rpmPresenceQueryResults.put( rpmPackage, Boolean.valueOf( result ) );
            }
            catch ( CommandLineException e )
            {
                throw new RpmQueryException( "Error executing RPM query.", e );
            }
        }
        
        getLogger().info( "RPM: " + rpmPackage + ( result ? " is installed." : " is NOT installed." ) );
        return result;
    }
    
    public String eval( String expression ) throws RpmEvalException
    {
        String result = null;
        
        Commandline cmdLine = new Commandline();
        
        cmdLine.setExecutable( "rpm" );

        cmdLine.createArg().setLine( "--eval \"%{" + expression + "}\"" );

        try
        {
            CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();
            
            int exitValue = cliManager.execute( cmdLine, consumer, cliManager.newDebugStreamConsumer() );
            
            if ( exitValue != 0 )
            {
                throw new RpmEvalException( "RPM evaluation exited abnormally for expression: "
                    + expression + "(returned: " + exitValue + ")." );
            }
            
            result = consumer.getOutput().trim();
        }
        catch ( CommandLineException e )
        {
            throw new RpmEvalException( "Error evaluating RPM expression: " + expression, e );
        }
        
        return result;
    }
    
    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "RpmMediator:internal" );
        }
        
        return logger;
    }

    public void enableLogging( Logger log )
    {
        this.logger = log;
    }

}
