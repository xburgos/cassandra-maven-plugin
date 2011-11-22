/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.codehaus.mojo.jardiff;

import java.io.File;
import java.util.Locale;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.osjava.jardiff.DOMDiffHandler;
import org.osjava.jardiff.DiffException;
import org.osjava.jardiff.JarDiff;
import org.osjava.jardiff.SimpleDiffCriteria;

/**
 * @goal jardiff
 * @execute phase="package"
 * */
public final class JardiffReportMojo
    extends AbstractMavenReport
{

    /**
     * @parameter expression="${executedProject}"
     * @required
     * @readonly
     */
    private MavenProject project;


    /**
     * @component
     */
    private SiteRenderer siteRenderer;


    /**
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;


    /**
     * @parameter expression="${project.reporting.outputDirectory}/jardiff"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * Artifact resolver, needed to download source jars.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;
    
    /**
     * Artifact factory, needed to download source jars.
     * 
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;    
    
    /**
     * Location of the local repository.
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository artifactRepository;

    /**
     * @parameter
     */
    private String oldGroupId;

    /**
     * @parameter
     */
    private String oldArtifactId;

    /**
     * @parameter
     */
    private String oldVersion;

    /**
     * @parameter
     */
    private String oldType;

    /**
     * @parameter expression="resource:/org/codehaus/mojo/jardiff/stylesheets/html.xsl"
     */
    private String stylesheet;

    /**
     * List of Remote Repositories used by the resolver
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private java.util.List remoteArtifactRepositories;
    
    public void executeReport( Locale locale )
        throws MavenReportException
    {
        Artifact newArtifact = project.getArtifact();
        
        getLog().info("new artifact " + newArtifact);

        if (oldGroupId == null) {
            oldGroupId = newArtifact.getGroupId();
        }

        if (oldArtifactId == null) {
            oldArtifactId = newArtifact.getArtifactId();
        }

        if (oldVersion == null) {
            oldVersion = newArtifact.getVersion();
        }

        if (oldType == null) {
            oldType = newArtifact.getType();
        }

        Artifact oldArtifact = artifactFactory.createArtifact(
                oldGroupId,
                oldArtifactId,
                oldVersion,
                null,
                oldType
                );

        getLog().info("old artifact " + oldArtifact);

        try
        {
            artifactResolver.resolve( oldArtifact, remoteArtifactRepositories, artifactRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MavenReportException( "Unable to resolve artifact.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MavenReportException( "Unable to find artifact.", e );
        }

        if (newArtifact.getFile() == null) {
            throw new MavenReportException( "Unable to find new artifact file " + newArtifact);
        }

        if (oldArtifact.getFile() == null) {
            throw new MavenReportException( "Unable to find old artifact file " + oldArtifact);
        }
        
        final File output = new File(outputDirectory, getOutputName());
        output.getParentFile().mkdirs();

        getLog().info("creating report at " + output);
        
        final JarDiff diff = new JarDiff();
        
        diff.setOldVersion(oldArtifact.getVersion());
        diff.setNewVersion(newArtifact.getVersion());
        
        StreamSource stylesheetSource = getStylesheet(stylesheet);
        
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
            
            diff.loadOldClasses(oldArtifact.getFile());
            diff.loadNewClasses(newArtifact.getFile());
            
            diff.diff(new DOMDiffHandler(transformer, new StreamResult(output)), new SimpleDiffCriteria());
            //diff.diff(new StreamDiffHandler(new FileOutputStream(output)), new SimpleDiffCriteria());
        } catch (DiffException e) {
            throw new MavenReportException("" ,e);
        } catch (TransformerConfigurationException e) {
            throw new MavenReportException("" ,e);
        }
    }

    private StreamSource getStylesheet( String path )
    {
        getLog().info("stylesheet at " + path);

	if (path.startsWith("resource:")) {
            String resource = path.substring(10);
            getLog().info("resource " + resource);
            return new StreamSource(getClass().getClassLoader().getResourceAsStream(resource));
        }

	if (path.startsWith("file:")) {
            String file = path.substring(6);
            getLog().info("file " + file);
            return new StreamSource(new File(file));
        }        

        getLog().info("file " + path);
        return new StreamSource(new File(path));
    }

    public String getOutputName()
    {
        return "jardiff.html";
    }

    public String getDescription( Locale locale )
    {
        //return getBundle( locale ).getString( "report.jardiff.description" );
        return "jardiffdesc";
    }

//    private static ResourceBundle getBundle( Locale locale )
//    {
//        return ResourceBundle.getBundle( "jardiff-report", locale, JardiffReportMojo.class.getClassLoader() );
//    }

    protected String getOutputDirectory()
    {
        return this.outputDirectory.getAbsoluteFile().toString();
    }

    protected SiteRenderer getSiteRenderer()
    {
        return this.siteRenderer;
    }

    protected MavenProject getProject()
    {
        return this.project;
    }

    public String getName( Locale locale )
    {
        //return getBundle( locale ).getString( "report.jardiff.name" );
        return "Jardiff Report";
    }

    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        executeReport( locale );
    }

    public boolean isExternalReport()
    {
        return true;
    }

    public boolean canGenerateReport()
    {
        final ArtifactHandler artifactHandler = this.project.getArtifact().getArtifactHandler();
        return "java".equals( artifactHandler.getLanguage() );
    }
}
