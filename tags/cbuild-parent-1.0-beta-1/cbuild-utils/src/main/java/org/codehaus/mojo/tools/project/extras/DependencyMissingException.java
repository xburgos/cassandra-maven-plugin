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

/**
 * Dependency Missing Exception object
 */
public class DependencyMissingException
    extends Exception
{

    /**
     * Serializable objects need a serialVersionUID per coding standards
     */
    private static final long serialVersionUID = 1L;

    /**
     * Group name of the project artifact
     */
    private final String groupId;

    /**
     * Name of the project artifact
     */
    private final String artifactId;

    /**
     * Constructor to use when you project has a missing dependency
     * 
     * @param groupId The groupId of the missing dependency
     * @param artifactId The artifactId of the missing dependency
     */
    public DependencyMissingException( String groupId, String artifactId )
    {
        super( "Cannot find artifact path for dependency: " + groupId
            + ":" + artifactId + ". Corresponding artifact is missing." );
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * Returns a string value of the artifactId for a missing dependency
     * 
     * @return The artifactId of the missing dependency
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Returns a string value of the groupId for a missing dependency
     * 
     * @return The groupId of the missing dependency
     */
    public String getGroupId()
    {
        return groupId;
    }

}
