package org.codehaus.mojo.surefire;

import java.io.File;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @description  Creates a nicely formatted Surefire Test Report in html format
 * @goal         report
 * @execute      phase="test"
 * @author       <a href="mailto:jruiz@exist.com">Johnny R. Ruiz III</a>
 * @version      $Id: SurefireReportMojo.java 439 2005-09-02 01:07:05Z jruiz $
 */
public class SurefireReportMojo
    extends AbstractMavenReport
{
    /**
     * Location where generated html will be created.
     *
     * @parameter expression="${project.build.directory}/site "
     *
     */
    private String outputDirectory;

    /**
     * Doxia Site Renderer
     *
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @required @readonly
     */
    private SiteRenderer siteRenderer;

    /**
     * Maven Project
     *
     * @parameter expression="${project}"
     * @required @readonly
     */
    private MavenProject project;
    
    /**
     * This directory contains the XML Report files that must be parsed and rendered to HTML format.
     *
     * @parameter expression="${project.build.directory}/surefire-reports"
     * @required
     */
    private File reportsDirectory;

    public void executeReport( Locale locale )
                       throws MavenReportException
    {
        SurefireReportGenerator report = new SurefireReportGenerator( reportsDirectory );

        try
        {
            report.doGenerateReport( getBundle( locale ),
                                     getSink(  ) );
        } catch ( Exception e )
        {
            e.printStackTrace(  );
        }
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.surefire.name" );
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.surefire.description" );
    }

    protected SiteRenderer getSiteRenderer(  )
    {
        return siteRenderer;
    }

    protected MavenProject getProject(  )
    {
        return project;
    }

    public String getOutputName(  )
    {
        return "surefire-report";
    }

    protected String getOutputDirectory(  )
    {
        return outputDirectory;
    }

    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "surefire-report",
                                         locale,
                                         this.getClass(  ).getClassLoader(  ) );
    }
}
