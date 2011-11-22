package org.apache.maven.plugin.deb;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.plugin.AbstractPlugin;
import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.FileUtils;

/**
 * @plugin.id deb
 * @plugin.description A Maven2 plugin which creates debian packages from a Maven2 project
 * @plugin.instantiation singleton
 * @plugin.mode integrated
 *
 * @goal.name deb:create-from-pom
 * @goal.deb:create-from-pom.parameter project #project
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DebPlugin
    extends AbstractPlugin
{
    Dpkg dpkg = new Dpkg();

    public DebPlugin()
    {
    }

    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {
        MavenProject project = (MavenProject) request.getParameter( "project" );

        File basedir = project.getFile().getParentFile();

        File buildDir = new File( basedir, "target" );

        File debian = new File( basedir, "target/debian" );

        if ( !project.getModel().getType().equals( "jar" ) )
            throw new Exception( "This plugin can only package jar files." );

        if ( project.getBuild().getNagEmailAddress() == null )
            throw new Exception( "Missing required elemen from the POM: nagEmailAddress." );

        if ( project.getGroupId() == null )
            throw new Exception( "Missing required elemen from the POM: groupId." );

        if ( project.getArtifactId() == null )
            throw new Exception( "Missing required elemen from the POM: artifactId." );

        if ( project.getVersion() == null )
            throw new Exception( "Missing required elemen from the POM: version." );

        // Populate the control file

        ControlFile control = new ControlFile( project );

        initFileSystem( debian );

        // Make the control file
        writeControlFile( debian, control );

        // Copy the resources
        copyFiles( basedir, debian, null, project.getGroupId(), project.getArtifactId(), project.getVersion() );

        // build the package
        dpkg.buildPackage( buildDir, project.getGroupId() + "-" + project.getArtifactId() + "-" + project.getVersion() );
    }

    private void writeControlFile( File debian, ControlFile control )
        throws Exception
    {
        File basedir = new File( debian, "DEBIAN" );

        basedir.mkdirs();

        PrintWriter output = new PrintWriter( new FileWriter( new File( basedir, "control" ) ) );

//        output.println( "Source: " + control.getSource() );
        output.println( "Section: " + control.getSection() );
        output.println( "Priority: " + control.getPriority() );
        output.println( "Maintainer: " + control.getMaintainer() );
//        output.println( "Standards-Version: " + control.getStandardsVersion() );
//        output.println( "Build-Depends: " + control.getBuildDepends() );
//        output.println( "" );
        output.println( "Package: " + control.getPackageName() );
        output.println( "Version: " + control.getVersion() );
        output.println( "Architecture: " + control.getArchitecture() );
        output.println( "Depends: " + control.getDepends() );
        output.println( "Description: " + control.getDescription() );

        output.close();
    }

    private void initFileSystem( File debian )
        throws IOException
    {
        debug("Creating filesystem in " + debian );

        FileUtils.deleteDirectory( debian );
        FileUtils.forceMkdir( new File( debian, "DEBIAN") );
        FileUtils.forceMkdir( new File( debian, "usr/local/bin") );
        FileUtils.forceMkdir( new File( debian, "usr/local/jars") );
    }

    private void copyFiles( File basedir, File debian, String resourcesDir, String groupId, String artifactId, String version )
        throws Exception
    {
        String jarName = artifactId + "-" + version + ".jar";

        debug("Copying the package control files.");

        File jar = new File( basedir, "target/" + jarName );

        if( jar.exists() )
            FileUtils.copyFileToDirectory( jar, new File( debian, "usr/lib/jars/" + groupId ) );
        else
            throw new Exception( "Could not find the jar file: " + jar );

        chmod( "755", debian );
    }

    private void chmod( String mode, File file )
        throws Exception
    {
        new SystemCommand()
            .setCommand("chmod")
            .addArgument(mode)
            .addArgument(file.getPath())
            .execute();
    }

    private void debug( String msg )
    {
        System.out.println( msg );
    }

    private void fatal( String msg )
    {
        System.err.println( msg );
    }
}
