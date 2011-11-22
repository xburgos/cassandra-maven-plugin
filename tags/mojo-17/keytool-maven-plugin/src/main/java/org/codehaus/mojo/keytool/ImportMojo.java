package org.codehaus.mojo.keytool;

/*
 * Copyright 2005-2008 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
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
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Reads the certificate or certificate chain (where the latter is supplied in a PKCS#7 formatted reply) from the file
 * <code>cert_file</code>, and stores it in the keystore entry identified by <code>alias</code>.
 * <p/>
 * Implemented as a wrapper around the SDK <code>keytool -import</code> command.
 *
 * <pre>
 * -import   [-v] [-protected]
 * [-alias &lt;alias&gt;]
 * [-file &lt;file&gt;]
 * [-keypass &lt;keypass&gt;]
 * [-storepass &lt;storepass&gt;]
 * [-storetype &lt;storetype&gt;]
 * [-keystore &lt;keystore&gt;]
 * </pre>
 *
 * @author <a>Juergen Mayrbaeurl</a>
 * @version 1.0
 * @goal import
 * @phase package
 * @requiresProject
 * @todo refactor the common code with javadoc plugin
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html">keystore documentation</a>.
 */
public class ImportMojo
    extends AbstractCmdLineKeyToolMojo
{

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${alias}"
     */
    private String alias;

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${file}"
     */
    private String file;

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${keypass}"
     */
    private String keypass;

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${storetype}"
     */
    private String storetype;

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${storepass}"
     */
    private String storepass;

    /**
     * Enable verbose.
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html#Commands">options</a>.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Imports Cert in current Java Runtime lib/security/cacerts.
     *
     * @parameter expression="${useJREcacerts}" default-value="false"
     */
    private boolean useJREcacerts;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException
    {

        List arguments = new ArrayList();

        Commandline commandLine = new Commandline();

        commandLine.setExecutable( getKeytoolPath() );

        arguments.add( "-import" );
        arguments.add( "-noprompt" );

        addArgIf( arguments, this.verbose, "-v" );

        // I believe Commandline to add quotes where appropriate, although I
        // haven't tested it enough.
        addArgIfNotEmpty( arguments, "-alias", this.alias );

        addArgIfNotEmpty( arguments, "-file", this.file );

        if ( this.keystore != null )
        {
            addArgIfNotEmpty( arguments, "-keystore", this.keystore );
        }
        else if ( this.useJREcacerts )
        {
            arguments.add( "-keystore" );
            arguments.add( KeyToolMojoSupport.getJRECACerts().getAbsolutePath() );
        }

        addArgIfNotEmpty( arguments, "-keypass", this.keypass );
        addArgIfNotEmpty( arguments, "-storetype", this.storetype );
        addArgIfNotEmpty( arguments, "-storepass", this.storepass );

        for ( Iterator it = arguments.iterator(); it.hasNext(); )
        {
            commandLine.createArgument().setValue( it.next().toString() );
        }

        if ( workingDirectory != null )
        {
            commandLine.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        }

        createParentDirIfNecessary( keystore );

        getLog().debug( "Executing: " + commandLine );

        // jarsigner may ask for some input if the parameters are missing or
        // incorrect.
        // This should take care of it and make it fail gracefully
        final InputStream inputStream = new InputStream()
        {
            public int read()
            {
                return -1;
            }
        };
        StreamConsumer outConsumer = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLog().info( line );
            }
        };

        final StringBuffer errBuffer = new StringBuffer();
        StreamConsumer errConsumer = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLog().warn( line );
                errBuffer.append( line );
            }
        };

        try
        {
            int result = executeCommandLine( commandLine, inputStream, outConsumer, errConsumer );

            if ( result != 0 )
            {
                throw new MojoExecutionException( "Result of " + commandLine + " execution is: \'" + result + "\': "
                    + errBuffer.toString() + "." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "command execution failed", e );
        }
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias( String alias )
    {
        this.alias = alias;
    }

    /**
     * @param file the file to set
     */
    public void setFile( String file )
    {
        this.file = file;
    }

    /**
     * @param keypass the keypass to set
     */
    public void setKeypass( String keypass )
    {
        this.keypass = keypass;
    }

    /**
     * @param storetype the storetype to set
     */
    public void setStoretype( String storetype )
    {
        this.storetype = storetype;
    }

    /**
     * @param storepass the storepass to set
     */
    public void setStorepass( String storepass )
    {
        this.storepass = storepass;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    /**
     * @param useJREcacerts the useJREcacerts to set
     */
    public void setUseJREcacerts( boolean useJREcacerts )
    {
        this.useJREcacerts = useJREcacerts;
    }

}
