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

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;
import org.easymock.MockControl;

public class RpmInfoFormatterTest
    extends TestCase
{

    public void testFormatProjectVersionAndRelease()
        throws RpmFormattingException
    {
        Model model = new Model();
        model.setVersion( "1.0-3" );

        MavenProject project = new MavenProject( model );

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

        RpmInfoFormatter formatter = new RpmInfoFormatter( detective, null );

        String[] formatted = formatter.formatProjectVersionAndRelease( project, null, false );

        System.out.println( "Release should be: " + "3." + formatter.formatRPMPlatformName() );
        System.out.println( "Release is: " + formatted[1] );

        assertEquals( "1.0", formatted[0] );
        assertEquals( "3." + formatter.formatRPMPlatformName(), formatted[1] );

        ctl.verify();
    }

    public void testFormatPlatformArchitecture()
        throws RpmFormattingException
    {
        if ( !"i386".equals( System.getProperty( "os.arch" ) ) )
        {
            System.out.println( "Skipping formatPlatformArchitecture test; architecture is not i386." );
            return;
        }

        MockControl ctl = MockControl.createControl( PlatformDetective.class );

        PlatformDetective detective = (PlatformDetective) ctl.getMock();

        try
        {
            detective.getArchitectureToken();
            ctl.setReturnValue( "i386", MockControl.ZERO_OR_MORE );
        }
        catch ( PlatformDetectionException e )
        {
            fail( "should never happen" );
        }

        ctl.replay();

        RpmInfoFormatter formatter = new RpmInfoFormatter( detective, null );

        String formatted = formatter.formatPlatformArchitecture();

        assertEquals( "i386", formatted );

        ctl.verify();
    }

    public void testFormatRpmDependency_RangeWithUnBoundedUpperLimit()
        throws InvalidVersionSpecificationException
    {
        Model model = new Model();
        model.setGroupId( "group" );
        model.setArtifactId( "artifact" );
        model.setVersion( "[1.0,)" );

        MavenProject project = new MavenProject( model );

        String result = new RpmInfoFormatter( null, new VersionRangeFormatter() ).formatRpmDependency( project );
        
        assertEquals( "group_artifact >= 1.0", result );
    }
}
