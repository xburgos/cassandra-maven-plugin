package org.codehaus.mojo.fitnesse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.mojo.fitnesse.runner.ClassPathBuilder;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * This goal uses the <code>fitnesse.runner.TestRunner</code> class for calling a remote FitNesse web page and
 * executes the <i>tests</i> or <i>suites</i> locally into a forked JVM. It's possible to define several pages and/or
 * servers.
 * 
 * @goal run
 * @requiresDependencyResolution runtime
 * @aggregator
 */
public class FitnesseRunnerMojo extends FitnesseAbstractMojo
{
    public static final String FITNESSE_RESULT_PREFIX = "/fitnesseResult";

    /**
     * This property defines how the plugin will create the classpath for running fixtures. It accepts a couple of
     * value: "fitnesse" (default) or "maven".<BR/> With "fitnesse" mode, the classpath is downloaded from the FitNesse
     * server page. Then classpath of the plugin is appended (for providing the good FitNesse implementation).<BR/>
     * With "maven" mode the classpath is only defined with the one of the project (POM).
     * 
     * @parameter default-value="fitnesse"
     */
    private String classPathProvider;

    /**
     * @component
     * @readonly
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     */
    private List remoteRepositories;

    /**
     * The Maven project instance for the executing project.
     * 
     * <p>
     * Note: This is passed by Maven and must not be configured by the user.
     * </p>
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * List of all artifacts for this plugin provided by Maven. This is used internally to get the FitnesseRunner.
     * 
     * <p>
     * Note: This is passed by Maven and must not be configured by the user.
     * </p>
     * 
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     * @required
     */
    private List<Artifact> pluginArtifacts;

    /**
     * The set of dependencies required by the project
     * 
     * @parameter default-value="${project.dependencies}"
     * @required
     * @readonly
     */
    private java.util.List dependencies;

    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * Artifact resolver used to find clovered artifacts (artifacts with a clover classifier).
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * Local maven repository.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${plugin.artifactId}"
     * @required
     * @readonly
     */
    private String pluginArtifactId;

    /**
     * @parameter expression="${plugin.groupId}"
     * @required
     * @readonly
     */
    private String pluginGroupId;

    /**
     * @parameter expression="${plugin.version}"
     * @required
     * @readonly
     */
    private String pluginVersion;

    /**
     * @parameter expression="${project.build.directory}/fitnesse"
     * @required
     */
    private String workingDir;

    /**
     * Java executable.
     * 
     * @parameter default-value="java"
     */
    private String jdk;

    /**
     * Jvm arguments.
     * 
     * @parameter default-value=""
     */
    private String jdkOpts;

    /**
     * Run FitnesseRunner with verbose option.
     * 
     * @parameter default-value="true"
     */
    private boolean verbose;

    /**
     * Run FitnesseRunner with debug option.
     * 
     * @parameter default-value="false"
     */
    private boolean debug;

    /**
     * Fitnesse runner class.
     * 
     * @parameter default-value="fitnesse.runner.TestRunner"
     */
    private String fitnesseRunnerClass;

    /**
     * List of Classpath substitution. Substitutions allow to change the FitNesse class path.<BR/> It should be usefull
     * when the server classpath is in a Unix syntaxe, or when libs are not located in the same folder on the server and
     * on the developer desktop.<BR/> The order of substitutions is guaranteed to be the same than the definition.
     * <BR/> The substitutions use String replacements (not patterns). <code>
     * &lt;classPathSubstitutions&gt;<BR/>
     * &#160;&#160;&lt;classPathSubstitution&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;search&gt;The key that will be replace&lt;/search&gt;<BR/>
     * &#160;&#160;&#160;&#160;&lt;replaceWith&gt;The value that should use to replace the key&lt;/replaceWith&gt;<BR/>
     * &#160;&#160;&lt;/classPathSubstitution&gt;<BR/>
     * &#160;&#160;... <BR/>
     * &lt;/classPathSubstitutions&gt;:<BR/>
     * </code>
     * This parameter can only be use if param <i>classPathProvider</i> has <i>fitnesse</i> value.
     * 
     * @parameter
     */
    private List<ClassPathSubstitution> classPathSubstitutions = new ArrayList<ClassPathSubstitution>();

    /**
     * Command for the execution of the FitRunner
     */
    private Commandline mCmd = new Commandline();

    private Artifact pluginArtifact;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        new File( this.workingDir ).mkdirs();
        checkConfiguration();
        if (verbose)
        	getLog().info( "Found " + getFitnesseSize() + " Fitnesse configuration." );
        for ( int i = 0; i < getFitnesseSize(); i++ )
        {
            callFitnesse( i );
        }
    }

    private String getClassPath( Fitnesse tServer ) throws MojoExecutionException
    {
        if ( "fitnesse".equals( classPathProvider ) )
        {
            StringBuilder tBuffer = new StringBuilder();
            ClassPathBuilder tBuilder =
                new ClassPathBuilder( tServer.getHostName(), tServer.getPort(), tServer.getPageName(), getLog() );
            tBuffer.append( tBuilder.getPath( classPathSubstitutions, getLog() ) );
            for ( Artifact curArt : pluginArtifacts )
            {
                if ( !curArt.getScope().equals( Artifact.SCOPE_PROVIDED )
                                && !curArt.getScope().equals( Artifact.SCOPE_TEST ) )
                {
                    tBuffer.append( File.pathSeparatorChar ).append( curArt.getFile().getAbsolutePath() );
                }
            }
            tBuffer.append( File.pathSeparatorChar ).append( resolvePlugin().getFile().getAbsolutePath() );
            if (verbose)
            	getLog().info( "Try to download classpath from FitNesse server..." );
            return tBuffer.toString();
        }
        else
        {
            return getMavenClassPath();
        }
    }

    String getMavenClassPath() throws MojoExecutionException
    {
        StringBuilder tBuffer = new StringBuilder();
        Set tArtifacts = transitivelyResolvePomDependencies();
        if ( tArtifacts != null && !tArtifacts.isEmpty() )
        {
            for ( Iterator it = tArtifacts.iterator(); it.hasNext(); )
            {
                Artifact curArtififact = (Artifact) it.next();
                tBuffer.append( curArtififact.getFile().getAbsolutePath() ).append( File.pathSeparatorChar );
            }
        }
        return tBuffer.toString();
    }

    @SuppressWarnings("unchecked")
	public Set transitivelyResolvePomDependencies() throws MojoExecutionException
    {
        // make Artifacts of all the dependencies
        Set<Artifact> dependencyArtifacts;
        try
        {
            dependencyArtifacts = MavenMetadataSource.createArtifacts( artifactFactory, dependencies, null, null, null );
        }
        catch ( InvalidDependencyVersionException e )
        {
            throw new MojoExecutionException( "Invalid dependency", e );
        }

        // not forgetting the Artifact of the project itself
        dependencyArtifacts.add( project.getArtifact() );

        List listeners = Collections.EMPTY_LIST;

        // resolve all dependencies transitively to obtain a comprehensive list of jars
        ArtifactResolutionResult result;
        try
        {
            result =
                artifactResolver.resolveTransitively( dependencyArtifacts, project.getArtifact(),
                                                      Collections.EMPTY_MAP, localRepository, remoteRepositories,
                                                      metadataSource, null, listeners );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Unable to resolve Artifact.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Unable to resolve Artifact.", e );
        }

        return result.getArtifacts();
    }

    /**
     * Call a Fitnesse server page.
     * 
     * @param serverConfPosition
     *            The number of the Fitnesse configuration.
     * @throws MojoFailureException
     * @throws MojoExecutionException
     */
    void callFitnesse( int serverConfPosition ) throws MojoFailureException, MojoExecutionException
    {
        getLog().info( "Call result of the server," + getFitnesse( serverConfPosition ) );
        Commandline tCmd = prepareCommandLine( serverConfPosition, getClassPath( getFitnesse( serverConfPosition ) ) );
        executeCommand( tCmd );
    }

    void executeCommand( Commandline tCmd ) throws MojoFailureException, MojoExecutionException
    {
        LogConsumer tInfoConsumer = new LogConsumer( getLog(), Level.INFO );
        int tResult;
        try
        {
            tResult =
                CommandLineUtils.executeCommandLine( tCmd, tInfoConsumer, new LogConsumer( getLog(), Level.SEVERE ) );
        }
        catch ( CommandLineException e )
        {
            getLog().error( "Unable to start fitnesse [" + tCmd.toString() + "]", e );
            throw new MojoExecutionException( "Unable to start fitnesse [" + tCmd.toString() + "]", e );
        }
        if ( tResult != 0 )
        {
            if ( tInfoConsumer.hasGeneratedResultFile() )
            {
                if ( isFailOnError() )
                {
                    throw new MojoFailureException( "Fitnesse command ended with errors, exit code:" + tResult );
                }
                else
                {
                    getLog().info(
                                   "Fitnesse command ended with errors, exit code:" + tResult
                                                   + ", but failOnError is configure to \"false\""
                                                   + " change your configuration if you want to fail your build" );
                }
            }
            else
            {
                throw new MojoExecutionException( "Unable to run Fitnesse, exit code [" + tResult + "]" );
            }
        }
        getLog().info( "Fitnesse invocation ended with result code [" + tResult + "]" );
    }

    void checkConfiguration() throws MojoExecutionException
    {
        super.checkConfiguration();
        try
        {
            Class<?> tClass = Class.forName( fitnesseRunnerClass );
            tClass.getMethod( "main", new Class[] { String[].class } );
        }
        catch ( ClassNotFoundException e )
        {
            throw new MojoExecutionException(
                                              "The class ["
                                                              + fitnesseRunnerClass
                                                              + "] could not be found, check your maven-fitnesse-plugin configuration and the plugin documentation." );
        }
        catch ( SecurityException e )
        {
            throw new MojoExecutionException( "The class [" + fitnesseRunnerClass
                            + "] doesn't have a \"main\" accessible method.", e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new MojoExecutionException( "The class [" + fitnesseRunnerClass
                            + "] doesn't have a \"main\" accessible method.", e );
        }
        if ( ( !"fitnesse".equals( classPathProvider ) ) && ( !"maven".equals( classPathProvider ) ) )
        {
            throw new MojoExecutionException( "classPathProvider accepts only \"fitnesse\" ou \"maven\" values. ["
                            + classPathProvider + "] is not valid." );
        }
    }

    Commandline prepareCommandLine( int i, String pClassPath ) throws MojoExecutionException
    {
        mCmd.clear();
        Fitnesse tServer = getFitnesse( i );
        mCmd.setExecutable( jdk );
        if ( jdkOpts != null && jdkOpts.length() > 0 )
        {
            StringTokenizer tTok = new StringTokenizer( jdkOpts, " " );
            while ( tTok.hasMoreTokens() )
            {
                mCmd.createArgument().setValue( tTok.nextToken() );
            }
        }
        mCmd.createArgument().setValue( "-cp" );
        mCmd.createArgument().setValue( pClassPath );
        mCmd.createArgument().setValue( fitnesseRunnerClass );
        if ( verbose )
        {
            mCmd.createArgument().setValue( "-v" );
        }
        if ( debug )
        {
            mCmd.createArgument().setValue( "-debug" );
        }
        mCmd.createArgument().setValue( "-html" );
        mCmd.createArgument().setValue(
                                        this.workingDir + FITNESSE_RESULT_PREFIX + "_" + tServer.getHostName() + "_"
                                                        + tServer.getPageName() + ".html" );
        mCmd.createArgument().setValue( "-nopath" );
        mCmd.createArgument().setValue( tServer.getHostName() );
        ;
        mCmd.createArgument().setValue( "" + tServer.getPort() );
        mCmd.createArgument().setValue( tServer.getPageName() );
        mCmd.setWorkingDirectory( workingDir );
        if (verbose)
        {
        	getLog().info( "Execute =" + mCmd.toString() );
        	getLog().info( "From =" + mCmd.getWorkingDirectory() );
        }
        return mCmd;
    }

    public void setFitnesseRunnerClass( String pFitnesseRunnerClass )
    {
        fitnesseRunnerClass = pFitnesseRunnerClass;
    }

    public void setJdk( String pJdk )
    {
        jdk = pJdk;
    }

    public void setWorkingDir( String pWorkingDir )
    {
        workingDir = pWorkingDir;
    }

    void setDebug( boolean pDebug )
    {
        debug = pDebug;
    }

    void setVerbose( boolean pVerbose )
    {
        verbose = pVerbose;
    }

    void setCmd( Commandline pCmd )
    {
        this.mCmd = pCmd;
    }

    void setJdkOpts( String jdkOpts )
    {
        this.jdkOpts = jdkOpts;
    }

    public void setPluginArtifacts( List<Artifact> pluginArtifacts )
    {
        this.pluginArtifacts = pluginArtifacts;
    }

    public Artifact resolvePlugin() throws MojoExecutionException
    {
        if ( pluginArtifact == null )
        {
            Artifact tPluginArtifact =
                this.artifactFactory.createArtifactWithClassifier( pluginGroupId, pluginArtifactId, pluginVersion,
                                                                   "maven-plugin", "" );
            try
            {
                this.artifactResolver.resolve( tPluginArtifact, new ArrayList<Object>(), localRepository );
            }
            catch ( ArtifactResolutionException e )
            {
                throw new MojoExecutionException( "Unable to resolve artificat", e );
            }
            catch ( ArtifactNotFoundException e )
            {
                throw new MojoExecutionException( "Unable to resolve artificat", e );
            }
            return tPluginArtifact;
        }
        else
        {
            return pluginArtifact;
        }
    }

    /**
     * For testing purpose
     */
    void setPluginArtifactInfo( String pGroupId, String pArtifactId, String pVersion )
    {
        pluginGroupId = pGroupId;
        pluginArtifactId = pArtifactId;
        pluginVersion = pVersion;
    }

    /**
     * For testing purpose
     */
    public void setPluginArtifact( Artifact pArtifact )
    {
        this.pluginArtifact = pArtifact;
    }

    public void setClassPathSubstitions( List<ClassPathSubstitution> classPathSubstitions )
    {
        this.classPathSubstitutions = classPathSubstitions;
    }

    public void setClassPathProvider( String classPathProvider )
    {
        this.classPathProvider = classPathProvider;
    }

}
