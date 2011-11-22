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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component
 */
public class DefaultArchetypeTemplateResolver
extends AbstractLogEnabled
implements ArchetypeTemplateResolver
{
    public String getCommonPackage ( String packageName, String templatePackage )
    {
        String common = "";

        String difference = StringUtils.difference ( packageName, templatePackage );
        if ( StringUtils.isNotEmpty ( difference ) )
        {
            String temporaryCommon =
                StringUtils.substring (
                    templatePackage,
                    0,
                    templatePackage.lastIndexOf ( difference )
                );
            if ( !difference.startsWith ( "." ) )
            {
                common =
                    StringUtils.substring (
                        temporaryCommon,
                        0,
                        temporaryCommon.lastIndexOf ( "." )
                    );
            }
            else
            {
                common = temporaryCommon;
            }
        }
        else
        {
            common = packageName;
        }

        return common;
    }

    public String resolvePackage ( File basedir )
    throws IOException, TemplateCreationException
    {
        List templates = resolveTemplates ( basedir );

        return resolvePackage ( templates );
    }

    public String resolvePackage ( List templates )
    {
        String packageName = null;
        Iterator templatesIterator = templates.iterator ();
        while ( templatesIterator.hasNext () )
        {
            Template template = (Template) templatesIterator.next ();
            if ( template.isSource () )
            {
                String templatePackage =
                    StringUtils.replace ( template.getInnerPath (), File.separator, "." );
                if ( packageName == null )
                {
                    packageName = templatePackage;
                }
                else
                {
                    packageName = getCommonPackage ( packageName, templatePackage );
                }
            }
        }

        getLogger ().debug ( "package resolved to " + packageName );

        return packageName;
    }

    public List resolveTemplates ( File basedir )
    throws IOException, TemplateCreationException
    {
        List templateAbsolutes = FileUtils.getFiles ( basedir, "pom.xml,src/**/*", "target/**/*" );

        List templates = new ArrayList ( templateAbsolutes.size () );

        Iterator templatesIterator = templateAbsolutes.iterator ();
        while ( templatesIterator.hasNext () )
        {
            File template = (File) templatesIterator.next ();
            String templatePath =
                StringUtils.prechomp (
                    template.getAbsolutePath (),
                    basedir.getAbsolutePath () + File.separator
                );

            templates.add ( new Template ( templatePath ) );
        }
        return templates;
    }

    public List getResourcesGroupsTemplates ( List templates, boolean isTest )
    {
        Map resourcesGroupsByDirectory = new HashMap ();
        Iterator templatesIterator = templates.iterator ();
        while ( templatesIterator.hasNext () )
        {
            Template template = (Template) templatesIterator.next ();

            if ( template.isResource ()
                && ( ( !isTest && !template.isTest () ) || ( isTest && template.isTest () ) )
            )
            {
                List directoryTemplates =
                    (List) resourcesGroupsByDirectory.get ( template.getDirectory () );
                if ( directoryTemplates == null )
                {
                    directoryTemplates = new ArrayList ();
                    resourcesGroupsByDirectory.put ( template.getDirectory (), directoryTemplates );
                }
                directoryTemplates.add ( template );
            }
        }
        return new ArrayList ( resourcesGroupsByDirectory.values () );
    }

    public List getSiteResourceGroupTemplates ( List templates )
    {
        List siteTemplates = new ArrayList ();
        Iterator templatesIterator = templates.iterator ();
        while ( templatesIterator.hasNext () )
        {
            Template template = (Template) templatesIterator.next ();

            if ( template.isSiteResource () )
            {
                siteTemplates.add ( template );
            }
        }
        return siteTemplates;
    }

    public List getSourcesGroupsTemplates ( List templates, boolean isTest )
    {
        Map sourcesGroupsByLanguage = new HashMap ();
        Iterator templatesIterator = templates.iterator ();
        while ( templatesIterator.hasNext () )
        {
            Template template = (Template) templatesIterator.next ();

            if ( template.isSource ()
                && ( ( !isTest && !template.isTest () ) || ( isTest && template.isTest () ) )
            )
            {
                List languageTemplates =
                    (List) sourcesGroupsByLanguage.get ( template.getLanguage () );
                if ( languageTemplates == null )
                {
                    languageTemplates = new ArrayList ();
                    sourcesGroupsByLanguage.put ( template.getLanguage (), languageTemplates );
                }
                languageTemplates.add ( template );
            }
        }
        return new ArrayList ( sourcesGroupsByLanguage.values () );
    }
}
