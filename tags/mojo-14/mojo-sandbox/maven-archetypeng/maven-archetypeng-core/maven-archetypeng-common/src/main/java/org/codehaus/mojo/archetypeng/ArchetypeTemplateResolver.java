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

import org.codehaus.mojo.archetypeng.exception.TemplateCreationException;

import java.io.File;
import java.io.IOException;

import java.util.List;

public interface ArchetypeTemplateResolver
{
    String ROLE = ArchetypeTemplateResolver.class.getName ();

    String resolvePackage ( List templates );
    String resolvePackage ( File file )
    throws IOException, TemplateCreationException;
    List resolveTemplates ( File basedir )
    throws IOException, TemplateCreationException;
    List getResourcesGroupsTemplates ( List templates, boolean isTest );
    List getSiteResourceGroupTemplates ( List templates );
    List getSourcesGroupsTemplates ( List templates, boolean isTest );
}