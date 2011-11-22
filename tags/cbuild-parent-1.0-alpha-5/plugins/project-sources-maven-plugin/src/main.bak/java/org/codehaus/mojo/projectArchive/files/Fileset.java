package org.codehaus.mojo.projectArchive.files;

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

import java.util.Iterator;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.util.interpolation.MapBasedValueSource;
import org.codehaus.plexus.util.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.util.interpolation.RegexBasedInterpolator;

public class Fileset extends FileSet
{
    private static final long serialVersionUID = 1L;
    
    public static final String ROOT_REFERENCE_PATTERN = "project";
    
    public Fileset getInterpolatedCopy( MavenProject project )
    {
        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        
        interpolator.addValueSource( new ObjectBasedValueSource( project ) );
        interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
        
        Fileset copy = new Fileset();
        
        copy.setDirectory( interpolator.interpolate( getDirectory(), ROOT_REFERENCE_PATTERN ) );
        
        List includes = getIncludes();
        for ( Iterator it = includes.iterator(); it.hasNext(); )
        {
            String include = (String) it.next();
            
            copy.addInclude( interpolator.interpolate( include, ROOT_REFERENCE_PATTERN ) );
        }
        
        List excludes = getExcludes();
        for ( Iterator it = excludes.iterator(); it.hasNext(); )
        {
            String exclude = (String) it.next();
            
            copy.addExclude( interpolator.interpolate( exclude, ROOT_REFERENCE_PATTERN ) );
        }
        
        String outputDirectory = getOutputDirectory();
        if ( outputDirectory != null )
        {
            copy.setOutputDirectory( interpolator.interpolate( outputDirectory, ROOT_REFERENCE_PATTERN ) );
        }
        
        return copy;
    }

}
