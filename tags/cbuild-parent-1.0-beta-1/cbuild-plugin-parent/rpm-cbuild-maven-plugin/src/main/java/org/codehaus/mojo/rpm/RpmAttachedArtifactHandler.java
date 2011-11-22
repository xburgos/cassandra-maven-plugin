package org.codehaus.mojo.rpm;

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

import org.apache.maven.artifact.handler.ArtifactHandler;

/**
 * Object gives maven an idea how to handle RPM artifacts. 
 * @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
 */
public class RpmAttachedArtifactHandler implements ArtifactHandler
{
    private String classifier, dir;

    public RpmAttachedArtifactHandler( String classifier, String myArch )
    {
        this.classifier = classifier;
        this.dir = "RPMS/" + myArch;
    }

    public String getExtension()
    {
        return "rpm";
    }
    
    public String getClassifier()
    {
        return classifier;
    }

    public String getDirectory()
    {
        return dir;
    }

    public String getLanguage()
    {
        return "native";
    }

    public String getPackaging()
    {
        return getExtension();
    }

    public boolean isAddedToClasspath()
    {
        return false;
    }

    public boolean isIncludesDependencies()
    {
        return false;
    }
}
