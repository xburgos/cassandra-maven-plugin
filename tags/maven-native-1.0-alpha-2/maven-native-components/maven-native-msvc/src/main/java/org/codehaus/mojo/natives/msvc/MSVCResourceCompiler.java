package org.codehaus.mojo.natives.msvc;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
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
 */

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.compiler.ResourceCompilerConfiguration;
import org.codehaus.mojo.natives.compiler.AbstractResourceCompiler;
import org.codehaus.mojo.natives.util.EnvUtil;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.Os;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan Tran</a>
 * @version $Id$
 */
public class MSVCResourceCompiler
    extends AbstractResourceCompiler
{

    protected Commandline getCommandLine( ResourceCompilerConfiguration config, File source )
        throws NativeBuildException
    {

        Commandline cl = new Commandline();

        EnvUtil.setupCommandlineEnv( cl, config.getEnvFactoryName() );

        if ( config.getWorkingDirectory() != null )
        {
            cl.setWorkingDirectory( config.getWorkingDirectory().getPath() );
        }

	if ( config.getExecutable() == null || config.getExecutable().trim().length() == 0 )
	{
  	   config.setExecutable ( "rc.exe" );
	}
	cl.setExecutable(config.getExecutable().trim());

        cl.addArguments( config.getOptions() );

        for ( int i = 0; i < config.getIncludePaths().length; ++i )
        {
            String includePath = config.getIncludePaths()[i].getPath();

            cl.createArgument().setValue( "/i" );

            cl.createArgument().setValue( includePath );
        }

        for ( int i = 0; i < config.getSystemIncludePaths().length; ++i )
        {
            String includePath = config.getIncludePaths()[i].getPath();

            cl.createArgument().setValue( "/i" );

            cl.createArgument().setValue( includePath );
        }

        cl.createArgument().setValue( "/fo" );

        cl.createArgument().setValue( config.getOutputFile( source ).getPath() );

        cl.createArgument().setValue( source.getPath() );

        return cl;
    }

}