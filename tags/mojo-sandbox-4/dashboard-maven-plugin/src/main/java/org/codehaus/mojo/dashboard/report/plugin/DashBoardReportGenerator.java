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
import org.codehaus.mojo.dashboard.report.plugin.beans.JDependReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.PmdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.SurefireReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.XRefPackageBean;
import org.codehaus.mojo.dashboard.report.plugin.chart.AbstractChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.BarChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.CheckstyleErrorsPieChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CheckstylePieChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CloverBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CoberturaBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.FindbugsCategoriesPieChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.FindbugsPrioritiesPieChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.PieChart3DRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.PieChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.StackedBarChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.SurefirePieChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.utils.ChartUtils;

/**
 * <ul>
 * <li>Add "CheckStyle Violations" graph written by <a href="mailto:srivollet@objectif-informatique.fr">Sylvain
 * Rivollet</a></li>
 * <li>Add Clover support written by <a href="mailto:mbeerman@yahoo.com">Matthew Beermann</a></li>
 * </ul>
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardReportGenerator extends AbstractDashBoardGenerator
{
    /**
     * 
     */
    private static final int SPECIFIC_WIDTH = 600;

    /**
     * 
     */
    private DashBoardMavenProject mavenProject;

    private Map map = new Hashtable();

    private boolean isDBAvailable = false;

    /**
     * 
     */
    private boolean summary = false;

    public DashBoardReportGenerator( DashBoardMavenProject mavenProject, boolean summary, boolean isDBAvailable, Log log )
    {
        super( log );
        this.mavenProject = mavenProject;
        this.summary = summary;
        this.isDBAvailable = isDBAvailable;
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
        if ( map.get( JDependReportBean.class ) != null )
        {
            createJDependSection( bundle, sink, (JDependReportBean) map.get( JDependReportBean.class ) );
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

        if ( this.summary )
        {
            sink.sectionTitle3();
            sink.text( "[" );
            sink.link( "dashboard-report-details.html" );
            sink.text( "Detailed Dashboard" );
            sink.link_();
            sink.text( "]" );
            sink.sectionTitle3_();
            sink.horizontalRule();
        }

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
        if ( map.get( FindBugsReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#findbugs" );
            sink.text( bundle.getString( "report.findbugs.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }
        if ( map.get( JDependReportBean.class ) != null )
        {
            sink.text( "[" );
            sink.link( "#jdepend" );
            sink.text( bundle.getString( "report.xrefpackage.header" ) );
            sink.link_();
            sink.text( "]" );
            sink.lineBreak();
        }

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
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#surefire" );
            sink.text( bundle.getString( "report.surefire.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./surefire-report.html" );
            sink.text( bundle.getString( "report.surefire.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "surefire" );
        }
        sink.section1_();
        sink.lineBreak();
        sink.table();
        sink.tableRow();

        sinkHeader( sink, bundle.getString( "report.surefire.label.tests" ) );

        sinkHeader( sink, bundle.getString( "report.surefire.label.errors" ) );

        sinkHeader( sink, bundle.getString( "report.surefire.label.failures" ) );

        sinkHeader( sink, bundle.getString( "report.surefire.label.skipped" ) );

        sinkHeader( sink, bundle.getString( "report.surefire.label.successrate" ) );

        sinkHeader( sink, bundle.getString( "report.surefire.label.time" ) );

        sink.tableRow_();

        if ( report != null )
        {
            // List testSuites = report.parseXMLReportFiles();
            // Map summary = report.getSummary( testSuites );
            sink.tableRow();

            sinkCell( sink, Integer.toString( report.getNbTests() ) );

            sinkCell( sink, Integer.toString( report.getNbErrors() ) );

            sinkCell( sink, Integer.toString( report.getNbFailures() ) );

            sinkCell( sink, Integer.toString( report.getNbSkipped() ) );

            sinkCell( sink, Double.toString( report.getSucessRate() ) + "%" );

            sinkCell( sink, Double.toString( report.getElapsedTime() ) );

            sink.tableRow_();
        }
        else
        {
            sink.tableRow();

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" + "%" );

            sinkCell( sink, "0" );

            sink.tableRow_();
        }

        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart =
            new PieChartRenderer( new SurefirePieChartStrategy( bundle, this.mavenProject.getProjectName(), report ),
                                  DashBoardReportGenerator.SPECIFIC_WIDTH, ChartUtils.STANDARD_HEIGHT );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_Surefire." + chart.getFileExtension();
            }
            else
            {
                filename += "_Surefire." + chart.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            getLog().debug( "createSurefireGraph = " + filename );
            try
            {
                chart.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createSurefireGraph exception = " + e.getMessage() );
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
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#clover" );
            sink.text( bundle.getString( "report.clover.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./clover/index.html" );
            sink.text( bundle.getString( "report.clover.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "clover" );
        }
        sink.section1_();
        sink.lineBreak();
        sink.table();
        sink.tableRow();

        sinkHeader( sink, bundle.getString( "report.clover.label.conditionals" ) );

        sinkHeader( sink, bundle.getString( "report.clover.label.statements" ) );

        sinkHeader( sink, bundle.getString( "report.clover.label.methods" ) );

        sinkHeader( sink, bundle.getString( "report.clover.label.total" ) );

        sink.tableRow_();

        if ( report != null )
        {
            sink.tableRow();

            sinkCell( sink, report.getConditionalsLabel() );

            sinkCell( sink, report.getStatementsLabel() );

            sinkCell( sink, report.getMethodsLabel() );

            sinkCell( sink, report.getElementsLabel() );

            sink.tableRow_();
        }
        else
        {
            sink.tableRow();

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" );

            sinkCell( sink, "0" + "%" );

            sinkCell( sink, "0" );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        Map datas = new Hashtable();
        datas.put( this.mavenProject.getProjectName(), report );
        AbstractChartRenderer chart =
            new StackedBarChartRenderer(
                                         new CloverBarChartStrategy( bundle, this.mavenProject.getProjectName(), datas ),
                                         DashBoardReportGenerator.SPECIFIC_WIDTH, ChartUtils.STANDARD_HEIGHT );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_Clover." + chart.getFileExtension();
            }
            else
            {
                filename += "_Clover." + chart.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            getLog().debug( "createCloverGraph = " + filename );
            try
            {
                chart.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createCloverGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCoberturaSection( ResourceBundle bundle, Sink sink, CoberturaReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "cobertura" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#cobertura" );
            sink.text( bundle.getString( "report.cobertura.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./cobertura/index.html" );
            sink.text( bundle.getString( "report.cobertura.header" ) );
            sink.link_();
        }

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "cobertura" );
        }
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from Cobertura data file ." );
        }
        else
        {
            sink.table();
            sink.tableRow();

            sinkHeader( sink, bundle.getString( "report.cobertura.label.nbclasses" ) );

            sinkHeader( sink, bundle.getString( "report.cobertura.label.linecover" ) );

            sinkHeader( sink, bundle.getString( "report.cobertura.label.branchcover" ) );

            sink.tableRow_();

            sink.tableRow();
            sinkCell( sink, Integer.toString( report.getNbClasses() ) );

            sinkCell( sink, getPercentValue( report.getLineCoverRate() ) );
            sinkCell( sink, getPercentValue( report.getBranchCoverRate() ) );

            sink.tableRow_();

            sink.table_();
        }
        sink.lineBreak();
        Map datas = new Hashtable();
        datas.put( this.mavenProject.getProjectName(), report );
        AbstractChartRenderer chart =
            new BarChartRenderer( new CoberturaBarChartStrategy( bundle, this.mavenProject.getProjectName(), datas ),
                                  DashBoardReportGenerator.SPECIFIC_WIDTH, ChartUtils.STANDARD_HEIGHT );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_Cobertura." + chart.getFileExtension();
            }
            else
            {
                filename += "_Cobertura." + chart.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            getLog().debug( "createCoberturaGraph = " + filename );
            try
            {
                chart.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createCoberturaGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createPmdSection( ResourceBundle bundle, Sink sink, PmdReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "pmd" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#pmd" );
            sink.text( bundle.getString( "report.pmd.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./pmd.html" );
            sink.text( bundle.getString( "report.pmd.header" ) );
            sink.link_();
        }

        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "pmd" );
        }
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from PMD data file ." );
        }
        else
        {
            sink.table();
            sink.tableRow();

            sinkHeader( sink, bundle.getString( "report.pmd.label.nbclasses" ) );

            sinkHeader( sink, bundle.getString( "report.pmd.label.nbviolations" ) );

            sink.tableRow_();

            sink.tableRow();

            sinkCell( sink, Integer.toString( report.getNbClasses() ) );

            sinkCell( sink, Integer.toString( report.getNbViolations() ) );

            sink.tableRow_();

            sink.table_();
        }
        sink.lineBreak();
    }

    public void createCpdSection( ResourceBundle bundle, Sink sink, CpdReportBean report )
    {

        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "cpd" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#cpd" );
            sink.text( bundle.getString( "report.cpd.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./cpd.html" );
            sink.text( bundle.getString( "report.cpd.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "cpd" );
        }
        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from CPD data file ." );
        }
        else
        {
            sink.table();
            sink.tableRow();

            sinkHeader( sink, bundle.getString( "report.cpd.label.nbclasses" ) );

            sinkHeader( sink, bundle.getString( "report.cpd.label.nbduplicate" ) );

            sink.tableRow_();

            sink.tableRow();

            sinkCell( sink, Integer.toString( report.getNbClasses() ) );

            sinkCell( sink, Integer.toString( report.getNbDuplicate() ) );

            sink.tableRow_();

            sink.table_();
        }
        sink.lineBreak();
    }

    public void createCheckStyleSection( ResourceBundle bundle, Sink sink, CheckstyleReportBean report )
    {

        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "checkstyle" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#checkstyle" );
            sink.text( bundle.getString( "report.checkstyle.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./checkstyle.html" );
            sink.text( bundle.getString( "report.checkstyle.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "checkstyle" );
        }
        sink.section1_();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from checkstyle data file ." );
        }
        else
        {
            sink.table();

            sink.tableRow();
            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.checkstyle.files" ) );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.checkstyle.column.total" ) );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.checkstyle.column.infos" ) );
            sink.nonBreakingSpace();
            iconInfo( sink );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.checkstyle.column.warnings" ) );
            sink.nonBreakingSpace();
            iconWarning( sink );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.checkstyle.column.errors" ) );
            sink.nonBreakingSpace();
            iconError( sink );
            sink.tableHeaderCell_();
            sink.tableRow_();

            sink.tableRow();

            sinkCell( sink, Integer.toString( report.getNbClasses() ) );

            sinkCell( sink, Integer.toString( report.getNbTotal() ) );
            sinkCell( sink, Integer.toString( report.getNbInfos() ) );
            sinkCell( sink, Integer.toString( report.getNbWarnings() ) );
            sinkCell( sink, Integer.toString( report.getNbErrors() ) );

            sink.tableRow_();

            sink.table_();
        }
        sink.lineBreak();
        AbstractChartRenderer chart =
            new PieChartRenderer( new CheckstylePieChartStrategy( bundle, this.mavenProject.getProjectName(), report ),
                                  DashBoardReportGenerator.SPECIFIC_WIDTH, ChartUtils.STANDARD_HEIGHT );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_CheckStyle." + chart.getFileExtension();
            }
            else
            {
                filename += "_CheckStyle." + chart.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            getLog().debug( "createCheckStyleGraph = " + filename );
            try
            {
                chart.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createCheckStyleGraph exception = " + e.getMessage() );
            }
        }

        // error management for Checkstyle Violations Chart. Fixes MOJO-679 .
        // Written by <a href="mailto:srivollet@objectif-informatique.fr">Sylvain Rivollet</a>.

        AbstractChartRenderer chartError =
            new PieChart3DRenderer( new CheckstyleErrorsPieChartStrategy( bundle, this.mavenProject.getProjectName()
                            + " " + bundle.getString( "chart.checkstyle.violations.title" ), report ),
                                    ChartUtils.STANDARD_WIDTH, ChartUtils.STANDARD_HEIGHT );
        sink.lineBreak();
        sink.lineBreak();

        if ( !chartError.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_CheckStyle_Error." + chartError.getFileExtension();
            }
            else
            {
                filename += "_CheckStyle_Error." + chartError.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            try
            {
                chartError.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createCheckStyleGraphError exception = " + e.getMessage() );
            }
        }
    }

    public void createJDependSection( ResourceBundle bundle, Sink sink, JDependReportBean report )
    {
        getLog().debug( "createJDependSection creation." );
        sink.section1();

        sink.sectionTitle2();
        sink.anchor( "jdepend" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#jdepend" );
            sink.text( "Top " + this.getNbExportedPackagesSummary() + " "
                            + bundle.getString( "report.xrefpackage.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./jdepend-report.html" );
            sink.text( bundle.getString( "report.xrefpackage.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();

        sink.section1_();
        sink.lineBreak();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from Jdepend data file ." );
        }
        else
        {
            double averageAC = report.getAverageAfferentCoupling();

            double calcul = ( Math.rint( averageAC * 1000 ) ) / 1000;

            sink.sectionTitle3();
            sink.text( bundle.getString( "report.xrefpackage.label.average" ) + " = " + calcul );
            sink.lineBreak();
            sink.text( " Nb Packages = " + report.getNbPackages() );
            sink.sectionTitle3_();
            sink.table();
            sink.tableRow();

            sinkHeader( sink, bundle.getString( "report.xrefpackage.label.package" ) );

            sinkHeader( sink, bundle.getString( "report.xrefpackage.label.ac" ) );

            sinkHeader( sink, bundle.getString( "report.xrefpackage.label.linecover" ) );

            sinkHeader( sink, bundle.getString( "report.xrefpackage.label.branchcover" ) );

            sink.tableRow_();

            List pack = report.getPackages();
            Iterator iter = pack.iterator();
            boolean threshold = false;
            int nbPack = 0;
            while ( iter.hasNext() )
            {
                nbPack = nbPack + 1;
                XRefPackageBean bean = (XRefPackageBean) iter.next();
                Integer ac = bean.getAfferentCoupling();
                if ( ac.doubleValue() <= averageAC && !threshold )
                {
                    threshold = true;
                    sink.tableRow();
                    sinkCellBold( sink, bundle.getString( "report.xrefpackage.label.threshold" ) );
                    sinkCellBold( sink, String.valueOf( calcul ) );
                    // sinkHeader( sink, "" );
                    // sinkHeader( sink, "" );
                    sink.tableRow_();
                }
                sink.tableRow();
                sinkCell( sink, bean.getPackageName() );
                sinkCell( sink, ac.toString() );
                sinkCell( sink, getPercentValue( bean.getLineCoverRate() ) );
                sinkCell( sink, getPercentValue( bean.getBranchCoverRate() ) );
                sink.tableRow_();
                if ( this.summary && nbPack >= this.getNbExportedPackagesSummary() )
                {
                    break;
                }
            }

            sink.table_();
        }
        sink.lineBreak();
    }

    public void createFindBugsSection( ResourceBundle bundle, Sink sink, FindBugsReportBean report )
    {

        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "findbugs" );
        sink.anchor_();
        if ( this.summary )
        {
            sink.link( "dashboard-report-details.html#findbugs" );
            sink.text( bundle.getString( "report.findbugs.header" ) );
            sink.link_();
        }
        else
        {
            sink.link( "./findbugs.html" );
            sink.text( bundle.getString( "report.findbugs.header" ) );
            sink.link_();
        }
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.lineBreak();
        if ( this.isDBAvailable )
        {
            linkToHistoricPage( sink, "findbugs" );
        }
        sink.section1_();
        if ( report == null )
        {
            sink.text( "Error: Unable to read from findbugs data file ." );
        }
        else
        {
            sink.table();

            sink.tableRow();
            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.findbugs.label.nbclasses" ) );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.findbugs.label.nbbugs" ) );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.findbugs.label.nberrors" ) );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.text( bundle.getString( "report.findbugs.label.nbMissingClasses" ) );
            sink.tableHeaderCell_();

            sink.tableRow_();

            sink.tableRow();

            sinkCell( sink, Integer.toString( report.getNbClasses() ) );
            sinkCell( sink, Integer.toString( report.getNbBugs() ) );
            sinkCell( sink, Integer.toString( report.getNbErrors() ) );
            sinkCell( sink, Integer.toString( report.getNbMissingClasses() ) );

            sink.tableRow_();

            sink.table_();
        }
        sink.lineBreak();

        AbstractChartRenderer chartError =
            new PieChart3DRenderer( new FindbugsCategoriesPieChartStrategy( bundle, this.mavenProject.getProjectName()
                            + " " + bundle.getString( "chart.findbugs.bycategory.title" ), report ),
                                    ChartUtils.STANDARD_WIDTH, ChartUtils.STANDARD_HEIGHT );
        sink.lineBreak();
        sink.lineBreak();

        if ( !chartError.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_Findbugs_Categories." + chartError.getFileExtension();
            }
            else
            {
                filename += "_Findbugs_Categories." + chartError.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            try
            {
                chartError.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createFindbugsCategoriesGraphError exception = " + e.getMessage() );
            }
        }
        sink.lineBreak();
        chartError =
            new PieChart3DRenderer( new FindbugsPrioritiesPieChartStrategy( bundle, this.mavenProject.getProjectName()
                            + " " + bundle.getString( "chart.findbugs.bypriority.title" ), report ),
                                    ChartUtils.STANDARD_WIDTH, ChartUtils.STANDARD_HEIGHT );
        sink.lineBreak();
        sink.lineBreak();

        if ( !chartError.isEmpty() )
        {
            String filename = replaceForbiddenChar( this.mavenProject.getProjectName() );
            if ( this.summary )
            {
                filename += "_Summary_Findbugs_Priorities." + chartError.getFileExtension();
            }
            else
            {
                filename += "_Findbugs_Priorities." + chartError.getFileExtension();
            }
            filename = filename.replace( ' ', '_' );
            String filenameWithPath = getImagesPath() + "/" + filename;
            try
            {
                chartError.saveToFile( filenameWithPath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                getLog().debug( "createFindbugsPrioritiesGraphError exception = " + e.getMessage() );
            }
        }
    }
}
