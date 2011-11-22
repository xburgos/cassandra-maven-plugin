package org.codehaus.mojo.tools.project.extras;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
//import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.StringUtils;

/**
 * This object is useful to query the installation directory of a project dependency
 * 
 * Currently, we support one expressions:
 * 
 * <code>@pathOf(groupId:artifactId)@</code> - which will substitute the systemPath of
 *   the dependency referenced by groupId:artifactId, if it's system-scoped.
 *
 * may want to support <code>@destDir@</code> in the future - which simply substitutes in the destDir mojo parameter
 *
 */
public class DependencyPathResolver
{
    /**
     * Collection of artifacts to run the matcher expression upon
     */
    private final Collection < Artifact > artifacts;
    /**
     * Plexus Logger
     */
    private final Log log;
    /**
     * Map of artifacts resolved by maven into specific versions
     */
    private Map < String, Artifact > artifactMap;
    /**
     * The path resolver which can resolve the <code>@pathOf(groupId:artifactId)@</code> expression
     */
    private final ArtifactPathResolver pathResolver;

    /**
     * Currently, we support one expressions:
     * 
     * <code>@pathOf(groupId:artifactId)@</code> - which will substitute the systemPath of
     *   the dependency referenced by groupId:artifactId, if it's system-scoped.
     * 
     * @param artifacts Collection of artifacts resolved by maven into specific versions
     * @param pathResolver path resolver which can resolve the <code>@pathOf(groupId:artifactId)@</code> expression
     * @param log plexus logger
     */
    public DependencyPathResolver( Collection < Artifact > artifacts, ArtifactPathResolver pathResolver, Log log )
    {
        this.artifacts = artifacts;
        this.pathResolver = pathResolver;
        this.log = log;
    }

    /**
     * Resolve expressions in the command-line options.
     * 
     * Currently, we support one expressions:
     * 
     * <code>@pathOf(groupId:artifactId)@</code> - which will substitute the systemPath of
     *   the dependency referenced by groupId:artifactId, if it's system-scoped.
     *
     * @param src the <code>@pathOf@</code> expression to be translated
     * @throws IOException thown if there was an IO error encountered
     * @throws PathResolutionException thrown if method can not resolve path in the project
     * @return the resolved dependency patch
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
     * 
     * @param matcher a regular expression matcher
     * @param src the <code>@pathOf@</code> expression to be translated
     * @return the resolved dependency patch
     * @throws IOException thown if there was an IO error encountered
     * @throws PathResolutionException thrown if method can not resolve path in the project
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
        
        log.debug( "Artifact Map for Resolving @pathOf expressions:\n\n"
            + artifactMap.toString().replace( ',', '\n' ) );

        Artifact artifact = artifactMap.get( depKey );
        
        log.debug( "Resolving path for: " + artifact + " with key: " + depKey );
        
        File path = pathResolver.resolve( artifact );
        
        log.debug( "Artifact for key: " + depKey + " is: " + artifact );
        log.debug( "Dependency path for artifact: " + artifact + " is: " + path );

        return StringUtils.replace( src, matcher.group( 0 ), path.getCanonicalPath() );
    }
    
}
