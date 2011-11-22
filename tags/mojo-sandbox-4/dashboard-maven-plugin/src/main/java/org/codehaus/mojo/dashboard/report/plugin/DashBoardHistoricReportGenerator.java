package org.codehaus.mojo.dashboard.report.plugin;

/*
 * Copyright 2007 David Vicente
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.dashboard.report.plugin.beans.CheckstyleReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.CloverReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.CoberturaReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.CpdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMavenProject;
import org.codehaus.mojo.dashboard.report.plugin.beans.FindBugsReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.PmdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.SurefireReportBean;
import org.codehaus.mojo.dashboard.report.plugin.chart.AbstractChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.CheckstyleTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.CloverTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.CoberturaTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.CpdTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.FindBugsTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.PmdTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.SurefireTimeChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.time.TimeChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.configuration.Configuration;
import org.codehaus.mojo.dashboard.report.plugin.configuration.Graph;
import org.codehaus.mojo.dashboard.report.plugin.configuration.Section;
import org.codehaus.mojo.dashboard.report.plugin.hibernate.HibernateService;
import org.hibernate.Query;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardHistoricReportGenerator extends AbstractDashBoardGenerator
{
    /**
     * 
     */
    private DashBoardMavenProject mavenProject;

    private Map map = new Hashtable();

    private HibernateService hibernateService;

    private Long dashBoardMavenProjectID;

    private Configuration configuration;

    public DashBoardHistoricReportGenerator( DashBoardMavenProject mavenProject, HibernateService hibernateService,
                                             Configuration configuration, Log log )
    {
        super( log );
        this.mavenProject = mavenProject;
        this.hibernateService = hibernateService;
        this.configuration = configuration;
        Set reports = mavenProject.getReports();
        Iterator iter = reports.iterator();
        while ( iter.hasNext() )
        {
            IDashBoardReportBean report = (IDashBoardReportBean) iter.next();
            if ( report != null )
            {
                map.put( report.getClass(), report );
            }
        }

        Query query =
            hibernateService.getSession().getNamedQuery(
                                                         "org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMavenProject.getDashBoardMavenProjectID" );
        query.setParameter( "artifactid", mavenProject.getArtifactId() );
        query.setParameter( "groupid", mavenProject.getGroupId() );
        query.setParameter( "version", mavenProject.getVersion() );
        List result = query.list();
        if ( result != null && !result.isEmpty() )
        {
            dashBoardMavenProjectID = (Long) ( result.get( 0 ) );
        }

    }

    public void doGenerateReport( ResourceBundle bundle, Sink sink )
    {

        createTitle( bundle, sink );

        sink.body();
        sink.anchor( "top" );
        sink.anchor_();
        createHeader( bundle, sink );

        if ( map.get( CoberturaReportBean.class ) != null )
        {
            createCoberturaSection( bundle, sink, (CoberturaReportBean) map.get( CoberturaReportBean.class ) );
        }
        if ( map.get( CloverReportBean.class ) != null )
        {
            createCloverSection( bundle, sink, (CloverReportBean) map.get( CloverReportBean.class ) );
        }
        if ( map.get( SurefireReportBean.class ) != null )
        {
            createSurefireSection( bundle, sink, (SurefireReportBean) map.get( SurefireReportBean.class ) );
        }
        if ( map.get( CheckstyleReportBean.class ) != null )
        {
            createCheckStyleSection( bundle, sink, (CheckstyleReportBean) map.get( CheckstyleReportBean.class ) );
        }
        if ( map.get( PmdReportBean.class ) != null )
        {
            createPmdSection( bundle, sink, (PmdReportBean) map.get( PmdReportBean.class ) );
        }
        if ( map.get( CpdReportBean.class ) != null )
        {
            createCpdSection( bundle, sink, (CpdReportBean) map.get( CpdReportBean.class ) );
        }
        if ( map.get( FindBugsReportBean.class ) != null )
        {
            createFindBugsSection( bundle, sink, (FindBugsReportBean) map.get( FindBugsReportBean.class ) );
        }

        sink.body_();

        sink.flush();

        sink.close();
    }

    public void createTitle( ResourceBundle bundle, Sink sink )
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "dashboard.report.name" ) );
        sink.title_();
        sink.head_();
    }

    public void createHeader( ResourceBundle bundle, Sink sink )
    {
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "dashboard.report.name" ) );
        sink.sectionTitle1_();

        sink.text( "Date Generated: " + new SimpleDateFormat().format( new Date( System.currentTimeMillis() ) ) );
        sink.horizontalRule();

        sink.sectionTitle3();
        sink.text( "[" );
        sink.link( "dashboard-report.html" );
        sink.text( "Summary Dashboard" );
        sink.link_();
        sink.text( "]" );
        sink.sectionTitle3_();
        sink.horizontalRule();

        if ( map.get( CoberturaReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#cobertura" );
            sink.text( bundle.getString( "report.cobertura.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( CloverReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#clover" );
            sink.text( bundle.getString( "report.clover.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( SurefireReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#surefire" );
            sink.text( bundle.getString( "report.surefire.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( CheckstyleReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#checkstyle" );
            sink.text( bundle.getString( "report.checkstyle.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( PmdReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#pmd" );
            sink.text( bundle.getString( "report.pmd.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( CpdReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#cpd" );
            sink.text( bundle.getString( "report.cpd.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        /*
         * if ( map.get( JDependReportBean.class ) != null ) { sink.text( "[" ); sink.link( "#jdepend" ); sink.text(
         * bundle.getString( "report.xrefpackage.header" ) ); sink.link_(); sink.text( "]" ); sink.lineBreak(); }
         */

        sink.horizontalRule();
        sink.lineBreak();

        sink.section1_();
    }

    public void createSurefireSection( ResourceBundle bundle, Sink sink, SurefireReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "surefire" );
        sink.anchor_();
        sink.link( "dashboard-report.html#surefire" );
        sink.text( bundle.getString( "report.surefire.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();

        if ( report == null )
        {
            sink.text( "Error: Unable to generate Surefire historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "surefire.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate Surefire historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.SurefireReportBean.getSurefireByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new SurefireTimeChartStrategy( bundle,
                                                                              this.mavenProject.getProjectName()
                                                                                              + " : "
                                                                                              + graph.getTitle(),
                                                                              result, graph.getTimeUnit(),
                                                                              graph.getStartPeriodDate(),
                                                                              graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Surefire." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoSurefireGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoSurefireGraph exception = " + e.getMessage() );
                        }
                    }

                }
            }
        }

    }

    /**
     * Fixes MOJO-813. addition of Clover support. written by <a href="mailto:mbeerman@yahoo.com">Matthew Beermann</a>
     * 
     * @param bundle
     * @param sink
     */
    public void createCloverSection( ResourceBundle bundle, Sink sink, CloverReportBean report )
    {
        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "clover" );
        sink.anchor_();
        sink.link( "dashboard-report.html#clover" );
        sink.text( bundle.getString( "report.clover.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();

        if ( report == null )
        {
            sink.text( "Error: Unable to generate Clover historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "clover.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate Clover historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.CloverReportBean.getCloverByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new CloverTimeChartStrategy( bundle, this.mavenProject.getProjectName()
                                        + " : " + graph.getTitle(), result, graph.getTimeUnit(),
                                                                            graph.getStartPeriodDate(),
                                                                            graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Clover." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoCloverGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoCloverGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }

        sink.lineBreak();

    }

    public void createCoberturaSection( ResourceBundle bundle, Sink sink, CoberturaReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "cobertura" );
        sink.anchor_();
        sink.link( "dashboard-report.html#cobertura" );
        sink.text( bundle.getString( "report.cobertura.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to generate Cobertura historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "cobertura.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate Cobertura historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.CoberturaReportBean.getCoberturaByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new CoberturaTimeChartStrategy( bundle,
                                                                               this.mavenProject.getProjectName()
                                                                                               + " : "
                                                                                               + graph.getTitle(),
                                                                               result, graph.getTimeUnit(),
                                                                               graph.getStartPeriodDate(),
                                                                               graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Cobertura." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoCoberturaGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoCoberturaGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }
        sink.lineBreak();

    }

    public void createPmdSection( ResourceBundle bundle, Sink sink, PmdReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "pmd" );
        sink.anchor_();
        sink.link( "dashboard-report.html#pmd" );
        sink.text( bundle.getString( "report.pmd.header" ) );
        sink.link_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to generate PMD historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "pmd.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate PMD historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.PmdReportBean.getPmdByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new PmdTimeChartStrategy( bundle, this.mavenProject.getProjectName()
                                        + " : " + graph.getTitle(), result, graph.getTimeUnit(),
                                                                         graph.getStartPeriodDate(),
                                                                         graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Pmd." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoPmdGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoPmdGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }
        sink.lineBreak();
    }

    public void createCpdSection( ResourceBundle bundle, Sink sink, CpdReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "cpd" );
        sink.anchor_();
        sink.link( "dashboard-report.html#cpd" );
        sink.text( bundle.getString( "report.cpd.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to generate CPD historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "cpd.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate CPD historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.CpdReportBean.getCpdByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new CpdTimeChartStrategy( bundle, this.mavenProject.getProjectName()
                                        + " : " + graph.getTitle(), result, graph.getTimeUnit(),
                                                                         graph.getStartPeriodDate(),
                                                                         graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Cpd." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoCpdGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoCpdGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }
        sink.lineBreak();
    }

    public void createCheckStyleSection( ResourceBundle bundle, Sink sink, CheckstyleReportBean report )
    {

        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "checkstyle" );
        sink.anchor_();
        sink.link( "dashboard-report.html#checkstyle" );
        sink.text( bundle.getString( "report.checkstyle.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        if ( report == null )
        {
            sink.text( "Error: Unable to generate Checkstyle historic graphs." );
        }
        else
        {
            Section section = this.configuration.getSectionById( "checkstyle.summary" );
            if ( section == null )
            {
                sink.text( "Error: Unable to generate Checkstyle historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.CheckstyleReportBean.getCheckstyleByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new CheckstyleTimeChartStrategy( bundle,
                                                                                this.mavenProject.getProjectName()
                                                                                                + " : "
                                                                                                + graph.getTitle(),
                                                                                result, graph.getTimeUnit(),
                                                                                graph.getStartPeriodDate(),
                                                                                graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_Checkstyle." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoCheckstyleGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoCheckstyleGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }
        sink.lineBreak();

    }

    public void createFindBugsSection( ResourceBundle bundle, Sink sink, FindBugsReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "findbugs" );
        sink.anchor_();
        sink.link( "dashboard-report.html#findbugs" );
        sink.text( bundle.getString( "report.findbugs.header" ) );
        sink.link_();

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        sink.lineBreak();

        Section section = this.configuration.getSectionById( "findbugs.summary" );
        if ( report == null )
        {
            sink.text( "Error: Unable to generate FindBugs historic graphs." );
        }
        else
        {
            if ( section == null )
            {
                sink.text( "Error: Unable to generate FindBugs historic graphs." );
            }
            else
            {
                List graphs = section.getGraphs();

                Iterator iter = graphs.iterator();

                while ( iter.hasNext() )
                {
                    Graph graph = (Graph) iter.next();

                    Query query =
                        hibernateService.getSession().getNamedQuery(
                                                                     "org.codehaus.mojo.dashboard.report.plugin.beans.FindBugsReportBean.getFindBugsByPeriod" );
                    query.setParameter( "id", dashBoardMavenProjectID );
                    query.setParameter( "startdate", graph.getStartPeriodDate() );
                    query.setParameter( "enddate", graph.getEndPeriodDate() );
                    List result = query.list();
                    sink.lineBreak();
                    sink.lineBreak();
                    AbstractChartRenderer chart1 =
                        new TimeChartRenderer( new FindBugsTimeChartStrategy( bundle,
                                                                              this.mavenProject.getProjectName()
                                                                                              + " : "
                                                                                              + graph.getTitle(),
                                                                              result, graph.getTimeUnit(),
                                                                              graph.getStartPeriodDate(),
                                                                              graph.getEndPeriodDate() ) );
                    if ( !chart1.isEmpty() )
                    {
                        String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
                        filename += "_Histo_FindBugs." + chart1.getFileExtension();
                        String prefix = graph.getId();
                        filename = prefix.replace( '.', '_' ) + filename;
                        filename = filename.replace( ' ', '_' );
                        String filenameWithPath = getImagesPath() + "/" + filename;
                        getLog().debug( "createHistoFindBugsGraph = " + filename );
                        try
                        {
                            chart1.saveToFile( filenameWithPath );
                            String link = "images/" + filename;
                            link = link.replace( ' ', '_' );
                            sink.figure();
                            sink.figureGraphics( link );
                            sink.figure_();
                        }
                        catch ( IOException e )
                        {
                            getLog().debug( "createHistoFindBugsGraph exception = " + e.getMessage() );
                        }
                    }
                }
            }
        }
        sink.lineBreak();
    }
}
