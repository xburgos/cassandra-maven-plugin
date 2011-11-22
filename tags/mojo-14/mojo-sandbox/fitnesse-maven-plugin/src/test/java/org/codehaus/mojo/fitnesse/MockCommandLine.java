package org.codehaus.mojo.fitnesse;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

public class MockCommandLine extends Commandline
{

    private Process mProcess;

    public MockCommandLine( Process pProcess )
    {
        super();
        mProcess = pProcess;
    }

    @Override
    public Process execute() throws CommandLineException
    {
        return mProcess;
    }

}
