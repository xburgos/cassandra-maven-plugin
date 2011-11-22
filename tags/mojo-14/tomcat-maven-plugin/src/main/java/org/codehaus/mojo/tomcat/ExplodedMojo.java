package org.codehaus.mojo.tomcat;

/*
 * Copyright 2005 Mark Hobson.
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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Deploy an exploded WAR to Tomcat.
 *
 * @goal exploded
 *
 * @todo depend on war:exploded when MNG-1649 resolved
 *
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class ExplodedMojo
    extends AbstractDeployMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The path of the exploded WAR directory to deploy.
     *
     * @parameter expression = "${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File warDirectory;

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /*
     * @see org.codehaus.mojo.tomcat.AbstractDeployMojo#getWarFile()
     */
    protected File getWarFile()
    {
        return warDirectory;
    }

    /*
     * @see org.codehaus.mojo.tomcat.AbstractDeployMojo#validateWarFile()
     */
    protected void validateWarFile()
        throws MojoExecutionException
    {
        if ( !warDirectory.exists() || !warDirectory.isDirectory() )
        {
            throw new MojoExecutionException( getMessage( "ExplodedMojo.missingWar", warDirectory.getPath() ) );
        }
    }
}
