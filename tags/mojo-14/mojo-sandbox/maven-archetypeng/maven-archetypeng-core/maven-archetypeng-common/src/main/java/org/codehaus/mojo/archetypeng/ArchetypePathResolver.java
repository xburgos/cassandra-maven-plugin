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

public interface ArchetypePathResolver
{
    String ROLE = ArchetypePathResolver.class.getName ();

    String getDescriptorPath ();
    String getPomPath ();
    String getResourcePath ( Template template );
    String getResourcePath ( String templatePath, boolean isTest, String directory );
    String getSitePath ( Template template );
    String getSitePath ( String templatePath );
    String getSourcePath ( Template template, String packageName );
    String getSourcePath (
        String templatePath,
        boolean isTest,
        String language,
        String packageName
    );
    String getTemplatePath ( Template template );
    String getTemplatePathWithoutPackage ( Template template, String packageName );
    String getTemplatePathWithoutPackage (
        String templatePath,
        String templateName,
        String packageName
    );
    String getTemplatePomPath ();
    String getTemplateResourcePath ( Template template );
    String getTemplateResourcePath ( String templatePath, boolean isTest, String directory );
    String getTemplateSitePath ( Template template );
    String getTemplateSitePath ( String templatePath );
    String getTemplateSourcePath ( Template template, String packageName );
    String getTemplateSourcePath (
        String templatePath,
        boolean isTest,
        String language,
        String packageName
    );
}
