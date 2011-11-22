package org.apache.maven.plugin.deb;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @description A Maven 2 mojo which creates a Debian package from a Maven2 project.
 *
 * @goal package-binary
 * @phase package
 * @requiresDependencyResolution runtime
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PackageBinaryMojo
    extends AbstractDebMojo
{
    /**
     * @parameter expression="${project.build.directory}/debian"
     * @optional
     */
    private File assemblyDirectory;

    /**
     * @parameter expression="${project.build.directory}"
     * @optional
     * @readonly
     */
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Dpkg dpkg = new Dpkg();

        try
        {
            getControlFileGenerator().generateControl( assemblyDirectory );

            File debFileName = new File( outputDirectory, getControlFileGenerator().getDebFileName() );

            dpkg.buildPackage( outputDirectory, assemblyDirectory, debFileName, useFakeroot );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error while calling dpkg.", e );
        }
    }
}
