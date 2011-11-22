package org.codehaus.mojo.tools.rpm;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.antcall.MojoLogAdapter;
import org.codehaus.mojo.tools.rpm.RpmMediator;
import org.codehaus.mojo.tools.rpm.RpmQueryException;
import org.codehaus.plexus.PlexusTestCase;

public class RpmMediatorTest
    extends PlexusTestCase
{
    
    public void testRpmQueryIsSilent() throws RpmQueryException
    {
        DefaultLog log = new DefaultLog( getContainer().getLogger() );
        
        MojoLogAdapter logAdapter = new MojoLogAdapter( log );
        
        RpmMediator mediator = new RpmMediator( true, logAdapter );
        
        MavenProject project = new MavenProject( new Model() );
        project.setFile( new File( ".", "pom.xml" ) );
        
        mediator.isRpmInstalled( "test" );
    }

}
