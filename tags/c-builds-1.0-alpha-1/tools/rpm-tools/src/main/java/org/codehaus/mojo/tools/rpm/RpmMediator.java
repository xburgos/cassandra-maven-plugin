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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.mojo.tools.antcall.AntCallLogger;
import org.codehaus.mojo.tools.antcall.SysoutLogAdapter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

/**
 * @plexus.component role="org.codehaus.mojo.tools.rpm.RpmMediator" role-hint="default"
 * @author jdcasey
 */
public class RpmMediator
{
    
    /**
     * @plexus.configuration default-value="true"
     */
    private boolean useSudo = true;

    private AntCallLogger log;

    // cache
    private Map rpmPresenceQueryResults = new HashMap();
    
    public RpmMediator()
    {
        // used for plexus init.
    }

    public RpmMediator( boolean useSudo, AntCallLogger log )
    {
        this.useSudo = useSudo;
        this.log = log;
    }
    
    public void setAntCallLogger( AntCallLogger log )
    {
        this.log = log;
    }
    
    public void install( String rpmPackage, File rpmFile, boolean force ) throws RpmInstallException
    {
        install( rpmPackage, rpmFile, null, force );
    }
    
    public void install( String rpmPackage, File rpmFile, String rpmDbPath, boolean force ) throws RpmInstallException
    {
        getLog().info( "Installing RPM: " + rpmPackage );
        
        Commandline cmdLine = new Commandline();
        if ( useSudo )
        {
            cmdLine.setExecutable( "sudo" );
            cmdLine.createArgument().setLine( "rpm" );
        }
        else
        {
            cmdLine.setExecutable( "rpm" );
        }
        
        cmdLine.createArgument().setLine( "-Uvh" );
        
        if ( force )
        {
            cmdLine.createArgument().setLine( "--force" );
        }
        
        try
        {
            cmdLine.createArgument().setLine( Commandline.quoteArgument( rpmFile.getAbsolutePath() ) );
        }
        catch ( CommandLineException e )
        {
            throw new RpmInstallException( "Error setting up RPM installation command line.", e );
        }
        
        if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
        {
            cmdLine.createArgument().setLine( "--dbpath" );
            cmdLine.createArgument().setLine( rpmDbPath );
            
            getLog().info("RPM DB Path is set to: \'" + rpmDbPath + "\'");
        }
        else
        {
            getLog().info("RPM DB Path is not set.");
        }
        
        try
        {
            DefaultConsumer output = new DefaultConsumer();

            int exitValue = CommandLineUtils.executeCommandLine( cmdLine, output, output );

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
    
    private AntCallLogger getLog()
    {
        if ( log == null )
        {
            log = new SysoutLogAdapter();
        }
        
        return log;
    }

    public void remove( String rpmPackage ) throws RpmInstallException
    {
        remove( rpmPackage, null );
    }
    
    public void remove( String rpmPackage, String rpmDbPath ) throws RpmInstallException
    {
        getLog().info( "Attempting to remove: " + rpmPackage );
        
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
                cmdLine.createArgument().setLine( "rpm" );
            }
            else
            {
                cmdLine.setExecutable( "rpm" );
            }

            cmdLine.createArgument().setLine( "-e" );
            cmdLine.createArgument().setLine( "--allmatches" );

            cmdLine.createArgument().setLine( rpmPackage );
            
            if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
            {
                cmdLine.createArgument().setLine( "--dbpath" );
                cmdLine.createArgument().setLine( rpmDbPath );
                
                getLog().info("RPM DB Path is set to: \'" + rpmDbPath + "\'");
            }
            else
            {
                getLog().info("RPM DB Path is not set.");
            }

            try
            {
                DefaultConsumer output = new DefaultConsumer();
                int exitValue = CommandLineUtils.executeCommandLine( cmdLine, output, output );
                
                if ( exitValue == 0 )
                {
                    rpmPresenceQueryResults.put( rpmPackage, Boolean.FALSE );
                }
                else
                {
                    throw new RpmInstallException( "RPM removal process exited abnormally (returned: " + exitValue + ")." );
                }
            }
            catch ( CommandLineException e )
            {
                throw new RpmInstallException( "Error executing RPM removal.", e );
            }
            
            getLog().info( "Removed RPM: " + rpmPackage );
        }
        else
        {
            getLog().info( "RPM: " + rpmPackage + " is not installed. Skipping removal." );
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
        boolean result = false;
        
        if ( version != null && version.trim().length() > 0 )
        {
            rpmPackage += "-" + version;
        }
        
        getLog().debug( "rpmPackage='" + rpmPackage + "'");
        
        Boolean cached = (Boolean) rpmPresenceQueryResults.get( rpmPackage );
        
        if ( cached != null )
        {
            result = cached.booleanValue();
        }
        else
        {
            Commandline cmdLine = new Commandline();

            if ( useSudo )
            {
                cmdLine.setExecutable( "sudo" );
                cmdLine.createArgument().setLine( "rpm" );
            }
            else
            {
                cmdLine.setExecutable( "rpm" );
            }

            cmdLine.createArgument().setLine( "-q" );
            cmdLine.createArgument().setLine( rpmPackage );
            
            if ( rpmDbPath != null && rpmDbPath.trim().length() > 0 )
            {
                cmdLine.createArgument().setLine( "--dbpath" );
                cmdLine.createArgument().setLine( rpmDbPath );
                
                getLog().info("RPM DB Path is set to: \'" + rpmDbPath + "\'");
            }
            else
            {
                getLog().info("RPM DB Path is not set.");
            }

            try
            {
                int exitValue = CommandLineUtils.executeCommandLine( cmdLine, null, null );
                
                result = exitValue == 0;
                
                rpmPresenceQueryResults.put( rpmPackage, Boolean.valueOf( result ) );
            }
            catch ( CommandLineException e )
            {
                throw new RpmQueryException( "Error executing RPM removal.", e );
            }
        }
        
        System.out.println( "RPM: " + rpmPackage + (result?" is installed.":" is NOT installed.") );
        return result;
    }
    
    public String eval( String expression ) throws RpmEvalException
    {
        String result = null;
        
        Commandline cmdLine = new Commandline();
        
        cmdLine.setExecutable( "rpm" );

        cmdLine.createArgument().setLine( "--eval \"%{" + expression + "}\"" );

        try
        {
            CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();
            
            int exitValue = CommandLineUtils.executeCommandLine( cmdLine, consumer, new DefaultConsumer() );
            
            if ( exitValue != 0 )
            {
                throw new RpmEvalException( "RPM evaluation exited abnormally for expression: " + expression + "(returned: " + exitValue + ")." );
            }
            
            result = consumer.getOutput().trim();
        }
        catch ( CommandLineException e )
        {
            throw new RpmEvalException( "Error evaluating RPM expression: " + expression, e );
        }
        
        return result;
    }

}
