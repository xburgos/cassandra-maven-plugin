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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DebianPluginTest
    extends PlexusTestCase
{
    public void testTest1()
        throws Exception
    {
        String projectName = "project-1";

        String artifactName = getTestFile( "src/test/resources/" + projectName + "/target/" + projectName + "-1.0.jar" );

        FileUtils.fileWrite( artifactName, "foo" );

        testControlFile( projectName );
    }

    private void testControlFile( String projectName )
        throws Exception
    {
        MavenProjectBuilder builder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        File basedir = new File( System.getProperty( "basedir" ), "src/test/resources/" + projectName );

        DebPlugin plugin = new DebPlugin();

        PluginExecutionRequest request = new PluginExecutionRequest( new HashMap() );

        PluginExecutionResponse response = new PluginExecutionResponse();

        MavenProject project = builder.build( new File( basedir.getAbsolutePath(), "project.xml" ) );

        Map params = new HashMap();

        params.put( "project", project );

        request.setParameters( params );

        plugin.execute( request, response );

        assertEquals( new File( basedir, "control" ), new File( basedir, "target/debian/DEBIAN/control" ) );

        release( builder );
    }

    private void assertEquals( File expectedFile, File actualFile )
        throws IOException
    {
        List expectedLines = getLines( expectedFile );

        List actualLines = getLines( actualFile );

        for ( int i = 0; i < expectedLines.size(); i++ )
        {
            String expected = expectedLines.get( i ).toString();

            if ( actualLines.size() - 1 < i )
                fail( "Too few lines in the actual file. Was " + actualLines.size() + ", expected: " + expectedLines.size() );

            String actual = actualLines.get( i ).toString();

            assertEquals( "Checking line #" + (i + 1), expected, actual );
        }

        assertEquals( "Unequal number of lines.", expectedLines.size(), actualLines.size() );
    }

    private List getLines( File file )
        throws IOException
    {
        List lines = new ArrayList();

        BufferedReader reader = new BufferedReader( new FileReader( file ) );

        String line;

        while ( (line = reader.readLine()) != null )
        {
            lines.add( line );
        }

        return lines;
    }
}
