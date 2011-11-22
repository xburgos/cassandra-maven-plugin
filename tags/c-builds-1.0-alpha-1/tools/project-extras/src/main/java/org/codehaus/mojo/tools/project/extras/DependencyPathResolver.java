package org.codehaus.mojo.tools.project.extras;

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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.StringUtils;


public class DependencyPathResolver
{
    
    private final Collection artifacts;
    private final Log log;
    private Map artifactMap;
    private final ArtifactPathResolver pathResolver;

    public DependencyPathResolver( Collection artifacts, ArtifactPathResolver pathResolver, Log log )
    {
        this.artifacts = artifacts;
        this.pathResolver = pathResolver;
        this.log = log;
    }

    /**
     * Resolve expressions in the command-line options.
     * 
     * Currently, we support two expressions:
     * 
     * @destDir@ - which simply substitutes in the destDir mojo parameter
     * @throws PathResolutionException 
     * @pathOf(groupId:artifactId)@ - which will substitute the systemPath of
     *   the dependency referenced by groupId:artifactId, iff it's system-scoped.
     */
    public String resolveDependencyPaths( String src )
        throws IOException, PathResolutionException
    {
        String result = src;
        
        Pattern depPathPattern = Pattern.compile( "@pathOf\\(([^:]+):([^)]+)\\)@" );

        Matcher matcher = depPathPattern.matcher( result );

        while ( matcher.find() )
        {
            result = addDependencyPath( matcher, result );

            matcher.reset( result );
        }

        return result;
    }

    /**
     * Append the systemPath of the referenced dependency to the buffer we're using
     * to build up the resolved command-line option.
     * @throws PathResolutionException 
     * @throws ProjectBuildingException 
     */
    private String addDependencyPath( Matcher matcher, String src )
        throws IOException, PathResolutionException
    {
        String groupId = matcher.group( 1 );
        String artifactId = matcher.group( 2 );

        String depKey = ArtifactUtils.versionlessKey( groupId, artifactId );
        
        if ( artifactMap == null )
        {
            artifactMap = ArtifactUtils.artifactMapByVersionlessId( artifacts );
        }
        
        log.debug( "Artifact Map for Resolving @pathOf expressions:\n\n" + artifactMap.toString().replace( ',', '\n' ) );

        Artifact artifact = (Artifact) artifactMap.get( depKey );
        
        log.debug( "Resolving path for: " + artifact + " with key: " + depKey );
        
        File path = pathResolver.resolve( artifact );
        
        log.debug( "Artifact for key: " + depKey + " is: " + artifact );
        log.debug( "Dependency path for artifact: " + artifact + " is: " + path );

        return StringUtils.replace(src, matcher.group( 0 ), path.getCanonicalPath() );
    }
    
}
