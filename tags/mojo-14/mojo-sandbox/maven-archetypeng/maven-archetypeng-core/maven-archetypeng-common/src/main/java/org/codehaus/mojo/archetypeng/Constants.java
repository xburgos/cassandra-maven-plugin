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

package org.codehaus.mojo.archetypeng;

import java.util.Arrays;
import java.util.List;

public interface Constants
{
    String ARCHETYPE_ARTIFACT_ID = "archetype.artifactId";

    String ARCHETYPE_DESCRIPTOR = "META-INF/maven/archetype.xml";

    String ARCHETYPE_GROUP_ID = "archetype.groupId";

    String ARCHETYPE_POM = "pom.xml";

    String ARCHETYPE_RESOURCES = "archetype-resources";

    String ARCHETYPE_SUFFIX = "-archetype";

    String ARCHETYPE_VERSION = "archetype.version";

    String ARTIFACT_ID = "artifactId";

    String GROUP_ID = "groupId";

    List LANGUAGES = Arrays.asList ( new String[] { "java", "c", "aspectj" } );

    String MAIN = "main";

    String MAVEN = "maven";

    String META_INF = "META-INF";

    String PACKAGE = "package";

    String PROPERTY_NOT_DEFINED = "org.codehaus.mojo.archetypeng.property.not.found";

    String RESOURCES = "resources";

    String SITE = "site";

    String SRC = "src";

    String TEST = "test";

    String TMP = ".tmp";

    String VERSION = "version";
}
