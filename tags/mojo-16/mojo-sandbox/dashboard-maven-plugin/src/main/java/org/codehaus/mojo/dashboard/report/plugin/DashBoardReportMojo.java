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
import java.util.Iterator;
import java.util.List;
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
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMavenProject;
import org.codehaus.mojo.dashboard.report.plugin.configuration.Configuration;
import org.codehaus.mojo.dashboard.report.plugin.configuration.ConfigurationService;
import org.codehaus.mojo.dashboard.report.plugin.configuration.ConfigurationServiceException;
import org.codehaus.mojo.dashboard.report.plugin.configuration.IConfigurationService;
import org.codehaus.mojo.dashboard.report.plugin.hibernate.HibernateService;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
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
    
    /**
     * Hibernate Service
     * 
     * @component
     * 
     */
    protected HibernateService hibernateService;

    /**
     * Hibernate dialect
     * 
     * @parameter expression="${dialect}"
     * 
     */
    protected String dialect;

    /**
     * Hibernate driver class
     * 
     * @parameter expression="${driverClass}"
     * 
     */
    protected String driverClass;

    /**
     * Hibernate connection URL
     * 
     * @parameter expression="${connectionUrl}"
     * 
     */
    protected String connectionUrl;

    /**
     * Hibernate database username
     * 
     * @parameter expression="${username}"
     * 
     */
    protected String username;

    /**
     * Hibernate database password
     * 
     * @parameter expression="${password}"
     * 
     */
    protected String password;
    
    /**
     * <p>
     * Specifies the location of the XML configuration to use.
     * </p>
     * 
     * <p>
     * Potential values are a filesystem path, a URL, or a classpath resource.
     * This parameter expects that the contents of the location conform to the
     * xml format (<a
     * href="http://mojo.codehaus.org/dashboard-maven-plugin/">Dashboard
     * Maven plugin</a>) configuration .
     * </p>
     * 
     * <p>
     * This parameter is resolved as resource, URL, then file. If successfully
     * resolved, the contents of the configuration is copied into the
     * <code>${project.build.directory}/default-dashboard-config.xml</code>
     * file before being passed to dashboard as a configuration.
     * </p>
     * 
     * <p>
     * There are 1 predefined config.
     * </p>
     * 
     * <ul>
     * <li><code>config/default-dashboard-config.xml</code>: default config.</li>
     * </ul>
     * 
     * @parameter expression="${configLocation}"
     *            default-value="config/default-dashboard-config.xml"
     */
    private String configLocation;
        
    /**
     * @component
     * @required
     * @readonly
     */
    private ResourceManager locator;
    

    private DashBoardUtils dashBoardUtils;

    private Locale locale;
    
    protected boolean isPropHibernateSet = false;

    /**
     * 
     */
    protected void executeReport( Locale arg0 ) throws MavenReportException
    {
        this.locale = arg0;
        
        //Thanks to the Checkstyle Maven plugin team for this part of code.
        locator.addSearchPath( FileResourceLoader.ID, project.getFile().getParentFile().getAbsolutePath() );
        locator.addSearchPath( "url", "" );
        locator.setOutputDirectory( new File( project.getBuild().getDirectory() ) );
        //Thanks end.
        getLog().info( "MultiReportMojo project = " + project.getName() );
        getLog().info( "MultiReportMojo nb modules = " + project.getModules().size() );
        getLog().info( "MultiReportMojo base directory = " + project.getBasedir() );
        getLog().info( "MultiReportMojo output directory = " + outputDirectory );
        getLog().info( "MultiReportMojo report output directory = " + this.getReportOutputDirectory() );
        getLog().info( "MultiReportMojo project language = " + project.getArtifact().getArtifactHandler().getLanguage() );

        dashBoardUtils = DashBoardUtils.getInstance( getLog(), mavenProjectBuilder, localRepository, false );
        isPropHibernateSet = isDBAvailable();
        if(isPropHibernateSet){
            configureHibernateDriver();
        }
        boolean canGenerate = canGenerateReport();
        if ( canGenerate )
        {
            DashBoardMavenProject mavenProject = null;
            
            if ( isPropHibernateSet )
            {
                configureHibernateDriver();

            }
            Date generatedDate = new Date( System.currentTimeMillis() );

            mavenProject = dashBoardUtils.getDashBoardMavenProject( project, dashboardDataFile, generatedDate );
            dashBoardUtils.saveXMLDashBoardReport( project, mavenProject, dashboardDataFile );
            
            
            if ( mavenProject != null )
            {
                boolean isSummary = false;
                if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty())
                {
                    isSummary = true;
                }
                
                DashBoardReportGenerator reportGenerator =
                    new DashBoardReportGenerator( mavenProject, isSummary, isPropHibernateSet, getLog() );
                reportGenerator.setImagesPath( this.getReportOutputDirectory() + "/images" );
                reportGenerator.setNbExportedPackagesSummary( this.nbExportedPackagesSummary );
                reportGenerator.doGenerateReport( getBundle( this.locale ), (Sink) getSink() );
                if(isPropHibernateSet)
                {
                    //Thanks to the Checkstyle Maven plugin team for this part of code.
                    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                    Configuration dashConfig = null;
                    try
                    {
                        // dashboard will always use the context classloader in order
                        // to load resources (xml schema)
                        ClassLoader dashboardClassLoader = DashBoardUtils.class.getClassLoader();
                        Thread.currentThread().setContextClassLoader( dashboardClassLoader );
                        

                        String configFile = getConfigFile();
                        getLog().info( "getConfigFile() = " + configFile );
                        IConfigurationService configService = new ConfigurationService(configFile);
                        
                        dashConfig = configService.getConfiguration();
                        
                        if(!configService.isValidConfig() ){
                            List warningMsg = configService.getWarningMessages();
                            
                            Iterator iter = warningMsg.iterator();
                            while(iter.hasNext()){
                                getLog().error( (String )iter.next());
                            }
                            throw new MavenReportException("The maven-dashboard-config.xml is not valid. see error messages above or see the maven-dashboard-config.xsd file.");
                        }

                    }
                    catch ( ConfigurationServiceException e )
                    {
                        getLog().error( "DashBoardHistoricReportMojo executeReport() failed.", e );
                        throw new MavenReportException("The maven-dashboard-config.xml is not valid. see error messages above or see the maven-dashboard-config.xsd file.");
                    }
                    catch ( Exception e )
                    {
                        getLog().error( "DashBoardHistoricReportMojo executeReport() failed.", e );
                        throw new MavenReportException("The maven-dashboard-config.xml is not valid. see error messages above or see the maven-dashboard-config.xsd file.");
                    }
                    finally
                    {
                        //be sure to restore original context classloader
                        Thread.currentThread().setContextClassLoader( currentClassLoader );
                    }
                    //Thanks end.
                    try
                    {
                        if ( dashConfig != null )
                        {
                            org.codehaus.doxia.module.xhtml.XhtmlSink sink =
                                getSiteRenderer().createSink( this.getReportOutputDirectory(),
                                                              getOutputName() + "-historic.html",
                                                              this.getReportOutputDirectory().getPath(),
                                                              getSiteDescriptor(), "maven" );
                            DashBoardHistoricReportGenerator histoReportGenerator =
                                new DashBoardHistoricReportGenerator( mavenProject, hibernateService, dashConfig , getLog() );
                            histoReportGenerator.setImagesPath( this.getReportOutputDirectory() + "/images" );
                            histoReportGenerator.doGenerateReport( getBundle( this.locale ), sink );
                        }
                    }
                    catch ( MojoExecutionException e )
                    {
                        getLog().error( "DashBoardHistoricReportMojo executeReport() failed.", e );
                    }
                    catch ( Exception e )
                    {
                        getLog().error( "DashBoardHistoricReportMojo executeReport() failed.", e );
                    }
                }
                if ( project.getModules() != null && project.getModules().size() > 0 )
                {   
                    try
                    {
                        org.codehaus.doxia.module.xhtml.XhtmlSink sink =
                            getSiteRenderer().createSink( this.getReportOutputDirectory(),
                                                          getOutputName() + "-details.html",
                                                          this.getReportOutputDirectory().getPath(), getSiteDescriptor(),
                                                          "maven" );
                        DashBoardMultiReportGenerator detailReportGenerator =
                            new DashBoardMultiReportGenerator( mavenProject , getLog() );
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
            getLog().info( "DashBoardReportMojo: Not recursive into sub-projects - skipping report." );
            return false;
        }
        return true;
    }
    protected void configureHibernateDriver()
    {
        hibernateService.setDialect( dialect );
        hibernateService.setDriverClass( driverClass );
        hibernateService.setConnectionUrl( connectionUrl );
        hibernateService.setUsername( username );
        hibernateService.setPassword( password );
    }
    
    protected boolean isDBAvailable(){
        boolean isDBAvailable = false;
        if ( ( dialect != null && dialect.length() > 0 ) && ( driverClass != null && driverClass.length() > 0 )
                        && ( connectionUrl != null && connectionUrl.length() > 0 )
                        && ( username != null && username.length() > 0 )
                        && ( password != null && password.length() > 0 ) )
        {
            isDBAvailable = true;
        }
        return isDBAvailable;
    }
    /**
     * 
     * @return
     * @throws MavenReportException
     */
    private String getConfigFile() throws MavenReportException
    {
        // Thanks to the Checkstyle Maven plugin team for this part of code.
        try
        {
            getLog().info( "getConfigFile() = " + configLocation );

            File configFile = locator.getResourceAsFile( configLocation, "default-dashboard-config.xml" );

            if ( configFile == null )
            {
                throw new MavenReportException( "Unable to process dashboard config location: " + configLocation );
            }
            return configFile.getAbsolutePath();
        }
        catch ( org.codehaus.plexus.resource.loader.ResourceNotFoundException e )
        {
            throw new MavenReportException(
                                            "Unable to find dashboard configuration file at location " + configLocation,
                                            e );
        }
        catch ( FileResourceCreationException e )
        {
            throw new MavenReportException(
                                            "Unable to process dashboard configuration file location " + configLocation,
                                            e );
        }
        //Thanks end.
    }

}
