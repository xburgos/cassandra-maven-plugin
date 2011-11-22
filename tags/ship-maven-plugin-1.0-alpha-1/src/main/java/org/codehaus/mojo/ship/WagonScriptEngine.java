package org.codehaus.mojo.ship;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.*;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;

import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A script that uses wagon to provide a means of pushing files to arbitrary locations.
 */
public class WagonScriptEngine
    implements ScriptEngine
{

    private final Pattern syntax = Pattern.compile( "\\s*[pP][uU][tT]\\s+([^\\s]+)\\s+[iI][nN]\\s+([^\\s]+)(?:\\s+[iI][dD]\\s+([^\\s]+))?\\s*" );

    public String getExtension()
    {
        return "wagon";
    }

    public static Object lookupGlobalVariable( Map globalVariables, String key, Class clazz )
        throws ScriptException
    {
        if ( !globalVariables.containsKey( key ) )
        {
            throw new ScriptException( "Missing required global variable '" + key + "'" );
        }
        Object result = globalVariables.get( key );
        if ( result == null )
        {
            return result;
        }
        if ( clazz.isAssignableFrom( result.getClass() ) )
        {
            return result;
        }
        throw new ScriptException( "Required global variable '" + key + "' must implement " + clazz.getName() );
    }

    /**
     * {@inheritDoc}
     */
    public Object eval( String script, Map globalVariables )
        throws ScriptException
    {
        Map artifacts = (Map) lookupGlobalVariable( globalVariables, "artifacts", Map.class );
        WagonManager wagonManager =
            (WagonManager) lookupGlobalVariable( globalVariables, "wagonManager", WagonManager.class );
        Settings settings = (Settings) lookupGlobalVariable( globalVariables, "settings", Settings.class );
        Log log = (Log) lookupGlobalVariable( globalVariables, "log", Log.class );
        //MavenProject project = (MavenProject) lookupGlobalVariable( globalVariables, "project", MavenProject.class );

        List commands = new ArrayList();
        try
        {
            LineNumberReader reader = new LineNumberReader( new StringReader( script ) );
            String line;
            while ( null != ( line = reader.readLine() ) )
            {
                Matcher matcher = syntax.matcher( line );
                if ( matcher.matches() )
                {
                    String artifactId = matcher.group( 1 );
                    String url = matcher.group( 2 );
                    File artifact = (File) artifacts.get( artifactId );
                    if ( artifact == null )
                    {
                        throw new ScriptException(
                            "Unknown artifact " + artifactId + " at line " + reader.getLineNumber() );
                    }
                    String serverId = matcher.group(3);
                    if (serverId == null) {
                        serverId = "serverId";
                    }
                    String name;
                    if ( url.endsWith( "/" ) || url.endsWith( "\\" ) )
                    {
                        name = artifact.getName();
                    }
                    else
                    {
                        int i = Math.max( url.lastIndexOf( '/' ), url.lastIndexOf( '\\' ) );
                        if ( i == -1 )
                        {
                            throw new ScriptException(
                                "Cannot parse destination " + url + " at line " + reader.getLineNumber() );
                        }
                        name = url.substring( i + 1 );
                        url = url.substring( 0, i );
                    }
                    commands.add( new Transfer( artifact, serverId, url, name ) );
                }
            }

            for ( Iterator i = commands.iterator(); i.hasNext(); )
            {
                Transfer command = (Transfer) i.next();
                Wagon wagon = null;
                try
                {
                    log.info( "Opening connection to " + command.getUrl() + " with server credentials for id " + command.getServerId() );
                    wagon = createWagon( command.getServerId(), command.getUrl(), wagonManager, settings, log );
                    try
                    {
                        log.info( "Transferring " + command.getArtifact() + " to " + command.getUrl() + " as "
                                      + command.getName() );
                        wagon.put( command.getArtifact(), command.getName() );
                    }
                    catch ( TransferFailedException e )
                    {
                        throw new ScriptException(
                            "Unable to transfer " + command.getArtifact() + " to " + command.getUrl() + " as "
                                + command.getName(), e );
                    }
                    catch ( ResourceDoesNotExistException e )
                    {
                        throw new ScriptException(
                            "Unable to transfer " + command.getArtifact() + " to " + command.getUrl() + " as "
                                + command.getName(), e );
                    }
                    catch ( AuthorizationException e )
                    {
                        throw new ScriptException(
                            "Unauthorized to transfer " + command.getArtifact() + " to " + command.getUrl() + " as "
                                + command.getName(), e );
                    }
                }
                catch ( UnsupportedProtocolException e )
                {
                    throw new ScriptException( "Unsupported protocol for " + command.getUrl(), e );
                }
                catch ( WagonConfigurationException e)
                {
                    throw new ScriptException( "Unable to configure a Wagon instance for " + command.getUrl(), e );
                }
                catch ( WagonException e )
                {
                    throw new ScriptException( "Unable to create a Wagon instance for " + command.getUrl(), e );
                }
                finally
                {
                    if ( wagon != null )
                    {
                        wagon.disconnect();
                    }
                }

            }
            return Boolean.TRUE;
        }
        catch ( ThreadDeath e )
        {
            throw e;
        }
        catch ( ScriptException e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            throw new ScriptException( e );
        }
    }

    /**
     * Convenient method to create a wagon
     *
     * @param id
     * @param url
     * @param wagonManager
     * @param settings
     * @param logger
     * @return
     * @throws MojoExecutionException
     */
    public static Wagon createWagon( String id, String url, WagonManager wagonManager, Settings settings, Log logger )
        throws WagonException, UnsupportedProtocolException, WagonConfigurationException
    {
        Wagon wagon = null;

        final Repository repository = new Repository( id, url );

        wagon = wagonManager.getWagon( repository );

        if ( logger.isDebugEnabled() )
        {
            Debug debug = new Debug();
            wagon.addSessionListener( debug );
            wagon.addTransferListener( debug );
        }

        ProxyInfo proxyInfo = getProxyInfo( settings );
        if ( proxyInfo != null )
        {
            wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ), proxyInfo );
        }
        else
        {
            wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ) );
        }

        return wagon;
    }

    /**
     * Convenience method to map a <code>Proxy</code> object from the user system settings to a
     * <code>ProxyInfo</code> object.
     *
     * @return a proxyInfo object or null if no active proxy is define in the settings.xml
     */
    public static ProxyInfo getProxyInfo( Settings settings )
    {
        ProxyInfo proxyInfo = null;
        if ( settings != null && settings.getActiveProxy() != null )
        {
            Proxy settingsProxy = settings.getActiveProxy();

            proxyInfo = new ProxyInfo();
            proxyInfo.setHost( settingsProxy.getHost() );
            proxyInfo.setType( settingsProxy.getProtocol() );
            proxyInfo.setPort( settingsProxy.getPort() );
            proxyInfo.setNonProxyHosts( settingsProxy.getNonProxyHosts() );
            proxyInfo.setUserName( settingsProxy.getUsername() );
            proxyInfo.setPassword( settingsProxy.getPassword() );
        }

        return proxyInfo;
    }


    private static final class Transfer
    {
        private final File artifact;

        private final String serverId;

        private final String url;

        private final String name;

        private Transfer( File artifact, String serverId, String url, String name )
        {
            this.artifact = artifact;
            this.serverId = serverId;
            this.url = url;
            this.name = name;
        }

        public File getArtifact()
        {
            return artifact;
        }

        public String getServerId()
        {
            return serverId;
        }

        public String getUrl()
        {
            return url;
        }

        public String getName()
        {
            return name;
        }
    }

}
