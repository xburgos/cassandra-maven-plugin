package org.codehaus.mojo.fitnesse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class FitnesseReportMojoTest extends MockObjectTestCase
{
	private Log mMockLog = null;

	protected void setUp()
	{
		Mock tMockLog = mock( Log.class );
        tMockLog.stubs().method("info").withAnyArguments();
        tMockLog.stubs().method("debug").withAnyArguments();
        mMockLog = (Log)tMockLog.proxy();
	}

    public void testCopyFile() throws IOException, MavenReportException
    {
        File tInFile = null;
        File tOutFile = null;
        try
        {
            tInFile = new File( "tempIn.txt" );
            assertTrue( !tInFile.exists() );
            assertTrue( tInFile.createNewFile() );
            FileWriter tWriter = new FileWriter( tInFile );
            tWriter.write( "Chaine bidon" );
            tWriter.close();

            tOutFile = new File( "tempOut.txt" );
            assertFalse( tOutFile.exists() );
            FitnesseReportMojo.copyFile( mMockLog, new FileInputStream( tInFile ), tOutFile );
            assertTrue( tOutFile.exists() );
        }
        finally
        {
            tOutFile.delete();
            tInFile.delete();
        }
    }

    public void testCheckReport() throws MavenReportException
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "bidon" ) );
        try
        {
            tMojo.checkReport();
            fail( "Should not pass checkReport" );
        }
        catch ( MavenReportException e )
        {
            assertTrue( e.getMessage().startsWith( "The directory doesn't contain any Fitnesse report: " ) );
            assertTrue( e.getMessage().endsWith( "fitnesse-maven-plugin\\bidon" ) );
        }

        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/reportDir/Empty/fitnesse" ) );
        try
        {
            tMojo.checkReport();
            fail( "Should not pass checkReport" );
        }
        catch ( MavenReportException e )
        {
            assertTrue( e.getMessage().startsWith( "The directory doesn't contain any Fitnesse report: " ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin\\target\\test-classes\\reportDir\\Empty\\fitnesse" ) );
        }

        tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/onlyOneReport/fitnesse" ) );
        tMojo.checkReport();

    }

    public void testCreateIndex() throws MavenReportException
    {
        File tFile = new File( "target/test-classes/onlyOneReport/fitnesse/index.html" );
        if ( tFile.exists() )
        {
            tFile.delete();
        }
        tFile = new File( "target/test-classes/multiReport/fitnesse/index.html" );
        if ( tFile.exists() )
        {
            tFile.delete();
        }

        FitnesseReportMojo tMojo = getMojo();
        tMojo.setOutputDirectory( new File( "target/test-classes/onlyOneReport/fitnesse" ) );
        new File( "target/test-classes/onlyOneReportXml/fitnesse" ).mkdirs();
        tMojo.setXmlOutputDirectory( new File( "target/test-classes/onlyOneReportXml/fitnesse" ) );
        tMojo.createIndex();
        assertFalse( new File( "target/test-classes/onlyOneReportXml/fitnesse/index.xml" ).exists() );

        tMojo.setOutputDirectory( new File( "target/test-classes/multiReport/fitnesse" ) );
        new File( "target/test-classes/multiReportXml/fitnesse" ).mkdirs();
        tMojo.setXmlOutputDirectory( new File( "target/test-classes/multiReportXml/fitnesse" ) );
        tMojo.createIndex();
        assertTrue( new File( "target/test-classes/multiReportXml/fitnesse/index.xml" ).exists() );
        assertTrue( new File( "target/test-classes/multiReportXml/fitnesse/index.xml" ).length() > 30 );
    }

    private FitnesseReportMojo getMojo()
    {
        FitnesseReportMojo tMojo = new FitnesseReportMojo();
        tMojo.setWorkingDir( new File( "target/fitnesse" ) );
        tMojo.setLog(mMockLog);
        return tMojo;
    }

    public void testGetFitnesseReportDirWithReport() throws MavenReportException
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/onlyOneReport/fitnesse/" ) );
        tMojo.setWorkingDir( new File( "." ) );
        assertTrue( tMojo.getFitnesseReportDir().exists() );
    }

    public void testGetFitnesseReportDirWithBadExpliciteReport()
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/badfolder/fitnesse/" ) );
        tMojo.setWorkingDir( new File( "." ) );
        try
        {
            tMojo.getFitnesseReportDir();
            fail( "Report file shouldn't be found" );
        }
        catch ( MavenReportException e )
        {
            assertTrue( e.getMessage().startsWith( "The directory doesn't contain any Fitnesse report: " ) );
            assertTrue( e.getMessage().endsWith( "fitnesse-maven-plugin\\target\\test-classes\\badfolder\\fitnesse" ) );
        }
    }

    public void testGetFitnesseReportDirWithoutReportAndWithoutClover() throws MavenReportException
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( null );
        tMojo.setWorkingDir( new File( "target/test-classes/reportDir/WithoutClover" ) );
        File tFile = tMojo.getFitnesseReportDir();
        assertEquals( "target\\test-classes\\reportDir\\WithoutClover\\fitnesse", "" + tFile );
    }

    public void testGetFitnesseReportDirWithoutReportAndWithClover() throws MavenReportException
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( null );
        tMojo.setWorkingDir( new File( "target/test-classes/reportDir/WithClover" ) );
        File tFile = tMojo.getFitnesseReportDir();
        assertEquals( "target\\test-classes\\reportDir\\WithClover\\clover\\fitnesse", "" + tFile );
    }

    public void testGetFitnesseReportDirWithEmptyReport()
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/reportDir/Empty/fitnesse" ) );
        tMojo.setWorkingDir( new File( "." ) );
        try
        {
            tMojo.getFitnesseReportDir();
            fail( "Report file shouldn't be found" );
        }
        catch ( MavenReportException e )
        {
            assertTrue( e.getMessage().startsWith( "The directory doesn't contain any Fitnesse report: " ) );
            assertTrue( e.getMessage().endsWith(
                                                 "fitnesse-maven-plugin\\target\\test-classes\\reportDir\\Empty\\fitnesse" ) );
        }
    }

    public void testGetOutputName()
    {
        FitnesseReportMojo tMojo = getMojo();
        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/onlyOneReport/fitnesse" ) );
        assertEquals( "fitnesse/fitnesseResult_localhost_SuiteCoverage3", tMojo.getOutputName() );

        tMojo.setFitnesseOutputDirectory( new File( "target/test-classes/multiReport/fitnesse" ) );
        assertEquals( "fitnesse/index", tMojo.getOutputName() );
    }

    public void testGetFitnessePageName() throws MavenReportException
    {
        FitnesseReportMojo tMojo = getMojo();
        assertEquals(
                      "localhost_SuiteCoverage3",
                      tMojo.getFitnessePageName( new File(
                                                           "target\\test-classes\\reportDir\\WithClover\\clover\\fitnesse\\fitnesseResult_localhost_SuiteCoverage3.html" ) ) );
        assertEquals(
                      "localhost_SuiteCoverage3",
                      tMojo.getFitnessePageName( new File(
                                                           "target\\test-classes\\onlyOneReport\\fitnesse\\fitnesseResult_localhost_SuiteCoverage3.html" ) ) );
    }
}
