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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.interpolation.RegexBasedInterpolator;

public class Interpolator
{
    
    private final List valueSources;

    public Interpolator( List valueSources )
    {
        this.valueSources = valueSources;
    }

    public void interpolate( File input, File output, String prefix ) throws IOException
    {
        FileReader reader = null;
        FileWriter writer = null;
        
        try
        {
            reader = new FileReader( input );
            writer = new FileWriter( output );
            
            interpolate( reader, writer, prefix );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( writer );
        }        
    }
    
    public void interpolate( Reader input, Writer output, String prefix ) throws IOException
    {
        RegexBasedInterpolator interpolator = new RegexBasedInterpolator( valueSources );
        
        BufferedReader reader = new BufferedReader( input );
        BufferedWriter writer = new BufferedWriter( output );
        
        String line = null;
        
        try
        {
            while ( ( line = reader.readLine() ) != null )
            {
                line = interpolator.interpolate( line, prefix );

                writer.write( line );
                writer.newLine();
            }
        }
        finally
        {
            writer.flush();
        }        
    }

}
