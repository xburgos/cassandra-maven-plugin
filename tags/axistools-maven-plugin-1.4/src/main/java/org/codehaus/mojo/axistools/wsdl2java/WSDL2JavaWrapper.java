package org.codehaus.mojo.axistools.wsdl2java;

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

import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.WSDL2Java;
import org.apache.axis.wsdl.gen.WSDL2;
import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author: jesse
 * @version: $Id$
 */
public class WSDL2JavaWrapper
    extends WSDL2Java
{
    public void execute( String args[] )
        throws AxisPluginException
    {
        try
        {
            // Extremely ugly hack because the "options" static field in WSDL2Java
            // shadows the "options" instance field in WSDL2. It is the field
            // in WSDL2 that we need because the command line options
            // defined in subclasses get copied to it.
            // The result is that options defined in WSDL2 ( timeout, Debug )
            // are not available otherwise.  (MOJO-318)
            Field field = WSDL2.class.getDeclaredField( "options" );

            CLOptionDescriptor[] options = (CLOptionDescriptor[]) field.get( this );

            // Parse the arguments
            CLArgsParser argsParser = new CLArgsParser( args, options );

            // Print parser errors, if any
            if ( null != argsParser.getErrorString() )
            {
                System.err.println( Messages.getMessage( "error01", argsParser.getErrorString() ) );
                printUsage();
            }

            // Get a list of parsed options
            List clOptions = argsParser.getArguments();
            int size = clOptions.size();

            // Parse the options and configure the emitter as appropriate.
            for ( int i = 0; i < size; i++ )
            {
                parseOption( (CLOption) clOptions.get( i ) );
            }

            // validate argument combinations

            validateOptions();

            parser.run( wsdlURI );
        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "Error running Axis", e );
        }
    }
}
