package org.codehaus.mojo.tomcat;

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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Deploy a WAR in-place to Tomcat.
 * 
 * @goal inplace
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 * @todo depend on war:inplace when MNG-1649 resolved
 */
public class InplaceMojo
    extends AbstractDeployMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The path of the inplace WAR directory to deploy.
     * 
     * @parameter expression = "${basedir}/src/main/webapp"
     * @required
     */
    private File warSourceDirectory;

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getWarFile()
    {
        return warSourceDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateWarFile()
        throws MojoExecutionException
    {
        if ( !warSourceDirectory.exists() || !warSourceDirectory.isDirectory() )
        {
            throw new MojoExecutionException( getMessage( "InplaceMojo.missingWar", warSourceDirectory.getPath() ) );
        }
    }
}
