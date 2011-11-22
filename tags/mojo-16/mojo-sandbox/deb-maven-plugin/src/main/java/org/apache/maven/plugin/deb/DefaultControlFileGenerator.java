package org.apache.maven.plugin.deb;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultControlFileGenerator
    extends AbstractLogEnabled
    implements ControlFileGenerator
{
    private static final String EOL = System.getProperty( "line.separator" );

    private Set dependencies;

    private String groupId;

    private String artifactId;

    private String version;

    private String description;

    private String shortDescription;

    private String architecture;

    private String maintainer;

    private String packageName;

    private String priority;

    private String section;

    private String maintainerRevision;

    private String debFileName;

    // ----------------------------------------------------------------------
    // ControlFileGenerator Implementation
    // ----------------------------------------------------------------------

    public void generateControl( File basedir )
        throws IOException, MojoFailureException
    {
        File debian = new File( basedir, "DEBIAN" );

        if ( !debian.exists() && !debian.mkdirs() )
        {
            throw new IOException( "Could not make directory: " + debian.getAbsolutePath() );
        }

        File control = new File( debian, "control" );

        PrintWriter output = new PrintWriter( new FileWriter( control ) );

        output.println( "Section: " + getSection() );
        output.println( "Priority: " + getPriority() );
        output.println( "Maintainer: " + getMaintainer() );
        output.println( "Package: " + getDebianPackageName() );
        output.println( "Version: " + getDebianVersion() );
        output.println( "Architecture: " + getArchitecture() );
        output.println( "Depends: " + getDepends() );
        output.println( "Description: " + getDebianDescription() );

        output.close();
    }

    public String getDebFileName()
        throws MojoFailureException
    {
        if ( debFileName != null )
        {
            return debFileName;
        }

        return getGroupId() + "-" + getArtifactId() + "-" + getDebianVersionString() +  ".deb";
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setDependencies( Set dependencies )
    {
        this.dependencies = dependencies;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = StringUtils.clean( groupId );
    }

    public String getGroupId()
        throws MojoFailureException
    {
        if ( groupId.length() == 0 )
        {
            throw new MojoFailureException( "Missing required field group id." );
        }

        return groupId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = StringUtils.clean( artifactId );
    }

    public String getArtifactId()
        throws MojoFailureException
    {
        if ( artifactId.length() == 0 )
        {
            throw new MojoFailureException( "Missing required field artifact id." );
        }

        return artifactId;
    }

    public void setVersion( String version )
    {
        this.version = StringUtils.clean( version );
    }

    public String getVersion()
        throws MojoFailureException
    {
        if ( version.length() == 0 )
        {
            throw new MojoFailureException( "Missing required field version." );
        }

        return version;
    }

    public String getArchitecture()
        throws MojoFailureException
    {
        if ( architecture.length() == 0 )
        {
            throw new MojoFailureException( "Missing required field architecture." );
        }

        return architecture;
    }

    public void setDescription( String description )
    {
        this.description = StringUtils.clean( description );
    }

    public void setShortDescription( String shortDescription )
    {
        this.shortDescription = shortDescription;
    }

    public String getShortDescription()
    {
        return StringUtils.clean( shortDescription );
    }

    public void setArchitecture( String architecture )
    {
        this.architecture = StringUtils.clean( architecture );
    }

    public String getMaintainer()
        throws MojoFailureException
    {
        if ( maintainer == null )
        {
            throw new MojoFailureException( "Missing required configuration 'maintainer'." );
        }

        return maintainer;
    }

    public void setMaintainer( String maintainer )
    {
        this.maintainer = StringUtils.clean( maintainer );
    }

    public void setPackageName( String packageName )
    {
        this.packageName = StringUtils.clean( packageName );
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
        return section;
    }

    public void setSection( String section )
    {
        this.section = section;
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

    public void setDebFileName( String debFileName )
    {
        this.debFileName = debFileName;
    }

    // ----------------------------------------------------------------------
    // Synthetic Getters
    // ----------------------------------------------------------------------

    public String getDepends()
    {
        String depends = "";

        if ( dependencies != null && dependencies.size() > 0 )
        {
            for( Iterator it = dependencies.iterator(); it.hasNext(); )
            {
                DebianDependency debianDependency = (DebianDependency) it.next();

                if ( depends.length() > 0 )
                {
                    depends += ", ";
                }

                // This will happen if this is an extra dependency
                if ( ! StringUtils.isEmpty( debianDependency.getGroupId() ) )
                {
                    depends += debianDependency.getGroupId() + "-";
                }

                depends += debianDependency.getArtifactId() + " ";

                // This will happen if this is an extra dependency
                if ( ! StringUtils.isEmpty( debianDependency.getVersion() ) )
                {
                    depends += "(" + debianDependency.getVersion() + ")";
                }
            }
        }

        return depends;
    }

    public String getDebianVersion()
        throws MojoFailureException
    {
        String debianVersion = getDebianVersionString();

        return debianVersion.toLowerCase();
    }

    private String getDebianVersionString()
            throws MojoFailureException
    {
        String result;
        if (getMaintainerRevision().equals("0")) {
            // ignore the maintainer revision in this case
            result = getVersion();
        } else {
            result = getVersion() + "-" + getMaintainerRevision();
        }
        return result;
    }

    public String getDebianPackageName()
        throws MojoFailureException
    {
        packageName = StringUtils.clean( packageName );

        if ( packageName.length() == 0 )
        {
            String name = getGroupId() + "-" + getArtifactId();

//            name = name.replaceAll( "-", "_" ).toLowerCase();

            name = name.toLowerCase();

            return name;
        }
        else
        {
            return packageName;
        }
    }

    public String getDebianDescription()
        throws MojoFailureException
    {
        // ----------------------------------------------------------------------
        // If the short description is set, use it. If now synthesize one.
        // ----------------------------------------------------------------------

        String sd = getShortDescription();

        String d = StringUtils.clean( description );

        if ( sd.length() == 0 )
        {
            int index = d.indexOf( '.' );

            if ( index > 0 )
            {
                sd = d.substring( 0, index + 1 );

                d = d.substring( index + 1 );
            }
        }

        sd = sd.trim();
        d = d.trim();

        if ( d.length() > 0 )
        {
            d = sd + EOL + d;
        }
        else
        {
            d = sd;
        }

        // ----------------------------------------------------------------------
        // Trim each line, replace blank lines with " ."
        // ----------------------------------------------------------------------

        String debianDescription;

        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( d.trim() ) );

            String line;

            debianDescription = reader.readLine();

            line = reader.readLine();

            if ( line != null )
            {
                debianDescription += EOL + " " + line.trim();

                line = reader.readLine();
            }

            while ( line != null )
            {
                line = line.trim();

                if ( line.equals( "" ) )
                {
                    debianDescription += EOL + ".";
                }
                else
                {
                    debianDescription += EOL + " " + line;
                }

                line = reader.readLine();
            }
        }
        catch ( IOException e )
        {
            // This won't happen.
            throw new RuntimeException( "Internal error", e );
        }

        return debianDescription;
    }
}
