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

import org.apache.maven.artifact.Artifact;

/**
 * Abstract path resolver for artifacts
 *
 */
public interface ArtifactPathResolver
{

    /**
     * Resolve means search for artifacts in maven repos and bring to the local repository in maven speak
     * 
     * @param artifact project artifact to be found
     * @return File object of the artifact resolved
     * @throws PathResolutionException thrown if there is a problem resolving the requested artifact
     */
    File resolve( Artifact artifact ) throws PathResolutionException;

}
