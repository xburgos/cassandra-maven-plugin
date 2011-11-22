package org.codehaus.mojo.fitnesse.log;

import java.io.File;

import junit.framework.TestCase;

public class FileConsumerTest extends TestCase
{

    public void testConsumeLine()
    {
        File tTmpFile = new File( "target/tmpFile.txt" );
        if ( tTmpFile.exists() )
        {
            tTmpFile.delete();
        }
        FileConsumer tConsumer = new FileConsumer( tTmpFile );
        tConsumer.consumeLine( "AaAaAa" );
        tConsumer.consumeLine( "Bababa" );

        tConsumer.close();
        tTmpFile = new File( "target/tmpFile.txt" );
        
        long tSize = tTmpFile.length();
        assertTrue( "File lenght should be at least 12, but was only [" + tSize + "]", tSize > 12 );
    }

}
