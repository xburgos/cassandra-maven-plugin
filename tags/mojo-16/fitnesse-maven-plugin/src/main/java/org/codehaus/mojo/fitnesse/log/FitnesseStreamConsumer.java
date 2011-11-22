package org.codehaus.mojo.fitnesse.log;

import org.codehaus.plexus.util.cli.StreamConsumer;

public interface FitnesseStreamConsumer extends StreamConsumer
{
    public boolean hasGeneratedResultFile();

}
