package org.apache.maven.plugin.eclipse;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.xdoc.render.DefaultXMLWriter;
import org.apache.maven.xdoc.render.XMLWriter;

import org.codehaus.plexus.util.InterpolationFilterReader;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EclipseWriter
{
    public void write( MavenProject project, File basedir )
        throws Exception
    {
        Map map = new HashMap();

        map.put( "project.artifactId", project.getArtifactId() );

        System.out.println( "Writing Eclipse project for " + project.getGroupId() + "-" + project.getArtifactId() );

        writeEclipseProject( basedir, project, map );

        writeEclipseClasspath( basedir, project, map );

        System.out.println( "Wrote Eclipse project files to " + basedir.getAbsolutePath() );
    }

    // ----------------------------------------------------------------------
    // .project
    // ----------------------------------------------------------------------

    protected void writeEclipseProject( File basedir, MavenProject project, Map map )
        throws Exception
    {
        FileWriter w = new FileWriter( new File( basedir, ".project" ) );

        XMLWriter writer = new DefaultXMLWriter( w );

        writer.startElement( "projectDescription" );

        writer.startElement( "name" );

        if ( project.getArtifactId() == null )
            throw new Exception( "Missing element from the POM: artifactId." );

        writer.writeText( project.getArtifactId() );

        writer.endElement();

        // TODO: this entire element might be dropped if the comment is null.
        // but as the maven1 eclipse plugin does it, it's better to be safe than sorry
        // A eclipse developer might want to look at this.
        writer.startElement( "comment" );

        if ( project.getDescription() != null )
        {
            writer.writeText( project.getDescription() );
        }

        writer.endElement();

        // TODO: Add project dependencies here
        // Should look in the reactor for other projects

        writer.startElement( "projects" );

        writer.endElement(); // projects

        writer.startElement( "buildSpec" );

        writer.startElement( "buildCommand" );

        writer.startElement( "name" );

        writer.writeText( "org.eclipse.jdt.core.javabuilder" );

        writer.endElement(); // name

        writer.startElement( "arguments" );

        writer.endElement(); // arguments

        writer.endElement(); // buildCommand

        writer.endElement(); // buildSpec

        writer.startElement( "natures" );

        writer.startElement( "nature" );

        writer.writeText( "org.eclipse.jdt.core.javanature" );

        writer.endElement(); // nature

        writer.endElement(); // natures

        writer.endElement(); // projectDescription

        w.flush();

        w.close();
    }

    // ----------------------------------------------------------------------
    // .classpath
    // ----------------------------------------------------------------------

    protected void writeEclipseClasspath( File basedir, MavenProject project, Map map )
        throws Exception
    {
        FileWriter w = new FileWriter( new File( basedir, ".classpath" ) );

        XMLWriter writer = new DefaultXMLWriter( w );

        writer.startElement( "classpath" );

        // The source roots

        List sourceRoots = project.getCompileSourceRootsList();

        for ( int i = 0; i < sourceRoots.size(); i++ )
        {
            if ( new File( sourceRoots.get(i).toString() ).isDirectory() )
                addSourcePath( writer, basedir, sourceRoots.get(i).toString() );
        }

        // The test source roots

        List testSourceRoots = project.getTestCompileSourceRootsList();

        for ( int i = 0; i < testSourceRoots.size(); i++ )
        {
            // TODO: don't hardcode this string.

            if ( new File( testSourceRoots.get(i).toString() ).isDirectory() )
                addTestSourcePath( writer, basedir, testSourceRoots.get(i).toString(), "target/test-classes" );
        }

        // TODO: don't hardcode this string.
        // The output
        writer.startElement( "classpathentry" );

        writer.addAttribute( "kind", "output" );

        writer.addAttribute( "path", "target/classes" );

        writer.endElement();

        // The JRE reference
        writer.startElement( "classpathentry" );

        writer.addAttribute( "kind", "var" );

        writer.addAttribute( "rootpath", "JRE_SRCROOT" );

        writer.addAttribute( "path", "JRE_LIB" );

        writer.addAttribute( "sourcepath", "JRE_SRC" );

        writer.endElement();

        // The dependencies

        List artifacts = project.getDependencies();

        boolean hasJUnit = false;

        for ( int i = 0; i < artifacts.size(); i++ )
        {
            Dependency dependency = (Dependency)artifacts.get( i );

            if ( clean( dependency.getGroupId() ).equals( "junit" ) &&
                 clean( dependency.getArtifactId() ).equals( "junit" ) &&
                 clean( dependency.getType() ).equals( "jar" ))
                hasJUnit = true;

            addDependency( writer, dependency );
        }

        // Add the special junit dependency
//        Dependency dependency = (Dependency)artifacts.get( i );

        if ( !hasJUnit )
        {
            Dependency dependency = new Dependency();

            dependency.setGroupId( "junit" );

            dependency.setArtifactId( "junit" );

            dependency.setType( "jar" );

            dependency.setVersion( "3.8.1" );

            addDependency( writer, dependency );
        }

        writer.endElement();

        w.flush();

        w.close();
    }

    private void addSourcePath( XMLWriter writer, File basedir, String path )
    {
        writer.startElement( "classpathentry" );
    
        writer.addAttribute( "kind", "src" );

        if ( path.startsWith( basedir.getAbsolutePath() ) )
            path = path.substring( basedir.getAbsolutePath().length() + 1 );

        writer.addAttribute( "path", path );
    
        writer.endElement();
    }

    private void addTestSourcePath( XMLWriter writer, File basedir, String path, String outputPath )
    {
        writer.startElement( "classpathentry" );
    
        writer.addAttribute( "kind", "src" );

        if ( path.startsWith( basedir.getAbsolutePath() ) )
            path = path.substring( basedir.getAbsolutePath().length() + 1 );
    
        writer.addAttribute( "path", path );

        writer.addAttribute( "output", outputPath );

        writer.endElement();
    }

    private void addDependency( XMLWriter writer, Dependency dependency )
        throws Exception
    {
        writer.startElement( "classpathentry" );

        writer.addAttribute( "kind", "var" );

        String groupId = clean( dependency.getGroupId() );

        if ( groupId.length() == 0 )
            throw new Exception( "Missing element from dependency: group id." );

        String artifactId = clean( dependency.getArtifactId() );

        if ( artifactId.length() == 0 )
            throw new Exception( "Missing element from dependency: artifact id." );

        String version = clean( dependency.getVersion() );

        if ( version.length() == 0 )
            throw new Exception( "Missing element from dependency: version." );

        String type = clean( dependency.getType() );

        if ( type.length() == 0 )
            throw new Exception( "Missing element from dependency: type." );

        // TODO: need to check that the artifacts exist?
        // TODO: need to check that the values of the dependency != null (use check())?

        writer.addAttribute( "path", "MAVEN_REPO/" + groupId + "/" + 
                                                     type + "s/" + 
                                                     artifactId + "-" + 
                                                     version + "." + 
                                                     type);

        writer.endElement();
    }

    private void copy( InputStream is, Writer writer, Map map )
        throws Exception
    {
        InterpolationFilterReader reader = new InterpolationFilterReader( new InputStreamReader( is ), map, "@", "@" );

        char[] buffer = new char[1024];

        int n;

        while ( -1 != ( n = reader.read( buffer ) ) )
        {
            writer.write( buffer, 0, n );
        }

        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( Exception e )
            {
            }
        }

        if ( writer != null )
        {

            try
            {
                writer.flush();

                writer.close();
            }
            catch ( Exception e )
            {
            }
        }
    }

    private String clean( String string )
    {
        return (string == null) ? "" : string;
    }
}
