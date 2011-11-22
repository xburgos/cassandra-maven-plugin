package org.codehaus.mojo.fitnesse;

import org.codehaus.mojo.fitnesse.plexus.FCommandLineException;
import org.codehaus.mojo.fitnesse.plexus.FCommandline;

public class MockCommandLine extends FCommandline
{

    private Process mProcess;

    public MockCommandLine( Process pProcess )
    {
        super();
        mProcess = pProcess;
    }

    /**
     * @Override
     */
    public Process execute() throws FCommandLineException
    {
        return mProcess;
    }

}
