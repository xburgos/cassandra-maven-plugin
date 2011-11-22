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
import org.apache.maven.reporting.MavenReportException;
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
import org.codehaus.mojo.dashboard.report.plugin.chart.CheckstyleBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CoberturaBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.CpdBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.FindBugsBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.MultiCloverBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.PmdBarChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.chart.StackedBarChartRenderer;
import org.codehaus.mojo.dashboard.report.plugin.chart.SurefireBarChartStrategy;

/**
 * <ul>
 * <li>Add "CheckStyle Violations" graph written by <a href="mailto:srivollet@objectif-informatique.fr">Sylvain Rivollet</a></li>
 * <li>Add Clover support written by <a href="mailto:mbeerman@yahoo.com">Matthew Beermann</a></li>
 * </ul>
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardMultiReportGenerator extends AbstractDashBoardGenerator
{
    
    private String dashboardReportFile = "dashboard-report.html" ;
    
    private String coberturaAnchorLink = "/" + dashboardReportFile + "#cobertura";
    
    private String cloverAnchorLink = "/" + dashboardReportFile + "#clover";
    
    private String surefireAnchorLink = "/" + dashboardReportFile + "#surefire";
    
    private String checkstyleAnchorLink = "/" + dashboardReportFile + "#checkstyle";
    
    private String pmdAnchorLink = "/" + dashboardReportFile + "#pmd";
    
    private String cpdAnchorLink = "/" + dashboardReportFile + "#cpd";
    
    private String findbugsAnchorLink = "/" + dashboardReportFile + "#findbugs";
       
    private DashBoardMavenProject mavenProject;
    
    private Map map = new Hashtable();
    
    /**
     * 
     * @param dashboardReport
     */
    public DashBoardMultiReportGenerator( DashBoardMavenProject mavenProject, Log log )
    {
        super( log );
        this.mavenProject = mavenProject;
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

        getLog().debug( "DashBoardMultiReportGenerator doGenerateReport(...)" );

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

    public void createBodyReport( ResourceBundle bundle, Sink sink )
    {
        getLog().debug( "DashBoardMultiReportGenerator createBodyByReport(...)" );
        if ( map.get( CoberturaReportBean.class ) != null )
        {
            createCoberturaSection( bundle, sink );
        }
        if ( map.get( CloverReportBean.class ) != null )
        {
            createCloverSection( bundle, sink );
        }
        if ( map.get( SurefireReportBean.class ) != null )
        {
            createSurefireSection( bundle, sink );
        }
        if ( map.get( CheckstyleReportBean.class ) != null )
        {
            createCheckStyleSection( bundle, sink );
        }
        if ( map.get( PmdReportBean.class ) != null )
        {
            createPmdSection( bundle, sink );
        }
        if ( map.get( CpdReportBean.class ) != null )
        {
            createCpdSection( bundle, sink );
        }
        if ( map.get( FindBugsReportBean.class ) != null )
        {
            createFindBugsSection( bundle, sink );
        }
        if ( map.get( JDependReportBean.class ) != null )
        {
            createJDependSection( bundle, sink );
        }
    }

    public void createSurefireSection( ResourceBundle bundle, Sink sink )
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
        Map datas = new Hashtable();
        createSurefireLineByReport( bundle, sink, mavenProject, true, "", datas );
        
        SurefireReportBean fireReportBean = (SurefireReportBean) mavenProject.getReportsByType( SurefireReportBean.class );
        if( fireReportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );
            
            sinkHeader( sink, Integer.toString( fireReportBean.getNbTests() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbErrors() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbFailures() ) );

            sinkHeader( sink, Integer.toString( fireReportBean.getNbSkipped() ) );

            sinkHeader( sink, Double.toString( fireReportBean.getSucessRate() ) + "%" );

            sinkHeader( sink, Double.toString( fireReportBean.getElapsedTime() ) );
            
            sink.tableRow_();
        }
        
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart =
            new StackedBarChartRenderer( new SurefireBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mavenProject.getProjectName() );
        	filename =  filename + "_Surefire." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createSurefireGraph = " + filenamePath );
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
                getLog().debug( "createSurefireGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createSurefireLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                            boolean isRoot, String prefix, Map datas )
    {

        if( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, surefireAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createSurefireLineByReport( bundle, sink, subproject, false, prefix, datas);
            }
        }
        else
        {
                SurefireReportBean fireReportBean = (SurefireReportBean) mavenProject.getReportsByType( SurefireReportBean.class );
                if( fireReportBean != null )
                {
                    sink.tableRow();
                    datas.put( mavenProject.getProjectName(), fireReportBean );
                    writeProjectCell( sink, mavenProject, prefix, surefireAnchorLink );
                    
                    sinkCell( sink, Integer.toString( fireReportBean.getNbTests() ) );
    
                    sinkCell( sink, Integer.toString( fireReportBean.getNbErrors() ) );
    
                    sinkCell( sink, Integer.toString( fireReportBean.getNbFailures() ) );
    
                    sinkCell( sink, Integer.toString( fireReportBean.getNbSkipped() ) );
    
                    sinkCell( sink, Double.toString( fireReportBean.getSucessRate() ) + "%" );
    
                    sinkCell( sink, Double.toString( fireReportBean.getElapsedTime() ) );
                    sink.tableRow_();
                }
        }
    }
    /**
     * Fixes MOJO-813. addition of Clover support
     * written by <a href="mailto:mbeerman@yahoo.com">Matthew Beermann</a>
     * @param bundle
     * @param sink
     */
    public void createCloverSection( ResourceBundle bundle, Sink sink )
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "clover" );
        sink.text( bundle.getString( "report.clover.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.clover.label.conditionals" ) );
        sinkHeader( sink, bundle.getString( "report.clover.label.statements" ) );
        sinkHeader( sink, bundle.getString( "report.clover.label.methods" ) );
        sinkHeader( sink, bundle.getString( "report.clover.label.total" ) );
        sink.tableRow_();
        Map datas = new Hashtable();
        createCloverLineByReport( bundle, sink, mavenProject, true, "", datas );
        
        CloverReportBean reportBean = (CloverReportBean) mavenProject.getReportsByType( CloverReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );
            
            sinkHeader( sink, reportBean.getConditionalsLabel() );

            sinkHeader( sink, reportBean.getStatementsLabel() );

            sinkHeader( sink, reportBean.getMethodsLabel() );

            sinkHeader( sink, reportBean.getElementsLabel() );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart =
            new StackedBarChartRenderer( new MultiCloverBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( mavenProject.getProjectName() );
            filename =  filename + "_Clover." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createCloverGraph = " + filenamePath );
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
                getLog().debug( "createCloverGraph exception = " + e.getMessage() );
            }
        }
    }
    /**
     * Fixes MOJO-813. addition of Clover support
     * written by <a href="mailto:mbeerman@yahoo.com">Matthew Beermann</a>
     * @param bundle
     * @param sink
     * @param DashBoardMavenProject
     * @param isRoot
     * @param prefix
     */
    public void createCloverLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                          boolean isRoot, String prefix, Map datas )
    {
        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, cloverAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createCloverLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            CloverReportBean cloverReportBean =
                (CloverReportBean) mavenProject.getReportsByType( CloverReportBean.class );
            if ( cloverReportBean != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), cloverReportBean );
                writeProjectCell( sink, mavenProject, prefix, cloverAnchorLink );

                sinkCell( sink, cloverReportBean.getConditionalsLabel() );

                sinkCell( sink, cloverReportBean.getStatementsLabel() );

                sinkCell( sink, cloverReportBean.getMethodsLabel() );

                sinkCell( sink, cloverReportBean.getElementsLabel() );
                sink.tableRow_();
            }
        }
    }
    
    public void createCoberturaSection( ResourceBundle bundle, Sink sink )
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
        Map datas = new Hashtable();
        createCoberturaLineByReport( bundle, sink, mavenProject, true, "", datas );
        CoberturaReportBean reportBean = (CoberturaReportBean) mavenProject.getReportsByType( CoberturaReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );      
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, getPercentValue( reportBean.getLineCoverRate() ) );
            sinkHeader( sink, getPercentValue( reportBean.getBranchCoverRate() ) );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( new CoberturaBarChartStrategy( bundle, mavenProject.getProjectName(),datas) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mavenProject.getProjectName() );
        	filename =  filename + "_Cobertura." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createCoberturaGraph = " + filenamePath );
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
                getLog().debug( "createCoberturaGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCoberturaLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                             boolean isRoot, String prefix,Map datas )
    {
        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, coberturaAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createCoberturaLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            CoberturaReportBean coberReportBean =
                (CoberturaReportBean) mavenProject.getReportsByType( CoberturaReportBean.class );
            if( coberReportBean != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), coberReportBean );
                writeProjectCell( sink, mavenProject, prefix, coberturaAnchorLink );
    
                sinkCell( sink, Integer.toString( coberReportBean.getNbClasses() ) );
                sinkCell( sink, getPercentValue( coberReportBean.getLineCoverRate() ) );
                sinkCell( sink, getPercentValue( coberReportBean.getBranchCoverRate() ) );
                sink.tableRow_();
            }
        }
    }

    public void createPmdSection( ResourceBundle bundle, Sink sink )
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
        Map datas = new Hashtable();
        createPmdLineByReport( bundle, sink, mavenProject, true, "", datas );
        PmdReportBean reportBean = (PmdReportBean) mavenProject.getReportsByType( PmdReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );      
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbViolations() ) );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( new PmdBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mavenProject.getProjectName() );
        	filename =  filename + "_Pmd." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createPmdGraph = " + filenamePath );
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
                getLog().debug( "createPmdGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createPmdLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                       boolean isRoot, String prefix, Map datas )
    {
        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, pmdAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createPmdLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            PmdReportBean pmdReportBean = (PmdReportBean) mavenProject.getReportsByType( PmdReportBean.class );
            if ( pmdReportBean != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), pmdReportBean );
                writeProjectCell( sink, mavenProject, prefix, pmdAnchorLink );

                sinkCell( sink, Integer.toString( pmdReportBean.getNbClasses() ) );
                sinkCell( sink, Integer.toString( pmdReportBean.getNbViolations() ) );
                sink.tableRow_();
            }
        }
    }

    public void createCpdSection( ResourceBundle bundle, Sink sink )
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
        Map datas = new Hashtable();
        createCpdLineByReport( bundle, sink, mavenProject, true, "", datas );
        CpdReportBean reportBean = (CpdReportBean) mavenProject.getReportsByType( CpdReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );      
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbDuplicate() ) );
            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart = new BarChartRenderer( new CpdBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mavenProject.getProjectName() );
        	filename =  filename + "_Cpd." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createCpdGraph = " + filenamePath );
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
                getLog().debug( "createCpdGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCpdLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                       boolean isRoot, String prefix, Map datas )
    {

        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, cpdAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createCpdLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            CpdReportBean cpdReportBean = (CpdReportBean) mavenProject.getReportsByType( CpdReportBean.class );
            if ( cpdReportBean != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), cpdReportBean );
                writeProjectCell( sink, mavenProject, prefix, cpdAnchorLink );

                sinkCell( sink, Integer.toString( cpdReportBean.getNbClasses() ) );
                sinkCell( sink, Integer.toString( cpdReportBean.getNbDuplicate() ) );
                sink.tableRow_();
            }
        }
    }
    
    public void createFindBugsSection( ResourceBundle bundle, Sink sink )
    {
        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "findbugs" );
        sink.text( bundle.getString( "report.findbugs.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();
        // sink.lineBreak();

        sink.table();
        sink.tableRow();
        sinkHeader( sink, bundle.getString( "report.project.name.header" ) );
        sinkHeader( sink, bundle.getString( "report.findbugs.label.nbclasses" ) );
        sinkHeader( sink, bundle.getString( "report.findbugs.label.nbbugs" ) );
        sinkHeader( sink, bundle.getString( "report.findbugs.label.nberrors" ) );
        sinkHeader( sink, bundle.getString( "report.findbugs.label.nbMissingClasses" ) );
        sink.tableRow_();
        Map datas = new Hashtable();
        createFindBugsLineByReport( bundle, sink, mavenProject, true, "", datas );
        FindBugsReportBean reportBean = (FindBugsReportBean) mavenProject.getReportsByType( FindBugsReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );      
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbBugs() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbErrors() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbMissingClasses() ) );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();
        AbstractChartRenderer chart =
            new BarChartRenderer( new FindBugsBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
            String filename = replaceForbiddenChar( mavenProject.getProjectName() );
            filename = filename + "_FindBugs." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createFindBugsGraph = " + filenamePath );
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
                getLog().debug( "createFindBugsGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createFindBugsLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                       boolean isRoot, String prefix, Map datas )
    {

        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, this.findbugsAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createFindBugsLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            FindBugsReportBean findBugsReportBean = (FindBugsReportBean) mavenProject.getReportsByType( FindBugsReportBean.class );
            if ( findBugsReportBean != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), findBugsReportBean );
                writeProjectCell( sink, mavenProject, prefix, this.findbugsAnchorLink );

                sinkCell( sink, Integer.toString( findBugsReportBean.getNbClasses() ) );
                sinkCell( sink, Integer.toString( findBugsReportBean.getNbBugs() ) );
                sinkCell( sink, Integer.toString( findBugsReportBean.getNbErrors() ) );
                sinkCell( sink, Integer.toString( findBugsReportBean.getNbMissingClasses() ) );
                sink.tableRow_();
            }
        }
    }
    
    
    public void createCheckStyleSection( ResourceBundle bundle, Sink sink )
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
        Map datas = new Hashtable();
        createCheckStyleLineByReport( bundle, sink, mavenProject, true, "", datas );
        CheckstyleReportBean reportBean = (CheckstyleReportBean) mavenProject.getReportsByType( CheckstyleReportBean.class );
        if( reportBean != null )
        {
            sink.tableRow();
            
            sinkHeader( sink, "Total" );      
            sinkHeader( sink, Integer.toString( reportBean.getNbClasses() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbTotal() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbInfos() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbWarnings() ) );
            sinkHeader( sink, Integer.toString( reportBean.getNbErrors() ) );

            sink.tableRow_();
        }
        sink.table_();

        sink.lineBreak();

        AbstractChartRenderer chart =
            new StackedBarChartRenderer( new CheckstyleBarChartStrategy( bundle, mavenProject.getProjectName(), datas ) );
        if ( !chart.isEmpty() )
        {
        	String filename = replaceForbiddenChar( mavenProject.getProjectName() );
        	filename =  filename + "_CheckStyle." + chart.getFileExtension();
            filename = filename.replace( ' ', '_' );
            String filenamePath = getImagesPath() + "/" + filename;
            getLog().debug( "createCheckStyleGraph = " + filenamePath );
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
                getLog().debug( "createCheckStyleGraph exception = " + e.getMessage() );
            }
        }
    }

    public void createCheckStyleLineByReport( ResourceBundle bundle, Sink sink, DashBoardMavenProject mavenProject,
                                              boolean isRoot, String prefix, Map datas )
    {

        if ( mavenProject.getModules() != null && !mavenProject.getModules().isEmpty() )
        {
            Iterator iter = mavenProject.getModules().iterator();
            if ( !isRoot )
            {
                prefix = writeMultiProjectRow( sink, mavenProject, prefix, checkstyleAnchorLink );
            }
            while ( iter.hasNext() )
            {
                DashBoardMavenProject subproject = (DashBoardMavenProject) iter.next();
                createCheckStyleLineByReport( bundle, sink, subproject, false, prefix, datas );
            }
        }
        else
        {
            CheckstyleReportBean checkStyleReport =
                (CheckstyleReportBean) mavenProject.getReportsByType( CheckstyleReportBean.class );
            if ( checkStyleReport != null )
            {
                sink.tableRow();
                datas.put( mavenProject.getProjectName(), checkStyleReport );
                writeProjectCell( sink, mavenProject, prefix, checkstyleAnchorLink );

                sinkCell( sink, Integer.toString( checkStyleReport.getNbClasses() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbTotal() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbInfos() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbWarnings() ) );
                sinkCell( sink, Integer.toString( checkStyleReport.getNbErrors() ) );
                sink.tableRow_();
            }
        }
    }

    /**
     * 
     * @param bundle
     * @param sink
     * @throws MavenReportException
     */
    public void createJDependSection( ResourceBundle bundle, Sink sink )
    {

        sink.section1();
        sink.sectionTitle2();
        sink.anchor( "jdepend" );
        sink.text( bundle.getString( "report.xrefpackage.header" ) );
        sink.anchor_();
        sink.sectionTitle2_();
        linkToTopPage( sink );
        sink.section1_();

        JDependReportBean report = (JDependReportBean)mavenProject.getReportsByType( JDependReportBean.class );
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
    
    private void writeProjectCell( Sink sink, DashBoardMavenProject mavenProject, String prefix,
                                   String suffix )
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
    
    private String writeMultiProjectRow( Sink sink, DashBoardMavenProject mavenProject, String prefix,
                                   String suffix )
    {
        if ( prefix == null || prefix.length() == 0 )
        {
            String artefactId = mavenProject.getArtifactId();
            prefix = artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sink.tableRow();
            sinkCellBoldWithLink( sink, mavenProject.getProjectName(),
                                  prefix + suffix );
            sink.tableRow_();
        }
        else
        {
            sink.tableRow();
            int nbTab = prefix.split( "/" ).length;
            String artefactId = mavenProject.getArtifactId();
            prefix =
                prefix + "/" + artefactId.substring( artefactId.lastIndexOf( "." ) + 1, artefactId.length() );
            sinkCellTabBoldWithLink( sink, mavenProject.getProjectName(),
                                     nbTab, prefix + suffix );
            sink.tableRow_();

        }
        return prefix;
    }
}
