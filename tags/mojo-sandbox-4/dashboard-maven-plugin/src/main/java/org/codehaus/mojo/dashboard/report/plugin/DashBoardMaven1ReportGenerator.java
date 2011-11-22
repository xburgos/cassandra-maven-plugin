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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
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

/**
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardMaven1ReportGenerator extends AbstractDashBoardGenerator
{

    private String dashboardAnchorLink = "/dashboard-report.html";

    private DashBoardMavenProject mavenProject;

    private Map map = new Hashtable();
    
    private boolean isDBAvailable = false;

    /**
     * 
     * @param dashboardReport
     */
    public DashBoardMaven1ReportGenerator( DashBoardMavenProject mavenProject, boolean isDBAvailable, Log log )
    {

        super( log );
        this.mavenProject = mavenProject;
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

        createBodyReport( bundle, sink );

        sink.body_();

        sink.flush();

        sink.close();
    }

    public void createTitle( ResourceBundle bundle, Sink sink )
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "dashboard.multireport.name" ) );
        sink.title_();
        sink.head_();
    }

    public void createHeader( ResourceBundle bundle, Sink sink )
    {
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "dashboard.multireport.name" ) + " : " + this.mavenProject.getProjectName() );
        sink.sectionTitle1_();

        sink.text( "Date Generated: " + new SimpleDateFormat().format( new Date( System.currentTimeMillis() ) ) );
        sink.horizontalRule();
 
        if ( this.isDBAvailable )
        {
            sink.sectionTitle3();
            sink.bold();
            sink.text( "[" );
            sink.link( "dashboard-report-historic.html" );
            sink.text( "Go to Historic page" );
            sink.link_();
            sink.text( "]" );
            sink.bold_();
            sink.sectionTitle3_();
            sink.horizontalRule();
        }
        sink.lineBreak();
        sink.section1_();
    }

    public void createBodyReport( ResourceBundle bundle, Sink sink )
    {
        System.out.println( "DashBoardMultiReportGenerator createBodyByReport(...)" );

        createAllSection( bundle, sink );

    }

    public void createAllSection( ResourceBundle bundle, Sink sink )
    {

        sink.table();
        writeSuperHeader( sink );
        writeHeader( bundle, sink, true );
        createAllLineByReport( bundle, sink, mavenProject, true, "" );
        createTotalLine( bundle, sink, mavenProject );
        writeHeader( bundle, sink, false );
        writeSuperHeader( sink );
        sink.table_();
        sink.lineBreak();

    }

    public void createAllLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                       boolean isRoot, String prefix )
    {

        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, dashboardAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createAllLineByReport( bundle, sink, subproject, false, prefix );
            }
        }
        else
        {
            sink.tableRow();
            writeProjectCell( sink, mavenProject, prefix, dashboardAnchorLink );

            if ( map.get( CoberturaReportBean.class ) != null )
            {
                CoberturaReportBean coberReportBean =
                    (CoberturaReportBean) mavenProject.getReportsByType( CoberturaReportBean.class );
                if ( coberReportBean != null )
                {

                    sinkCell( sink, Integer.toString( coberReportBean.getNbClasses() ) );
                    sinkCell( sink, getPercentValue( coberReportBean.getLineCoverRate() ) );
                    sinkCell( sink, getPercentValue( coberReportBean.getBranchCoverRate() ) );
                    sinkHeaderBold( sink, "|" );

                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( CloverReportBean.class ) != null )
            {
                CloverReportBean cloverReportBean =
                    (CloverReportBean) mavenProject.getReportsByType( CloverReportBean.class );
                if ( cloverReportBean != null )
                {
                    sinkCell( sink, cloverReportBean.getConditionalsLabel() );

                    sinkCell( sink, cloverReportBean.getStatementsLabel() );

                    sinkCell( sink, cloverReportBean.getMethodsLabel() );

                    sinkCell( sink, cloverReportBean.getElementsLabel() );
                    sinkHeaderBold( sink, "|" );
                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( SurefireReportBean.class ) != null )
            {
                SurefireReportBean fireReportBean =
                    (SurefireReportBean) mavenProject.getReportsByType( SurefireReportBean.class );
                if ( fireReportBean != null )
                {

                    sinkCell( sink, Integer.toString( fireReportBean.getNbTests() ) );

                    sinkCell( sink, Integer.toString( fireReportBean.getNbErrors() ) );

                    sinkCell( sink, Integer.toString( fireReportBean.getNbFailures() ) );

                    sinkCell( sink, Integer.toString( fireReportBean.getNbSkipped() ) );

                    sinkCell( sink, Double.toString( fireReportBean.getSucessRate() ) + "%" );

                    sinkCell( sink, Double.toString( fireReportBean.getElapsedTime() ) );
                    sinkHeaderBold( sink, "|" );

                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( CheckstyleReportBean.class ) != null )
            {
                CheckstyleReportBean checkStyleReport =
                    (CheckstyleReportBean) mavenProject.getReportsByType( CheckstyleReportBean.class );
                if ( checkStyleReport != null )
                {
                    sinkCell( sink, Integer.toString( checkStyleReport.getNbClasses() ) );
                    sinkCell( sink, Integer.toString( checkStyleReport.getNbTotal() ) );
                    sinkCell( sink, Integer.toString( checkStyleReport.getNbInfos() ) );
                    sinkCell( sink, Integer.toString( checkStyleReport.getNbWarnings() ) );
                    sinkCell( sink, Integer.toString( checkStyleReport.getNbErrors() ) );
                    sinkHeaderBold( sink, "|" );
                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( PmdReportBean.class ) != null )
            {
                PmdReportBean pmdReportBean = (PmdReportBean) mavenProject.getReportsByType( PmdReportBean.class );
                if ( pmdReportBean != null )
                {
                    sinkCell( sink, Integer.toString( pmdReportBean.getNbClasses() ) );
                    sinkCell( sink, Integer.toString( pmdReportBean.getNbViolations() ) );
                    sinkHeaderBold( sink, "|" );

                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( CpdReportBean.class ) != null )
            {
                CpdReportBean cpdReportBean = (CpdReportBean) mavenProject.getReportsByType( CpdReportBean.class );
                if ( cpdReportBean != null )
                {
                    sinkCell( sink, Integer.toString( cpdReportBean.getNbClasses() ) );
                    sinkCell( sink, Integer.toString( cpdReportBean.getNbDuplicate() ) );
                    sinkHeaderBold( sink, "|" );
                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sinkHeaderBold( sink, "|" );
                }
            }
            if ( map.get( FindBugsReportBean.class ) != null )
            {
                FindBugsReportBean findBugsReportBean =
                    (FindBugsReportBean) mavenProject.getReportsByType( FindBugsReportBean.class );
                if ( findBugsReportBean != null )
                {
                    sinkCell( sink, Integer.toString( findBugsReportBean.getNbClasses() ) );
                    sinkCell( sink, Integer.toString( findBugsReportBean.getNbBugs() ) );
                    sinkCell( sink, Integer.toString( findBugsReportBean.getNbErrors() ) );
                    sinkCell( sink, Integer.toString( findBugsReportBean.getNbMissingClasses() ) );
                }
                else
                {
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                    sink.tableCell();
                    sink.nonBreakingSpace();
                    sink.tableCell_();
                }
            }

            sink.tableRow_();
        }
    }

    private void writeProjectCell( Sink sink, DashBoardMavenProject mavenProject, String prefix, String suffix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            String artefactId = mavenProject.getArtifactId();
            String link = artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellWithLink( sink, mavenProject.getProjectName(), link + suffix );
        }
        else
        {
            int nbTab = prefix.split( "/" ).length;
            String artefactId = mavenProject.getArtifactId();
            String link = prefix + "/" + artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellTabWithLink( sink, mavenProject.getProjectName(), nbTab, link + suffix );
        }
    }

    private String writeMultiProjectRow( Sink sink, DashBoardMavenProject mavenProject, String prefix, String suffix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            String artefactId = mavenProject.getArtifactId();
            prefix = artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sink.tableRow();
            sinkCellBoldWithLink( sink, mavenProject.getProjectName(), prefix + suffix );
            sink.tableRow_();
        }
        else
        {
            sink.tableRow();
            int nbTab = prefix.split( "/" ).length;
            String artefactId = mavenProject.getArtifactId();
            prefix = prefix + "/" + artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellTabBoldWithLink( sink, mavenProject.getProjectName(), nbTab, prefix + suffix );
            sink.tableRow_();

        }
        return prefix;
    }

    private void writeSuperHeader( Sink sink )
    {
        sink.tableRow();
        sink.tableHeaderCell();
        sink.nonBreakingSpace();
        sink.tableHeaderCell_();
        if ( map.get( CoberturaReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "Cobertura", 4 );
        }
        if ( map.get( CloverReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "Clover", 5 );
        }
        if ( map.get( SurefireReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "Surefire", 7 );
        }
        if ( map.get( CheckstyleReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "Checkstyle", 6 );
        }
        if ( map.get( PmdReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "PMD", 3 );
        }
        if ( map.get( CpdReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "CPD", 3 );
        }
        if ( map.get( FindBugsReportBean.class ) != null )
        {
            sinkSuperHeader( sink, "FindBugs", 4 );
        }

        sink.tableRow_();
    }

    private void writeHeader( ResourceBundle bundle, Sink sink, boolean upside )
    {
        sink.tableRow();
        if ( upside )
        {
            sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        }
        else
        {
            sinkHeader( sink, "" );
        }
        if ( map.get( CoberturaReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.cobertura.label.nbclasses" ) );
            sinkHeader( sink, bundle.getString( "report.cobertura.label.linecover" ) );
            sinkHeader( sink, bundle.getString( "report.cobertura.label.branchcover" ) );
            sinkHeaderBold( sink, "" );
        }
        if ( map.get( CloverReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.clover.label.conditionals" ) );
            sinkHeader( sink, bundle.getString( "report.clover.label.statements" ) );
            sinkHeader( sink, bundle.getString( "report.clover.label.methods" ) );
            sinkHeader( sink, bundle.getString( "report.clover.label.total" ) );
            sinkHeaderBold( sink, "" );
        }
        if ( map.get( SurefireReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.surefire.label.tests" ) );
            sinkHeader( sink, bundle.getString( "report.surefire.label.errors" ) );
            sinkHeader( sink, bundle.getString( "report.surefire.label.failures" ) );
            sinkHeader( sink, bundle.getString( "report.surefire.label.skipped" ) );
            sinkHeader( sink, bundle.getString( "report.surefire.label.successrate" ) );
            sinkHeader( sink, bundle.getString( "report.surefire.label.time" ) );
            sinkHeaderBold( sink, "" );
        }
        if ( map.get( CheckstyleReportBean.class ) != null )
        {
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

            sinkHeaderBold( sink, "" );
        }
        if ( map.get( PmdReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.pmd.label.nbclasses" ) );
            sinkHeader( sink, bundle.getString( "report.pmd.label.nbviolations" ) );
            sinkHeaderBold( sink, "" );
        }
        if ( map.get( CpdReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.cpd.label.nbclasses" ) );
            sinkHeader( sink, bundle.getString( "report.cpd.label.nbduplicate" ) );
            sinkHeaderBold( sink, "" );
        }
        if ( map.get( FindBugsReportBean.class ) != null )
        {
            sinkHeader( sink, bundle.getString( "report.findbugs.label.nbclasses" ) );
            sinkHeader( sink, bundle.getString( "report.findbugs.label.nbbugs" ) );
            sinkHeader( sink, bundle.getString( "report.findbugs.label.nberrors" ) );
            sinkHeader( sink, bundle.getString( "report.findbugs.label.nbMissingClasses" ) );
        }
        sink.tableRow_();
    }

    public void createTotalLine( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject )
    {
        sink.tableRow();
        sinkHeader( sink, "Total" );
        CoberturaReportBean reportBean =
            (CoberturaReportBean) mavenProject.getReportsByType( CoberturaReportBean.class );
        if ( reportBean != null )
        {
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, getPercentValue( reportBean.getLineCoverRate() ) );
            sinkHeader( sink, getPercentValue( reportBean.getBranchCoverRate() ) );
            sinkHeaderBold( sink, "|" );
        }
        CloverReportBean cloverReportBean = (CloverReportBean) mavenProject.getReportsByType( CloverReportBean.class );
        if ( cloverReportBean != null )
        {
            sinkHeader( sink, cloverReportBean.getConditionalsLabel() );

            sinkHeader( sink, cloverReportBean.getStatementsLabel() );

            sinkHeader( sink, cloverReportBean.getMethodsLabel() );

            sinkHeader( sink, cloverReportBean.getElementsLabel() );
            sinkHeaderBold( sink, "|" );
        }
        SurefireReportBean fireReportBean =
            (SurefireReportBean) mavenProject.getReportsByType( SurefireReportBean.class );
        if ( fireReportBean != null )
        {
            sinkHeader( sink, Integer.toString( fireReportBean.getNbTests() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbErrors() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbFailures() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbSkipped() ) );

            sinkHeader( sink, Double.toString( fireReportBean.getSucessRate() ) + "%" );

            sinkHeader( sink, Double.toString( fireReportBean.getElapsedTime() ) );
            sinkHeaderBold( sink, "|" );
        }
        CheckstyleReportBean checkstyleReportBean =
            (CheckstyleReportBean) mavenProject.getReportsByType( CheckstyleReportBean.class );
        if ( checkstyleReportBean != null )
        {
            sinkHeader( sink, Integer.toString( checkstyleReportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( checkstyleReportBean.getNbTotal() ) );
            sinkHeader( sink, Integer.toString( checkstyleReportBean.getNbInfos() ) );
            sinkHeader( sink, Integer.toString( checkstyleReportBean.getNbWarnings() ) );
            sinkHeader( sink, Integer.toString( checkstyleReportBean.getNbErrors() ) );
            sinkHeaderBold( sink, "|" );
        }
        PmdReportBean pmdReportBean = (PmdReportBean) mavenProject.getReportsByType( PmdReportBean.class );
        if ( pmdReportBean != null )
        {
            sinkHeader( sink, Integer.toString( pmdReportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( pmdReportBean.getNbViolations() ) );
            sinkHeaderBold( sink, "|" );

        }
        CpdReportBean cpdReportBean = (CpdReportBean) mavenProject.getReportsByType( CpdReportBean.class );
        if ( cpdReportBean != null )
        {
            sinkHeader( sink, Integer.toString( cpdReportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( cpdReportBean.getNbDuplicate() ) );
            sinkHeaderBold( sink, "|" );

        }
        FindBugsReportBean findBugsReportBean =
            (FindBugsReportBean) mavenProject.getReportsByType( FindBugsReportBean.class );
        if ( findBugsReportBean != null )
        {
            sinkHeader( sink, Integer.toString( findBugsReportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( findBugsReportBean.getNbBugs() ) );
            sinkHeader( sink, Integer.toString( findBugsReportBean.getNbErrors() ) );
            sinkHeader( sink, Integer.toString( findBugsReportBean.getNbMissingClasses() ) );
        }
        sink.tableRow_();
    }
}
