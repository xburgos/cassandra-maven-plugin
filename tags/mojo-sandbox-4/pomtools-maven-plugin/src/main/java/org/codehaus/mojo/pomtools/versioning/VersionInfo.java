package org.codehaus.mojo.pomtools.versioning;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
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
 */

import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * 
 * @author <a href="mailto:dhawkins@codehaus.org">David Hawkins</a>
 * @version $Id$
 */
public interface VersionInfo
    extends Comparable
{
    /** Returns a string representing the version without modification.
     * 
     * @return
     */
    String getVersionString();
    
    ArtifactVersion getArtifactVersion();

    /** Returns a string representing the version with a snapshot specification
     * 
     * @return
     */
    String getSnapshotVersionString();

    /** Returns a string representing the version without a snapshot specification.
     * 
     * @return
     */
    String getReleaseVersionString();

    /** Returns a {@link VersionInfo} object which represents the next version of this object.
     * 
     * @return
     */
    VersionInfo getNextVersion();

    /** Returns whether this represents a snapshot version. ("xxx-SNAPSHOT");
     * 
     * @return
     */
    boolean isSnapshot();
    
    boolean isParsed();
}
