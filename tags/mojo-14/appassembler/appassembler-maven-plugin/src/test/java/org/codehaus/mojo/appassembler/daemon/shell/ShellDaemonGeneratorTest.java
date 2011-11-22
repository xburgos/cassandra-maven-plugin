package org.codehaus.mojo.appassembler.daemon.shell;

import org.codehaus.mojo.appassembler.daemon.AbstractDaemonGeneratorTest;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @version $Id$
 */
public class ShellDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest 
{
    public void testGenerateWindowsShellDaemon()
        throws Exception
    {
        runTest( "booter-windows", "src/test/resources/project-1/pom.xml", "src/test/resources/project-1/descriptor.xml", "target/output-1-bat" );

        File wrapper = new File( getTestFile( "target/output-1-bat" ), "bin/app.bat" );

        assertTrue( "Windows batch file is missing: " + wrapper.getAbsolutePath(), wrapper.isFile());

        assertTrue( "Generated batch file does not match template",FileUtils.contentEquals( getTestFile( "src/test/resources/org/codehaus/mojo/appassembler/daemon/booter-windows/app.bat" ), wrapper ));
        
        File manifest = new File( getTestFile( "target/output-1-bat" ), "etc/app.xml" );
        
        assertTrue( "Manifest file is mising: " + manifest.getAbsolutePath(), manifest.isFile());
    }
}
