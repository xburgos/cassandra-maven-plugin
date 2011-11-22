package org.apache.maven.plugin.source;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;
import org.apache.maven.plugin.AbstractPlugin;

import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * @goal jar
 *
 * @description This plugin bundles all the generated sources into a jar archive.
 *
 * @parameter
 *  name="outputDirectory"
 *  type=""
 *  required=""
 *  validator=""
 *  expression="#project.build.directory"
 *  description=""
 *
 * @parameter
 *  name="artifactId"
 *  type=""
 *  required=""
 *  validator=""
 *  expression="#project.artifactId"
 *  description=""
 *
 * @parameter
 *  name="version"
 *  type=""
 *  required=""
 *  validator=""
 *  expression="#project.version"
 *  description=""
 *
 * @parameter
 *  name="mainSourceDirectory"
 *  type=""
 *  required=""
 *  validator=""
 *  expression="#project.build.sourceDirectory"
 *  description=""
 *
 * @parameter
 *  name="archiver"
 *  type=""
 *  required=""
 *  validator=""
 *  expression="#component.org.codehaus.plexus.archiver.Archiverjar"
 *  description=""
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JarSourceMojo
    extends AbstractPlugin
{
    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {
        String outputDirectory = (String) request.getParameter( "outputDirectory" );

        String artifactId = (String) request.getParameter( "artifactId" );

        String version = (String) request.getParameter( "version" );

        String mainSourceDirectory = (String) request.getParameter( "mainSourceDirectory" );

//        Archiver archiver = (Archiver) request.getParameter( "archiver" );
        JarArchiver archiver = new JarArchiver();

        archiver.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "" ) );

        SourceBundler sourceBundler = new SourceBundler();

        File[] sourceDirectories =
            {
                new File( mainSourceDirectory )
            };

        File outputFile = new File( outputDirectory, artifactId + "-" + version + "-sources.jar" );

        sourceBundler.makeSourceBundle( outputFile, sourceDirectories, archiver );
    }
 }
