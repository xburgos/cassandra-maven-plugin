package org.codehaus.mojo.jboss.packaging;

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
 * Abstract super class for building a deployable JBoss Hibernate Archive.
 */
public abstract class AbstractHarPackagingMojo
    extends AbstractSarPackagingMojo
{
    /**
     * The artifact type.
     */
    private static final String ARTIFACT_TYPE = "jboss-har";

    /**
     * If the SAR default descriptor file does not exist, then
     * we expect 'hibernate-service.xml' in its stead.
     * 
     * @return String array containing the name of the jboss hibernate deployment descriptor.
     */
    public String[] getAlternateDeploymentDescriptorFilenames()
    {
        return new String[]{"hibernate-service.xml"};
    }

    /**
     * Get the type of the artifact.
     *
     * @return The type of the generated artifact.
     */
    public String getArtifactType()
    {
        return ARTIFACT_TYPE;
    }
}
