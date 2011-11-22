package org.codehaus.mojo.surefire;

import org.apache.maven.reporting.MavenReportException;

import org.codehaus.doxia.sink.Sink;

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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;

public class SurefireReportGenerator
{
    private SurefireReportParser report;
    private List testSuites;

    public SurefireReportGenerator( File reportsDirectory )
    {
        report = new SurefireReportParser( reportsDirectory );
    }

    public void doGenerateReport( ResourceBundle bundle, Sink sink )
                          throws MavenReportException
    {
        testSuites = report.parseXMLReportFiles(  );

        try
        {
            sink.head(  );

            sink.text( bundle.getString( "report.surefire.description" ) );

            sink.head_(  );

            sink.body(  );

            constructSummarySection( bundle, sink );

            constructPackagesSection( bundle, sink );

            constructTestCasesSection( bundle, sink );

            constructFailureDetails( sink, testSuites, bundle );

            sinkLineBreak( sink );

            sink.body_(  );

            sink.flush(  );

            sink.close(  );
        } catch ( Exception e )
        {
            e.printStackTrace(  );
        }
    }

    private void constructSummarySection( ResourceBundle bundle, Sink sink )
    {
        Map summary = report.getSummary( testSuites );
        
        sink.sectionTitle1(  );

        sinkAnchor( sink, "Summary" );

        sink.text( bundle.getString( "report.surefire.label.summary" ) );

        sink.sectionTitle1_(  );

        constructHotLinks( sink, bundle );

        sinkLineBreak( sink );

        sink.table(  );

        sink.tableRow(  );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.tests" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.errors" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.failures" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.successrate" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.time" ) );

        sink.tableRow_(  );

        sink.tableRow(  );

        sinkCell( sink, (String) summary.get( "totalTests" ) );

        sinkCell( sink, (String) summary.get( "totalErrors" ) );

        sinkCell( sink, (String) summary.get( "totalFailures" ) );

        sinkCell( sink, (String) summary.get( "totalPercentage" ) + "%" );

        sinkCell( sink, (String) summary.get( "totalElapsedTime" ) );

        sink.tableRow_(  );

        sink.table_(  );

        sink.lineBreak(  );

        sink.rawText( bundle.getString( "report.surefire.text.note1" ) );

        sinkLineBreak( sink );
    }

    private void constructPackagesSection( ResourceBundle bundle, Sink sink )
    {
        HashMap suitePackages = report.getSuitesGroupByPackage( testSuites );

        DecimalFormat decFormat = new DecimalFormat("##0.00");
        
        if ( suitePackages.isEmpty(  ) )
        {
            return;
        }

        sink.sectionTitle1(  );

        sinkAnchor( sink, "Package_List" );

        sink.text( bundle.getString( "report.surefire.label.packagelist" ) );

        sink.sectionTitle1_(  );

        constructHotLinks( sink, bundle );

        sinkLineBreak( sink );

        sink.table(  );

        sink.tableRow(  );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.package" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.tests" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.errors" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.failures" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.successrate" ) );

        sinkHeader( sink,
                    bundle.getString( "report.surefire.label.time" ) );

        sink.tableRow_(  );

        Iterator packIter = suitePackages.keySet(  ).iterator(  );

        while ( packIter.hasNext(  ) )
        {
            sink.tableRow(  );

            String packageName = (String) packIter.next(  );

            List testSuiteList = (List) suitePackages.get( packageName );

            Map packageSummary = report.getSummary( testSuiteList );

            sinkCellLink( sink, packageName, "#" + packageName );

            sinkCell( sink, (String) packageSummary.get( "totalTests" ) );

            sinkCell( sink, (String) packageSummary.get( "totalErrors" ) );

            sinkCell( sink, (String) packageSummary.get( "totalFailures" ) );

            sinkCell( sink, (String) packageSummary.get( "totalPercentage" ) + "%" );

            sinkCell( sink, (String) packageSummary.get( "totalElapsedTime" ) );

            sink.tableRow_(  );
        }

        sink.table_(  );

        sink.lineBreak(  );

        sink.rawText( bundle.getString( "report.surefire.text.note2" ) );

        packIter = suitePackages.keySet(  ).iterator(  );

        while ( packIter.hasNext(  ) )
        {
            String packageName = (String) packIter.next(  );

            List testSuiteList = (List) suitePackages.get( packageName );

            Iterator suiteIterator = testSuiteList.iterator(  );

            sink.sectionTitle2(  );

            sinkAnchor( sink, packageName );

            sink.text( packageName );

            sink.sectionTitle2_(  );

            sink.table(  );

            sink.tableRow(  );

            sinkHeader( sink, "" );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.class" ) );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.tests" ) );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.errors" ) );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.failures" ) );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.successrate" ) );

            sinkHeader( sink,
                        bundle.getString( "report.surefire.label.time" ) );

            sink.tableRow_(  );

            while ( suiteIterator.hasNext(  ) )
            {
                ReportTestSuite suite = (ReportTestSuite) suiteIterator.next(  );

                sink.tableRow(  );

                sink.tableCell(  );

                sink.link( "#" + suite.getPackageName(  ) + suite.getName(  ) );

                if ( suite.getNumberOfErrors(  ) > 0 )
                {
                    sinkIcon( "error", sink );
                } else if ( suite.getNumberOfFailures(  ) > 0 )
                {
                    sinkIcon( "junit.framework", sink );
                } else
                {
                    sinkIcon( "success", sink );
                }

                sink.link_(  );

                sink.tableCell_(  );

                sinkCellLink( sink,
                              suite.getName(  ),
                              "#" + suite.getPackageName(  ) + suite.getName(  ) );

                sinkCell( sink,
                          Integer.toString( suite.getNumberOfTests(  ) ) );

                sinkCell( sink,
                          Integer.toString( suite.getNumberOfErrors(  ) ) );

                sinkCell( sink,
                          Integer.toString( suite.getNumberOfFailures(  ) ) );

                String percentage =
                    report.computePercentage( suite.getNumberOfTests(  ),
                                              suite.getNumberOfErrors(  ),
                                              suite.getNumberOfFailures(  ) );
                sinkCell( sink, percentage + "%" );

                sinkCell( sink,
                          decFormat.format( suite.getTimeElapsed(  ) ) );

                sink.tableRow_(  );
            }

            sink.table_(  );
        }

        sinkLineBreak( sink );
    }

    private void constructTestCasesSection( ResourceBundle bundle, Sink sink )
    {
        if ( testSuites.isEmpty(  ) )
        {
            return;
        }

        DecimalFormat decFormat = new DecimalFormat("##0.00");
        
        sink.sectionTitle1(  );

        sinkAnchor( sink, "Test_Cases" );

        sink.text( bundle.getString( "report.surefire.label.testcases" ) );

        sink.sectionTitle1_(  );

        constructHotLinks( sink, bundle );

        ListIterator suiteIterator = testSuites.listIterator(  );

        while ( suiteIterator.hasNext(  ) )
        {
            ReportTestSuite suite = (ReportTestSuite) suiteIterator.next(  );

            List testCases = suite.getTestCases(  );

            ListIterator caseIterator = testCases.listIterator(  );

            sink.sectionTitle2(  );

            sinkAnchor( sink, suite.getPackageName(  ) + suite.getName(  ) );

            sink.text( suite.getName(  ) );

            sink.sectionTitle2_(  );

            sink.table(  );

            while ( caseIterator.hasNext(  ) )
            {
                ReportTestCase testCase = (ReportTestCase) caseIterator.next(  );

                sink.tableRow(  );

                sink.tableCell(  );

                if ( ( testCase.getFailure(  ) != null ) )
                {
                    sink.link( "#" + (String) testCase.getFullName(  ) );

                    sinkIcon( (String) testCase.getFailure(  ).get( "type" ), sink );

                    sink.link_(  );
                } else
                {
                    sinkIcon( "success", sink );
                }

                sink.tableCell_(  );

                sinkCell( sink,
                          testCase.getName(  ) );

                sinkCell( sink,
                          decFormat.format( testCase.getTime(  ) ) );

                sink.tableRow_(  );

                if ( testCase.getFailure(  ) != null )
                {
                    sink.tableRow(  );

                    sinkCell( sink, "" );

                    sinkCell( sink, (String) testCase.getFailure(  ).get( "message" ) );

                    sinkCell( sink, "" );

                    sink.tableRow_(  );
                }
            }

            sink.table_(  );
        }

        sinkLineBreak( sink );
    }

    private void constructFailureDetails( Sink sink, List testSuiteList, ResourceBundle bundle )
    {
        List failureList = report.getFailureDetails( testSuiteList );

        if ( failureList.isEmpty(  ) )
        {
            return;
        }

        Iterator failIter = failureList.iterator(  );

        if ( failIter != null )
        {
            sink.sectionTitle1(  );

            sinkAnchor( sink, "Failure_Details" );

            sink.text( bundle.getString( "report.surefire.label.failuredetails" ) );

            sink.sectionTitle1_(  );

            constructHotLinks( sink, bundle );
            
            sinkLineBreak( sink );

            sink.table(  );

            while ( failIter.hasNext(  ) )
            {
                ReportTestCase tCase = (ReportTestCase) failIter.next(  );

                sink.tableRow(  );

                sink.tableCell(  );

                sinkIcon( (String) tCase.getFailure(  ).get( "type" ), sink );

                sink.tableCell_(  );

                sinkCellAnchor( sink,
                                tCase.getName(  ),
                                tCase.getFullName(  ) );

                sink.tableRow_(  );

                sink.tableRow(  );

                sinkCell( sink, "" );

                sinkCell( sink, (String) tCase.getFailure(  ).get( "message" ) );

                sink.tableRow_(  );

                sink.tableRow(  );

                sinkCell( sink, "" );

                sinkCell( sink,
                          (String) tCase.getFailure(  ).get( "type" ) + ": " +
                          (String) tCase.getFailure(  ).get( "message" ) + " " +
                          (String) tCase.getFailure(  ).get( "detail" ) );

                sink.tableRow_(  );
            }

            sink.table_(  );
        }

        sinkLineBreak( sink );
    }

    private void constructHotLinks( Sink sink, ResourceBundle bundle )
    {
        if ( testSuites.isEmpty(  ) )
        {
            return;
        }

        sink.section2(  );

        sink.rawText( "[" );
        sinkLink( sink,
                  bundle.getString( "report.surefire.label.summary" ),
                  "#Summary" );
        sink.rawText( "]" );

        sink.rawText( "[" );
        sinkLink( sink,
                  bundle.getString( "report.surefire.label.packagelist" ),
                  "#Package_List" );
        sink.rawText( "]" );

        sink.rawText( "[" );
        sinkLink( sink,
                  bundle.getString( "report.surefire.label.testcases" ),
                  "#Test_Cases" );
        sink.rawText( "]" );
        sink.section2_(  );
    }

    private void sinkLineBreak( Sink sink )
    {
        sink.table(  );
        sink.tableRow(  );
        sink.tableRow_(  );
        sink.tableRow(  );
        sink.tableRow_(  );
        sink.table_(  );
    }

    private void sinkIcon( String type, Sink sink )
    {
        sink.figure(  );

        if ( type.startsWith( "junit.framework" ) )
        {
            sink.figureGraphics( "images/icon_warning_sml.gif" );
        } else if ( type.startsWith( "success" ) )
        {
            sink.figureGraphics( "images/icon_success_sml.gif" );
        } else
        {
            sink.figureGraphics( "images/icon_error_sml.gif" );
        }

        sink.figure_(  );
    }

    private void sinkHeader( Sink sink, String header )
    {
        sink.tableHeaderCell(  );
        sink.text( header );
        sink.tableHeaderCell_(  );
    }

    private void sinkCell( Sink sink, String text )
    {
        sink.tableCell(  );
        sink.text( text );
        sink.tableCell_(  );
    }

    private void sinkLink( Sink sink, String text, String link )
    {
        sink.link( link );
        sink.text( text );
        sink.link_(  );
    }

    private void sinkCellLink( Sink sink, String text, String link )
    {
        sink.tableCell(  );
        sinkLink( sink, text, link );
        sink.tableCell_(  );
    }

    private void sinkCellAnchor( Sink sink, String text, String anchor )
    {
        sink.tableCell(  );
        sinkAnchor( sink, anchor );
        sink.text( text );
        sink.tableCell_(  );
    }

    private void sinkAnchor( Sink sink, String anchor )
    {
        sink.anchor( anchor );
        sink.anchor_(  );
    }
}
