package org.codehaus.mojo.make;

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
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Unpack a tar (or similar) target to unpack the distrution artifact.
 * Goal exists only because of plexus bugs while handling tarballs.  If plexus
 * tar UnArchiver gets fixed, maven-dependency-plugin should be used instead.
 * 
 * @goal unpack
 * @phase initialize
 * @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
 */
public class UnpackExecMojo
    extends AbstractMakeExecMojo
{

    /**
     * The command we should invoke to do the unpack
     * 
     * @parameter default-value="tar"
     * @since 1.0-beta-1
     */
    private String unpackCommand;

    /**
     * Command-line options for use in the unpack command invocation. 
     * If no values are passed in and the plugin recognizes the archive type,
     * it will put in the normal options for tar.
     * 
     * @parameter
     * @since 1.0-beta-1
     */
    private List < String > unpackOptions;

    /**
     * The Make test target to execute.
     * 
     * @parameter
     * @since 1.0-beta-1
     */
    private String unpackTarget = "check";

    /**
     * This is the target directory in which the archive should be unpacked.
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${DYNAMIC.CBUILDPROP.RPM.VERSION}.tar.gz"
     * @readonly
     * @since 1.0-beta-1
     */
    private File unpackArchive;

    /**
     * This is the target directory in which the archive should be unpacked.
     * 
     * @parameter default-value="${project.build.directory}"
     * @readonly
     * @since 1.0-beta-1
     */
    private File unpackDirectory;

    /**
     * Whether we should skip unpacking this project. This is merely a short-circuit mechanism, 
     * in case this mojo will be included in a standard lifecycle mapping.
     * 
     * @parameter default-value="false"
     * @since 1.0-beta-1
     */
    private boolean skipUnpack;

    /**
     * Unpack an archive file.  The archive file must be on the local system. The plugin
     * will typically be needed for tarballs as the Java implementation in Plexus is 
     * very buggy.  This plugin will typically do the right thing for tgz, tar.gz,
     * and tar.bz2 files.  Might as well use maven-dependency-plugin's unpack for ZIP
     * files as that works well.  Use maven-dependency-plugin's "copy" and this unpack
     * goal for tarballs
     * 
     * @throws MojoExecutionException Failure to unpack will throw this exception
     */
    public void execute() throws MojoExecutionException
    {
        if ( unpackOptions == null )
        {
            unpackOptions = new ArrayList < String > ();
            if ( unpackArchive.getName().endsWith( "tar.gz" )
              || unpackArchive.getName().endsWith( "tgz" ) )
            {
                unpackOptions.add( "-xzf" );
            }
            else if ( unpackArchive.getName().endsWith( "tar.bz2" ) )
            {
                unpackOptions.add( "-xjf" );
            }
            else if ( unpackArchive.getName().endsWith( "tar" ) )
            {
                unpackOptions.add( "-xf" );
            }
            else
            {
                throw new MojoExecutionException( "unpackOptions not defined." );
            }
        }
        setCommand( unpackCommand );
        setOptions( unpackOptions );
        setTarget( unpackArchive.getName() );
        setSkipped( skipUnpack );
        setWorkDir( unpackDirectory );

        super.execute();
    }
}
