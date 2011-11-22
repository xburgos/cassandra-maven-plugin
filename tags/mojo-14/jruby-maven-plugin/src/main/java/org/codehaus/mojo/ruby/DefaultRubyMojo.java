package org.codehaus.mojo.ruby;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.jruby.JRubyInvoker;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.util.StringOutputStream;

/**
 * This is the default implementation for the RubyMojo, which
 * uses the RubyInvoker.
 * @author Eric Redmond
 */
public class DefaultRubyMojo
    extends AbstractMojo
    implements RubyMojo
{
    private JRubyInvoker jinvoker;

    private Object returned;

    public DefaultRubyMojo( JRubyInvoker invoker )
    {
        this.jinvoker = invoker;
    }

    /**
     * Sets any string key with an object value.
     * @param key identifier for this object.
     * @param value some value object.
     */
    public void set( String key, Object value )
    {
        if ( "reader".equals( key ) )
        {
            jinvoker.setReader( (Reader) value );
        }
        else
        {
            jinvoker.inputValue( key, value );
        }
    }

    /**
     * Gets the value associated with the given key.
     * 
     * TODO: This is really dumb... get rid of it
     * 
     * @param key the key to retrieve values by.
     */
    public Object get( String key )
    {
        if( returned instanceof Map )
        {
            return returned == null ? null : ((Map)returned).get( key );
        }
        else
        {
            return returned;
        }
    }

    /**
     * Implementation of Mojo.execute. Invokes the underlying
     * Ruby script.
     * @throws MojoExecutionException satisfies Mojo contact, not used.
     * @throws MojoFailureException satisfies Mojo contact, not used.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        //jinvoker.setDebug( true );
        try
        {
            StringOutputStream stdout = new StringOutputStream();
            StringOutputStream stderr = new StringOutputStream();

            returned = jinvoker.invoke( stdout, stderr );

            // TODO: This is for future work. Returned object may be a Mojo,
            // to work like BeanShell
            if( returned instanceof Mojo )
            {
                ((Mojo)returned).execute();
            }

            logOutput( stdout.toString(), false );
            logOutput( stderr.toString(), true );
        }
        catch ( Throwable e )
        {
            throw new MojoFailureException( e.getMessage() );
        }
    }

    public Object getReturned()
    {
    	return returned;
    }

    /**
     * Outputs Strings as info or error to the mojo's log.
     * 
     * @param out 
     * @param error true if error
     */
    private void logOutput(  String output, boolean error )
    {
        if ( output != null && output.length() > 0 )
        {
            for ( StringTokenizer tokens = new StringTokenizer( output, "\n" ); tokens.hasMoreTokens(); )
            {
                if ( error )
                {
                    getLog().error( tokens.nextToken() );
                }
                else
                {
                    getLog().info( tokens.nextToken() );
                }
            }
        }
    }

    public void addComponentRequirement( ComponentRequirement componentrequirement, Object obj )
        throws ComponentConfigurationException
    {
        set( componentrequirement.getFieldName(), obj );
    }

    public void setComponentConfiguration( Map map )
        throws ComponentConfigurationException 
    {
        for( Iterator iter = map.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)iter.next();
            set( (String)entry.getKey(), entry.getValue() );
        }
    }
}
