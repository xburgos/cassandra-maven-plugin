package org.codehaus.mojo.settings;

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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.interpolation.MapBasedValueSource;

import junit.framework.TestCase;

public class InterpolatorTest
    extends TestCase
{

//    public void testInterpolationWithPrompter() throws IOException
//    {
//        String toInterpolate = "this is a test. User is: ${prompt.What is your name?}";
//        
//        StringReader reader = new StringReader( toInterpolate );
//        StringWriter writer = new StringWriter();
//        
//        Interpolator interp = new Interpolator( Collections.singletonList( new PrompterValueSource() ) );
//        interp.interpolate( reader, writer, "prompt" );
//        
//        System.out.println( "Interpolation result:\n\n\'" + writer.toString() + "\'" );
//        
//    }
    
    public void testInterpolationWithPrompterSyspropsSplitterWithSysprop() throws IOException
    {
        testInterpolationWithPrompterSyspropsSplitter( "name", "John" );
    }

//    public void testInterpolationWithPrompterSyspropsSplitterWithoutSysprop() throws IOException
//    {
//        testInterpolationWithPrompterSyspropsSplitter( null, null );
//    }
    
    private void testInterpolationWithPrompterSyspropsSplitter( String key, String value ) throws IOException
    {
        Properties origSysprops = System.getProperties();
        Properties newSysprops = new Properties( origSysprops );
        
        try
        {
            if ( key != null && value != null )
            {
                newSysprops.setProperty( key, value );
            }
            
            System.setProperties( newSysprops );
            
            String toInterpolate = "this is a test. User is: ${prompt.name:What is your name?}";
            
            PrompterValueSource pvs = new PrompterValueSource();
            MapBasedValueSource mbvs = new MapBasedValueSource( System.getProperties() );
            
            List segments = new ArrayList();
            segments.add( Collections.singletonList( mbvs ) );
            segments.add( Collections.singletonList( pvs ) );
            
            SplitterValueSource svs = new SplitterValueSource( ":", segments );
            
            StringReader reader = new StringReader( toInterpolate );
            StringWriter writer = new StringWriter();
            
            Interpolator interp = new Interpolator( Collections.singletonList( svs ) );
            
            interp.interpolate( reader, writer, "prompt" );
            
            System.out.println( "Interpolation result:\n\n\'" + writer.toString() + "\'" );
        }
        finally
        {
            System.setProperties( origSysprops );
        }        
    }
    
}
