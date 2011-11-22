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
import java.io.IOException;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;
import org.apache.maven.artifact.Artifact;

/**
 * Runs the current project as a dynamic web application using an embedded Tomcat server.
 * 
 * @goal run
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 * @author Jurgen Lust
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class RunMojo
    extends AbstractRunMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The classes directory for the web application being run.
     * 
     * @parameter expression = "${project.build.outputDirectory}"
     */
    private File classesDir;

    /**
     * The set of dependencies for the web application being run.
     * 
     * @parameter default-value = "${project.artifacts}"
     * @required
     * @readonly
     */
    private Set<Artifact> dependencies;

    /**
     * The web resources directory for the web application being run.
     * 
     * @parameter expression="${basedir}/src/main/webapp"
     */
    private File warSourceDirectory;

    /**
     * The path of the Tomcat context XML file.
     * 
     * @parameter expression = "src/main/webapp/META-INF/context.xml"
     */
    private File contextFile;

    // ----------------------------------------------------------------------
    // AbstractRunMojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected Context createContext( Embedded container )
        throws IOException
    {
        Context context = super.createContext( container );

        context.setReloadable( true );

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebappLoader createWebappLoader()
        throws IOException
    {
        WebappLoader loader = super.createWebappLoader();

        // add classes directory to loader
        if ( classesDir != null )
        {
            loader.addRepository( classesDir.toURL().toString() );
        }

        // add artifacts to loader
        if ( dependencies != null )
        {
            for ( Artifact artifact : dependencies )
            {
                String scope = artifact.getScope();

                // skip provided and test scoped artifacts
                if ( !Artifact.SCOPE_PROVIDED.equals( scope ) && !Artifact.SCOPE_TEST.equals( scope ) )
                {
                    getLog().debug( artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()
                                       + ":" + artifact.getScope() );

                    loader.addRepository( artifact.getFile().toURL().toString() );
                }
            }
        }

        return loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getDocBase()
    {
        return warSourceDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File getContextFile()
    {
        return contextFile;
    }
}
