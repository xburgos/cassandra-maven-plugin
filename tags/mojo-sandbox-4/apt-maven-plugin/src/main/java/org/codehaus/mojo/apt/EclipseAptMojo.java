package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generate: ./settings/org.eclipse.jdt.apt.core.prefs and .factorypath.xml
 * @author <a href="mailto:jubu@codehaus.org">Juraj Burian</a>
 * @version $Id:$
 * 
 * @goal eclipse
 * @requiresDependencyResolution compile
 * @description create ./settings/org.eclipse.jdt.apt.core.prefs and .factorypath.xml
 */
public class EclipseAptMojo extends AptMojo
{
    /**
     * The directory to run the APT.
     * 
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    protected File basedir;

    /**
     * run container processors in batch mode
     * 
     * @parameter default-value="false"
     */
    protected boolean batchMode;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    public void execute() throws MojoExecutionException
    {
        // exclude this :
        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        if ( !"java".equals( artifactHandler.getLanguage() ) )
        {
            getLog().info( "Not executing apt eclipse goal as the project is not a Java classpath-capable package" );
            return;
        }
        if ( !isAptDefined() )
        {
            getLog().info( "Not executing apt eclipse goal, plugin is not configuret for this project." );
            return;
        }
        getLog().info( "Executing apt eclipse goal!" );

        // write prefs file
        File prefs = new File( basedir, ".settings" + FILE_SEPARATOR + "org.eclipse.jdt.apt.core.prefs" );
        try
        {
            prefs.getParentFile().mkdirs();
            prefs.createNewFile();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Can't create file: " + prefs.getPath() );
        }
        PrintWriter out = null;
        try
        {
            out = new PrintWriter( prefs );
        }
        catch ( FileNotFoundException e )
        {
            // can't happen
        }
        out.println( "#" + new Date() );
        out.println( "eclipse.preferences.version=1" );
        out.println( "org.eclipse.jdt.apt.aptEnabled=true" );
        out.println( "org.eclipse.jdt.apt.genSrcDir=" + getGeneratedFinalDir() );
        // write processor options
        if ( getOptions() != null )
        {
            for ( int i = 0; i < getOptions().length; i++ )
            {
                out.println( "org.eclipse.jdt.apt.processorOptions/" + getOptions()[i] );
            }
        }
        out.close();

        // write .factorypath
        File factorypathFile = new File( basedir, ".factorypath" );

        try
        {
            prefs.createNewFile();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Can't create file: " + factorypathFile.getPath() );
        }
        try
        {
            out = new PrintWriter( factorypathFile );
        }
        catch ( FileNotFoundException e )
        {
            // can't happen
        }

        String localRepo = null;
        try
        {
            localRepo = new File( localRepository.getBasedir() ).getCanonicalPath();
        }
        catch ( IOException e )
        {

            throw new MojoExecutionException( "Local repository: " + localRepository.getBasedir() + " doesn't exists!" );
        }

        out.println( "<factorypath> " );

        for ( Iterator it = getClasspathElements().iterator(); it.hasNext(); )
        {
            String factorypathentry = (String) it.next();

            // EXTJAR VARJAR
            String kind = "EXTJAR";

            // force skip tools jar
            if ( factorypathentry.endsWith( "tools.jar" ) )
            {
                continue;
            }

            try
            {
                String tmp = new File( factorypathentry ).getCanonicalPath();
                if ( tmp.startsWith( localRepo ) )
                {
                    kind = "VARJAR";
                    factorypathentry = tmp.replace( localRepo, "" );
                    factorypathentry = "M2_REPO" + factorypathentry.replace( "\\", "/" );
                }
            }
            catch ( IOException e )
            {
                // ignore this
            }

            String batchModeString = hasAnnotationProcessorFactory( factorypathentry ) ? "true" : "false";

            out.println( "    <factorypathentry kind=\"" + kind + "\" id=\"" + factorypathentry
                            + " \" enabled=\"true\" runInBatchMode=\"" + batchModeString + "\"/>" );
        }

        out.println( "</factorypath> " );
        out.close();
    }

    private boolean isAptDefined()
    {
        for ( Iterator it = project.getPluginArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();
            if ( "apt-maven-plugin".equals( artifact.getArtifactId() ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotationProcessorFactory( String factorypathentry )
    {
        try
        {
            if ( factorypathentry.endsWith( "jar" ) )
            {
                JarFile jf;
                jf = new JarFile( factorypathentry );
                if ( jf.getEntry( "META-INF/services/com.sun.mirror.apt.AnnotationProcessorFactory" ) != null )
                {
                    return true;
                }
            }
        }
        catch ( IOException e )
        {
            // ignore this
        }
        return false;
    }

    protected String getGeneratedFinalDir()
    {
        // return only relative part of generated dir
        // and replace \ -> /
        return super.getGeneratedFinalDir().replace( project.getBasedir().getAbsolutePath(), "" ).replace( "\\", "/" );
    }

}
