package org.apache.maven.plugin.deb;

import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.project.MavenProject;
import org.apache.maven.monitor.event.EventMonitor;
import org.apache.maven.monitor.event.DefaultEventMonitor;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DebPluginTest
    extends PlexusTestCase
{
    private MavenProject project;

    private MavenEmbedder mvn;

    public void setUp()
        throws Exception
    {
        super.setUp();

        mvn = new MavenEmbedder();

        String pom = getTestPath( "src/test/resources/project-a/pom.xml" );

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        mvn.setClassLoader( classLoader );

        mvn.setLogger( new MavenEmbedderConsoleLogger() );

        mvn.start();

        project = mvn.readProject( new File( pom ) );

        FileUtils.fileWrite( getTestPath( "src/test/resources/project-a/target/project-a-1.0.jar" ), "project-a-1.0.jar" );
    }

    public void testControlFileProject1()
        throws Exception
    {
        EventMonitor eventMonitor = new DefaultEventMonitor( new ConsoleLogger( Logger.LEVEL_INFO, "" ) );

        List arguments = new ArrayList();
        arguments.add( "org.codehaus.mojo:deb-maven-plugin:deb" );

        TransferListener transferListener = new TransferListener()
        {
            public void transferInitiated( TransferEvent transferEvent ) { }
            public void transferStarted( TransferEvent transferEvent ) { }
            public void transferProgress( TransferEvent transferEvent, byte[] bytes, int i ) { }
            public void transferCompleted( TransferEvent transferEvent ) { }
            public void transferError( TransferEvent transferEvent ) { }
            public void debug( String string ) { }
        };

//        mvn.execute( project, arguments, eventMonitor, transferListener, new Properties(), getTestFile( "src/test/resources/project-a/pom.xml" ) );
//
//        File expectedFile = new File( getBasedir() + "/src/test/resources/project-a/control" );
//        File actualFile = new File( getBasedir() + "/src/test/resources/project-a/target/debian/DEBIAN/control" );
//        assertEquals( expectedFile, actualFile );
    }

    // ----------------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------------

    private void assertEquals( File expectedFile, File actualFile )
        throws IOException
    {
        List expectedLines = getLines( expectedFile );

        List actualLines = getLines( actualFile );

        for ( int i = 0; i < expectedLines.size(); i++ )
        {
            String expected = expectedLines.get( i ).toString();

            if ( actualLines.size() - 1 < i )
            {
                fail( "Too few lines in the actual file. Was " + actualLines.size() + ", expected: "
                    + expectedLines.size() );
            }

            String actual = actualLines.get( i ).toString();

            assertEquals( "Checking line #" + ( i + 1 ), expected, actual );
        }

        assertEquals( "Unequal number of lines.", expectedLines.size(), actualLines.size() );
    }

    private List getLines( File file )
        throws IOException
    {
        List lines = new ArrayList();

        assertTrue( "The file doesn't exist: " + file.getAbsolutePath(), file.exists() );

        BufferedReader reader = new BufferedReader( new FileReader( file ) );

        String line;

        while ( ( line = reader.readLine() ) != null )
        {
            lines.add( line );
        }

        return lines;
    }
}
