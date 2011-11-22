package org.codehaus.mojo.surefire;

import junit.framework.*;

import java.util.ArrayList;
import java.util.List;

public class ReportTestSuiteTest
    extends TestCase
{
    ReportTestSuite tSuite;

    public ReportTestSuiteTest( String testName )
    {
        super( testName );
    }

    protected void setUp(  )
                  throws Exception
    {
        tSuite = new ReportTestSuite(  );
    }

    protected void tearDown(  )
                     throws Exception
    {
    }

    public static Test suite(  )
    {
        TestSuite suite = new TestSuite( ReportTestSuiteTest.class );

        return suite;
    }

    public void testSetTestCases(  )
    {
        ReportTestCase tCase = new ReportTestCase(  );

        List tCaseList = new ArrayList(  );

        tCaseList.add( tCase );

        tSuite.setTestCases( tCaseList );

        assertEquals( tCase, (ReportTestCase) tSuite.getTestCases(  ).get( 0 ) );
    }

    public void testSetNumberdOfErrors(  )
    {
        tSuite.setNumberOfErrors( 9 );

        assertEquals( 9,
                      tSuite.getNumberOfErrors(  ) );
    }

    public void testSetNumberOfFailures(  )
    {
        tSuite.setNumberOfFailures( 10 );

        assertEquals( 10,
                      tSuite.getNumberOfFailures(  ) );
    }

    public void testSetNumberOfTests(  )
    {
        tSuite.setNumberOfTests( 11 );

        assertEquals( 11,
                      tSuite.getNumberOfTests(  ) );
    }

    public void testSetName(  )
    {
        tSuite.setName( "Suite Name" );

        assertEquals( "Suite Name",
                      tSuite.getName(  ) );
    }

    public void testSetPackageName(  )
    {
        tSuite.setPackageName( "Suite Package Name" );

        assertEquals( "Suite Package Name",
                      tSuite.getPackageName(  ) );
    }

    public void testSetTimeElapsed(  )
    {
        tSuite.setTimeElapsed( .06f );

        assertTrue( .06f == tSuite.getTimeElapsed(  ) );
    }
}
