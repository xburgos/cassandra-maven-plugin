package org.codehaus.mojo.tools.fs.archive.tar;

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

import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.cli.CommandLineException;
//import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;

public class TarUnArchiver
    implements UnArchiver, LogEnabled
{
    
    private CommandLineManager cliManager;

    private File destDirectory;
    
    private boolean overwrite = true;
    
    private File sourceFile;
    
    private Logger logger;

    public void extract()
        throws ArchiverException
    {
        getLogger().info( "Expanding: " + getSourceFile() + " into " + getDestDirectory() );
        Commandline cli = new Commandline();
        cli.setExecutable( "tar" );
        cli.createArgument().setLine( "-x" );
        
        String extension = ArchiveFileExtensions.getArchiveFileExtension( getSourceFile() );
        
        if ( ArchiveFileExtensions.BZIP_EXTENSIONS.contains( extension ) )
        {
            cli.createArgument().setLine( "-j" );
        }
        else if ( ArchiveFileExtensions.GZIP_EXTENSIONS.contains( extension ) )
        {
            cli.createArgument().setLine( "-z" );
        }
        
        if ( !overwrite )
        {
            cli.createArgument().setLine( "-k" );
        }
        
        cli.createArgument().setLine( "-f " + sourceFile.getAbsolutePath() );
        cli.createArgument().setLine( "-C " + destDirectory.getAbsolutePath() );
        
        StreamConsumer consumer = cliManager.newDebugStreamConsumer();
        
        try
        {
            cliManager.execute( cli, consumer, consumer );
        }
        catch ( CommandLineException e )
        {
            throw new ArchiverException( "Failed to extract: " + sourceFile + " into: " + destDirectory, e );
        }
    }

    public void extract( String path, File outputDirectory )
        throws ArchiverException
    {
        throw new UnsupportedOperationException( "Extraction of a single file is not supported by this UnArchiver." );
    }

    public File getDestDirectory()
    {
        return destDirectory;
    }

    public File getDestFile()
    {
        return null;
    }

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setDestDirectory( File destDirectory )
    {
        this.destDirectory = destDirectory;
    }

    public void setDestFile( File destFile )
    {
        throw new UnsupportedOperationException( "Destination files are not supported by this UnArchiver." );
    }

    public void setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
    }

    public void setSourceFile( File sourceFile )
    {
        this.sourceFile = sourceFile;
    }
    
    public void setFileSelectors( FileSelector[] selectors )
    {
        throw new UnsupportedOperationException( "File mining not yet supported by this UnArchiver." );
    }

    public FileSelector[] getFileSelectors( )
    {
        return null;
    }

    private Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "TarUnArchiver(local)::internal" );
        }
        
        return logger;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

}
