package org.codehaus.mojo.surefire;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SurefireReportParserTest
    extends TestCase
{
    SurefireReportParser report;
    File testFile;

    public SurefireReportParserTest( String testName )
    {
        super( testName );
    }

    protected void setUp(  )
                  throws Exception
    {
        report = new SurefireReportParser(  );
    }

    protected void tearDown(  )
                     throws Exception
    {
    }

    public static Test suite(  )
    {
        TestSuite suite = new TestSuite( SurefireReportParserTest.class );

        return suite;
    }

    public void testParseXMLReportFiles(  )
    {
        report.setReportsDirectory( new File( System.getProperty( "basedir" ),
                                              "target/test-classes/test-reports" ) );

        List suites = report.parseXMLReportFiles(  );

        assertEquals( suites.size(  ),
                      2 );
    }

    public void testParseTestSuiteName(  )
    {
        assertEquals( "CircleTest",
                      report.parseTestSuiteName( "Battery: com.shape.CircleTest" ) );
    }

    public void testParseTestSuitePackageName(  )
    {
        assertEquals( "com.shape",
                      report.parseTestSuitePackageName( "Battery: com.shape.CircleTest" ) );
    }

    public void testParseTestCaseName(  )
    {
        assertTrue( "testCase".equals( report.parseTestCaseName( "testCase(com.shape.CircleTest)" ) ) );
    }

    public void testGetSummary(  )
    {
        ReportTestSuite tSuite1 = new ReportTestSuite(  );

        ReportTestSuite tSuite2 = new ReportTestSuite(  );

        tSuite1.setNumberOfErrors( 10 );

        tSuite1.setNumberOfFailures( 20 );

        tSuite1.setTimeElapsed( 1.0f );

        tSuite1.setNumberOfTests( 100 );

        tSuite2.setNumberOfErrors( 10 );

        tSuite2.setNumberOfFailures( 20 );

        tSuite2.setTimeElapsed( 1.0f );

        tSuite2.setNumberOfTests( 100 );

        List suiteList = new ArrayList(  );

        suiteList.add( tSuite1 );

        suiteList.add( tSuite2 );

        Map testMap = report.getSummary( suiteList );

        assertEquals( 20,
                      Integer.parseInt( testMap.get( "totalErrors" ).toString(  ) ) );

        assertEquals( 40,
                      Integer.parseInt( testMap.get( "totalFailures" ).toString(  ) ) );

        assertEquals( 200,
                      Integer.parseInt( testMap.get( "totalTests" ).toString(  ) ) );

        assertEquals( 2.0f,
                      Float.parseFloat( testMap.get( "totalElapsedTime" ).toString(  ) ),
                      0.0f );

        assertEquals( "70.00", (String) testMap.get( "totalPercentage" ) );
    }

    public void testSetReportsDirectory(  )
    {
        report.setReportsDirectory( new File( "Reports_Directory" ) );

        assertEquals( new File( "Reports_Directory" ),
                      report.getReportsDirectory(  ) );
    }

    public void testGetSuitesGroupByPackage(  )
    {
        ReportTestSuite tSuite1 = new ReportTestSuite(  );

        ReportTestSuite tSuite2 = new ReportTestSuite(  );

        ReportTestSuite tSuite3 = new ReportTestSuite(  );

        tSuite1.setPackageName( "Package1" );

        tSuite2.setPackageName( "Package1" );

        tSuite3.setPackageName( "Package2" );

        List suiteList = new ArrayList(  );

        suiteList.add( tSuite1 );

        suiteList.add( tSuite2 );

        suiteList.add( tSuite3 );

        HashMap groupMap = report.getSuitesGroupByPackage( suiteList );

        assertEquals( 2,
                      groupMap.size(  ) );

        assertEquals( tSuite1,
                      ( (List) groupMap.get( "Package1" ) ).get( 0 ) );

        assertEquals( tSuite2,
                      ( (List) groupMap.get( "Package1" ) ).get( 1 ) );

        assertEquals( tSuite3,
                      ( (List) groupMap.get( "Package2" ) ).get( 0 ) );
    }

    public void testComputePercentage(  )
    {
        assertEquals( "70.00",
                      report.computePercentage( 100, 20, 10 ) );
    }

    public void testGetFailureDetails(  )
    {
        ReportTestSuite tSuite1 = new ReportTestSuite(  );

        ReportTestSuite tSuite2 = new ReportTestSuite(  );

        ReportTestCase tCase1 = new ReportTestCase(  );

        ReportTestCase tCase2 = new ReportTestCase(  );

        ReportTestCase tCase3 = new ReportTestCase(  );

        tCase1.setFailure( new HashMap(  ) );

        tCase3.setFailure( new HashMap(  ) );

        List tCaseList = new ArrayList(  );

        List tCaseList2 = new ArrayList(  );

        tCaseList.add( tCase1 );

        tCaseList.add( tCase2 );

        tCaseList2.add( tCase3 );

        tSuite1.setTestCases( tCaseList );

        tSuite2.setTestCases( tCaseList2 );

        List suiteList = new ArrayList(  );

        suiteList.add( tSuite1 );

        suiteList.add( tSuite2 );

        List failList = report.getFailureDetails( suiteList );

        assertEquals( 2,
                      failList.size(  ) );

        assertEquals( tCase1,
                      failList.get( 0 ) );

        assertEquals( tCase3,
                      failList.get( 1 ) );
    }
}