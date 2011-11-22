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

import org.apache.catalina.Context;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;

/**
 * Runs the current project as a packaged web application using an embedded Tomcat server.
 * 
 * @goal run-war
 * @execute phase = "package"
 * @requiresDependencyResolution runtime
 * 
 * @todo depend on war:exploded when MNG-1649 resolved
 * 
 * @author Mark Hobson <markhobson@gmail.com>
 * @version $Id$
 */
public class RunWarMojo extends AbstractRunMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The path of the exploded WAR directory to run.
     * 
     * @parameter expression = "${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File warDirectory;

    // ----------------------------------------------------------------------
    // AbstractRunMojo Implementation
    // ----------------------------------------------------------------------

    /*
     * @see org.codehaus.mojo.tomcat.AbstractRunMojo#createContext(org.apache.catalina.startup.Embedded)
     */
    protected Context createContext( Embedded container ) throws MalformedURLException
    {
        // create context
        Context context = container.createContext( getPath(), warDirectory.getAbsolutePath() );
        context.setLoader( new WebappLoader() );

        return context;
    }
}
