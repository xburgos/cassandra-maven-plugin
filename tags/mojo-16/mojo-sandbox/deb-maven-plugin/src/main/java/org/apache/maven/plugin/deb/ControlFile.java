package org.apache.maven.plugin.deb;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * A simple bean representing the <code>DEBIAN/control</code> file.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ControlFile
{
    private static final String EOL = System.getProperty( "line.separator" );

    private String section;

    private String priority;

    private String maintainer;

    private String packageName;

    private String architecture;

    private List dependencies;

    private String shortDescription;

    private String description;

    private String version;

    private String maintainerRevision;

    private MavenProject project;

    private Log log;

    public ControlFile( MavenProject project, Log log )
    {
        this.project = project;
        this.log = log;
    }

    public String getArchitecture()
    {
        if ( architecture == null )
        {
            return "all";
        }

        return architecture;
    }

    public void setArchitecture( String architecture )
    {
        this.architecture = architecture;
    }

    public String getDepends()
    {
        String depends = "";

        List dependencies = project.getDependencies();

        for ( int i = 0; i < dependencies.size(); i++ )
        {
            Dependency dependency = (Dependency) dependencies.get( i );

            // TODO we should have a configuration option for setting scope of dependency to check here
            // String include = dependency.getProperties().getProperty( "deb.depends" );
            if ( dependency.getScope() != null && dependency.getScope().equals( "runtime" ) )
            {

                if ( depends.length() > 0 )
                {
                    depends += ", ";
                }

                depends += dependency.getGroupId() + "-" + dependency.getArtifactId() + " ";
                depends += "(=" + dependency.getVersion() + ")";
            }
        }

        return depends;
    }

    public void setDependencies( List dependencies )
    {
        this.dependencies = dependencies;
    }

    public String getDescription()
        throws IOException
    {
        String shortDescription, description;

        shortDescription = StringUtils.clean( this.shortDescription );

        description = StringUtils.clean( this.description );

        if ( shortDescription.length() == 0 )
        {
            // TODO there was a short description here, check if still available
            shortDescription = StringUtils.clean( getShortDescription( project.getDescription() ) );
        }

        if ( description.length() == 0 )
        {
            if ( project.getDescription() == null || project.getDescription().length() == 0 )
            {
                description = "Missing project description";
            }
            else
            {
                description = StringUtils.clean( project.getDescription() );
            }
        }

        // TODO this is probably unnecessary
        if ( shortDescription.length() == 0 )
        {
            shortDescription = "Missing description.";
        }

        if ( shortDescription.length() > 60 )
        {
            log.warn( "The short description was cropped to 60 chars." );
            shortDescription = shortDescription.substring( 0, 60 );
        }

        if ( description.trim().length() == 0 )
        {
            return shortDescription;
        }

        BufferedReader reader = new BufferedReader( new StringReader( description ) );

        String line;

        boolean isFirstLine = true;

        line = reader.readLine();

        if ( line == null && line.trim().length() == 0 )
        {
            return shortDescription;
        }

        description = " " + line;

        while ( ( line = reader.readLine() ) != null )
        {
            line = line.trim();

            if ( line.equals( "" ) )
            {
                if ( !isFirstLine )
                {
                    line = ".";
                }
                else
                {
                    continue;
                }
            }

            isFirstLine = false;
            description += EOL + " " + line;
        }

        return shortDescription + EOL + description + EOL;
    }

    private String getShortDescription( String d )
    {
        if ( d == null )
        {
            return "Missing description";
        }
        int i = d.indexOf( '.' );
        if ( i > 0 )
        {
            return d.substring( 0, i );
        }
        return d;
    }

    public void setDescription( String shortDescription, String description )
    {
        this.shortDescription = shortDescription;
        this.description = description;
    }

    public String getMaintainer()
    {
        if ( maintainer == null )
        {
            // FIXME get from developer list or optional configuration
            return "";
            //return project.getCiManagement().getNagEmailAddress();
        }
        else
        {
            return maintainer;
        }
    }

    public void setMaintainer( String maintainer )
    {
        this.maintainer = maintainer;
    }

    public String getPackageName()
    {
        if ( packageName == null )
        {
            // debianize names
            String debnameGroup = project.getGroupId();
            debnameGroup = debnameGroup.replaceAll( "-", "_" );
            String debnameArtifact = project.getArtifactId();
            debnameArtifact = debnameArtifact.replaceAll( "-", "_" );
            return debnameGroup + "-" + debnameArtifact;
        }
        else
        {
            return packageName;
        }
    }

    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

    public String getPriority()
    {
        if ( priority == null )
        {
            return "standard";
        }

        return priority;
    }

    public void setPriority( String priority )
    {
        this.priority = priority;
    }

    public String getSection()
    {
        if ( section == null )
        {
            return "devel";
        }

        return section;
    }

    public void setSection( String section )
    {
        this.section = section;
    }

    public String getVersion()
    {
        if ( version == null )
        {
            return ( project.getVersion() + "-" + getMaintainerRevision() ).toLowerCase();
        }

        return version.toLowerCase();
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getMaintainerRevision()
    {
        if ( maintainerRevision == null )
        {
            return "1";
        }

        return maintainerRevision;
    }

    public void setMaintainerRevision( String maintainerRevision )
    {
        this.maintainerRevision = maintainerRevision;
    }
}
