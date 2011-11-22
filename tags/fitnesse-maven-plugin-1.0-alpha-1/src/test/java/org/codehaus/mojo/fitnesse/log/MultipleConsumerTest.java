package org.codehaus.mojo.fitnesse.log;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class MultipleConsumerTest extends MockObjectTestCase
{

    public void testConsumeLine()
    {
        String tStrA = "AaAaAa";
        String tStrB = "Bababa";

        Mock tMockLog = mock( LogConsumer.class );
        Mock tMockFile = mock( FileConsumer.class );
        tMockLog.expects( once() ).method( "consumeLine" ).with( eq( tStrA ) );
        tMockLog.expects( once() ).method( "consumeLine" ).with( eq( tStrB ) );
        tMockFile.expects( once() ).method( "consumeLine" ).with( eq( tStrA ) );
        tMockFile.expects( once() ).method( "consumeLine" ).with( eq( tStrB ) );

        LogConsumer tLog = (LogConsumer) tMockLog.proxy();
        FileConsumer tFile = (FileConsumer) tMockFile.proxy();
        MultipleConsumer tMult = new MultipleConsumer( tLog, tFile );

        tMult.consumeLine( tStrA );
        tMult.consumeLine( tStrB );

        tMockLog.verify();
        tMockFile.verify();
    }

}
