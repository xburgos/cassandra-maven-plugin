package org.apache.maven.jira;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.settings.Settings;

import org.codehaus.doxia.site.renderer.SiteRenderer;

/*
 * Copyright 2001-2005 The Codehaus.
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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Report on issues from the Issue Tracking System
 * @goal jira-report
 * @description Goal wich downloads issues from Issue Tracking System and generates a report.
 * @author <a href="mailto:jruiz@exist.com">Johnny R. Ruiz III</a>
 * @version $Id: JiraMojo.java 439 2005-09-02 01:07:05Z jruiz $
 */
public class JiraMojo
    extends AbstractMavenReport
{
    /**
     * Output Directory of the report.
     * 
     * @parameter expression="${project.build.directory}/site "
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * Path of the Jira XML to be parsed.
     * 
     * @parameter expression="${project.build.directory}/jira-results.xml "
     * @required
     * @readonly
     */
    private String xmlPath;

    /**
     * Doxia Site Renderer.
     * 
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @required
     * @readonly
     */
    private SiteRenderer siteRenderer;

    /**
     * The Maven Project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Settings XML configuration.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * Maximum number of entries to be displayed by the Jira Report.
     * 
     * @parameter default-value=100
     *
     */
    private int maxEntries;

    /**
     * Defines the filter parameters to restrict the result issues from Jira.
     * The filter parameters property must use the same format of url parameters from the Jira search
     * @parameter default-value=""
     */
    private String filter;

    /**
     * Sets the status(es) of the project you want to limit your report to.
     * Valid statuses are: Open, In Progress, Reopened, Resolved and Closed. Multiple values can be separated by commas.
     * @parameter default-value=""
     */
    private String statusIds;

    /**
     * Sets the resolution(s) of the project you want to limit your report to.
     * Valid statuses are: Unresolved, Fixed, Won't Fix, Duplicate, Incomplete, Cannot Reproduce.
     * Multiple values can be separated by comma.
     * @parameter default-value=""
     */
    private String resolutionIds;

    /**
     * Sets the priority(s) of the project you want to limit your report to.
     * Valid statuses are: Blocker, Critical, Major, Minor, Trivial. Multiple values can be separated by commas.
     * @parameter default-value=""
     */
    private String priorityIds;

    /**
     * Sets the component(s) of the project you want to limit your report to.
     * Multiple components can be separated by commas (such as 10011,10012).
     * Default-value -  empty, meaning all components.
     * @parameter default-value=""
     */
    private String component;

    /**
     * Defines the jira username for authentication into a private Jira instalation.
     * @parameter default-value=""
     */
    private String jiraUser;

    /**
     * Defines the jira password for authentication into a private Jira instalation.
     * @parameter default-value=""
     */
    private String jiraPassword;

    /**
     * Defines the http user for basic authentication into the Jira webserver.
     * @parameter default-value=""
     */
    private String webUser;

    /**
     * Defines the http password for basic authentication into the Jira webserver.
     * @parameter default-value=""
     */
    private String webPassword;

    public void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( validateIfIssueManagementComplete() )
        {
            JiraDownloader2 jira = new JiraDownloader2();

            setJiraDownloaderParameter( jira );

            JiraReportGenerator report;

            try
            {
                jira.doExecute();

                report = new JiraReportGenerator( xmlPath );

                report.doGenerateReport( getBundle( locale ), getSink() );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        else
        {
            JiraReportGenerator report = new JiraReportGenerator();

            report.doGenerateEmptyReport( getBundle( locale ), getSink() );
        }
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.jira.name" );
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.jira.description" );
    }

    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    public String getOutputName()
    {
        return "jira-report";
    }

    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "jira-report", locale, this.getClass().getClassLoader() );
    }

    private void setJiraDownloaderParameter( JiraDownloader2 jira )
    {
        jira.setLog( getLog() );

        jira.setMavenProject( project );

        jira.setOutput( new File( xmlPath ) );

        jira.setNbEntries( maxEntries );

        jira.setComponent( component );

        jira.setStatusIds( statusIds );

        jira.setResolutionIds( resolutionIds );

        jira.setPriorityIds( priorityIds );

        jira.setFilter( filter );

        jira.setJiraUser( jiraUser );

        jira.setJiraPassword( jiraPassword );

        jira.setWebUser( webUser );

        jira.setWebPassword( webPassword );
        
        jira.setSettings(settings);
    }

    private boolean validateIfIssueManagementComplete()
    {
        if ( ( (MavenProject) project ).getIssueManagement() == null )
        {
            getLog().error( "No Issue Management set. Jira Report will not be generated." );

            return false;
        }
        else if ( ( (MavenProject) project ).getIssueManagement().getUrl() == null )
        {
            getLog().error( "No URL set in Issue Management. Jira Report will not be generated." );

            return false;
        }
        else if ( ( ( (MavenProject) project ).getIssueManagement().getSystem() != null ) 
            && !( ( (MavenProject) project ).getIssueManagement().getSystem().equalsIgnoreCase( "jira" ) ) )
        {
            getLog().error( "Jira Report only supports JIRA.  Jira Report will not be generated." );

            return false;
        }
        return true;
    }
}
