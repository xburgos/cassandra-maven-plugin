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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @plexus.component
 */
public class DefaultArchetypePathResolver
extends AbstractLogEnabled
implements ArchetypePathResolver
{
    public String getDescriptorPath ()
    {
        return Constants.ARCHETYPE_DESCRIPTOR;
    }

    public String getPomPath ()
    {
        return Constants.ARCHETYPE_POM;
    }

    public String getResourcePath ( Template template )
    {
        return
            getResourcePath (
            template.getInnerPath (),
            template.isTest (),
            template.getDirectory ()
            );
    }

    public String getResourcePath ( String templatePath, boolean isTest, String directory )
    {
        return
            Constants.SRC + File.separator + getMainOrTest ( isTest ) + File.separator + directory
            + File.separator + templatePath;
    }

    public String getSitePath ( Template template )
    {
        return getSitePath ( template.getInnerPath () );
    }

    public String getSitePath ( String templatePath )
    {
        return Constants.SRC + File.separator + Constants.SITE + File.separator + templatePath;
    }

    public String getSourcePath ( Template template, String packageName )
    {
        return
            getSourcePath (
            template.getInnerPath (),
            template.isTest (),
            template.getLanguage (),
            packageName
            );
    }

    public String getSourcePath (
        String templatePath,
        boolean isTest,
        String language,
        String packageName
    )
    {
        return
            Constants.SRC + File.separator + getMainOrTest ( isTest ) + File.separator + language
            + File.separator + getPackageAsDirectory ( packageName ) + File.separator
            + templatePath;
    }

    public String getTemplatePath ( Template template )
    {
        String templatePath =
            getTemplatePath ( template.getInnerPath (), template.getTemplateName () );
        return ( templatePath.startsWith ( "/" ) ? templatePath.substring ( 1 ) : templatePath );
    }

    public String getTemplatePathWithoutPackage ( Template template, String packageName )
    {
        return
            getTemplatePathWithoutPackage (
            template.getInnerPath (),
            template.getTemplateName (),
            packageName
            );
    }

    public String getTemplatePathWithoutPackage (
        String templatePath,
        String templateName,
        String packageName
    )
    {
        return
            getTemplatePath (
            StringUtils.replace (
                templatePath,
                getPackageAsDirectory ( packageName ),
                ""
            ),
            templateName
            );
    }

    public String getTemplatePomPath ()
    {
        return Constants.ARCHETYPE_RESOURCES + File.separator + Constants.ARCHETYPE_POM;
    }

    public String getTemplateResourcePath ( Template template )
    {
        return
            getTemplateResourcePath (
            getTemplatePath ( template ),
            template.isTest (),
            template.getDirectory ()
            );
    }

    public String getTemplateResourcePath ( String templatePath, boolean isTest, String directory )
    {
        return
            Constants.ARCHETYPE_RESOURCES + File.separator + Constants.SRC + File.separator
            + getMainOrTest ( isTest ) + File.separator + directory + File.separator + templatePath;
    }

    public String getTemplateSitePath ( Template template )
    {
        return getTemplateSitePath ( getTemplatePath ( template ) );
    }

    public String getTemplateSitePath ( String templatePath )
    {
        return
            Constants.ARCHETYPE_RESOURCES + File.separator + Constants.SRC + File.separator
            + Constants.SITE + File.separator + templatePath;
    }

    public String getTemplateSourcePath ( Template template, String packageName )
    {
        return
            getTemplateSourcePath (
            getTemplatePath ( template ),
            template.isTest (),
            template.getLanguage (),
            packageName
            );
    }

    public String getTemplateSourcePath (
        String templatePath,
        boolean isTest,
        String language,
        String packageName
    )
    {
        return
            Constants.ARCHETYPE_RESOURCES + File.separator + Constants.SRC + File.separator
            + getMainOrTest ( isTest ) + File.separator + language + File.separator
            + templatePath.replaceFirst (
                getPackageAsDirectory ( packageName ) + File.separator,
                ""
            );
    }

    private String getMainOrTest ( boolean isTest )
    {
        return ( isTest ? Constants.TEST : Constants.MAIN );
    }

    private String getPackageAsDirectory ( String packageName )
    {
        return StringUtils.replace ( packageName, ".", File.separator );
    }

    private String getTemplatePath ( String templatePath, String templateName )
    {
        String path =
            ( ( StringUtils.isEmpty ( templatePath ) || File.separator.equals ( templatePath ) )
                ? templateName
                : ( templatePath + File.separator + templateName ) );
        return ( path.startsWith ( File.separator ) ? path.substring ( 1 ) : path );
    }
}
