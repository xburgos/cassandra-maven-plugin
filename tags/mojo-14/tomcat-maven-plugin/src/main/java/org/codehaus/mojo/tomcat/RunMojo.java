package org.codehaus.mojo.tomcat;

/*
 * Copyright 2006 Mark Hobson.
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
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

import org.apache.catalina.Context;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;
import org.apache.maven.artifact.Artifact;

/**
 * Runs the current project as a dynamic web application using an embedded Tomcat server.
 * 
 * @goal run
 * @execute phase = "compile"
 * @requiresDependencyResolution runtime
 * 
 * @author Jurgen Lust
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class RunMojo extends AbstractRunMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The classes directory for the web application being run.
     * 
     * @parameter expression = "${project.build.outputDirectory}"
     */
    private String classesDir;

    /**
     * The set of dependencies for the web application being run.
     * 
     * @parameter default-value = "${project.artifacts}"
     * @required
     * @readonly
     */
    private Set dependencies;

    /**
     * The web resources directory for the web application being run.
     * 
     * @parameter expression="${basedir}/src/main/webapp"
     */
    private String warSourceDirectory;

    // ----------------------------------------------------------------------
    // AbstractRunMojo Implementation
    // ----------------------------------------------------------------------

    /*
     * @see org.codehaus.mojo.tomcat.AbstractRunMojo#createContext(org.apache.catalina.startup.Embedded)
     */
    protected Context createContext( Embedded container ) throws MalformedURLException
    {
        // create webapp loader
        WebappLoader loader = new WebappLoader();

        // add classes directory to loader
        if ( classesDir != null )
        {
            loader.addRepository( new File( classesDir ).toURL().toString() );
        }

        // add artifacts to loader
        if ( dependencies != null )
        {
            for ( Iterator iterator = dependencies.iterator(); iterator.hasNext(); )
            {
                Artifact artifact = (Artifact) iterator.next();

                loader.addRepository( artifact.getFile().toURL().toString() );
            }
        }

        // create context
        Context context = container.createContext( getPath(), warSourceDirectory );
        context.setLoader( loader );
        context.setReloadable( true );

        return context;
    }
}
