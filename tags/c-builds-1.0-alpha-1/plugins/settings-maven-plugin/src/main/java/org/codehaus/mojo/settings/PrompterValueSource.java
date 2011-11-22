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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.codehaus.plexus.util.interpolation.ValueSource;

public class PrompterValueSource
    implements ValueSource
{
    
    public Object getValue( String question )
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
        
        System.out.print( "\n" + question + " " );
        
        String response;
        try
        {
            response = reader.readLine();
            
            System.out.println( "\n" );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Cannot prompt user for input. Reason: " + e.getMessage() );
        }
        
        return response;
    }

}
