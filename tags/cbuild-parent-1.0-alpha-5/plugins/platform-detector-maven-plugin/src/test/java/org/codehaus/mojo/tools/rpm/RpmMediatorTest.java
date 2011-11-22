package org.codehaus.mojo.tools.rpm;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;

public class RpmMediatorTest
    extends PlexusTestCase
{
    
    public void testRpmQueryIsSilent() throws Exception
    {
        RpmMediator mediator = (RpmMediator) lookup( RpmMediator.ROLE, "default" );
        
        MavenProject project = new MavenProject( new Model() );
        project.setFile( new File( ".", "pom.xml" ) );
        
        mediator.isRpmInstalled( "test" );
    }

}
