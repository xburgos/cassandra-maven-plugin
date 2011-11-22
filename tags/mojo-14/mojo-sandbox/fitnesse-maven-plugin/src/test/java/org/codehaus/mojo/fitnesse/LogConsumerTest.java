package org.codehaus.mojo.fitnesse;

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LogConsumerTest extends MockObjectTestCase
{
    public void testConsumeLine()
    {
    	Mock tMockLog = mock( Log.class );
        tMockLog.stubs().method("info").withAnyArguments();
        Log tLogProxy = (Log)tMockLog.proxy();
    	
        LogConsumer tConsumer = new LogConsumer( tLogProxy, Level.INFO );
        assertFalse( tConsumer.hasGeneratedResultFile() );

        tConsumer.consumeLine( "...." );
        assertFalse( tConsumer.hasGeneratedResultFile() );
        tConsumer.consumeLine( "TestSimpleClass1 has failures" );
        assertFalse( tConsumer.hasGeneratedResultFile() );
        tConsumer.consumeLine( "Test Pages: 0 right, 1 wrong, 0 ignored, 0 exceptions" );
        assertFalse( tConsumer.hasGeneratedResultFile() );
        tConsumer.consumeLine( "Assertions: 4 right, 1 wrong, 0 ignored, 0 exceptions" );
        assertFalse( tConsumer.hasGeneratedResultFile() );
        tConsumer.consumeLine( "Formatting as html to D:\\SCM\\ProjectSVN\\maven-fitnesse-plugin\\src\\it\\multiproject\\target/fitnesse/fitnesseResultSuiteCoverage2.html" );
        assertTrue( tConsumer.hasGeneratedResultFile() );
        tConsumer.consumeLine( "------------------------------------------------------------------------" );
        assertTrue( tConsumer.hasGeneratedResultFile() );
    }

}
