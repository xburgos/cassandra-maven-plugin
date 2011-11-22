package org.codehaus.mojo.tools.rpm;

/*
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
 *
 */

import junit.framework.TestCase;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;
import org.easymock.MockControl;

public class RpmInfoFormatterTest
    extends TestCase
{

    public void testFormatProjectVersionAndRelease() throws RpmFormattingException
    {
        Model model = new Model();
        model.setVersion( "1.0" );
        
        MavenProject project = new MavenProject( model );
        
        String release = "3";
        
        MockControl ctl = MockControl.createControl( PlatformDetective.class );
        
        PlatformDetective detective = (PlatformDetective) ctl.getMock();
        
        try
        {
            detective.getOperatingSystemToken();
            ctl.setReturnValue( "rhel3", MockControl.ZERO_OR_MORE );
        }
        catch ( PlatformDetectionException e )
        {
            fail( "should never happen" );
        }
        
        ctl.replay();
        
        RpmInfoFormatter formatter = new RpmInfoFormatter( detective );
        
        String[] formatted = formatter.formatProjectVersionAndRelease( project, release, null, false );
        
        System.out.println( "Release should be: " + release + "." + formatter.formatRPMPlatformName() );
        System.out.println( "Release is: " + formatted[1] );
        
        assertEquals( model.getVersion(), formatted[0] );
        assertEquals( release + "." + formatter.formatRPMPlatformName(), formatted[1] );
        
        ctl.verify();
    }
}
