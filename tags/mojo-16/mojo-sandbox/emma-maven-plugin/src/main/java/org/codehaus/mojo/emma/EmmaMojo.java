package org.codehaus.mojo.emma;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;

import com.vladium.emma.Command;

/**
 * Helper class for other emma Mojos.
 * 
 * @author <a href="anna.nieslony@sdm.de">Anna Nieslony</a>
 */
public abstract class EmmaMojo extends AbstractMojo
{

    /**
     * Run emma command
     * 
     * @param commandName
     *            name of the emma command: run, instr, report, merge
     * @param commandArgs
     *            parameters to run emma
     */
    protected void runEmma( String commandName, String[] commandArgs )
    {
        final Command command = Command.create( commandName, "emma ".concat( commandName ), commandArgs );
        if ( false )
        {
            StringBuffer cmdline = new StringBuffer( "emma " );
            cmdline.append( commandName ).append( ' ' );
            for ( int i = 0; i < commandArgs.length; i++ )
            {
                cmdline.append( "\r\n\t" ).append( commandArgs[i] );
            }
            getLog().info( cmdline );
        }
        command.run();
    }

    /**
     * Run emma command
     * 
     * @param commandName
     *            name of the emma command: run, instr, report, merge
     * @param commandArgs
     *            parameters to run emma (List of String)
     */
    protected void runEmma( String commandName, List commandArgs )
    {
        runEmma( commandName, (String[]) commandArgs.toArray( new String[commandArgs.size()] ) );
    }

    /**
     * @param s
     *            Text
     * @return Text, all \ are replaced by \\
     */
    protected String propertyFormat( CharSequence s )
    {
        StringBuffer result = new StringBuffer();
        for ( int i = 0; i < s.length(); i++ )
        {
            if ( s.charAt( i ) == '\\' )
            {
                result.append( "\\\\" );
            }
            else
            {
                result.append( s.charAt( i ) );
            }
        }
        return result.toString();
    }
}
