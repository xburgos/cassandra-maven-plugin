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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.cli.CommandLineManager;
import org.codehaus.mojo.tools.context.BuildAdvisor;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;
import org.codehaus.plexus.util.cli.shell.Shell;

/**
 * Harvest a RPM from the project binaries and the generated spec file.
 * 
 * @goal build
 * @phase package
 */
public class BuildRpmMojo
    extends AbstractMojo
{
    
    private static final String USER_HOME = System.getProperty( "user.home" );
    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );
    
    /**
     * This list was taken from the RPM(8) manpage for rpm version 4.4.1
     */
    public static final String[] DEFAULT_RPMRC_INCLUDES = {
        "/usr/lib/rpm/rpmrc",
        "/usr/lib/rpm/redhat/rpmrc",
        "/etc/rpmrc",
        USER_HOME + "/.rpmrc"
    };

    public static final String MACROFILES_HEADER = "macrofiles";
    
    /**
     * This is a list of locations to include in the generated rpmrc file that contains the ref to
     * the macro definitions that specify the topdir to be used here.
     * 
     * @parameter
     */
    private List rpmrcIncludes;

    /**
     * @parameter default-value="false" alias="rpm.build.skip"
     */
    private boolean skipBuild;

    /**
     * The configure prefix, to let the RPM harvester know how to build the dir structure.
     * 
     * @parameter
     * @required
     */
    private String prefix;

   /**
    *Override for platform postfix on RPM release number
    *
    * @parameter expression="${platformPostfix}" alias="rpm.genspec.platformPostfix"
    */
    private String platformPostfix;

    /**
     * Whether to skip postfix on RPM release number
     * rhoover - we can't use null on platformPostfix as an indication to skip the postfix
     *           until this bug is fixed (http://jira.codehaus.org/browse/MNG-1959;jsessionid=a9HqXpP8ZvX7DDXqNR?page=all)
     *           because currently specifying an empty string for a parameter in the POM yields null instead
     *           of an empty string.
     *
     * @parameter expression="${skipPlatformPostfix}" default-value="false" alias="rpm.genspec.skipPlatformPostfix"
     */
     private boolean skipPlatformPostfix;

    /**
     * Override parameter for the name of this RPM
     * 
     * @parameter
     */
    private String rpmName;

    /**
     * MavenProject instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Top directory of the RPM filesystem structure.
     * 
     * @parameter default-value="${project.build.directory}/rpm-topdir"
     * @required
     */
    private File topDir;

     /**
     * The Make DESTDIR, to let the RPM harvester know where the staged installation
     * directory is located for packaging
     * 
     * @parameter default-value="${project.build.directory}/rpm-basedir"
     * @required
     */
    private File destDir;

    /**
     * Directory, inside the RPM filesystem structure, where the RPM will be written. 
     */
    private File rpmsDir;

    /**
     * The RPM releaes level or build number
     * 
     * @parameter expression="${release}" default-value="1"
     */
    private String release;

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;
    
    /**
     * @component role-hint="default"
     */
    private CommandLineManager cliManager;

    /**
     * @component
     */
    private RpmInfoFormatter rpmInfoFormatter;

    /**
     * @component
     */
    private ProjectRpmFileManager projectRpmFileManager;

    /**
     * @component role-hint="default"
     */
    private BuildAdvisor buildAdvisor;
    
    /**
     * Build the RPM filesystem structure, setup the Rpm Ant task, and execute. Then, set the File for the
     * project's Artifact instance to the generated RPM for use in the install and deploy phases.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipBuild )
        {
            getLog().info( "Skipping RPM build (per configuration)." );
            return;
        }

        if ( buildAdvisor.isProjectBuildSkipped( session ) )
        {
            getLog().info( "Skipping execution per pre-existing advice." );
            return;
        }
        
        String rpmBaseName = calculateRpmBaseName();

        buildRpmDirectoryStructure();
        
        File rpmrc = createRpmConfigurationFiles();

        buildRpm( rpmBaseName, rpmrc );
        
        chmodTopDirContents();

        setProjectArtfactFile( rpmBaseName );
    }

    private File createRpmConfigurationFiles() throws MojoExecutionException
    {
        File rpmConfigDir = new File( topDir, "maven-rpm-config" );
        rpmConfigDir.mkdirs();
        
        File tmpDir = new File( topDir, "tmp" );
        tmpDir.mkdirs();
        
        File rpmrc = new File( rpmConfigDir, "rpmrc" );
        File macros = new File( rpmConfigDir, "macros" );
        
        List includes = getRpmrcIncludes();
        String macroFiles = getMacroFiles();
        
        FileWriter writer = null;
        
        // write a custom rpmrc that points to a custom macro file
        try
        {
            writer = new FileWriter( rpmrc );
            
            for ( Iterator it = includes.iterator(); it.hasNext(); )
            {
                String include = (String) it.next();
                
                writer.write( "include : " );
                writer.write( include );
                writer.write( LINE_SEPARATOR );
            }
            
            writer.write( "macrofiles : " );
            writer.write( macroFiles );
            if ( macroFiles.length() > 0 )
            {
                writer.write( ':' );
            }
            writer.write( macros.getCanonicalPath() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write rpmrc file in: " + rpmrc, e );
        }
        finally
        {
            IOUtil.close( writer );
        }
        
        writer = null;
        
        // write a custom macro file that defines topdir (and possibly other things)
        try
        {
            writer = new FileWriter( macros );
            
            writer.write( "%_topdir\t\t " );
            writer.write( topDir.getCanonicalPath() );
            writer.write( LINE_SEPARATOR );
            
            writer.write( "%_tmppath\t\t " );
            writer.write( tmpDir.getCanonicalPath() );
            writer.write( LINE_SEPARATOR );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write RPM macros file in: " + macros, e );
        }
        finally
        {
            IOUtil.close( writer );
        }
        
        return rpmrc;
    }

    private List getRpmrcIncludes() throws MojoExecutionException
    {
        List rpmrcIncludes = this.rpmrcIncludes;
        
        if ( rpmrcIncludes == null || rpmrcIncludes.isEmpty() )
        {
            List defaultLocations = Arrays.asList( DEFAULT_RPMRC_INCLUDES );
            
            getLog().debug( "Using default rpmrc locations: " + defaultLocations );
            
            rpmrcIncludes = new ArrayList( defaultLocations );
        }
        
        if ( rpmrcIncludes != null && !rpmrcIncludes.isEmpty() )
        {
            for ( Iterator it = rpmrcIncludes.iterator(); it.hasNext(); )
            {
                String include = (String) it.next();
                
                include = StringUtils.replace( include, "~", USER_HOME );
                
                File includeFile = new File( include );
                
                if ( !includeFile.exists() || !includeFile.isFile() )
                {
                    getLog().debug( "rpmrc file: " + includeFile + " does not exist. It will be excluded." );
                    it.remove();
                }
            }
        }
        
        return rpmrcIncludes;
    }
    
    private String getMacroFiles() throws MojoExecutionException
    {
        RpmrcParsingConsumer consumer = new RpmrcParsingConsumer( MACROFILES_HEADER );
        
        Commandline cli = new Commandline();
        
        Shell shell = new BourneShell( true );
        shell.setQuotedArgumentsEnabled( false );

        cli.setShell( shell );
        
        cli.setExecutable( "rpm" );
        cli.createArgument().setLine( "--showrc" );
        
        try
        {
            int result = cliManager.execute( cli, consumer, cliManager.newDebugStreamConsumer() );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( "rpm command returned an exit value != 0. Aborting build; see debug output for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Error reading rc info from rpm.", e );
        }
        
        return consumer.getValue();
    }

    private void setProjectArtfactFile( String rpmBaseName ) throws MojoExecutionException
    {
        if ( RpmInfoFormatter.getUseRpmFinalName( project ) )
        {
            rpmBaseName = rpmBaseName + "-" + project.getVersion() + "-" + release;
        }
        
        getLog().debug( "Just before setting the final RPM project-artifact, release is: " + release + "; rpmBaseName is: " + rpmBaseName );
        
        projectRpmFileManager.formatAndSetProjectArtifactFile( session, topDir, rpmBaseName, skipPlatformPostfix );
        
        File projectFile = project.getArtifact().getFile();
        
        // if this doesn't exist, then we either had a problem building the RPM, or we're pointing
        // at the wrong location. Either way, it's time to take a time-out and look around.
        if ( !projectFile.exists() )
        {
            throw new MojoExecutionException( "RPM file: " + projectFile + " does not exist." );
        }
    }

    private String calculateRpmBaseName() throws MojoExecutionException
    {
        String rpmBaseName = null;
        
        if ( rpmName != null && rpmName.trim().length() > 0 )
        {
            rpmBaseName = rpmName;
        }
        else
        {
            try
            {
                rpmBaseName = rpmInfoFormatter.formatRpmName( session, release, platformPostfix, skipPlatformPostfix );
            }
            catch ( RpmFormattingException e )
            {
                throw new MojoExecutionException( "Failed to format RPM name. Reason: " + e.getMessage(), e );
            }
        }
        
        return rpmBaseName;
    }

    private void buildRpm( String rpmBaseName, File rpmrc ) throws MojoExecutionException
    {
        Commandline cli = new Commandline();
        File tmpPath = new File( topDir, "tmp" );
        cli.setShell( new BourneShell( true ) );
        
        cli.setExecutable( "rpmbuild" );
        
        // More parameterization to cover MacPorts install of RPM
        cli.createArgument().setLine(
            "-bb --rcfile " + rpmrc.getPath() +
            " --define \"_tmppath " + tmpPath.getAbsolutePath() +
            "\" --define \"_topdir " + topDir.getAbsolutePath() +
            "\" --define \"buildroot " + destDir.getAbsolutePath() + "\" ");
        
        File specFile = new File( topDir + "/SPECS/" + rpmBaseName + ".spec" );

        cli.createArgument().setLine( specFile.getAbsolutePath() );
        
        StreamConsumer consumer = cliManager.newInfoStreamConsumer();
        
        try
        {
            int result = cliManager.execute( cli, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( "rpmbuild command returned an exit value != 0. Aborting build; see command output above for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to build RPM. Reason: " + e.getMessage(), e );
        }
    }

    private void chmodTopDirContents() throws MojoExecutionException
    {
        Commandline chmod = new Commandline();
        chmod.setExecutable( "chmod" );
        
        chmod.createArgument().setLine( "-R" );
        chmod.createArgument().setLine( "g+w" );
        chmod.createArgument().setLine( topDir.getAbsolutePath() );
        
        try
        {
            StreamConsumer consumer = cliManager.newDebugStreamConsumer();
            
            int result = cliManager.execute( chmod, consumer, consumer );
            
            if ( result != 0 )
            {
                throw new MojoExecutionException( "chmod command returned an exit value != 0. Aborting build; see debug output for more information." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Failed to chmod g+w RPM topdir: " + topDir + ". Reason: " + e.getMessage(), e );
        }
    }

    /**
     * Ensure that the RPM filesystem structure exists below the RPM top-directory.
     * @throws MojoExecutionException 
     */
    private void buildRpmDirectoryStructure() throws MojoExecutionException
    {
        topDir.mkdirs();

        // my @subdirs = ('BUILD', File::Spec->catfile('RPMS', SystemProperty->getArchitecture()), 'SOURCES', 
        // 'SPECS', 'SRPMS');
        File build = new File( topDir, "BUILD" );
        build.mkdirs();

        try
        {
            rpmsDir = new File( topDir, "RPMS/" + rpmInfoFormatter.formatPlatformArchitecture( session ) );
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot read OS architecture from rpm command.", e );
        }

        getLog().info( "RPMS Directory: \'" + rpmsDir.getAbsolutePath() + "\'" );

        rpmsDir.mkdirs();

        File sources = new File( topDir, "SOURCES" );
        sources.mkdirs();

        File specs = new File( topDir, "SPECS" );
        specs.mkdirs();

        File srpms = new File( topDir, "SRPMS" );
        srpms.mkdirs();

        File temps;
        try
        {
            temps = new File( topDir, "tmp/" + 
                rpmInfoFormatter.formatRpmNameWithoutVersion( session ) + "-root/" + prefix);
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot read OS arch from rpm command.", e );
        }

        temps.mkdirs();
    }
    
    public final class RpmrcParsingConsumer implements StreamConsumer
    {
        private String headerName;
        private String headerValue;
        
        RpmrcParsingConsumer( String headerName )
        {
            this.headerName = headerName;
        }
        
        public void consumeLine( String line )
        {
            getLog().debug( line );
            
            if ( line.startsWith( headerName ) ) 
            {
                int firstColonIdx = line.indexOf( ':' );
                headerValue = line.substring( firstColonIdx + 1 ).trim();
            }
        }
        
        public List getValueList()
        {
            List result = new ArrayList();
            StringTokenizer tokens = new StringTokenizer( headerValue, ":" );
            
            while( tokens.hasMoreTokens() )
            {
                result.add( tokens.nextToken() );
            }
            
            return result;
        }
        
        public String getValue()
        {
            return headerValue;
        }
    }

}
