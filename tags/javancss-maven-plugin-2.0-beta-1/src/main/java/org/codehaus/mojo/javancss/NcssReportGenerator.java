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
import java.util.Locale;
import java.util.ResourceBundle;

import org.codehaus.doxia.sink.Sink;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Generates the javaNcss maven report.
 * 
 * @author <a href="jeanlaurent@gmail.com">Jean-Laurent de Morlhon</a>
 */
public class NcssReportGenerator
{
    private ResourceBundle bundle;

    private Sink sink;

    private int lineThreshold;

    /**
     * build a new NcssReportGenerator.
     * 
     * @param sink the sink which will be used for reporting.
     * @param bundle the correct RessourceBundle to be used for reporting.
     */
    public NcssReportGenerator( Sink sink, ResourceBundle bundle )
    {
        this.bundle = bundle;
        this.sink = sink;
    }

    /**
     * Generates the JavaNcss reports.
     * 
     * @param locale the Locale used for this report.
     * @param document the javaNcss raw report as an XML document.
     * @param lineThreshold the maximum number of lines to keep in major reports.
     */
    public void doReport( Locale locale, Document document, int lineThreshold )
    {
        this.lineThreshold = lineThreshold;
        // HEADER
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.javancss.title" ) );
        sink.title_();
        sink.head_();
        // BODY
        sink.body();
        doIntro( locale );
        // packages
        startSection( locale, "report.javancss.package.link", "report.javancss.package.title" );
        doMainPackageAnalysis( locale, document );
        doTotalPackageAnalysis( locale, document );
        endSection();
        // Objects
        startSection( locale, "report.javancss.object.link", "report.javancss.object.title" );
        doTopObjectNcss( locale, document );
        doTopObjectFunctions( locale, document );
        doObjectAverage( locale, document );
        endSection();
        // Functions
        startSection( locale, "report.javancss.function.link", "report.javancss.function.title" );
        doFunctionAnalysis( locale, document );
        doFunctionAverage( locale, document );
        endSection();
        // Explanation
        startSection( locale, "report.javancss.explanation.link", "report.javancss.explanation.title" );
        doExplanation( locale );
        endSection();
        sink.body_();
    }

    private void doIntro( Locale locale )
    {
        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.javancss.main.title" ) );
        sink.sectionTitle1_();
        sink.paragraph();
        navigationBar( locale );
        sink.text( bundle.getString( "report.javancss.main.text" ) + " " );
        sink.lineBreak();
        sink.link( "http://www.kclee.de/clemens/java/javancss/" );
        sink.text( "JavaNCSS web site." );
        sink.link_();
        sink.paragraph_();
        sink.section1_();
    }

    private void doMainPackageAnalysis( Locale locale, Document document )
    {
        subtitleHelper( bundle.getString( "report.javancss.package.text" ) );
        sink.table();
        sink.tableRow();
        // HEADER
        headerCellHelper( bundle.getString( "report.javancss.header.package" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.classe" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.function" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ncss" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc_line" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.single_comment" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.multi_comment" ) );
        sink.tableRow_();
        // DATA
        List list = document.selectNodes( "//javancss/packages/package" );
        Collections.sort( list, new NumericNodeComparator( "ncss" ) );
        Iterator nodeIterator = list.iterator();
        while ( nodeIterator.hasNext() )
        {
            Node node = (Node) nodeIterator.next();
            sink.tableRow();
            tableCellHelper( node.valueOf( "name" ) );
            tableCellHelper( node.valueOf( "classes" ) );
            tableCellHelper( node.valueOf( "functions" ) );
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            tableCellHelper( node.valueOf( "javadoc_lines" ) );
            tableCellHelper( node.valueOf( "single_comment_lines" ) );
            tableCellHelper( node.valueOf( "multi_comment_lines" ) );
            sink.tableRow_();
        }
        sink.table_();
    }

    private void doTotalPackageAnalysis( Locale locale, Document document )
    {
        sink.table();
        sink.tableRow();
        headerCellHelper( bundle.getString( "report.javancss.header.classetotal" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.functiontotal" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ncsstotal" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc_line" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.single_comment" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.multi_comment" ) );
        sink.tableRow_();
        Node node = document.selectSingleNode( "//javancss/packages/total" );
        sink.tableRow();
        tableCellHelper( node.valueOf( "classes" ) );
        tableCellHelper( node.valueOf( "functions" ) );
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        tableCellHelper( node.valueOf( "javadoc_lines" ) );
        tableCellHelper( node.valueOf( "single_comment_lines" ) );
        tableCellHelper( node.valueOf( "multi_comment_lines" ) );
        sink.tableRow_();
        sink.table_();
    }

    private void doTopObjectNcss( Locale locale, Document document )
    {
        subtitleHelper( bundle.getString( "report.javancss.top" ) + " " + lineThreshold + " "
            + bundle.getString( "report.javancss.object.byncss" ) );
        List nodeList = document.selectNodes( "//javancss/objects/object" );
        Collections.sort( nodeList, new NumericNodeComparator( "ncss" ) );
        doTopObjectGeneric( locale, nodeList );
    }

    private void doTopObjectFunctions( Locale locale, Document document )
    {
        subtitleHelper( bundle.getString( "report.javancss.top" ) + " " + lineThreshold + " "
            + bundle.getString( "report.javancss.object.byfunction" ) );
        List nodeList = document.selectNodes( "//javancss/objects/object" );
        Collections.sort( nodeList, new NumericNodeComparator( "functions" ) );
        doTopObjectGeneric( locale, nodeList );
    }

    // generic method called by doTopObjectFunctions & doTopObjectNCss
    private void doTopObjectGeneric( Locale locale, List nodeList )
    {
        sink.table();
        sink.tableRow();
        headerCellHelper( bundle.getString( "report.javancss.header.object" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ncss" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.function" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.classe" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc" ) );
        sink.tableRow_();
        Iterator nodeIterator = nodeList.iterator();
        int i = 0;
        while ( nodeIterator.hasNext() && ( i++ < lineThreshold ) )
        {
            Node node = (Node) nodeIterator.next();
            sink.tableRow();
            tableCellHelper( node.valueOf( "name" ) );
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "functions" ) );
            tableCellHelper( node.valueOf( "classes" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            sink.tableRow_();
        }
        sink.table_();
    }

    private void doObjectAverage( Locale locale, Document document )
    {
        sink.bold();
        sink.text( bundle.getString( "report.javancss.averages" ) );
        sink.bold_();
        sink.table();
        sink.tableRow();
        headerCellHelper( bundle.getString( "report.javancss.header.ncssaverage" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.programncss" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.classeaverage" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.functionaverage" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadocaverage" ) );
        sink.tableRow_();
        Node node = document.selectSingleNode( "//javancss/objects/averages" );
        String totalNcss = document.selectSingleNode( "//javancss/objects/ncss" ).getText();
        sink.tableRow();
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( totalNcss );
        tableCellHelper( node.valueOf( "classes" ) );
        tableCellHelper( node.valueOf( "functions" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        sink.tableRow_();
        sink.table_();
    }

    private void doFunctionAnalysis( Locale locale, Document document )
    {
        subtitleHelper( bundle.getString( "report.javancss.top" ) + " " + lineThreshold + " "
            + bundle.getString( "report.javancss.function.byncss" ) );
        sink.paragraph();
        sink.table();
        sink.tableRow();
        headerCellHelper( bundle.getString( "report.javancss.header.function" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ncss" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ccn" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadoc" ) );
        sink.tableRow_();
        List list = document.selectNodes( "//javancss/functions/function" );
        Collections.sort( list, new NumericNodeComparator( "ncss" ) );
        Iterator nodeIterator = list.iterator();
        int i = 0;
        while ( nodeIterator.hasNext() && ( i++ < lineThreshold ) )
        {
            Node node = (Node) nodeIterator.next();
            sink.tableRow();
            tableCellHelper( node.valueOf( "name" ) );
            tableCellHelper( node.valueOf( "ncss" ) );
            tableCellHelper( node.valueOf( "ccn" ) );
            tableCellHelper( node.valueOf( "javadocs" ) );
            sink.tableRow_();
        }
        sink.table_();
        sink.paragraph_();
    }

    private void doFunctionAverage( Locale locale, Document document )
    {
        subtitleHelper( bundle.getString( "report.javancss.averages" ) );
        sink.paragraph();
        sink.table();
        sink.tableRow();
        headerCellHelper( bundle.getString( "report.javancss.header.programncss" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ncssaverage" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.ccnaverage" ) );
        headerCellHelper( bundle.getString( "report.javancss.header.javadocaverage" ) );
        sink.tableRow_();
        Node node = document.selectSingleNode( "//javancss/functions/function_averages" );
        String totalNcss = document.selectSingleNode( "//javancss/functions/ncss" ).getText();
        sink.tableRow();
        tableCellHelper( totalNcss );
        tableCellHelper( node.valueOf( "ncss" ) );
        tableCellHelper( node.valueOf( "ccn" ) );
        tableCellHelper( node.valueOf( "javadocs" ) );
        sink.tableRow_();
        sink.table_();
        sink.paragraph_();
    }

    private void doExplanation( Locale locale )
    {
        subtitleHelper( bundle.getString( "report.javancss.explanation.ncss.title" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ncss.paragraph1" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ncss.paragraph2" ) );
        sink.table();
        sink.tableRow();
        headerCellHelper( "" );
        headerCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.examples" ) );
        headerCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.comments" ) );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.package" ) );
        codeCellHelper( "package java.lang;" );
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.import" ) );
        codeCellHelper( "import java.awt.*;" );
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.class" ) );
        sink.tableCell();
        sink.list();
        codeItemListHelper( "public class Foo {" );
        codeItemListHelper( "public class Foo extends Bla {" );
        sink.list_();
        sink.tableCell_();
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.interface" ) );
        codeCellHelper( "public interface Able ; {" );
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.field" ) );
        sink.tableCell();
        sink.list();
        codeItemListHelper( "int a; " );
        codeItemListHelper( "int a, b, c = 5, d = 6;" );
        sink.list_();
        sink.tableCell_();
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.method" ) );
        sink.tableCell();
        sink.list();
        codeItemListHelper( "public void cry();" );
        codeItemListHelper( "public void gib() throws DeadException {" );
        sink.list_();
        sink.tableCell_();
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.constructorD" ) );
        codeCellHelper( "public Foo() {" );
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.constructorI" ) );
        sink.tableCell();
        sink.list();
        codeItemListHelper( "this();" );
        codeItemListHelper( "super();" );
        sink.list_();
        sink.tableCell_();
        tableCellHelper( "" );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.statement" ) );
        sink.tableCell();
        sink.list();
        codeItemListHelper( "i = 0;" );
        codeItemListHelper( "if (ok)" );
        codeItemListHelper( "if (exit) {" );
        codeItemListHelper( "if (3 == 4);" );
        codeItemListHelper( "if (4 == 4) { ;" );
        codeItemListHelper( "} else {" );
        sink.list_();
        sink.tableCell_();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.statement.comment" ) );
        sink.tableRow_();
        sink.tableRow();
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.label" ) );
        codeCellHelper( "fine :" );
        tableCellHelper( bundle.getString( "report.javancss.explanation.ncss.table.label.comment" ) );
        sink.tableRow_();
        sink.table_();
        paragraphHelper( bundle.getString( "report.javancss.explanation.ncss.paragraph3" ) );
        subtitleHelper( bundle.getString( "report.javancss.explanation.ccn.title" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph1" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph1b" ) );
        sink.list();
        codeItemListHelper( "if" );
        codeItemListHelper( "for" );
        codeItemListHelper( "while" );
        codeItemListHelper( "case" );
        codeItemListHelper( "catch" );
        sink.list_();
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph1c" ) );
        sink.list();
        codeItemListHelper( "if" );
        codeItemListHelper( "for" );
        sink.list_();
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph1d" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph2" ) );
        paragraphHelper( bundle.getString( "report.javancss.explanation.ccn.paragraph3" ) );
    }

    // sink helper to start a section
    private void startSection( Locale locale, String link, String title )
    {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor( bundle.getString( link ) );
        sink.text( bundle.getString( title ) );
        sink.anchor_();
        sink.sectionTitle1_();
        sink.paragraph();
        navigationBar( locale );
    }

    // sink helper to end a section
    private void endSection()
    {
        sink.paragraph_();
        sink.section1_();
    }

    // print out the navigation bar
    private void navigationBar( Locale locale )
    {
        sink.paragraph();
        sink.text( "[ " );
        sink.link( "#" + bundle.getString( "report.javancss.package.link" ) );
        sink.text( bundle.getString( "report.javancss.package.link" ) );
        sink.link_();
        sink.text( " ] [ " );
        sink.link( "#" + bundle.getString( "report.javancss.object.link" ) );
        sink.text( bundle.getString( "report.javancss.object.link" ) );
        sink.link_();
        sink.text( " ] [ " );
        sink.link( "#" + bundle.getString( "report.javancss.function.link" ) );
        sink.text( bundle.getString( "report.javancss.function.link" ) );
        sink.link_();
        sink.text( " ] [ " );
        sink.link( "#" + bundle.getString( "report.javancss.explanation.link" ) );
        sink.text( bundle.getString( "report.javancss.explanation.link" ) );
        sink.link_();
        sink.text( " ]" );
        sink.paragraph_();
    }

    // sink helper to write a "code" itemList
    private void codeItemListHelper( String text )
    {
        sink.listItem();
        sink.monospaced();
        sink.text( text );
        sink.monospaced_();
        sink.listItem_();
    }

    // sink helper to write a paragrah
    private void paragraphHelper( String text )
    {
        sink.paragraph();
        sink.text( text );
        sink.paragraph_();
    }

    // sink helper to write a subtitle
    private void subtitleHelper( String text )
    {
        sink.paragraph();
        sink.bold();
        sink.text( text );
        sink.bold_();
        sink.paragraph_();
    }

    // sink helper to write cell containing code
    private void codeCellHelper( String text )
    {
        sink.tableCell();
        sink.monospaced();
        sink.text( text );
        sink.monospaced_();
        sink.tableCell_();
    }

    // sink helper to write a simple table header cell
    private void headerCellHelper( String text )
    {
        sink.tableHeaderCell();
        sink.text( text );
        sink.tableHeaderCell_();
    }

    // sink helper to write a simple tabke cell
    private void tableCellHelper( String text )
    {
        sink.tableCell();
        sink.text( text );
        sink.tableCell_();
    }

}
