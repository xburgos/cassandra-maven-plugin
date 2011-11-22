package org.codehaus.mojo.rpm;

/*
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
 *
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.mojo.tools.rpm.RpmSpecFormatter;

/**
 * Build a RPM spec file and save it to disk for subsequent harvesting.
 * 
 * @goal generate-spec
 * @phase package
 */
public class GenerateSpecFileMojo
    extends AbstractMojo
{

    /**
     * Whether to skip RPM spec file generation.
     * 
     * @parameter expression="${skipSpecGeneration}" default-value="false" alias="rpm.genspec.skip"
     */
    private boolean skip;

   /**
    *Override for platform postfix on RPM release number
    *
    * @parameter expression="${platformPostfix}" alias="rpm.genspec.platformPostfix"
    */
    private String platformPostfix;

    /**
     * Whether to skip postfix on RPM release number
     * rhoover - we can't use null on platformPostfix as an indication to skip the postfix
     *           until this bug is fixed (http://jira.codehaus.org/browse/MNG-1959)
     *           because currently specifying an empty string for a parameter in
     *           the POM yields null instead of an empty string.
     *
     * @parameter expression="${skipPlatformPostfix}" default-value="false" alias="rpm.genspec.skipPlatformPostfix"
     */
     private boolean skipPlatformPostfix;

    /**
     * The Artifact instance for the current project. This will be added to the list of 
     * functionality provided by this RPM.
     * 
     * @parameter expression="${project.artifact}"
     * @required
     * @readonly
     */
    private Artifact projectArtifact;

    /**
     * The dependencies of this project, for use in defining the depends and provides sets for
     * the RPM.
     * 
     * @parameter expression="${project.dependencies}"
     * @required
     * @readonly
     */
    private List dependencies;

    /**
     * List of dependencies in the form 'groupId:artifactId' which should be excluded from
     * the list of provided functionality.
     * 
     * @parameter
     */
    private List providesExclusions;

    /**
     * List of dependencies in the form 'groupId:artifactId' which should be excluded from
     * the list of dependency functionality.
     * 
     * @parameter
     */
    private List dependsExclusions;

    /**
     * The current project being built. This instance is used to furnish the information required to
     * construct the RPM spec file
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Override parameter for the name of the RPM to be created.
     * 
     * @parameter
     */
    private String rpmName;

    /**
     * The top directory of the RPM harvesting structure.
     * 
     * @parameter default-value="${project.build.directory}/rpm-topdir"
     * @required
     */
    private File topDir;

    /**
     * The Make DESTDIR, to let the RPM harvester know where the software staged
     * installation directory is located for packaging
     * 
     * @parameter default-value="${project.build.directory}/rpm-basedir"
     * @required
     */
    private File destDir;

    /**
     * The configure prefix, to let the RPM harvester know how to build the dir structure.
     * 
     * @parameter
     * @required
     */
    private String prefix;

    /**
     * The build number of the RPM, so you get versions like 1.2-4 which
     * would be the fourth build of the 1.2 tarball.
     *
     * @parameter expression="${release}" default-value="1"
     * @required
     */
    private String release;

    /**
     * Turn off RPM compression and symbol stripping - this is very much
     * manditory for binary sources like Sybase, Oracle, and the SunJDK.
     *
     * @parameter expression="${rpmNoStrip}" default-value="false"
     */
    private boolean rpmNoStrip;

    /**
     * Create a simple RPM pre install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${preInstallFile}"
     */
    private File preInstallFile;

    /**
     * Create a simple RPM post un install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${postUninstallFile}"
     */
    private File postUninstallFile;

    /**
     * Create a simple RPM pre un install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (this is weak in the sense that template
     * expansion is not occuring)
     *
     * @parameter expression="${preUninstallFile}"
     */
    private File preUninstallFile;

    /**
     * Create a simple RPM post install mechanism
     * If defined, the RPM will insert the named file into the spec file
     * that the pluggin is building (some claim this is weak, but 
     * make the script a .sh.in file and let autoconf do some expanding)
     *
     * @parameter expression="${postInstallFile}"
     */
    private File postInstallFile;

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @component role-hint="default"
     */
    private RpmSpecFormatter rpmSpecFormatter;

    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component role-hint="default"
     */
    private BuildAdvisor buildAdvisor;
    
    /**
     * Generate a RPM spec file, and write it to disk.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping generation of RPM spec file (per configuration)." );
            return;
        }

        if ( buildAdvisor.isProjectBuildSkipped( session ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        Set depends = new HashSet();
        Set provides = Collections.singleton( projectArtifact );

        // TODO: [JDCASEY] Fix Depends/Provides once we figure out relocatability...
        getLog()
            .warn(
                   "Assuming all dependencies are real RPM dependencies. Provides statement is not currently being populated." );
        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        {
            Dependency dependency = (Dependency) it.next();

            depends.add( dependency );
        }

        // Marking this out for now...we have to engage in much more complex tactics to determine what is provided, and what
        // is a real RPM dependency...we'll use the above code instead in the interim.
        //        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        //        {
        //            Dependency dependency = (Dependency) it.next();
        //            
        //            if ( Artifact.SCOPE_SYSTEM.equals( dependency.getScope() ) )
        //            {
        //                depends.add( dependency );
        //            }
        //            else
        //            {
        //                provides.add( dependency );
        //            }
        //        }

        processExclusions( provides, depends );

        String spec;
        try
        {
            spec = rpmSpecFormatter.buildSpec(
                session,
                depends,
                provides,
                destDir,
                prefix,
                release,
                rpmNoStrip,
                preInstallFile,
                postInstallFile,
                postUninstallFile,
                preUninstallFile,
                platformPostfix,
                skipPlatformPostfix );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Failed to build RPM spec file. Reason: " + e.getMessage(), e );
        }

        writeSpecFile( spec );
    }

    /**
     * Persist the generated spec file to disk. This file is written into the RPM top-dir under
     * "/SPECS".
     */
    private void writeSpecFile( String spec )
        throws MojoExecutionException
    {
        String specFileName;

        if ( rpmName != null && rpmName.trim().length() > 0 )
        {
            specFileName = rpmName + ".spec";
        }
        else
        {
            try
            {
                specFileName = rpmInfoFormatter.formatRpmName( session, release, platformPostfix, skipPlatformPostfix ) + ".spec";
            }
            catch ( RpmFormattingException e )
            {
                throw new MojoExecutionException( "Failed to format RPM name. Reason: " + e.getMessage(), e );
            }
        }

        File specDir = new File( topDir, "SPECS" );
        specDir.mkdirs();

        File specFile = new File( specDir, specFileName );

        Writer writer = null;
        try
        {
            writer = new FileWriter( specFile );

            writer.write( spec );

            writer.flush();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write spec file: " + specFile, e );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( IOException e )
                {
                    getLog().debug( "Failed to close spec-file writer.", e );
                }
            }
        }
    }

    /**
     * Knock out any explicit exclusions from provided and dependency sets.
     */
    private void processExclusions( Set provides, Set depends )
    {
        Map dependencyMap = getDependencyMap();

        if ( providesExclusions != null )
        {
            for ( Iterator it = providesExclusions.iterator(); it.hasNext(); )
            {
                String key = (String) it.next();

                Dependency excluded = (Dependency) dependencyMap.get( key );

                provides.remove( excluded );
            }
        }

        if ( dependsExclusions != null )
        {
            for ( Iterator it = dependsExclusions.iterator(); it.hasNext(); )
            {
                String key = (String) it.next();

                Dependency excluded = (Dependency) dependencyMap.get( key );

                depends.remove( excluded );
            }
        }
    }

    /**
     * Retrieve a map of groupId:artifactId -> Dependency to help with excluding dependencies.
     */
    private Map getDependencyMap()
    {
        Map deps = new HashMap();

        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        {
            Dependency dep = (Dependency) it.next();

            String key = ArtifactUtils.versionlessKey( dep.getGroupId(), dep.getArtifactId() );
            deps.put( key, dep );
        }

        return deps;
    }

}
