/*
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
  *
  * Copyright (c) to all contributors
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JavaCommand.java $
  * $Id: JavaCommand.java 14221 2011-06-24 10:16:28Z soelvpil $
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * command line helper for exec'in external java proces.
 *
 * @author ksr@lakeside.dk
 */
public final class JavaCommand
{
    private Commandline commandLine;

    private Log log;

    public JavaCommand( String workingDir, Log log )
    {
        this.log = log;
        this.commandLine = new Commandline();
        commandLine.setExecutable( "java" );
        commandLine.setWorkingDirectory( workingDir );
    }

    void addSystemProperty( String name, String value )
    {
        addNameValue( "-D" + name, value );
    }

    void addJvmOption( String name, String value )
    {
        addNameValue( "-X" + name, value );
    }

    void addExtraJvmOption( String name, String value )
    {
        addNameValue( "-XX" + name, value );
    }

    void addNameValue( String name, String value )
    {
        addArgument( name + "=" + value );
    }

    void addArgument( String arg )
    {
        commandLine.createArgument().setValue( arg );
    }

    int execute()
        throws CommandLineException
    {
        StreamConsumer consumer = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                log.info( line );
            }
        };
        return CommandLineUtils.executeCommandLine( commandLine, consumer, consumer );
    }

    public String toString()
    {
        return commandLine.toString();
    }

}
