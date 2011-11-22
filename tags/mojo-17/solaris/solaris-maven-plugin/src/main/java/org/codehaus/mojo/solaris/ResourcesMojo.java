package org.codehaus.mojo.solaris;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Copies System V-specific resources.
 *
 * This goal will copy the following files from <code>src/main/solaris</code>:
 * <ul>
 *  <li>
 *   <code>pkginfo</code> - The pkginfo file will be read and these values will be interpolated:
 *   <ul>
 *    <li>${project.artifactId}</li>
 *    <li>${project.version}</li>
 *    <li>${project.name}</li>
 *    <li>${project.description}</li>
 *   </ul>
 *  </li>
 *  <li>
 *   <code>prototype</code> - The prototype file will be copied to the directory specified by <code>packageRoot</code>.
 *  </li>
 * </ul>
 *
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal resources
 */
public class ResourcesMojo
    extends AbstractSolarisMojo
{
    /**
     * The <code>pkginfo</code> file to interpolate and copy.
     *
     * @parameter expression="src/main/solaris/pkginfo"
     * @required
     */
    private File pkginfo;

    /**
     * The <code>prototype</code> file to copy.
     *
     * @parameter expression="src/main/solaris/prototype"
     * @required
     */
    private File prototype;

    /**
     * There directory where <code>pkgmk</code> and <code>pkgtrans</code> will be executed. All files that are to be
     * a part of the package has to be in this directory before the prototype file is generated.
     *
     * @parameter expression="${project.build.directory}/solaris/assembled-pkg"
     * @required
     */
    private File packageRoot;

    /**
     * Additional properties to be used when interpolating the <code>pkginfo</code> file.
     *
     * @parameter
     */
    private Properties properties;

    /**
     * The artifact id of the project. Will be used when interpolating the <code>pkginfo</code> file.
     *
     * @parameter expression="${project.artifactId}"
     * @read-only
     */
    private String artifactId;

    /**
     * The version of the project. Will be used when interpolating the <code>pkginfo</code> file.
     *
     * @parameter expression="${project.version}"
     * @read-only
     */
    private String version;

    /**
     * The name of the project. Will be used when interpolating the <code>pkginfo</code> file.
     *
     * @parameter expression="${project.name}"
     * @read-only
     */
    private String name;

    /**
     * The description of the project. Will be used when interpolating the <code>pkginfo</code> file.
     *
     * @parameter expression="${project.description}"
     * @read-only
     */
    private String description;

    // -----------------------------------------------------------------------
    // Mojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        mkdirs( packageRoot );

        copyPkginfo();
        copyPrototype();
    }

    private void copyPkginfo()
        throws MojoFailureException, MojoExecutionException
    {
        Properties properties = new Properties();

        properties.put( "project.artifactId", artifactId );
        properties.put( "project.version", version );
        properties.put( "project.name", StringUtils.clean( name ) );
        properties.put( "project.description", StringUtils.clean( description ) );

        if ( this.properties != null )
        {
            properties.putAll( this.properties );
        }

        // -----------------------------------------------------------------------
        // Validate
        // -----------------------------------------------------------------------

        if ( !pkginfo.canRead() )
        {
            throw new MojoFailureException( "Can't read template pkginfo file: '" + pkginfo.getAbsolutePath() + "'." );
        }

        File processedPkginfo = new File( packageRoot, "pkginfo" );

        // -----------------------------------------------------------------------
        // Do it!
        // -----------------------------------------------------------------------

        FileWriter pkginfoWriter = null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader( pkginfo );
            InterpolationFilterReader reader = new InterpolationFilterReader( fileReader, properties );

            pkginfoWriter = new FileWriter( processedPkginfo );
            IOUtil.copy( reader, pkginfoWriter );
        }
        catch ( IOException e )
        {
            // All common causes to this should have been removed with previous checks.
            throw new MojoExecutionException( "Error while interpolating pkginfo.", e );
        }
        finally
        {
            IOUtil.close( pkginfoWriter );
            IOUtil.close( fileReader );
        }
    }

    private void copyPrototype()
        throws MojoFailureException, MojoExecutionException
    {
        if ( !prototype.canRead() )
        {
            throw new MojoFailureException( "Can't read prototype file: '" + prototype.getAbsolutePath() + "'." );
        }

        try
        {
            FileUtils.copyFileToDirectory( prototype, packageRoot );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not copy prototype file to " +
                "'" + packageRoot.getAbsolutePath() + "'.", e );
        }
    }
}
