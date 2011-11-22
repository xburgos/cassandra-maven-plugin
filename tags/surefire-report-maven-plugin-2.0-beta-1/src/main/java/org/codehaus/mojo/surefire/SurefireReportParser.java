package org.codehaus.mojo.surefire;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

public class SurefireReportParser
{
    private File reportsDirectory;

    private List testSuites = new ArrayList();

    public SurefireReportParser()
    {
    }

    public SurefireReportParser( File reportsDirectory )
    {
        this.reportsDirectory = reportsDirectory;
    }

    public List parseXMLReportFiles()
    {
        ReportTestSuite testSuite;

        if ( !reportsDirectory.exists() )
        {
            return testSuites;
        }

        String[] xmlReportFiles = getIncludedFiles( reportsDirectory, "*.xml", "*.txt" );

        for ( int index = 0; index < xmlReportFiles.length; index++ )
        {
            testSuite = new ReportTestSuite( reportsDirectory + "/" + xmlReportFiles[index] );

            testSuites.add( testSuite );
        }

        return testSuites;
    }

    protected String parseTestSuiteName( String lineString )
    {
        return lineString.substring( lineString.lastIndexOf( "." ) + 1, lineString.length() );
    }

    protected String parseTestSuitePackageName( String lineString )
    {
        return lineString.substring( lineString.indexOf( ":" ) + 2, lineString.lastIndexOf( "." ) );
    }

    protected String parseTestCaseName( String lineString )
    {
        return lineString.substring( 0, lineString.indexOf( "(" ) );
    }

    public Map getSummary( List suites )
    {
        DecimalFormat decFormat = new DecimalFormat( "##0.00" );

        Map totalSummary = new HashMap();

        ListIterator iter = suites.listIterator();

        int totalNumberOfTests = 0;

        int totalNumberOfErrors = 0;

        int totalNumberOfFailures = 0;

        String totalPercentage = "";

        float totalElapsedTime = 0.0f;

        while ( iter.hasNext() )
        {
            ReportTestSuite suite = (ReportTestSuite) iter.next();

            totalNumberOfTests += suite.getNumberOfTests();

            totalNumberOfErrors += suite.getNumberOfErrors();

            totalNumberOfFailures += suite.getNumberOfFailures();

            totalElapsedTime += suite.getTimeElapsed();
        }

        totalPercentage = computePercentage( totalNumberOfTests, totalNumberOfErrors, totalNumberOfFailures );

        totalSummary.put( "totalTests", Integer.toString( totalNumberOfTests ) );

        totalSummary.put( "totalErrors", Integer.toString( totalNumberOfErrors ) );

        totalSummary.put( "totalFailures", Integer.toString( totalNumberOfFailures ) );

        totalSummary.put( "totalElapsedTime", decFormat.format( totalElapsedTime ) );

        totalSummary.put( "totalPercentage", totalPercentage );

        return totalSummary;
    }

    public void setReportsDirectory( File reportsDirectory )
    {
        this.reportsDirectory = reportsDirectory;
    }

    public File getReportsDirectory()
    {
        return this.reportsDirectory;
    }

    public HashMap getSuitesGroupByPackage( List testSuitesList )
    {
        ListIterator iter = testSuitesList.listIterator();

        HashMap suitePackage = new HashMap();

        while ( iter.hasNext() )
        {
            ReportTestSuite suite = (ReportTestSuite) iter.next();

            List suiteList = new ArrayList();

            if ( (List) suitePackage.get( suite.getPackageName() ) != null )
            {
                suiteList = (List) suitePackage.get( suite.getPackageName() );
            }

            suiteList.add( suite );

            suitePackage.put( suite.getPackageName(), suiteList );
        }

        return suitePackage;
    }

    public String computePercentage( int tests, int errors, int failures )
    {
        if ( tests == 0 )
        {
            return "0.00";
        }

        float percentage = ( (float) ( tests - errors - failures ) / (float) tests ) * 100;

        DecimalFormat decFormat = new DecimalFormat( "##0.00" );

        return decFormat.format( percentage );
    }

    public List getFailureDetails( List testSuitesList )
    {
        ListIterator iter = testSuitesList.listIterator();

        List failureDetailList = new ArrayList();

        while ( iter.hasNext() )
        {
            ReportTestSuite suite = (ReportTestSuite) iter.next();

            List testCaseList = suite.getTestCases();

            ListIterator caseIter = testCaseList.listIterator();

            while ( caseIter.hasNext() )
            {
                ReportTestCase tCase = (ReportTestCase) caseIter.next();

                if ( tCase.getFailure() != null )
                {
                    failureDetailList.add( tCase );
                }
            }
        }

        return failureDetailList;
    }

    private String[] getIncludedFiles( File directory, String includes, String excludes )
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( directory );

        scanner.setIncludes( StringUtils.split( includes, "," ) );

        scanner.setExcludes( StringUtils.split( excludes, "," ) );

        scanner.scan();

        String[] filesToFormat = scanner.getIncludedFiles();

        return filesToFormat;
    }
}
