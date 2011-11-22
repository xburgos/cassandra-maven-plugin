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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.mojo.dashboard.report.plugin.beans.CheckstyleReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.CoberturaReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.CpdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMultiReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.JDependReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.PmdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.SurefireReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.XRefPackageBean;
import org.codehaus.mojo.dashboard.report.plugin.chart.AbstractChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.BarChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.CheckstyleBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CoberturaBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CpdBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.PmdBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.StackedBarChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.SurefireBarChartStrategy;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardMultiReportGenerator extends AbstractDashBoardGenerator
{
    /**
     * 
     */
    private DashBoardMultiReportBean mDashboardReport;
    
    private String dashboardReportFile = "dashboard-report.html" ;
    
    private String coberturaAnchorLink = "/" + dashboardReportFile + "#cobertura";
    
    private String surefireAnchorLink = "/" + dashboardReportFile + "#surefire";
    
    private String checkstyleAnchorLink = "/" + dashboardReportFile + "#checkstyle";
    
    private String pmdAnchorLink = "/" + dashboardReportFile + "#pmd";
    
    private String cpdAnchorLink = "/" + dashboardReportFile + "#cpd";
    
    //private String jdependAnchorLink = "/" + dashboardReportFile + "#jdepend";
    
    /**
     * 
     * @param dashboardReport
     */
    public DashBoardMultiReportGenerator( DashBoardMultiReportBean dashboardReport )
    {
        this.mDashboardReport = dashboardReport;
    }

    public void doGenerateReport( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {

        System.out.println( "DashBoardMultiReportGenerator doGenerateReport(...)" );

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

    public void createTitle( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "dashboard.multireport.name" ) );
        sink.title_();
        sink.head_();
    }

    public void createHeader( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();

        sink.sectionTitle1();
        sink.text( bundle.getString( "dashboard.multireport.name" ) + " : " + mDashboardReport.getProjectName() );
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

        sink.text( "[" );
        sink.link( "#cobertura" );
        sink.text( bundle.getString( "report.cobertura.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();
        sink.text( "[" );
        sink.link( "#surefire" );
        sink.text( bundle.getString( "report.surefire.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();
        sink.text( "[" );
        sink.link( "#checkstyle" );
        sink.text( bundle.getString( "report.checkstyle.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();
        sink.text( "[" );
        sink.link( "#pmd" );
        sink.text( bundle.getString( "report.pmd.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();
        sink.text( "[" );
        sink.link( "#cpd" );
        sink.text( bundle.getString( "report.cpd.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();
        sink.text( "[" );
        sink.link( "#jdepend" );
        sink.text( bundle.getString( "report.xrefpackage.header" ) );
        sink.link_();
        sink.text( "]" );
        sink.lineBreak();

        sink.horizontalRule();
        sink.lineBreak();

        sink.section1_();
    }

    public void createBodyReport( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        System.out.println( "DashBoardMultiReportGenerator createBodyByReport(...)" );

        createCoberturaSection( bundle, sink );
        createSurefireSection( bundle, sink );
        createCheckStyleSection( bundle, sink );
        createPmdSection( bundle, sink );
        createCpdSection( bundle, sink );
        createJDependSection( bundle, sink );
    }

    public void createSurefireSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "surefire" );
        sink.text( bundle.getString( "report.surefire.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.tests" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.errors" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.failures" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.skipped" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.successrate" ) );
        sinkHeader( sink, bundle.getString( "report.surefire.label.time" ) );
        sink.tableRow_();

        createSurefireLineByReport( bundle, sink, mDashboardReport, true, "" );

        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart =
            new StackedBarChartRenderer( mDashboardReport, new SurefireBarChartStrategy( bundle ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mDashboardReport.getProjectName() );
        	filename =  filename + "_Surefire." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            System.out.println( "createSurefireGraph = " + filenamePath );
            try
            {
                chart.saveToFile( filenamePath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                System.out.println( "createSurefireGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createSurefireLineByReport( ResourceBundle bundle, Sink sink, IDashBoardReportBean dashboardReport,
                                            boolean isRoot, String prefix )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getSurefireReport() != null )
            {
                SurefireReportBean fireReportBean = ( (DashBoardReportBean) dashboardReport ).getSurefireReport();
                sink.tableRow();
                
                writeProjectCell( sink, dashboardReport, prefix, surefireAnchorLink );
                
                sinkCell( sink, Integer.toString( fireReportBean.getNbTests() ) );

                sinkCell( sink, Integer.toString( fireReportBean.getNbErrors() ) );

                sinkCell( sink, Integer.toString( fireReportBean.getNbFailures() ) );

                sinkCell( sink, Integer.toString( fireReportBean.getNbSkipped() ) );

                sinkCell( sink, Double.toString( fireReportBean.getSucessRate() ) + "%" );

                sinkCell( sink, Double.toString( fireReportBean.getElapsedTime() ) );
                sink.tableRow_();
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, dashboardReport, prefix, surefireAnchorLink );
            }
            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createSurefireLineByReport( bundle, sink, reportBean, false, prefix );
            }
        }
    }

    public void createCoberturaSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "cobertura" );
        sink.text( bundle.getString( "report.cobertura.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.cobertura.label.nbclasses" ) );
        sinkHeader( sink, bundle.getString( "report.cobertura.label.linecover" ) );
        sinkHeader( sink, bundle.getString( "report.cobertura.label.branchcover" ) );
        sink.tableRow_();

        createCoberturaLineByReport( bundle, sink, mDashboardReport, true, "" );

        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( mDashboardReport, new CoberturaBarChartStrategy( bundle ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mDashboardReport.getProjectName() );
        	filename =  filename + "_Cobertura." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            System.out.println( "createCoberturaGraph = " + filenamePath );
            try
            {
                chart.saveToFile( filenamePath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                System.out.println( "createCoberturaGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCoberturaLineByReport( ResourceBundle bundle, Sink sink, IDashBoardReportBean dashboardReport,
                                             boolean isRoot, String prefix )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getCoberturaReport() != null )
            {
                CoberturaReportBean coberReportBean = ( (DashBoardReportBean) dashboardReport ).getCoberturaReport();
                sink.tableRow();
                writeProjectCell( sink, dashboardReport, prefix, coberturaAnchorLink );                
                sinkCell( sink, Integer.toString( coberReportBean.getNbClasses() ) );
                sinkCell( sink, getPercentValue( coberReportBean.getLineCoverRate() ) );
                sinkCell( sink, getPercentValue( coberReportBean.getBranchCoverRate() ) );
                sink.tableRow_();
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();
            if ( !isRoot )
            {
               prefix = writeMultiProjectRow( sink, dashboardReport, prefix, coberturaAnchorLink );
            }
            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createCoberturaLineByReport( bundle, sink, reportBean, false, prefix );
            }
        }
    }

    public void createPmdSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "pmd" );
        sink.text( bundle.getString( "report.pmd.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.pmd.label.nbclasses" ) );
        sinkHeader( sink, bundle.getString( "report.pmd.label.nbviolations" ) );
        sink.tableRow_();

        createPmdLineByReport( bundle, sink, mDashboardReport, true, "" );

        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( mDashboardReport, new PmdBarChartStrategy( bundle ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mDashboardReport.getProjectName() );
        	filename =  filename + "_Pmd." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            System.out.println( "createPmdGraph = " + filenamePath );
            try
            {
                chart.saveToFile( filenamePath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                System.out.println( "createPmdGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createPmdLineByReport( ResourceBundle bundle, Sink sink, IDashBoardReportBean dashboardReport,
                                       boolean isRoot, String prefix )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getPmdReport() != null )
            {
                PmdReportBean pmdReportBean = ( (DashBoardReportBean) dashboardReport ).getPmdReport();
                sink.tableRow();
                writeProjectCell( sink, dashboardReport, prefix, pmdAnchorLink );
                sinkCell( sink, Integer.toString( pmdReportBean.getNbClasses() ) );
                sinkCell( sink, Integer.toString( pmdReportBean.getNbViolations() ) );
                sink.tableRow_();
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();

            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, dashboardReport, prefix, pmdAnchorLink );
            }

            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createPmdLineByReport( bundle, sink, reportBean, false, prefix );
            }
        }
    }

    public void createCpdSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "cpd" );
        sink.text( bundle.getString( "report.cpd.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.cpd.label.nbclasses" ) );
        sinkHeader( sink, bundle.getString( "report.cpd.label.nbduplicate" ) );
        sink.tableRow_();

        createCpdLineByReport( bundle, sink, mDashboardReport, true, "" );

        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( mDashboardReport, new CpdBarChartStrategy( bundle ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mDashboardReport.getProjectName() );
        	filename =  filename + "_Cpd." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            System.out.println( "createCpdGraph = " + filenamePath );
            try
            {
                chart.saveToFile( filenamePath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                System.out.println( "createCpdGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCpdLineByReport( ResourceBundle bundle, Sink sink, IDashBoardReportBean dashboardReport,
                                       boolean isRoot, String prefix )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getCpdReport() != null )
            {
                CpdReportBean cpdReportBean = ( (DashBoardReportBean) dashboardReport ).getCpdReport();
                sink.tableRow();
                writeProjectCell( sink, dashboardReport, prefix, cpdAnchorLink );
                sinkCell( sink, Integer.toString( cpdReportBean.getNbClasses() ) );
                sinkCell( sink, Integer.toString( cpdReportBean.getNbDuplicate() ) );
                sink.tableRow_();
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();

            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, dashboardReport, prefix, cpdAnchorLink );
            }

            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createCpdLineByReport( bundle, sink, reportBean, false, prefix );
            }
        }
    }

    public void createCheckStyleSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "checkstyle" );
        sink.text( bundle.getString( "report.checkstyle.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
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

        createCheckStyleLineByReport( bundle, sink, mDashboardReport, true, "" );

        sink.table_();

        sink.lineBreak();

        AbstractChartRenderer chart =
            new StackedBarChartRenderer( mDashboardReport, new CheckstyleBarChartStrategy( bundle ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mDashboardReport.getProjectName() );
        	filename =  filename + "_CheckStyle." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            System.out.println( "createCheckStyleGraph = " + filenamePath );
            try
            {
                chart.saveToFile( filenamePath );
                String link = "images/" + filename;
                link = link.replace( ' ', '_' );
                sink.figure();
                sink.figureGraphics( link );
                sink.figure_();
            }
            catch ( IOException e )
            {
                System.out.println( "createCheckStyleGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCheckStyleLineByReport( ResourceBundle bundle, Sink sink, IDashBoardReportBean dashboardReport,
                                              boolean isRoot, String prefix )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getCheckStyleReport() != null )
            {
                CheckstyleReportBean checkStyleReport = ( (DashBoardReportBean) dashboardReport ).getCheckStyleReport();
                sink.tableRow();
                writeProjectCell( sink, dashboardReport, prefix, checkstyleAnchorLink );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbClasses() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbTotal() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbInfos() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbWarnings() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbErrors() ) );
                sink.tableRow_();
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();

            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, dashboardReport, prefix, checkstyleAnchorLink );
            }

            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createCheckStyleLineByReport( bundle, sink, reportBean, false, prefix );
            }
        }
    }

    /**
     * 
     * @param bundle
     * @param sink
     * @throws MavenReportException
     */
    public void createJDependSection( ResourceBundle bundle, Sink sink ) throws MavenReportException
    {

        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "jdepend" );
        sink.text( bundle.getString( "report.xrefpackage.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();

        JDependReportBean report = ( (DashBoardReportBean) ( this.mDashboardReport.getSummary() ) ).getJDependReport();
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
            while ( iter.hasNext() )
            {

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
            }

            sink.table_();
        }
        sink.lineBreak();
    }
    
    private void writeProjectCell( Sink sink, IDashBoardReportBean dashboardReport, String prefix,
                                   String suffix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            String artefactId = ( (DashBoardReportBean) dashboardReport ).getArtefactId();
            String link = artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellWithLink( sink, dashboardReport.getProjectName(), link + suffix );
        }
        else
        {
            int nbTab = prefix.split( "/" ).length;
            String artefactId = ( (DashBoardReportBean) dashboardReport ).getArtefactId();
            String link = prefix + "/" + artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellTabWithLink( sink, dashboardReport.getProjectName(), nbTab, link + suffix );
        }
    }
    
    private String writeMultiProjectRow( Sink sink, IDashBoardReportBean dashboardReport, String prefix,
                                   String suffix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            String artefactId = ( (DashBoardMultiReportBean) dashboardReport ).getArtefactId();
            prefix = artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sink.tableRow();
            sinkCellBoldWithLink( sink, ( (DashBoardMultiReportBean) dashboardReport ).getProjectName(),
                                  prefix + suffix );
            sink.tableRow_();
        }
        else
        {
            sink.tableRow();
            int nbTab = prefix.split( "/" ).length;
            String artefactId = ( (DashBoardMultiReportBean) dashboardReport ).getArtefactId();
            prefix =
                prefix + "/" + artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellTabBoldWithLink( sink, ( (DashBoardMultiReportBean) dashboardReport ).getProjectName(),
                                     nbTab, prefix + suffix );
            sink.tableRow_();

        }
        return prefix;
    }
}
