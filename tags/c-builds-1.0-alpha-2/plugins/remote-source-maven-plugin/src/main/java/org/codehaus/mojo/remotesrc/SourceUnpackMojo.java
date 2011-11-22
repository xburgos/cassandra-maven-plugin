package org.codehaus.mojo.remotesrc;

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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.taskdefs.BUnzip2;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.GUnzip;
import org.codehaus.mojo.tools.antcall.AntCaller;
import org.codehaus.mojo.tools.antcall.AntExecutionException;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;

/**
 * Retrieve project sources from some external URL, and unpack them (into target, by default).
 * Dependency resolution is required here to keep from downloading a potentially huge source tarball
 * if the dependencies cannot be satisfied.
 * 
 * @goal unpack
 * @requiresDependencyResolution compile
 */
public class SourceUnpackMojo
    extends AbstractRemoteSourceMojo
{

    public static final String TGZ_TYPE = "tar.gz";

    public static final String TGZ_TYPE2 = "tgz";
    
    public static final String TBZ_TYPE = "tar.Z";
    
    public static final String TBZ_TYPE2 = "tar.bz2";

    public static final String[] UNPACK_TYPES = { TGZ_TYPE, TGZ_TYPE2, TBZ_TYPE, TBZ_TYPE2, "tar" };
    
    /**
     * The source archive to unpack.
     * 
     * @parameter
     */
    private File sourceArchive;
    
    /**
     * Whether to skip the unpack step.
     * 
     * @parameter default-value="false"
     */
    private boolean skipUnpack;
    
    /**
     * The target directory into which the project source archive should be expanded.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File expandTarget;
    
    /**
     * The Ant messageLevel to use.
     * 
     * @parameter expression="${messageLevel}"
     */
    private String messageLevel;
    
    /**
     * Whether to force this to unpack, regardless of whether there is already a directory in place.
     * @parameter default-value=false expression="${source.unpack.overwrite}"
     */
    private boolean overwrite;
    
    /**
     * The resulting unpacked directory to check when !overwrite.
     * 
     * @parameter default-value="${workDir}"
     * @required
     */
    private File workDir;

    /**
     * Perform the source archive retrieval, and unpack it.
     * 
     * This involves:
     * 
     * o 
     * o Setting up the transfer monitor (displays progress for the download)
     * o Download the archive to a temp file
     * o 
     */
    protected void doExecute()
        throws MojoExecutionException
    {
        // If we're set to NOT overwrite, we need to determine whether the unpack 
        // code should run.
        if ( !overwrite && workDir.exists() )
        {
            getLog().info( "Skipping unpack step because working directory already exists. Set '-Dsource.unpack.overwrite=true to override." );
            return;
        }
        
        // Setup the Ant manager so we can unpack the source archive.

        AntCaller antCaller = new AntCaller( new MojoLogAdapter( getLog() ) );
        
        if ( messageLevel != null )
        {
            antCaller.setMessageLevel( messageLevel );
        }
       
        try
        {
            // make sure the expansion target exists before we try to expand there.
            if ( !expandTarget.exists() )
            {
                expandTarget.mkdirs();
            }
            
            // add the Ant tasks required to unpack this particular archive...depends on the archive extension.
            configureExpandTarget( antCaller );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create temporary unpacked-source file", e );
        }
        
        try
        {
            // perform the unpack.
            antCaller.executeTasks( getProject() );
        }
        catch ( AntExecutionException e )
        {
            Throwable cause = e.getCause();

	    // cwb >>>>
	    getLog().error( "Error unpacking sources", e );
	    if ( cause != null ) 
		getLog().error( "Error unpacking sources", cause );
	    //<<<<

            if ( cause != null && cause.getStackTrace()[0].getClassName().equals( ExecTask.class.getName() ) )
            {
                getLog().debug( "Error unpacking sources", cause );

                throw new MojoExecutionException( "Failed to unpack sources." );
            }
            else
            {
                throw new MojoExecutionException( "Failed to unpack sources.", e );
            }
        }
    }

    /**
     * Determine what type of archive we're dealing with, and configure the Ant executor
     * appropriately to unpack the archive into the target directory.
     * @throws MojoExecutionException 
     */
    private void configureExpandTarget( AntCaller antCaller )
        throws IOException, MojoExecutionException
    {
        // give artifact-resolution results precedence.
        File resolvedSourceArchive = getSourceArchiveFile( getProject().getId() );
        
        if ( resolvedSourceArchive != null )
        {
            sourceArchive = resolvedSourceArchive;
        }
        
        if ( !sourceArchive.exists() )
        {
            throw new MojoExecutionException( "Source archive cannot be unpacked; it does not exist! (archive location: " + sourceArchive + ")" );
        }
        
        getLog().info( "Unpacking: " + sourceArchive.getCanonicalPath() );
        
        File unpacked = sourceArchive;
        String path = sourceArchive.getCanonicalPath();

        boolean isTar = false;

        // Look for archive file types, and determine whether the archive is a tarfile of sorts.
        for ( int i = 0; i < UNPACK_TYPES.length; i++ )
        {
            // if the archive is a tarfile, then we have to setup the unpack task, and possibly
            // preprocess with a decompression task.
            if ( path.endsWith( UNPACK_TYPES[i] ) )
            {
                isTar = true;

                // create the unpacked file target.
                unpacked = File.createTempFile( sourceArchive.getName(), ".unpacked" );
                unpacked.deleteOnExit();
                
                // we have to touch this file, to ensure it's OLDER than the original
                // archive download file
                unpacked.setLastModified( sourceArchive.lastModified() - 10000 );

                // if the archive is gzipped, we have to first gunzip it.
                if ( TGZ_TYPE == UNPACK_TYPES[i] || TGZ_TYPE2 == UNPACK_TYPES[i] )
                {
                    GUnzip gunzip = new GUnzip();
                    gunzip.setTaskName("gunzip");
                    gunzip.setSrc( sourceArchive );
                    gunzip.setDest( unpacked );

                    antCaller.addTask( gunzip );
                }
                // otherwise, if the archive is bzipped, we have to first bunzip it.
                else if ( TBZ_TYPE == UNPACK_TYPES[i] || TBZ_TYPE2 == UNPACK_TYPES[i] )
                {
                    BUnzip2 bunzip = new BUnzip2();
                    bunzip.setTaskName("bunzip");
                    bunzip.setSrc( sourceArchive );
                    bunzip.setDest( unpacked );

                    antCaller.addTask( bunzip );
                }
                // otherwise, simply reassign the unpacked file to the downloaded file.
                else
                {
                    unpacked = sourceArchive;
                }

                // once the file is decompressed (if need be), then we have to untar it.

		//>>>>>>>>>>>>>>>>>>>>>>>
		/** cwb --  Ant's tar task doesn't really work for symbolic links
		   
                Untar untar = new Untar();

                untar.setTaskName("untar");
                
                untar.setSrc( unpacked );
                untar.setDest( expandTarget );
                untar.setOverwrite( true );
                antCaller.addTask( untar );
		**/

		getLog().debug( "unpacked= " + unpacked );
		getLog().debug( "expandTarget= " + expandTarget );

		ExecTask exec = new ExecTask();
		exec.setTaskName( "exec..untar-ing" );

		exec.setExecutable( "tar" );
		exec.setDir( expandTarget );
		exec.setFailonerror( true );
		exec.setResolveExecutable( true );
		exec.createArg().setLine( "xvf " );
        exec.createArg().setLine( unpacked.getPath() );
        exec.createArg().setLine( " --force-local ");
//        exec.createArg().setLine( "--keep-newer-files" );
//        exec.createArg().setLine( "-C " + expandTarget );

		// FIXME
		//File output = new File( "tar.log" );
		//exec.setOutput( output );
		//exec.setLogError( true );
		//getLog().debug( "output= " + output );

                antCaller.addTask( exec );
		//<<<<<<<<<<<<<<<<<<

                break;
            }
        }

        // if we couldn't determine that this is a tar archive, we'll assume it's a zip.
        if ( !isTar )
        {
            Expand expander = new Expand();
            expander.setTaskName("expand");
            expander.setSrc( unpacked );
            expander.setDest( expandTarget );
            expander.setOverwrite( true );

            antCaller.addTask( expander );
        }
    }

    protected boolean isSkip()
    {
        return skipUnpack;
    }

    protected CharSequence getSkipMessage()
    {
        return "Skipping source-archive unpack step (per configuration).";
    }
    
}
