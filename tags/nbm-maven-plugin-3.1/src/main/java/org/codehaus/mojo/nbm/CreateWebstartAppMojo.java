/*
 *  Copyright 2008 Johan AndrÃ©n.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.codehaus.mojo.nbm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.jar.JarFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.GenerateKey;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.AndSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.selectors.OrSelector;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.netbeans.nbbuild.MakeJNLP;
import org.netbeans.nbbuild.ModuleSelector;

/**
 * Create webstartable binaries for a 'nbm-application'.
 * @author <a href="mailto:johan.andren@databyran.se">Johan Andren</a>
 * @author <a href="mailto:mkleint@codehaus.org">Milos Kleint</a>
 * @goal webstart-app
 * @phase packaging
 * @since 3.0
 */
public class CreateWebstartAppMojo
    extends AbstractMojo
{

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * The branding token for the application based on NetBeans platform.
     * @parameter expression="${netbeans.branding.token}"
     * @required
     */
    protected String brandingToken;

    /**
     * output directory where the the netbeans application will be created.
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Distributable zip file of NetBeans platform application
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}-webstart.zip"
     * @required
     */
    private File destinationFile;

    /**
     * Artifact Classifier to use for the webstart distributable zip file.
     *
     * @parameter expression="${nbm.webstart.classifier}" default-value="webstart"
     * @since 3.1
     */
    private String webstartClassifier;

    /**
     * Jnlp codebase value within *.jnlp files.
     * @parameter expression="${nbm.webstart.codebase}" default-value="$$codebase"
     * 
     */
    private String codebase;

    /**
     * A custom master JNLP file. If not defined, the 
     * <a href="http://mojo.codehaus.org/nbm-maven-plugin/masterjnlp.txt">default one</a> is used.
     * The following expressions can be used within the file and will
     * be replaced when generating content.
     * <ul>
     * <li>${jnlp.resources}</li>
     * <li>${jnlp.codebase} - the 'codebase' parameter value is passed in.</li>
     * <li>${app.name}</li>
     * <li>${app.icon}</li>
     * <li>${app.title}</li>
     * <li>${app.vendor}</li>
     * <li>${app.description}</li>
     * <li>${branding.token} - the 'brandingToken' parameter value is passed in.</li>
     * <li>${netbeans.jnlp.fixPolicy}</li>
     * </ul>
     * @parameter 
     */
    private File masterJnlpFile;

    /**
     * keystore location for signing the nbm file
     * @parameter expression="${keystore}"
     */
    private String keystore;

    /**
     * keystore password
     * @parameter expression="${keystorepass}"
     */
    private String keystorepassword;

    /**
     * keystore alias
     * @parameter expression="${keystorealias}"
     */
    private String keystorealias;

    /**
     * If set true, build-jnlp target creates versioning info in jnlp descriptors and version.xml files.
     * This allows for incremental updates of Webstart applications, but requires download via
     * JnlpDownloadServlet
     * Defaults to false, which means versioning
     * info is not generated (see
     * http://java.sun.com/j2se/1.5.0/docs/guide/javaws/developersguide/downloadservletguide.html#resources).
     *
     * @parameter expression="${nbm.webstart.versions}" default-value="false"
     */
    private boolean processJarVersions;

    /**
     * 
     * @throws org.apache.maven.plugin.MojoExecutionException 
     * @throws org.apache.maven.plugin.MojoFailureException 
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ("$codebase".equals(codebase)) {
            //MNBMODULE-47 maven core seems to strip one of the $ signs
            codebase = "$$codebase";
        }
        if ( !"nbm-application".equals( project.getPackaging() ) )
        {
            throw new MojoExecutionException(
                "This goal only makes sense on project with nbm-application packaging." );
        }
        Project antProject = new Project();
        antProject.init();

        if ( keystore != null && keystorealias != null && keystorepassword != null )
        {
            File ks = new File( keystore );
            if ( !ks.exists() )
            {
                throw new MojoFailureException(
                    "Cannot find keystore file at " + ks.getAbsolutePath() );
            }
            else
            {
                //proceed..
            }
        }
        else if ( keystore != null || keystorepassword != null || keystorealias != null )
        {
            throw new MojoFailureException(
                "If you want to sign the jnlp application, you need to define all three keystore related parameters." );
        }
        else
        {
            getLog().warn( "Keystore related parameters not set, generating a default keystore." );
            GenerateKey genTask = (GenerateKey) antProject.createTask( "genkey" );
            genTask.setAlias( "jnlp" );
            genTask.setStorepass( "netbeans" );
            genTask.setDname( "CN=" + System.getProperty( "user.name" ) );
            genTask.setKeystore( new File( outputDirectory, "generated.keystore" ).getAbsolutePath() );
            genTask.execute();
            keystore = new File( outputDirectory, "generated.keystore" ).getAbsolutePath();
            keystorepassword = "netbeans";
            keystorealias = "jnlp";
        }

        Taskdef taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.MakeJNLP" );
        taskdef.setName( "makejnlp" );
        taskdef.execute();

        taskdef = (Taskdef) antProject.createTask( "taskdef" );
        taskdef.setClassname( "org.netbeans.nbbuild.VerifyJNLP" );
        taskdef.setName( "verifyjnlp" );
        taskdef.execute();


        try
        {
            File webstartBuildDir = new File(
                outputDirectory + File.separator + "webstart" + File.separator + brandingToken );
            if ( webstartBuildDir.exists() )
            {
                FileUtils.deleteDirectory( webstartBuildDir );
            }
            webstartBuildDir.mkdirs();
            getLog().info( "Generating webstartable binaries at " + webstartBuildDir.getAbsolutePath() );

            File nbmBuildDirFile = new File( outputDirectory, brandingToken );

//            FileUtils.copyDirectoryStructureIfModified( nbmBuildDirFile, webstartBuildDir );

            MakeJNLP jnlpTask = (MakeJNLP) antProject.createTask( "makejnlp" );
            jnlpTask.setDir( webstartBuildDir );
            jnlpTask.setCodebase( codebase );
            //TODO, how to figure verify excludes..
            jnlpTask.setVerify( false );
            jnlpTask.setPermissions( "<security><all-permissions/></security>" );
            jnlpTask.setSignJars( true );

            jnlpTask.setAlias( keystorealias );
            jnlpTask.setKeystore( keystore );
            jnlpTask.setStorePass( keystorepassword );
            jnlpTask.setProcessJarVersions( processJarVersions );

            FileSet fs = jnlpTask.createModules();
            fs.setDir( nbmBuildDirFile );
            OrSelector or = new OrSelector();
            AndSelector and = new AndSelector();
            FilenameSelector inc = new FilenameSelector();
            inc.setName( "*/modules/**/*.jar" );
            or.addFilename( inc );
            inc = new FilenameSelector();
            inc.setName( "*/lib/**/*.jar" );
            or.addFilename( inc );
            inc = new FilenameSelector();
            inc.setName( "*/core/**/*.jar" );
            or.addFilename( inc );

            ModuleSelector ms = new ModuleSelector();
            Parameter included = new Parameter();
            included.setName( "includeClusters" );
            included.setValue( "" );
            Parameter excluded = new Parameter();
            excluded.setName( "excludeClusters" );
            excluded.setValue( "" );
            Parameter exModules = new Parameter();
            exModules.setName( "excludeModules" );
            exModules.setValue( "" );
            ms.setParameters( new Parameter[]
                {
                    included,
                    excluded,
                    exModules
                } );
            and.add( or );
            and.add( ms );
            fs.addAnd( and );
            jnlpTask.execute();

            //TODO is it really netbeans/
            String extSnippet = generateExtensions( fs, antProject, "" ); // "netbeans/"

            Properties props = new Properties();
            props.setProperty( "jnlp.resources", extSnippet );
            props.setProperty( "jnlp.codebase", codebase );
            props.setProperty( "app.name", brandingToken );
            props.setProperty( "app.icon", "master.png" );
            props.setProperty( "app.title", project.getName() );
            if ( project.getOrganization() != null )
            {
                props.setProperty( "app.vendor",
                    project.getOrganization().getName() );
            }
            else
            {
                props.setProperty( "app.vendor", "Nobody" );
            }
            String description = project.getDescription() != null ? project.getDescription() : "No Project Description";
            props.setProperty( "app.description", description );
            props.setProperty( "branding.token", brandingToken );
            props.setProperty( "netbeans.jnlp.fixPolicy", "true" );
            File masterJnlp = new File(
                webstartBuildDir.getAbsolutePath() + File.separator + "master.jnlp" );
            filterCopy( masterJnlpFile, "/master.jnlp", masterJnlp, props );


            File startup = copyLauncher( outputDirectory, nbmBuildDirFile );
            File jnlpDestination = new File(
                webstartBuildDir.getAbsolutePath() + File.separator + "startup.jar" );

            SignJar signTask = (SignJar) antProject.createTask( "signjar" );
            signTask.setKeystore( keystore );
            signTask.setStorepass( keystorepassword );
            signTask.setAlias( keystorealias );
            signTask.setSignedjar( jnlpDestination );
            signTask.setJar( startup );
            signTask.execute();

            //branding
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir( nbmBuildDirFile );
            ds.setIncludes( new String[]
                {
                    "**/locale/*.jar"
                } );
            ds.scan();
            String[] includes = ds.getIncludedFiles();
            StringBuffer brandRefs = new StringBuffer();
            if ( includes != null && includes.length > 0 )
            {
                File brandingDir = new File( webstartBuildDir, "branding" );
                brandingDir.mkdirs();
                for ( String incBran : includes )
                {
                    File source = new File( nbmBuildDirFile, incBran );
                    File dest = new File( brandingDir, source.getName() );
                    FileUtils.copyFile( source, dest );
                    brandRefs.append( "    <jar href=\'branding/" + dest.getName() + "\'/>\n" );
                }
            //signing of branding items doens't work for some reason.
            // -> branding.jnlp with <security/>
//                signTask = (SignJar)antProject.createTask("signjar");
//                signTask.setKeystore(keystore);
//                signTask.setStorepass(keystorepassword);
//                signTask.setAlias(keystorealias);
//                FileSet set = new FileSet();
//                set.setDir(brandingDir);
//                set.setIncludes("*.jar");
//                signTask.addFileset(fs);
//                signTask.execute();
            }

            File brandingJnlp = new File(
                webstartBuildDir.getAbsolutePath() + File.separator + "branding.jnlp" );
            props.setProperty( "jnlp.branding.jars", brandRefs.toString() );
            filterCopy( null, "/branding.jnlp", brandingJnlp, props );

// somehow expects a give folder/file format that we don't have..            
//            getLog().info( "Verifying generated webstartable content." );
//            VerifyJNLP verifyTask = (VerifyJNLP) antProject.createTask( "verifyjnlp" );
//            FileSet verify = new FileSet();
//            verify.setFile( masterJnlp );
//            verifyTask.addConfiguredFileset( verify );
//            verifyTask.execute();


            // create zip archive
            if ( destinationFile.exists() )
            {
                destinationFile.delete();
            }
            ZipArchiver archiver = new ZipArchiver();
            archiver.addDirectory( webstartBuildDir );
            archiver.setDestFile( destinationFile );
            archiver.createArchive();

            // attach standalone so that it gets installed/deployed
            projectHelper.attachArtifact( project, "zip", webstartClassifier,
                destinationFile );

        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( "Error creating webstartable binary.", ex );
        }


    }

    /**
     * @param standaloneBuildDir
     * @return The name of the jnlp-launcher jarfile in the build directory
     */
    private File copyLauncher( File standaloneBuildDir, File builtInstallation ) throws IOException
    {
        File jnlpStarter = new File( builtInstallation.getAbsolutePath() +
            File.separator + "harness" +
            File.separator + "jnlp" +
            File.separator + "jnlp-launcher.jar" );
        // buffer so it isn't reading a byte at a time!
        InputStream source = null;
        FileOutputStream outstream = null;
        try
        {
            if ( !jnlpStarter.exists() )
            {
                source = getClass().getClassLoader().getResourceAsStream(
                    "webstart/jnlp-launcher.jar" );
            }
            else
            {
                source = new FileInputStream( jnlpStarter );
            }
            File jnlpDestination = new File(
                standaloneBuildDir.getAbsolutePath() + File.separator + "jnlp-launcher.jar" );

            outstream = new FileOutputStream( jnlpDestination );
            IOUtil.copy( source, outstream );
            return jnlpDestination;
        }
        finally
        {
            IOUtil.close( source );
            IOUtil.close( outstream );
        }
    }

    private void filterCopy( File sourceFile, String resourcePath, File destinationFile, Properties filterProperties )
        throws IOException
    {
        // buffer so it isn't reading a byte at a time!
        Reader source = null;
        Writer destination = null;
        try
        {
            InputStream instream;
            if ( sourceFile != null )
            {
                instream = new FileInputStream( sourceFile );
            }
            else
            {
                instream = getClass().getClassLoader().getResourceAsStream(
                    resourcePath );
            }
            FileOutputStream outstream = new FileOutputStream( destinationFile );

            source = new BufferedReader( new InputStreamReader( instream,
                "UTF-8" ) );
            destination = new OutputStreamWriter( outstream, "UTF-8" );

            // support ${token}
            Reader reader = new InterpolationFilterReader( source,
                filterProperties, "${", "}" );

            IOUtil.copy( reader, destination );
        }
        finally
        {
            IOUtil.close( source );
            IOUtil.close( destination );
        }
    }

    /**
     * copied from MakeMasterJNLP ant task.
     * @param files
     * @param antProject
     * @param masterPrefix
     * @return
     * @throws java.io.IOException
     */
    private String generateExtensions( FileSet files, Project antProject, String masterPrefix ) throws IOException
    {
        StringBuffer buff = new StringBuffer();
        for ( String nm : files.getDirectoryScanner( antProject ).getIncludedFiles() )
        {
            File jar = new File( files.getDir( antProject ), nm );

            if ( !jar.canRead() )
            {
                throw new IOException( "Cannot read file: " + jar );
            }

            JarFile theJar = new JarFile( jar );
            String codenamebase = theJar.getManifest().getMainAttributes().getValue( "OpenIDE-Module" );
            if ( codenamebase == null )
            {
                throw new IOException( "Not a NetBeans Module: " + jar );
            }
            {
                int slash = codenamebase.indexOf( '/' );
                if ( slash >= 0 )
                {
                    codenamebase = codenamebase.substring( 0, slash );
                }
            }
            String dashcnb = codenamebase.replace( '.', '-' );

            buff.append( "    <extension name='" + codenamebase + "' href='" + masterPrefix + dashcnb + ".jnlp' />\n" );
            theJar.close();
        }
        return buff.toString();

    }
}
