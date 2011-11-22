package org.codehaus.mojo.dashboard.report.plugin;

/*
 * Copyright 2006 David Vicente
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMultiReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringUtils;

/**
 * A Dashboard report which aggregates all other report results.
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * @goal dashboard
 * 
 */

public class DashBoardReportMojo extends AbstractMavenReport
{
    /**
     * The maven project
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * Directory containing The generated DashBoard report Datafile "dashboard-report.xml".
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Site Renderer
     * 
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @readonly
     */
    private SiteRenderer siteRenderer;

    /**
     * <p>
     * The generated DashBoard report Datafile.
     * </p>
     * 
     * @parameter expression="dashboard-report.xml"
     * @readonly
     */
    protected String dashboardDataFile;

    /**
     * The filename to use for the report.
     * 
     * @parameter expression="dashboard-report"
     * @readonly
     */
    private String outputName;

    /**
     * The local repository.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * number of XRef JDepend/Cobertura packages to export in dashboard summary page
     * 
     * @parameter default-value="10"
     */
    private int nbExportedPackagesSummary;

    /**
     * Project builder
     * 
     * @component
     */
    protected MavenProjectBuilder mavenProjectBuilder;

    private DashBoardUtils dashBoardUtils;

    private Date generatedDate;

    private Locale locale;

    /**
     * 
     */
    protected void executeReport( Locale arg0 ) throws MavenReportException
    {
        this.locale = arg0;

        getLog().info( "MultiReportMojo project = " + project.getName() );
        getLog().info( "MultiReportMojo nb modules = " + project.getModules().size() );
        getLog().info( "MultiReportMojo base directory = " + project.getBasedir() );
        getLog().info( "MultiReportMojo output directory = " + outputDirectory );
        getLog().info( "MultiReportMojo report output directory = " + this.getReportOutputDirectory() );
        getLog().info( "MultiReportMojo project language = " + project.getArtifact().getArtifactHandler().getLanguage() );

        dashBoardUtils = DashBoardUtils.getInstance( getLog(), mavenProjectBuilder, localRepository );
        generatedDate = new Date( System.currentTimeMillis() );

        boolean canGenerate = canGenerateReport();
        if ( canGenerate )
        {
            IDashBoardReportBean dashBoardReport =
                dashBoardUtils.getDashBoardReportBean( project, dashboardDataFile, generatedDate );
            dashBoardUtils.saveXMLDashBoardReport( project, dashBoardReport, dashboardDataFile );

            if ( project.getModules().size() > 0 )
            {
                DashBoardReportGenerator reportGenerator =
                    new DashBoardReportGenerator(
                                                  (DashBoardReportBean) ( (DashBoardMultiReportBean) dashBoardReport ).getSummary(),
                                                  true );
                reportGenerator.setImagesPath( this.getReportOutputDirectory() + "/images" );
                reportGenerator.setNbExportedPackagesSummary( this.nbExportedPackagesSummary );
                reportGenerator.doGenerateReport( getBundle( this.locale ), (Sink) getSink() );

                try
                {
                    org.codehaus.doxia.module.xhtml.XhtmlSink sink =
                        getSiteRenderer().createSink( this.getReportOutputDirectory(),
                                                      getOutputName() + "-details.html",
                                                      this.getReportOutputDirectory().getPath(), getSiteDescriptor(),
                                                      "maven" );
                    DashBoardMultiReportGenerator detailReportGenerator =
                        new DashBoardMultiReportGenerator( (DashBoardMultiReportBean) dashBoardReport );
                    detailReportGenerator.setImagesPath( this.getReportOutputDirectory() + "/images" );
                    detailReportGenerator.doGenerateReport( getBundle( this.locale ), sink );
                }
                catch ( MojoExecutionException e )
                {
                    getLog().error( "DashBoardReportMojo executeReport() failed.", e );
                }
                catch ( Exception e )
                {
                    getLog().error( "DashBoardReportMojo executeReport() failed.", e );
                }

            }
            else
            {

                DashBoardReportGenerator reportGenerator =
                    new DashBoardReportGenerator( (DashBoardReportBean) dashBoardReport, false );
                reportGenerator.setImagesPath( this.getReportOutputDirectory() + "/images" );
                reportGenerator.doGenerateReport( getBundle( this.locale ), (Sink) getSink() );
            }
        }

    }
    /**
     * 
     */
    protected String getOutputDirectory()
    {
        return outputDirectory.getPath();
    }
    /**
     * 
     */
    protected MavenProject getProject()
    {
        return project;
    }

    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    public String getDescription( Locale locale )
    {
        String description = "";
        if ( project.getModules().size() > 0 )
        {
            description = getBundle( locale ).getString( "dashboard.multireport.description" );
        }
        else
        {
            description = getBundle( locale ).getString( "dashboard.report.description" );
        }
        return description;
    }

    public String getName( Locale locale )
    {
        String name = "";
        if ( project.getModules().size() > 0 )
        {
            name = getBundle( locale ).getString( "dashboard.multireport.name" );
        }
        else
        {
            name = getBundle( locale ).getString( "dashboard.report.name" );
        }
        return name;
    }

    public String getOutputName()
    {
        return outputName;
    }

    public ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "dashboard-report-plugin", locale, this.getClass().getClassLoader() );
    }

    public boolean usePageLinkBar()
    {
        return true;
    }

    private InputStream getSiteDescriptor() throws MojoExecutionException
    {
        String siteDescriptorContent = "";
        try
        {
            siteDescriptorContent = IOUtil.toString( getClass().getResourceAsStream( "/default-report.xml" ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "The site descriptor cannot be read!", e );
        }
        Map props = new HashMap();
        props.put( "reports", getReportsMenu() );
        if ( getProject().getName() != null )
        {
            props.put( "project.name", getProject().getName() );
        }
        else
        {
            props.put( "project.name", "NO_PROJECT_NAME_SET" );
        }
        if ( getProject().getUrl() != null )
        {
            props.put( "project.url", getProject().getUrl() );
        }
        else
        {
            props.put( "project.url", "NO_PROJECT_URL_SET" );
        }
        siteDescriptorContent = StringUtils.interpolate( siteDescriptorContent, props );
        return new StringInputStream( siteDescriptorContent );
    }

    private String getReportsMenu()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "<menu name=\"Project Reports\">\n" );
        buffer.append( "  <item name=\"Root\" href=\"/project-reports.html\"/>\n" );
        buffer.append( "  <item name=\"" + getName( this.locale ) + "\" href=\"/" + getOutputName() + ".html\"/>\n" );
        buffer.append( "</menu>\n" );
        return buffer.toString();
    }

    public boolean canGenerateReport()
    {
        if ( project.getCollectedProjects().size() < project.getModules().size() )
        {
            getLog().info( "DashBoardReportMojo: Not recusrsive into sub-projects - skipping report." );
            return false;
        }
        return true;
    }

}
