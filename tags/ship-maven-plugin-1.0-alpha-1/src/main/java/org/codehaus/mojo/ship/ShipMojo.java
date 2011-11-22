package org.codehaus.mojo.ship;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ships the {@link #shipVersion} of the project artifacts using the Continuous Deployment script.
 *
 * @author Stephen Connolly
 * @goal ship
 * @description Ships the {@link #shipVersion} of the project artifacts using the Continuous Deployment script.
 * @threadSafe
 * @since 0.1
 */
public class ShipMojo
    extends AbstractMojo
{
    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    private org.apache.maven.artifact.factory.ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private org.apache.maven.artifact.repository.ArtifactRepository local;

    /**
     * Used if scripts want access to the wagonManager.
     *
     * @component
     */
    private org.apache.maven.artifact.manager.WagonManager wagonManager;

    /**
     * The current user system settings for use in Maven.
     *
     * @parameter expression="${settings}"
     * @readonly
     */
    private Settings settings;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected java.util.List remoteRepos;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @readonly
     * @since 0.1
     */
    private MavenProject project;

    /**
     * Used to create Artifacts.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * The version of the project artifacts to ship.
     *
     * @parameter expression="${shipVersion}" default-value="${project.version}"
     * @since 0.1
     */
    private String shipVersion;

    /**
     * Whether to allow shipping -SNAPSHOT versions, if <code>true</code> and the {@link #shipVersion} is a -SNAPSHOT
     * version then the build will be failed unless {@link #shipSnapshotsSkipped} is <code>true</code>.
     *
     * @parameter expression="${shipSnapshotsAllowed}" default-value="false"
     * @since 0.1
     */
    private boolean shipSnapshotsAllowed;

    /**
     * Whether to try and ship -SNAPSHOT versions, if <code>true</code> and the {@link #shipVersion} is a -SNAPSHOT
     * version then an attempt will be made to ship the project artifacts .
     *
     * @parameter expression="${shipSnapshotsSkipped}" default-value="false"
     * @since 0.1
     */
    private boolean shipSnapshotsSkipped;

    /**
     * Whether to bother trying to ship anything at all.
     *
     * @parameter expression="${shipSkip}" default-value="false"
     * @since 0.1
     */
    private boolean shipSkip;

    /**
     * The project artifacts to ship, if undefined then it will default to the project artifact.
     *
     * @parameter
     * @since 0.1
     */
    private Selector[] selectors;

    /**
     * The directory containing the ship scripts.
     *
     * @parameter default-value="src/ship/script"
     */
    private String shipScriptDirectory;

    /**
     * The name of the ship script to execute, the selected artifact files will be passed as the global variable
     * <code>artifacts</code> which is a {@link Map} of the artifact files keyed by selector id. Wildcards can be
     * used in which case the order of scripts will be undefined.
     *
     * @parameter expression="${shipScript}"
     */
    private String shipScript;

    /**
     * The ship scripts to execute in order, defaults to <code>*</code>. The selected artifact files will be passed as the global variable
     * <code>artifacts</code> which is a {@link Map} of the artifact files keyed by selector id. If wildcards are used
     * for an entry, then the order of scripts in that entry will be undefined, but the previous entries will
     * be executed first and subsequent entries will be executed afterwards, e.g. <pre>
     *     &lt;shipScripts&gt;
     *       &lt;shipScript&gt;foo.groovy&lt;/shipScript&gt;
     *       &lt;shipScript&gt;ma*.groovy&lt;/shipScript&gt;
     *       &lt;shipScript&gt;bar.groovy&lt;/shipScript&gt;
     *     &lt;/shipScripts&gt;
     * </pre> will execute <code>foo.groovy</code> first, followed by all the scripts matching <code>ma*.groovy</code>
     * in what ever order it finds them, and finally <code>bar.groovy</code> will be executed.
     *
     * @parameter
     */
    private String[] shipScripts;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( shipSkip )
        {
            getLog().info( "Shipping skipped." );
            return;
        }
        if ( shipScript != null )
        {
            getLog().debug( "A single ship script has been specified via the shipScript configuration option" );
            shipScripts = new String[]{ shipScript };
        }
        else if ( shipScripts == null || shipScripts.length == 0 )
        {
            getLog().debug( "Using default ship script selector of '*'" );
            shipScripts = new String[]{ "*" };
        }
        getLog().info( "Ship version: " + shipVersion );
        if ( ArtifactUtils.isSnapshot( shipVersion ) )
        {
            if ( shipSnapshotsSkipped )
            {
                getLog().info( "Shipping skipped as ship version is a -SNAPSHOT" );
                return;
            }
            if ( !shipSnapshotsAllowed )
            {
                throw new MojoExecutionException(
                    "Shipping -SNAPSHOT version is not allowed and the requested shipVersion (" + shipVersion
                        + ") is a -SNAPSHOT" );
            }
        }
        boolean searchReactor = StringUtils.equals( shipVersion, project.getVersion() );
        if ( searchReactor )
        {
            getLog().debug( "Ship version is project version, will preferentially resolve from the reactor" );
        }
        if ( selectors == null )
        {
            if ( "pom".equals( project.getPackaging() ) )
            {
                selectors = new Selector[]{ new Selector( project.getPackaging() ) };
            }
            else
            {
                selectors = new Selector[]{ new Selector( "pom" ), new Selector( project.getPackaging() ) };
            }
        }
        List artifacts = new ArrayList();
        artifacts.addAll( project.getArtifacts() );
        if ( project.getArtifact() != null )
        {
            artifacts.add( project.getArtifact() );
        }
        if ( project.getAttachedArtifacts() != null )
        {
            artifacts.addAll( project.getAttachedArtifacts() );
        }
        if ( select( artifacts, new Selector( "pom" ) ) == null )
        {
            Artifact projectArtifact =
                artifactFactory.createProjectArtifact( project.getGroupId(), project.getArtifactId(),
                                                       project.getVersion() );
            projectArtifact.setFile( project.getFile() );
            artifacts.add( projectArtifact );
        }
        Map artifactFiles = new LinkedHashMap( selectors.length );
        for ( int i = 0; i < selectors.length; i++ )
        {
            if ( StringUtils.isEmpty( selectors[i].getType() ) )
            {
                selectors[i].setType( project.getPackaging() );
            }
            if ( StringUtils.isEmpty( selectors[i].getClassifier() ) )
            {
                selectors[i].setClassifier( null );
            }
            getLog().debug( "Using selector " + selectors[i] );
            Artifact artifact = null;
            if ( searchReactor )
            {
                artifact = select( artifacts, selectors[i] );
            }
            if ( artifact == null || artifact.getFile() == null || !artifact.getFile().isFile() )
            {
                try
                {
                    Artifact tmp = factory.createArtifactWithClassifier( project.getGroupId(), project.getArtifactId(),
                                                                         shipVersion, selectors[i].getType(),
                                                                         selectors[i].getClassifier() );
                    resolver.resolve( tmp, remoteRepos, local );
                    artifact = tmp;
                }
                catch ( ArtifactResolutionException e )
                {
                    throw new MojoExecutionException( e.getLocalizedMessage(), e );
                }
                catch ( ArtifactNotFoundException e )
                {
                    // ignore
                }
            }
            if ( artifact == null )
            {
                throw new MojoExecutionException(
                    "Could not find required artifact " + project.getGroupId() + ":" + project.getArtifactId() + ":"
                        + shipVersion + ":" + selectors[i].getType() + ":" + selectors[i].getClassifier() );
            }
            if ( artifact.getFile() == null )
            {
                throw new MojoExecutionException( "Resolved artifact " + artifact + " does not have a resolved file." );
            }
            if ( !artifact.getFile().isFile() )
            {
                throw new MojoExecutionException(
                    "Resolved artifact " + artifact + "'s resolved file does not exist." );
            }
            artifactFiles.put( selectors[i].getId(), artifact.getFile() );
        }
        getLog().info( "Shipping the following artifacts: " );
        for ( Iterator i = artifactFiles.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();
            getLog().info( "  " + entry.getKey() + " --> " + entry.getValue() );
        }
        ScriptEngineManager mgr = new ScriptEngineManager();

        Map scriptVars = new LinkedHashMap();
        scriptVars.put( "artifacts", Collections.unmodifiableMap( artifactFiles ) );
        scriptVars.put( "log", getLog() );
        scriptVars.put( "settings", settings );
        scriptVars.put( "wagonManager", wagonManager );

        List scripts = new ArrayList();
        File shipScriptDir = new File( project.getBasedir(), shipScriptDirectory );
        for ( int i = 0; i < shipScripts.length; i++ )
        {
            getLog().debug( "Looking for scripts matching " + shipScripts[i] + " in " + shipScriptDir );
            try
            {
                List files = FileUtils.getFiles( shipScriptDir, shipScripts[i], null );
                if ( files.isEmpty() )
                {
                    throw new MojoExecutionException(
                        "Could not find any scripts matching " + shipScripts[i] + " in " + shipScriptDir );
                }
                scripts.addAll( files );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "Could not find scripts matching " + shipScripts[i] + " in " + shipScriptDir, e );
            }
        }
        for ( Iterator i = scripts.iterator(); i.hasNext(); )
        {
            File script = (File) i.next();
            getLog().info( "Executing " + script );
            mgr.eval( script, scriptVars, getLog() );
        }
    }

    private Artifact select( List artifacts, Selector selector )
    {
        Iterator i = artifacts.iterator();
        while ( i.hasNext() )
        {
            Artifact artifact = (Artifact) i.next();
            if ( StringUtils.equals( project.getGroupId(), artifact.getGroupId() )
                && StringUtils.equals( project.getArtifactId(), artifact.getArtifactId() ) && selector.matches(
                artifact ) )
            {
                return artifact;
            }
        }
        return null;
    }
}
