package org.apache.maven.plugin.deb;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.StringUtils;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * A simple bean representing the <code>DEBIAN/control</code> file.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ControlFile
{
    private static final String EOL = System.getProperty("line.separator");

    private String section;
    private String priority;
    private String maintainer;
    private String packageName;
    private String architecture;
    private List dependencies;

    private String shortDescription;
    private String description;
    private String version;

    private MavenProject project;

    public ControlFile( MavenProject project )
    {
        this.project = project;
    }

    /**
     * @return Returns the architecture.
     */
    public String getArchitecture()
    {
        if ( architecture == null )
            return "all";

        return architecture;
    }

    /**
     * @param architecture The architecture to set.
     */
    public void setArchitecture( String architecture )
    {
        this.architecture = architecture;
    }

    /**
     * @return Returns the depends.
     */
    public String getDepends()
    {
        String depends = "";

        List dependencies = project.getDependencies();

        for ( int i = 0; i < dependencies.size(); i++ )
        {
            Dependency dependency = (Dependency)dependencies.get(i);
            String include = dependency.getProperties().getProperty("deb.depends");

            if(include == null || !include.equals("true"))
                continue;

            if(depends.length() > 0)
                depends += ", ";

            depends += dependency.getGroupId() + "-" + dependency.getArtifactId() + " ";
            depends += "(=" + dependency.getVersion() + ")";
        }

        return depends;
    }

    /**
     * @param dependencies The dependencies to set.
     */
    public void setDependencies( List dependencies )
    {
        this.dependencies = dependencies;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
        throws Exception
    {
        String shortDescription, description;

        shortDescription = StringUtils.clean( this.shortDescription );

        description = StringUtils.clean( this.description );

        if ( shortDescription.length() == 0 )
            shortDescription = StringUtils.clean( project.getShortDescription() );

        if ( description.length() == 0 )
            description = StringUtils.clean( project.getDescription() );

        if ( shortDescription.length() == 0 )
            throw new Exception( "Missing short description." );

        if ( shortDescription.length() > 60 )
        {
            System.err.println( "The short description was cropped to 60 chars." );
            shortDescription = shortDescription.substring( 0, 60 );
        }

        if ( description.trim().length() == 0 )
            return shortDescription;

        BufferedReader reader = new BufferedReader( new StringReader( description ) );

        String line;

        boolean isFirstLine = true;

        line = reader.readLine();

        if ( line == null && line.trim().length() == 0 )
            return shortDescription;

        description = " " + line;

        while ( ( line = reader.readLine() ) != null )
        {
            line = line.trim();

            if ( line.equals( "" ) )
                if ( !isFirstLine )
                    line = ".";
                else
                    continue;

            isFirstLine = false;
            description += EOL + " " + line;
        }

        return shortDescription + EOL + description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription( String shortDescription, String description )
    {
        this.shortDescription = shortDescription;
        this.description = description;
    }

    /**
     * @return Returns the maintainer.
     */
    public String getMaintainer()
    {
        if ( maintainer == null )
            return project.getBuild().getNagEmailAddress();
        else
            return maintainer;
    }

    /**
     * @param maintainer The maintainer to set.
     */
    public void setMaintainer( String maintainer )
    {
        this.maintainer = maintainer;
    }

    /**
     * @return Returns the packageName.
     */
    public String getPackageName()
    {
        if ( packageName == null )
            return project.getGroupId() + "-" + project.getArtifactId();
        else
            return packageName;
    }

    /**
     * @param packageName The packageName to set.
     */
    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    /**
     * @return Returns the priority.
     */
    public String getPriority()
    {
        if ( priority == null )
            return "standard";

        return priority;
    }

    /**
     * @param priority The priority to set.
     */
    public void setPriority( String priority )
    {
        this.priority = priority;
    }

    /**
     * @return Returns the section.
     */
    public String getSection()
    {
        if ( section == null )
            return "devel";

        return section;
    }

    /**
     * @param section The section to set.
     */
    public void setSection( String section )
    {
        this.section = section;
    }

    /**
     * @return Returns the source.
     */
/*
    public String getSource()
    {
        return source;
    }
*/
    /**
     * @param source The source to set.
     */
/*
    public void setSource( String source )
    {
        this.source = source;
    }
*/

    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        if ( version == null )
            return project.getVersion();

        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion( String version )
    {
        this.version = version;
    }
}
