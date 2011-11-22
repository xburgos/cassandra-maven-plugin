package org.codehaus.mojo.javancss;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.doxia.sink.Sink;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Generates the javaNcss maven report.
 * 
 * @author <a href="jeanlaurentATgmail.com">Jean-Laurent de Morlhon</a>
 * 
 * @version $Id$
 */
public class NcssReportGenerator extends AbstractNcssReportGenerator
{
    private String xrefLocation;

    private int lineThreshold;

    /**
     * build a new NcssReportGenerator.
     * 
     * @param sink
     *            the sink which will be used for reporting.
     * @param bundle
     *            the correct RessourceBundle to be used for reporting.
     */
    public NcssReportGenerator( Sink sink, ResourceBundle bundle, Log log, String xrefLocation )
    {
        super( sink, bundle, log );
        this.xrefLocation = xrefLocation;
    }

    /**
     * Generates the JavaNcss reports.
     * 
     * @param document
     *            the javaNcss raw report as an XML document.
     * @param lineThreshold
     *            the maximum number of lines to keep in major reports.
     */
    public void doReport( Document document, int lineThreshold )
    {
        this.lineThreshold = lineThreshold;
        // HEADER
        getSink().head();
        getSink().title();
        getSink().text( getResourceBundle().getString( "report.javancss.title" ) );
        getSink().title_();
        getSink().head_();
        // BODY
        getSink().body();
        doIntro();
        // packages
        startSection( "report.javancss.package.link", "report.javancss.package.title" );
        doMainPackageAnalysis( document );
        doTotalPackageAnalysis( document );
        endSection();
        // Objects
        startSection( "report.javancss.object.link", "report.javancss.object.title" );
        doTopObjectNcss( document );
        doTopObjectFunctions( document );
        doObjectAverage( document );
        endSection();
        // Functions
        startSection( "report.javancss.function.link", "report.javancss.function.title" );
        doFunctionAnalysis( document );
        doFunctionAverage( document );
        endSection();
        // Explanation
        startSection( "report.javancss.explanation.link", "report.javancss.explanation.title" );
        doExplanation();
        endSection();
        getSink().body_();
    }

    private void doIntro()
    {
        getSink().section1();
        getSink().sectionTitle1();
        getSink().text( getResourceBundle().getString( "report.javancss.main.title" ) );
        getSink().sectionTitle1_();
        getSink().paragraph();
        navigationBar();
        getSink().text( getResourceBundle().getString( "report.javancss.main.text" ) + " " );
        getSink().lineBreak();
        getSink().link( "http://www.kclee.de/clemens/java/javancss/" );
        getSink().text( "JavaNCSS web site." );
        getSink().link_();
        getSink().paragraph_();
        getSink().section1_();
    }

    private void doMainPackageAnalysis( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.package.text" ) );
        getSink().table();
        getSink().tableRow();
        // HEADER
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.package" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.classe" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.function" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncss" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc_line" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.single_comment" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.multi_comment" ) );
        getSink().tableRow_();
        // DATA
        List list = document.selectNodes( "//javancss/packages/package" );
        Collections.sort( list, new NumericNodeComparator( "ncss" ) );
        Iterator nodeIterator = list.iterator();
        while ( nodeIterator.hasNext() )
        {
            Node node = (Node) nodeIterator.next();
            getSink().tableRow();
            tableCellHelper( node.valueOf( "name" ) );
            tableCellHelper( node.valueOf( "classes" ) );
            tableCellHelper( node.valueOf( "functions" ) );
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            tableCellHelper( node.valueOf( "javadoc_lines" ) );
            tableCellHelper( node.valueOf( "single_comment_lines" ) );
            tableCellHelper( node.valueOf( "multi_comment_lines" ) );
            getSink().tableRow_();
        }
        getSink().table_();
    }

    private void doTotalPackageAnalysis( Document document )
    {
        getSink().table();
        getSink().tableRow();
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.classetotal" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.functiontotal" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncsstotal" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc_line" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.single_comment" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.multi_comment" ) );
        getSink().tableRow_();
        Node node = document.selectSingleNode( "//javancss/packages/total" );
        getSink().tableRow();
        tableCellHelper( node.valueOf( "classes" ) );
        tableCellHelper( node.valueOf( "functions" ) );
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        tableCellHelper( node.valueOf( "javadoc_lines" ) );
        tableCellHelper( node.valueOf( "single_comment_lines" ) );
        tableCellHelper( node.valueOf( "multi_comment_lines" ) );
        getSink().tableRow_();
        getSink().table_();
    }

    private void doTopObjectNcss( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.top" ) + " " + lineThreshold + " "
                        + getResourceBundle().getString( "report.javancss.object.byncss" ) );
        List nodeList = document.selectNodes( "//javancss/objects/object" );
        Collections.sort( nodeList, new NumericNodeComparator( "ncss" ) );
        doTopObjectGeneric( nodeList );
    }

    private void doTopObjectFunctions( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.top" ) + " " + lineThreshold + " "
                        + getResourceBundle().getString( "report.javancss.object.byfunction" ) );
        List nodeList = document.selectNodes( "//javancss/objects/object" );
        Collections.sort( nodeList, new NumericNodeComparator( "functions" ) );
        doTopObjectGeneric( nodeList );
    }

    // generic method called by doTopObjectFunctions & doTopObjectNCss
    private void doTopObjectGeneric( List nodeList )
    {
        getSink().table();
        getSink().tableRow();
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.object" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncss" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.function" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.classe" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc" ) );
        getSink().tableRow_();
        Iterator nodeIterator = nodeList.iterator();
        int i = 0;
        while ( nodeIterator.hasNext() && ( i++ < lineThreshold ) )
        {
            Node node = (Node) nodeIterator.next();
            getSink().tableRow();
            getSink().tableCell();
            jxrLink( node.valueOf( "name" ) );
            getSink().tableCell_();
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "functions" ) );
            tableCellHelper( node.valueOf( "classes" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            getSink().tableRow_();
        }
        getSink().table_();
    }

    private void doObjectAverage( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.averages" ) );
        getSink().table();
        getSink().tableRow();
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncssaverage" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.programncss" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.classeaverage" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.functionaverage" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadocaverage" ) );
        getSink().tableRow_();
        Node node = document.selectSingleNode( "//javancss/objects/averages" );
        String totalNcss = document.selectSingleNode( "//javancss/objects/ncss" ).getText();
        getSink().tableRow();
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( totalNcss );
        tableCellHelper( node.valueOf( "classes" ) );
        tableCellHelper( node.valueOf( "functions" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        getSink().tableRow_();
        getSink().table_();
    }

    private void doFunctionAnalysis( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.top" ) + " " + lineThreshold + " "
                        + getResourceBundle().getString( "report.javancss.function.byncss" ) );
        getSink().paragraph();
        getSink().table();
        getSink().tableRow();
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.function" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncss" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ccn" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadoc" ) );
        getSink().tableRow_();
        List list = document.selectNodes( "//javancss/functions/function" );
        Collections.sort( list, new NumericNodeComparator( "ncss" ) );
        Iterator nodeIterator = list.iterator();
        int i = 0;
        while ( nodeIterator.hasNext() && ( i++ < lineThreshold ) )
        {
            Node node = (Node) nodeIterator.next();
            getSink().tableRow();
            getSink().tableCell();
            jxrFunctionLink( node.valueOf( "name" ) );
            getSink().tableCell_();
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "ccn" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            getSink().tableRow_();
        }
        getSink().table_();
        getSink().paragraph_();
    }

    private void doFunctionAverage( Document document )
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.averages" ) );
        getSink().paragraph();
        getSink().table();
        getSink().tableRow();
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.programncss" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ncssaverage" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.ccnaverage" ) );
        headerCellHelper( getResourceBundle().getString( "report.javancss.header.javadocaverage" ) );
        getSink().tableRow_();
        Node node = document.selectSingleNode( "//javancss/functions/function_averages" );
        String totalNcss = document.selectSingleNode( "//javancss/functions/ncss" ).getText();
        getSink().tableRow();
        tableCellHelper( totalNcss );
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( node.valueOf( "ccn" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        getSink().tableRow_();
        getSink().table_();
        getSink().paragraph_();
    }

    private void doExplanation()
    {
        subtitleHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.title" ) );
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.paragraph1" ) );
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.paragraph2" ) );
        getSink().table();

        getSink().tableRow();
        headerCellHelper( "" );
        headerCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.examples" ) );
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.package" ) );
        codeCellHelper( "package java.lang;" );
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.import" ) );
        codeCellHelper( "import java.awt.*;" );
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.class" ) );
        getSink().tableCell();
        getSink().list();
        codeItemListHelper( "public class Foo {" );
        codeItemListHelper( "public class Foo extends Bla {" );
        getSink().list_();
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.interface" ) );
        codeCellHelper( "public interface Able ; {" );
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.field" ) );
        getSink().tableCell();
        getSink().list();
        codeItemListHelper( "int a; " );
        codeItemListHelper( "int a, b, c = 5, d = 6;" );
        getSink().list_();
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.method" ) );
        getSink().tableCell();
        getSink().list();
        codeItemListHelper( "public void cry();" );
        codeItemListHelper( "public void gib() throws DeadException {" );
        getSink().list_();
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.constructorD" ) );
        codeCellHelper( "public Foo() {" );
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.constructorI" ) );
        getSink().tableCell();
        getSink().list();
        codeItemListHelper( "this();" );
        codeItemListHelper( "super();" );
        getSink().list_();
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.statement" ) );
        getSink().tableCell();
        getSink().list();
        codeItemListHelper( "i = 0;" );
        codeItemListHelper( "if (ok)" );
        codeItemListHelper( "if (exit) {" );
        codeItemListHelper( "if (3 == 4);" );
        codeItemListHelper( "if (4 == 4) { ;" );
        codeItemListHelper( "} else {" );
        getSink().list_();
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        tableCellHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.table.label" ) );
        codeCellHelper( "fine :" );
        getSink().tableRow_();

        getSink().table_();
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ncss.paragraph3" ) );

        // CCN Explanation
        subtitleHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.title" ) );
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.paragraph1" ) );
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.paragraph2" ) );
        getSink().list();
        codeItemListHelper( "if" );
        codeItemListHelper( "for" );
        codeItemListHelper( "while" );
        codeItemListHelper( "case" );
        codeItemListHelper( "catch" );
        getSink().list_();
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.paragraph3" ) );
        getSink().list();
        codeItemListHelper( "if" );
        codeItemListHelper( "for" );
        getSink().list_();
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.paragraph4" ) );
        paragraphHelper( getResourceBundle().getString( "report.javancss.explanation.ccn.paragraph5" ) );
    }

    // print out the navigation bar
    private void navigationBar()
    {
        getSink().paragraph();
        getSink().text( "[ " );
        getSink().link( "#" + getResourceBundle().getString( "report.javancss.package.link" ) );
        getSink().text( getResourceBundle().getString( "report.javancss.package.link" ) );
        getSink().link_();
        getSink().text( " ] [ " );
        getSink().link( "#" + getResourceBundle().getString( "report.javancss.object.link" ) );
        getSink().text( getResourceBundle().getString( "report.javancss.object.link" ) );
        getSink().link_();
        getSink().text( " ] [ " );
        getSink().link( "#" + getResourceBundle().getString( "report.javancss.function.link" ) );
        getSink().text( getResourceBundle().getString( "report.javancss.function.link" ) );
        getSink().link_();
        getSink().text( " ] [ " );
        getSink().link( "#" + getResourceBundle().getString( "report.javancss.explanation.link" ) );
        getSink().text( getResourceBundle().getString( "report.javancss.explanation.link" ) );
        getSink().link_();
        getSink().text( " ]" );
        getSink().paragraph_();
    }

    // sink helper to start a section
    protected void startSection( String link, String title )
    {
        super.startSection( link, title );
        navigationBar();
    }

    protected void jxrLink( String clazz )
    {
        if ( xrefLocation != null )
        {
            getSink().link( xrefLocation + "/" + clazz.replace( '.', '/' ) + ".html" );
        }
        getSink().text( clazz );
        if ( xrefLocation != null )
        {
            getSink().link_();
        }
    }

    protected void jxrFunctionLink( String clazz )
    {
        int indexDot = -1;
        if ( xrefLocation != null )
        {
            indexDot = clazz.lastIndexOf( '.' );
            if ( indexDot != -1 )
            {
                getSink().link( xrefLocation + "/" + clazz.substring( 0, indexDot ).replace( '.', '/' ) + ".html" );
            }
        }
        getSink().text( clazz );
        if ( xrefLocation != null && indexDot != -1 )
        {
            getSink().link_();
        }
    }

}
